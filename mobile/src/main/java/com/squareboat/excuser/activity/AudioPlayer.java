package com.squareboat.excuser.activity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;

public class AudioPlayer {

    private static final String LOG_TAG = AudioPlayer.class.getSimpleName();

    private Context mContext;
    private MediaPlayer mPlayer;
    private Vibrator mVibrator;

    public AudioPlayer(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void playRingtone() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        // Honour silent mode
        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL:
                mPlayer = new MediaPlayer();
                mPlayer.setAudioStreamType(AudioManager.STREAM_RING);

                try {
                    if(hasSIM())
                        mPlayer.setDataSource(mContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
                    else
                        mPlayer.setDataSource(mContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                    mPlayer.prepare();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Could not setup media player for ringtone");
                    mPlayer = null;
                    return;
                }
                mPlayer.setLooping(true);
                mPlayer.start();
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                if(mVibrator.hasVibrator()) {
                    long[] pattern = {0, 500, 200};

                    // The '0' here means to repeat indefinitely
                    // '0' is actually the index at which the pattern keeps repeating from (the start)
                    // To repeat the pattern from any other point, you could increase the index, e.g. '1'
                    mVibrator.vibrate(pattern, 0);
                }
                break;
        }
    }

    public void stopRingtone() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        if(mVibrator!=null){
            mVibrator.cancel();
        }
    }

    public void playProgressTone() {
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        int maxVolume = 100;
        final float volume = (float) (1 - (Math.log(maxVolume - 1) / Math.log(maxVolume)));
        mPlayer.setVolume(volume, volume);

        try {
            //mPlayer.setDataSource(mContext, Uri.parse("android.resource://" + mContext.getPackageName() + "/" + R.raw.phone_loud1));
            mPlayer.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Could not setup media player for ringtone");
            mPlayer = null;
            return;
        }
        mPlayer.setLooping(true);
        mPlayer.start();
    }

    public void stopProgressTone() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private boolean hasSIM(){
        TelephonyManager manager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

}
