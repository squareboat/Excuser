package com.squareboat.excuser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableStatusCodes;
import com.squareboat.excuser.service.AccelerometerSensorService;
import com.squareboat.excuser.utils.Utils;

import java.util.List;

public class MainWearActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainWearActivity";
    private AppCompatImageView mExcuserImage;
    private TextView mExcuserStatus;
    private GoogleApiClient mGoogleApiClient;
    private BoxInsetLayout mBoxInsetLayout;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("AccelerometerSensorService Running", "->"+ Utils.isMyServiceRunning(this, AccelerometerSensorService.class));

        mBoxInsetLayout = (BoxInsetLayout) findViewById(R.id.boxInsetLayout);
        mExcuserImage = (AppCompatImageView) findViewById(R.id.image_excuser);
        mExcuserStatus = (TextView) findViewById(R.id.text_excuser_status);

        //By default start service
        if(!Utils.isMyServiceRunning(MainWearActivity.this, AccelerometerSensorService.class)) {
            startService(new Intent(getApplicationContext(), AccelerometerSensorService.class));
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

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): connection to location client suspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): " + connectionResult);
    }
}
