package com.squareboat.excuser.activity.onboarding;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;
import com.squareboat.excuser.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vipul on 19/05/17.
 */

public class DeviceWearConnectionFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        CapabilityApi.CapabilityListener{

    private static final String TAG = "DeviceWearConnectionFragment";

    @BindString(R.string.checking_message)
    String CHECKING_MESSAGE;

    @BindString(R.string.no_devices_linked)
    String NO_DEVICES_LINKED;

    @BindString(R.string.no_devices)
    String NO_DEVICES;

    @BindString(R.string.missing_all_message)
    String MISSING_ALL_MESSAGE;

    @BindString(R.string.installed_some_devices_message)
    String INSTALLED_SOME_DEVICES_MESSAGE;

    @BindString(R.string.installed_all_devices_message)
    String INSTALLED_ALL_DEVICES_MESSAGE;

    @BindString(R.string.app_playstore_url)
    String PLAY_STORE_APP_URI;

    String CAPABILITY_WEAR_APP = "com_squareboat_excuser_wear_app";

    @BindView(R.id.image_device_wear_connection)
    AppCompatImageView mDeviceWearConnection;

    @BindView(R.id.text_connection_title)
    TextView mConnectionTitle;

    @BindView(R.id.text_connection_message)
    TextView mConnectionMessage;

    @BindView(R.id.button_wear_app)
    AppCompatButton mWearAppButton;

    @BindView(R.id.button_install_wear_app)
    AppCompatButton mInstallAppOnWear;

    @BindView(R.id.button_device_next)
    FloatingActionButton mButtonNext;

    private Set<Node> mWearNodesWithApp;
    private List<Node> mAllConnectedNodes;
    private GoogleApiClient mGoogleApiClient;

    final Animation mConnectionMessageAnimation = new AlphaAnimation(1.0f, 0.0f);

