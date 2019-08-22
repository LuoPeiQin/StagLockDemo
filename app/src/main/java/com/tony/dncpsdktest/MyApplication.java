package com.tony.dncpsdktest;

import android.app.Application;

import com.tony.dncpsdktest.utils.LogUtils;

public class MyApplication extends Application {
    private static final String TAG = "BluetoothApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        loadLibraries();
    }

    private void loadLibraries() {
        try {
            System.loadLibrary("rfodn"); // 加载jni
            LogUtils.d(TAG + "lpq", "loadLibraries: jni库加载成功");
        } catch (UnsatisfiedLinkError ule) {
            LogUtils.d(TAG + "lpq", "loadLibraries: " + ule);
        }
    }
}
