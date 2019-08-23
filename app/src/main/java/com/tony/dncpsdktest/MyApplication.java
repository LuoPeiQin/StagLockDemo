package com.tony.dncpsdktest;

import android.app.Application;

import com.staginfo.segs.sterm.DncpControl;

public class MyApplication extends Application {
    private static final String TAG = "BluetoothApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        DncpControl.loadLibraries();
    }

}
