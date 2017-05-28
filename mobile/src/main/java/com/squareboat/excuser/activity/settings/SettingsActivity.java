package com.squareboat.excuser.activity.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.squareboat.excuser.R;
import com.squareboat.excuser.activity.onboarding.WearShakeIntensityFragment;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Vipul on 22/05/17.
 */

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static void launchActivity(@NonNull Context startingActivity) {
        Intent intent = new Intent(startingActivity, SettingsActivity.class);
        startingActivity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    private void setupActionBar() {

        ViewGroup rootView = (ViewGroup)findViewById(R.id.action_bar_root);
        View view = getLayoutInflater().inflate(R.layout.layout_toolbar, rootView, false);
        rootView.addView(view, 0);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the back button in the action bar.
            actionBar.setTitle(getResources().getString(R.string.title_activity_settings));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    public static class MyPreferenceFragment extends PreferenceFragment implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener{

        private static final String TAG = "MyPreferenceFragment";

        private GoogleApiClient mGoogleApiClient;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference_screen);
            setGoogleApiClient();

            Preference preference = findPreference(getResources().getString(R.string.intensity_preference_key));
            preference.setOnPreferenceChangeListener(preferenceChangeListener);
            preferenceChangeListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }

        Preference.OnPreferenceChangeListener preferenceChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String stringValue = newValue.toString();
                Log.e("stringValue", "->"+stringValue);

                if (preference instanceof ListPreference) {
                    ((ListPreference) preference).setValue(stringValue);
                    String currValue = ((ListPreference) preference).getValue();
                    Log.e("value", "->"+currValue);
                    sendShakeIntensityToWear(currValue);
                }

                return false;
            }
        };

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
            putDataMapReq.getDataMap().putString(WearShakeIntensityFragment.SHAKE_INTENSITY_KEY, shakeIntensity);
            PutDataRequest putDataReq = putDataMapReq
                    .asPutDataRequest()
                    .setUrgent();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }
    }

}
