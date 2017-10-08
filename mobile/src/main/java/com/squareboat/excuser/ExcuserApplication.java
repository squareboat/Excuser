package com.squareboat.excuser;

import android.app.Application;
import android.os.StrictMode;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Vipul on 24/05/17.
 */

public class ExcuserApplication extends Application {

    private static ExcuserApplication instance;

    public static ExcuserApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        //Crashlytics, disabled for debug builds
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        final Fabric fabric = new Fabric.Builder(this)
                .kits(crashlytics)
                .debuggable(true)
                .build();

        Fabric.with(fabric);

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    public String getAppPackageName() {
        return getPackageName();
    }

}
