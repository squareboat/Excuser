package com.squareboat.excuser.activity.onboarding;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.squareboat.excuser.R;
import com.squareboat.excuser.activity.SplashActivity;
import com.squareboat.excuser.activity.home.MainActivity;
import com.squareboat.excuser.model.Contact;
import com.squareboat.excuser.utils.LocalStoreUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vipul on 21/05/17.
 */

public class WearShakeIntensityFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "WearShakeIntensityFragment";

    public static final String SHAKE_INTENSITY_KEY = "com.squareboat.excuser.shakeintensity";

    @BindView(R.id.button_done)
    FloatingActionButton mButtonDone;

    @BindView(R.id.image_shake_intensity)
    AppCompatImageView mShakeIntensity;

    @BindView(R.id.intensity_seekBar)
    AppCompatSeekBar mIntensitySeekBar;

    @BindView(R.id.text_intensity)
    TextView mIntensityText;

    private GoogleApiClient mGoogleApiClient;

    public static WearShakeIntensityFragment newInstance() {
        return new WearShakeIntensityFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wear_intensity, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        setGoogleApiClient();

        updateShakeIntensityIcon(R.drawable.avd_wear_shake_intensity);
        mIntensitySeekBar.setMax(2);

        mIntensitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0: mIntensityText.setText(getResources().getString(R.string.low)); updateShakeIntensityIcon(R.drawable.avd_wear_shake_intensity); break;
                    case 1: mIntensityText.setText(getResources().getString(R.string.moderate)); updateShakeIntensityIcon(R.drawable.avd_wear_shake_intensity_moderate); break;
                    case 2: mIntensityText.setText(getResources().getString(R.string.high)); updateShakeIntensityIcon(R.drawable.avd_wear_shake_intensity_high); break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mButtonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String shakeIntensity = String.valueOf(mIntensitySeekBar.getProgress()+1);

                sendShakeIntensityToWear(shakeIntensity);

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(getResources().getString(R.string.intensity_preference_key), shakeIntensity);
                editor.apply();

                LocalStoreUtils.setOnboardingCompleted(true, getActivity());
                saveDummyData();
                launchMainActivity();
            }
        });
    }

    private void saveDummyData(){
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(0, "9876543210", "Home"));
        contacts.add(new Contact(1, "9437674662", "Office"));
        contacts.add(new Contact(2, "9279123828", "Work"));
        contacts.add(new Contact(3, "9262823792", "Home 2"));
        LocalStoreUtils.setContacts(contacts, getActivity());
    }

    private void updateShakeIntensityIcon(int resId){
        AnimatedVectorDrawable avd = (AnimatedVectorDrawable) getActivity().getDrawable(resId);

        if (mShakeIntensity != null && avd != null) {
            mShakeIntensity.setImageDrawable(avd);
            avd.start();
        }
    }

    private void launchMainActivity(){
        startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().overridePendingTransition(0, R.anim.fade_out);
        getActivity().finish();
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
    }

    private void sendShakeIntensityToWear(String shakeIntensity){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/SHAKEINTENSITY");
        putDataMapReq.getDataMap().putString(SHAKE_INTENSITY_KEY, shakeIntensity);
        PutDataRequest putDataReq = putDataMapReq
                                    .asPutDataRequest()
                                    .setUrgent();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

}
