package com.squareboat.excuser.activity.onboarding;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareboat.excuser.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vipul on 21/05/17.
 */

public class WearShakeDemoFragment extends Fragment {

    @BindView(R.id.image_wear_Shake)
    AppCompatImageView mWearShake;

    @BindView(R.id.button_wear_next)
    FloatingActionButton mButtonNext;

    public static WearShakeDemoFragment newInstance() {
        return new WearShakeDemoFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wear_demo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OnboardingActivity) getActivity()).showWearShakeIntensityFragment();
            }
        });
    }

}
