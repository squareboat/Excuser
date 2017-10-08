package com.squareboat.excuser.activity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareboat.excuser.R;
import com.squareboat.excuser.activity.home.MainActivity;
import com.squareboat.excuser.activity.onboarding.OnboardingActivity;
import com.squareboat.excuser.utils.LocalStoreUtils;

/**
 * Created by Vipul on 16/05/17.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static int SPLASH_TIME_OUT = 1500;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkPlayServices()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (LocalStoreUtils.isOnboardingCompleted(SplashActivity.this)) {
                        launchMainActivity();
                    } else {
                        launchOnboardingActivity();
                    }
                }
            }, SPLASH_TIME_OUT);
        }
    }

    private void launchOnboardingActivity() {
        startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
        overridePendingTransition(0, R.anim.fade_out);
        finish();
    }

    private void launchMainActivity() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        overridePendingTransition(0, R.anim.fade_out);
        finish();
    }

    @SuppressLint("LongLogTag")
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
}
