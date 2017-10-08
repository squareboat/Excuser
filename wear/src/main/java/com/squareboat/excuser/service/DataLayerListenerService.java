package com.squareboat.excuser.service;

import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareboat.excuser.utils.LocalStoreUtils;

/**
 * Created by Vipul on 02/05/17.
 */

public class DataLayerListenerService extends WearableListenerService {

    private static final String SHAKE_INTENSITY_KEY = "com.squareboat.excuser.shakeintensity";

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/SHAKEINTENSITY") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                    Log.e("Data Event", "value->" + dataMap.getString(SHAKE_INTENSITY_KEY));
                    LocalStoreUtils.setShakeIntensity(dataMap.getString(SHAKE_INTENSITY_KEY), this);
                }
            }
        }

    }
}