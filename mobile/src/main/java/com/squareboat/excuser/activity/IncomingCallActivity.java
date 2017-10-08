package com.squareboat.excuser.activity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.squareboat.excuser.R;
import com.squareboat.excuser.model.Contact;
import com.squareboat.excuser.utils.LocalStoreUtils;
import com.squareboat.excuser.utils.StatusBarUtil;

import net.frakbot.glowpadbackport.GlowPadView;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Vipul on 02/05/17.
 */

public class IncomingCallActivity extends BaseActivity {

    @BindView(R.id.text_caller_name)
    protected TextView mCallerName;
    @BindView(R.id.text_caller_phone)
    protected TextView mCallerPhone;
    @BindView(R.id.text_call_duration)
    protected TextView mCallDuration;
    @BindView(R.id.button_call_end)
    protected FloatingActionButton mCallButton;
    @BindView(R.id.incomingCallWidget)
    protected GlowPadView mGlowPadView;

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private CountDownTimer mActivityTimeout;
    private long mCallStart = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.colorIncomingCallDark));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        ButterKnife.bind(this);

        initView();

        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();

        startActivityTimeout();
    }

    private void initView() {

        mCallButton.hide();
        initTimer();

        if (LocalStoreUtils.getContacts(this) != null && LocalStoreUtils.getContacts(this).size() > 0) {

            Random r = new Random();
            int randomId = r.nextInt(LocalStoreUtils.getContacts(this).size());

            Contact contact = LocalStoreUtils.getContacts(this).get(randomId);

            if (contact.getName().isEmpty()) {
                mCallerName.setText(contact.getMobile());
                mCallerPhone.setVisibility(View.GONE);
            } else {
                mCallerName.setText(contact.getName());
                mCallerPhone.setText(contact.getMobile());
                mCallerPhone.setVisibility(View.VISIBLE);
            }

        } else {
            mCallerName.setText(getResources().getString(R.string.demo_name));
            mCallerPhone.setText(getResources().getString(R.string.demo_number));
        }

        mGlowPadView.setOnTriggerListener(new GlowPadView.OnTriggerListener() {
            @Override
            public void onGrabbed(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onReleased(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onTrigger(View v, int target) {
                Log.e("target id", "->" + target);

                if (target == 0) { //accept
                    onCallAccept();
                } else { //decline
                    endActivity();
                }
            }

            @Override
            public void onGrabbedStateChange(View v, int handle) {
                // Do nothing
            }

            @Override
            public void onFinishFinalAnimation() {
                // Do nothing
            }
        });
    }

    @OnClick(R.id.button_call_end)
    protected void onCallEndClick() {
        endActivity();
    }

    private void initTimer() {
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
    }

    private void startTimer() {
        mCallStart = System.currentTimeMillis();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    private void stopTimer() {
        if (mDurationTask != null)
            mDurationTask.cancel();

        if (mTimer != null)
            mTimer.cancel();
    }

    private void onCallAccept() {
        mAudioPlayer.stopRingtone();
        mGlowPadView.setVisibility(View.GONE);
        mCallButton.show();
        startTimer();
        mActivityTimeout.cancel();
    }

    private void onCallReject() {
    }

    private void endActivity() {
        if (mAudioPlayer != null)
            mAudioPlayer.stopRingtone();

        mActivityTimeout.cancel();

        android.os.Process.killProcess(android.os.Process.myPid()); //completely destroy app instance
    }

    private void startActivityTimeout() {
        mActivityTimeout = new CountDownTimer(30000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                endActivity();
            }
        }.start();
    }

    private String formatTimeSpan(long timespan) {
        long totalSeconds = timespan / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        if (mCallStart > 0) {
            mCallDuration.setText(formatTimeSpan(System.currentTimeMillis() - mCallStart));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAudioPlayer != null)
            mAudioPlayer.stopRingtone();

        stopTimer();
    }

    private class UpdateCallDurationTask extends TimerTask {
        @Override
        public void run() {
            IncomingCallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }
}
