package com.squareboat.excuser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wear.widget.BoxInsetLayout;
import android.support.wearable.view.ConfirmationOverlay;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;
import com.google.android.wearable.playstore.PlayStoreAvailability;
import com.squareboat.excuser.service.AccelerometerSensorService;
import com.squareboat.excuser.utils.LocalStoreUtils;
import com.squareboat.excuser.utils.Utils;

import java.util.Set;

public class MainWearActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, CapabilityApi.CapabilityListener {

    private static final String TAG = "MainWearActivity";
    private ImageView mExcuserImage;
    private TextView mExcuserStatus;
    private Button mInstallAppOnPhone;
    private GoogleApiClient mGoogleApiClient;
    private BoxInsetLayout mBoxInsetLayout;

    String CAPABILITY_PHONE_APP = "com_squareboat_excuser_phone_app";

    private Node mAndroidPhoneNodeWithApp;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(checkPlayServices()) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N_MR1) {
                setGoogleApiClient();
                showMissingAppOnPhoneView();
            } else {
                showMainView();
            }
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                }).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void showMainView(){
        setContentView(R.layout.activity_main);

        mBoxInsetLayout = (BoxInsetLayout) findViewById(R.id.boxInsetLayout);
        mExcuserImage = (ImageView) findViewById(R.id.image_excuser);
        mExcuserStatus = (TextView) findViewById(R.id.text_excuser_status);

        //By default start service
        if(LocalStoreUtils.getIsFirstTime(this)) {
            startService(new Intent(getApplicationContext(), AccelerometerSensorService.class));
            LocalStoreUtils.setIsFirstTime(false, this);
        }

        updateView();

        mBoxInsetLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!Utils.isMyServiceRunning(MainWearActivity.this, AccelerometerSensorService.class)) {
                    startService(new Intent(getApplicationContext(), AccelerometerSensorService.class));
                } else {
                    stopService(new Intent(getApplicationContext(), AccelerometerSensorService.class));
                }

                updateView();
            }
        });
    }

    private void showMissingAppOnPhoneView(){
        setContentView(R.layout.activity_main_missing);

        mInstallAppOnPhone = (Button) findViewById(R.id.button_install_phone);
        mInstallAppOnPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppInStoreOnPhone();
            }
        });
    }

    private void setGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void updateView(){
        if(Utils.isMyServiceRunning(MainWearActivity.this, AccelerometerSensorService.class)) {
            mBoxInsetLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            mExcuserImage.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite));

            mExcuserStatus.setText(getResources().getString(R.string.on));
        } else {
            mBoxInsetLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGrey500));
            mExcuserImage.setColorFilter(ContextCompat.getColor(this, R.color.colorGrey400));

            mExcuserStatus.setText(getResources().getString(R.string.off));
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            Wearable.CapabilityApi.removeCapabilityListener(
                    mGoogleApiClient,
                    this,
                    CAPABILITY_PHONE_APP);

            mGoogleApiClient.disconnect();
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");

        // Set up listeners for capability changes (install/uninstall of remote app).
        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                this,
                CAPABILITY_PHONE_APP);

        checkIfPhoneHasApp();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): connection to location client suspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): " + connectionResult);
    }

    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): " + capabilityInfo);

        mAndroidPhoneNodeWithApp = pickBestNodeId(capabilityInfo.getNodes());
        verifyNodeAndUpdateUI();
    }

    private void checkIfPhoneHasApp() {
        Log.d(TAG, "checkIfPhoneHasApp()");

        PendingResult<CapabilityApi.GetCapabilityResult> pendingResult =
                Wearable.CapabilityApi.getCapability(
                        mGoogleApiClient,
                        CAPABILITY_PHONE_APP,
                        CapabilityApi.FILTER_ALL);

        pendingResult.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {

            @Override
            public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
                Log.d(TAG, "onResult(): " + getCapabilityResult);

                if (getCapabilityResult.getStatus().isSuccess()) {
                    CapabilityInfo capabilityInfo = getCapabilityResult.getCapability();
                    mAndroidPhoneNodeWithApp = pickBestNodeId(capabilityInfo.getNodes());
                    verifyNodeAndUpdateUI();

                } else {
                    Log.d(TAG, "Failed CapabilityApi: " + getCapabilityResult.getStatus());
                }
            }
        });
    }

    private void verifyNodeAndUpdateUI() {
        if (mAndroidPhoneNodeWithApp != null) {
            showMainView();
        } else {
            showMissingAppOnPhoneView();
        }
    }

    private void openAppInStoreOnPhone() {
        Log.d(TAG, "openAppInStoreOnPhone()");

        int playStoreAvailabilityOnPhone =
                PlayStoreAvailability.getPlayStoreAvailabilityOnPhone(getApplicationContext());

        switch (playStoreAvailabilityOnPhone) {

            // Android phone with the Play Store.
            case PlayStoreAvailability.PLAY_STORE_ON_PHONE_AVAILABLE:
                Log.d(TAG, "\tPLAY_STORE_ON_PHONE_AVAILABLE");

                Intent intentAndroid =
                        new Intent(Intent.ACTION_VIEW)
                                .addCategory(Intent.CATEGORY_BROWSABLE)
                                .setData(Uri.parse(getResources().getString(R.string.app_playstore_url)));

                RemoteIntent.startRemoteActivity(
                        getApplicationContext(),
                        intentAndroid,
                        mResultReceiver);
                break;

            // iPhone (iOS device) or Android without Play Store (not supported right now).
            case PlayStoreAvailability.PLAY_STORE_ON_PHONE_UNAVAILABLE:
                Log.d(TAG, "\tPLAY_STORE_ON_PHONE_UNAVAILABLE");
                break;

            case PlayStoreAvailability.PLAY_STORE_ON_PHONE_ERROR_UNKNOWN:
                Log.d(TAG, "\tPLAY_STORE_ON_PHONE_ERROR_UNKNOWN");
                break;
        }
    }

    // Result from sending RemoteIntent to phone to open app in play/app store.
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultCode == RemoteIntent.RESULT_OK) {
                new ConfirmationOverlay().showOn(MainWearActivity.this);

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                new ConfirmationOverlay()
                        .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                        .showOn(MainWearActivity.this);
            }
        }
    };

    private Node pickBestNodeId(Set<Node> nodes) {
        Log.d(TAG, "pickBestNodeId(): " + nodes);

        Node bestNodeId = null;
        // Find a nearby node/phone or pick one arbitrarily. Realistically, there is only one phone.
        for (Node node : nodes) {
            bestNodeId = node;
        }
        return bestNodeId;
    }
}
