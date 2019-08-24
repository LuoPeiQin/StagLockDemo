package com.tony.staglockdemo;

import android.app.Application;

import com.staginfo.segs.sterm.DncpControl;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DncpControl.loadLibraries();
    }
}