    public static DeviceWearConnectionFragment newInstance() {
        return new DeviceWearConnectionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_wear_connection, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mButtonNext.hide();
        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnboardingActivity)getActivity()).showWearShakeDemoFragment();
            }
        });

        mWearAppButton.setVisibility(View.GONE);
        mWearAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.wearable.app"));
                startActivity(intent);
            }
        });

        mInstallAppOnWear.setVisibility(View.GONE);
        mInstallAppOnWear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayStoreOnWearDevicesWithoutApp();
            }
        });

        setConnectionMessage(CHECKING_MESSAGE);
        updateDeviceWearConnectionIcon(R.drawable.avd_device_wear_connection);

        setGoogleApiClient();

        mConnectionMessageAnimation.setDuration(500);
        mConnectionMessageAnimation.setRepeatCount(1);
        mConnectionMessageAnimation.setRepeatMode(Animation.REVERSE);
    }

    private void updateDeviceWearConnectionIcon(int resId){
        AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getActivity().getDrawable(resId);

        if (mDeviceWearConnection != null && avd != null) {
            mDeviceWearConnection.setImageDrawable(avd);
            avd.start();
        }
    }

    private void setGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
                    CAPABILITY_WEAR_APP);

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

    @SuppressLint("LongLogTag")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");

        // Set up listeners for capability changes (install/uninstall of remote app).
        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                this,
                CAPABILITY_WEAR_APP);

        // Initial request for devices with our capability, aka, our Wear app installed.
        findWearDevicesWithApp();

        // Initial request for all Wear devices connected (with or without our capability).
        // Additional Note: Because there isn't a listener for ALL Nodes added/removed from network
        // that isn't deprecated, we simply update the full list when the Google API Client is
        // connected and when capability changes come through in the onCapabilityChanged() method.
        findAllWearDevices();
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): connection to location client suspended: " + i);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): " + connectionResult);
        if(connectionResult.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            mConnectionTitle.setText("Connection Failed");
            setConnectionMessage(NO_DEVICES_LINKED);
            updateDeviceWearConnectionIcon(R.drawable.avd_device_wear_connection_faliure);
            mWearAppButton.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
        Log.d(TAG, "onCapabilityChanged(): " + capabilityInfo);

        mWearNodesWithApp = capabilityInfo.getNodes();

        // Because we have an updated list of devices with/without our app, we need to also update
        // our list of active Wear devices.
        findAllWearDevices();
        verifyNodeAndUpdateUI();
    }

    @SuppressLint("LongLogTag")
    private void findAllWearDevices() {
        Log.d(TAG, "findAllWearDevices()");

        PendingResult<NodeApi.GetConnectedNodesResult> pendingResult =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);

        pendingResult.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult getConnectedNodesResult) {

                if (getConnectedNodesResult.getStatus().isSuccess()) {
                    mAllConnectedNodes = getConnectedNodesResult.getNodes();
                    verifyNodeAndUpdateUI();
                    Log.e("Connected Nodes", "->"+mAllConnectedNodes.toString());
                    findWearDevicesWithApp();

                } else {
                    Log.d(TAG, "Failed NodeApi: " + getConnectedNodesResult.getStatus());
                }
            }
        });
    }

    @SuppressLint("LongLogTag")
    private void findWearDevicesWithApp() {
        Log.d(TAG, "findWearDevicesWithApp()");

        // You can filter this by FILTER_REACHABLE if you only want to open Nodes (Wear Devices)
        // directly connect to your phone.
        PendingResult<CapabilityApi.GetCapabilityResult> pendingResult =
                Wearable.CapabilityApi.getCapability(
                        mGoogleApiClient,
                        CAPABILITY_WEAR_APP,
                        CapabilityApi.FILTER_ALL);

        pendingResult.setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
            @Override
            public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
                Log.d(TAG, "findWearDevicesWithApp onResult(): " + getCapabilityResult);

                if (getCapabilityResult.getStatus().isSuccess()) {
                    CapabilityInfo capabilityInfo = getCapabilityResult.getCapability();
                    mWearNodesWithApp = capabilityInfo.getNodes();
                    verifyNodeAndUpdateUI();

                } else {
                    Log.d(TAG, "Failed CapabilityApi: " + getCapabilityResult.getStatus());
                }
            }
        });
    }

    @SuppressLint("LongLogTag")
    private void verifyNodeAndUpdateUI(){
        mWearAppButton.setVisibility(View.GONE);
        mInstallAppOnWear.setVisibility(View.GONE);

        if ((mWearNodesWithApp == null) || (mAllConnectedNodes == null)) {
            Log.d(TAG, "Waiting on Results for both connected nodes and nodes with app");
            mConnectionTitle.setText(getResources().getString(R.string.connecting));
            setConnectionMessage(CHECKING_MESSAGE);
            mInstallAppOnWear.setVisibility(View.GONE);
            updateDeviceWearConnectionIcon(R.drawable.avd_device_wear_connection);

        } else if (mAllConnectedNodes.isEmpty()) {
            Log.d(TAG, NO_DEVICES);
            mConnectionTitle.setText("Connection Failed");
            mInstallAppOnWear.setVisibility(View.GONE);
            setConnectionMessage(NO_DEVICES);
            updateDeviceWearConnectionIcon(R.drawable.avd_device_wear_connection_faliure);

        } else if (mWearNodesWithApp.isEmpty()) {
            Log.d(TAG, MISSING_ALL_MESSAGE);
            mConnectionTitle.setText("Please wait...");
            mInstallAppOnWear.setVisibility(View.VISIBLE);
            setConnectionMessage(MISSING_ALL_MESSAGE);
            updateDeviceWearConnectionIcon(R.drawable.avd_device_wear_connection);

        } else if (mWearNodesWithApp.size() < mAllConnectedNodes.size()) {
            String installMessage = String.format(INSTALLED_SOME_DEVICES_MESSAGE, mWearNodesWithApp);
            Log.d(TAG, installMessage);
            mConnectionTitle.setText("Connected");
            mInstallAppOnWear.setVisibility(View.GONE);
            setConnectionMessage(INSTALLED_SOME_DEVICES_MESSAGE);
            updateDeviceWearConnectionIcon(R.drawable.avd_device_wear_connection_sucess);
            mButtonNext.show();

        } else {
            String installMessage = String.format(INSTALLED_ALL_DEVICES_MESSAGE, mWearNodesWithApp);
            Log.d(TAG, installMessage);
            mConnectionTitle.setText("Connected");
            mInstallAppOnWear.setVisibility(View.GONE);
            setConnectionMessage(INSTALLED_ALL_DEVICES_MESSAGE);
            updateDeviceWearConnectionIcon(R.drawable.avd_device_wear_connection_sucess);
            mButtonNext.show();
        }
    }

    private void setConnectionMessage(final String message){
        mConnectionMessageAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                mConnectionMessage.setText(message);
            }
        });

        mConnectionMessage.startAnimation(mConnectionMessageAnimation);
    }

    @SuppressLint("LongLogTag")
    private void openPlayStoreOnWearDevicesWithoutApp() {
        Log.d(TAG, "openPlayStoreOnWearDevicesWithoutApp()");

        // Create a List of Nodes (Wear devices) without your app.
        ArrayList<Node> nodesWithoutApp = new ArrayList<>();

        for (Node node : mAllConnectedNodes) {
            if (!mWearNodesWithApp.contains(node)) {
                nodesWithoutApp.add(node);
            }
        }

        if (!nodesWithoutApp.isEmpty()) {
            Log.d(TAG, "Number of nodes without app: " + nodesWithoutApp.size());

            Intent intent = new Intent(Intent.ACTION_VIEW)
                            .addCategory(Intent.CATEGORY_BROWSABLE)
                            .setData(Uri.parse(PLAY_STORE_APP_URI));

            for (Node node : nodesWithoutApp) {
                RemoteIntent.startRemoteActivity(getContext(), intent, mResultReceiver, node.getId());
            }
        }
    }

    // Result from sending RemoteIntent to wear device(s) to open app in play/app store.
    private final ResultReceiver mResultReceiver = new ResultReceiver(new Handler()) {
        @SuppressLint("LongLogTag")
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.d(TAG, "onReceiveResult: " + resultCode);

            if (resultCode == RemoteIntent.RESULT_OK) {
                showSnackBar(mInstallAppOnWear, R.string.playstore_request_successfull);

            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                showSnackBar(mInstallAppOnWear, R.string.playstore_request_fail);

            }
        }
    };

    private void showSnackBar(View view, String message){
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    private void showSnackBar(View view, int message){
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

}
