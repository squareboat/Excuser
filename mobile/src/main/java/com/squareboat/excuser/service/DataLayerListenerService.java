package com.squareboat.excuser.service;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareboat.excuser.activity.IncomingCallActivity;

/**
 * Created by Vipul on 02/05/17.
 */

public class DataLayerListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.e("Message Event", "->" + messageEvent.toString());

        if ("/CALL".equals(messageEvent.getPath())) {
            Intent intent = new Intent(this, IncomingCallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}