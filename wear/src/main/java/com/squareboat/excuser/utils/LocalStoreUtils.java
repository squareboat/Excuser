package com.squareboat.excuser.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Vipul on 15/11/16.
 */

public class LocalStoreUtils {

    private static final String PREF_FILE_NAME = "com.squareboat.excuser";
    private static final String KEY_SHAKE_INTENSITY_DATA = "shake_intensity_data";

    public static void setShakeIntensity(String value, Context context) {
        try {
            SharedPreferences.Editor editor = getSharedEditor(context);
            editor.putString(KEY_SHAKE_INTENSITY_DATA, value);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getShakeIntensity(Context context) {
        try {
            SharedPreferences pref = getSharedPreference(context);
            return pref.getString(KEY_SHAKE_INTENSITY_DATA, "1.1f");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "1.1f";
    }

    public static void clearSession(Context context) {
        try {
            SharedPreferences.Editor editor = getSharedEditor(context);
            editor.clear();
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SharedPreferences.Editor getSharedEditor(Context context)
            throws Exception {
        if (context == null) {
            throw new Exception("Context null Exception");
        }
        return getSharedPreference(context).edit();
    }

    private static SharedPreferences getSharedPreference(Context context)
            throws Exception {
        if (context == null) {
            throw new Exception("Context null Exception");
        }
        return context.getSharedPreferences(PREF_FILE_NAME, 0);
    }

}
