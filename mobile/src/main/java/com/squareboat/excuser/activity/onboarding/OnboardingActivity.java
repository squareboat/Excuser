package com.squareboat.excuser.activity.onboarding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.squareboat.excuser.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vipul on 19/05/17.
 */

public class OnboardingActivity extends AppCompatActivity {

    @BindView(R.id.onboardingView)
    View mOnboardingView;

    private String mCurrentFragmentTag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            Fragment fragment = getSupportFragmentManager().getFragment(savedInstanceState, "FRAGMENT");
            replaceFragment(fragment, fragment.getTag());
        } else {
            showDeviceWearFragment();
        }
    }

    public void showDeviceWearFragment() {
        replaceFragment(DeviceWearConnectionFragment.newInstance(), "DeviceWearConnectionFragment");
    }

    public void showWearShakeDemoFragment() {
        replaceFragment(WearShakeDemoFragment.newInstance(), "WearShakeDemoFragment");
    }

    public void showWearShakeIntensityFragment() {
        replaceFragment(WearShakeIntensityFragment.newInstance(), "WearShakeIntensityFragment");
    }

    private void replaceFragment(Fragment fragment, String tag) {
        mCurrentFragmentTag = tag;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.onboardingView, fragment, tag)
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(mCurrentFragmentTag);
        if (fragment != null) {
            getSupportFragmentManager().putFragment(outState, "FRAGMENT", fragment);
        }
    }
}
