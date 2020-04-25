/*
 * Copyright (c) 2020. stag All rights reserved.
 */

package com.tony.staglockdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.stag.bluetooth.BluetoothController;
import com.stag.bluetooth.OnBluetoothConnectStateChangeListener;
import com.stag.bluetooth.OnBluetoothScanListener;
import com.stag.bluetooth.OnBluetoothStateChangeListener;
import com.staginfo.segs.sterm.DncpControl;
import com.staginfo.segs.sterm.bluetooth.protocol.dncp.DncpProtocol;
import com.staginfo.segs.sterm.bluetooth.protocol.dncp.OnDncpEventListener;
import com.staginfo.segs.sterm.bluetooth.protocol.dncp.model.SensorStatus;
import com.staginfo.segs.sterm.bluetooth.protocol.dscp.DscpProtocol;
import com.staginfo.segs.sterm.callback.OnDncpOperateResult;
import com.staginfo.segs.sterm.entity.KeyInfo;
import com.staginfo.segs.sterm.entity.LockInfo;
import com.staginfo.segs.sterm.entity.RegisterLockParameter;
import com.tony.staglockdemo.utils.LogUtils;

import java.util.UUID;

public class DncpTestActivity extends AppCompatActivity implements OnBluetoothStateChangeListener, OnBluetoothConnectStateChangeListener, OnDncpEventListener {

    private static final String TAG = "Dncp";

    // 蓝牙相关
    private BluetoothController mController;
    private DncpControl mDncpControl;
    // 测试数据
    UUID testOrgKey = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffff11");
    UUID testOperateKey = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffff22");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dncp_test);
        initData();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mController != null) {
            mController.unregisterBluetoothStateChangeListener(this);
            mController.unregisterConnectStateChangeListener(this);
        }
    }

    private void initData() {
        initBluetooth();
        initDncp();
    }

    private void initDncp() {
        mDncpControl = new DncpControl();
    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        mController = BluetoothController.getController(this);
        mController.registerBluetoothStateChangeListener(this);
        mController.registerConnectStateChangeListener(this);
    }

    /**
     * ——————————————————蓝牙操作——————————————————————
     */
    /**
     * 搜索蓝牙
     */
    public void searchBle(View v) {
        LogUtils.i(TAG + "lpq", "onClick: 开始搜蓝牙");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        LogUtils.i(TAG + "lpq", "bluetoothEnable: " + bluetoothAdapter.isEnabled());
        mController.startScan(new OnBluetoothScanListener() {
            @Override
            public void onBluetoothScanFindDevice(BluetoothDevice device, int rssi, byte[] bytes) {
                LogUtils.i(TAG + "lpq", "发现设备");
                LogUtils.i(TAG + "lpq", "onFindDevice: " + device.getAddress());
            }

            @Override
            public void onBluetoothScanFinish() {
                LogUtils.i(TAG + "lpq", "onBluetoothScanFinish: ");
            }
        });
    }

    /**
     * 停止搜索蓝牙
     */
    public void stopSearchBle(View v) {
        mController.stopScan();
    }

    /**
     * 连接蓝牙
     */
    public void connectBle(View v) {
//        mController.connect("0C:B2:B7:3E:23:69");
        mController.connect("9C:1D:58:91:C2:5B", new DncpProtocol(this, this));
    }

    /**
     * 断开连接
     */
    public void disConnectBle(View v) {
        mController.disconnect();
    }

    /**
     * ——————————————————锁具操作——————————————————————
     */
    /**
     * 获取锁具安全信息
     */
    public void getLockSecureInfo(View v) {
//        mDncpControl.getSecureInfo(new OnDncpOperateResult<LockSecureInfo>() {
//            @Override
//            public void onResult(int code, String message, LockSecureInfo obj) {
//                LogUtils.i(TAG + "lpq", "获取安全信息: code = " + code);
//                LogUtils.i(TAG + "lpq", "获取安全信息: message = " + message);
//                LogUtils.i(TAG + "lpq", "获取安全信息: obj = " + obj);
//            }
//        });
    }

    /**
     * 开关锁
     */
    public void btnLockOrUnlock(View v) {
        mDncpControl.lockOrUnlock(null, new OnDncpOperateResult<Integer>() {
            @Override
            public void onResult(int code, String message, Integer obj) {
                // code 表示操作结果，为 0 时表示操作成功；
                // message 是对操作结果的额外说明；
                // Integer 表示经过操作之后锁具处于开锁还是上锁状态，1：锁开 2：锁关
                // 注意：极个别开锁错误的情况下，返回的锁具状态可能不正确，
                // 可辅助onDncpLock和onDncpUnlock进行正确性检查
                LogUtils.i(TAG + "lpq", "开关锁: code = " + code);
                LogUtils.i(TAG + "lpq", "开关锁: message = " + message);
                LogUtils.i(TAG + "lpq", "开关锁: obj = " + obj);
            }
        });
    }

    /**
     * 开关服役中的锁
     */
    public void btnLockOrUnlockInServiceLock(View view) {
        mDncpControl.lockOrUnlock(testOperateKey, new OnDncpOperateResult<Integer>() {
            @Override
            public void onResult(int code, String message, Integer obj) {
                LogUtils.i(TAG + "lpq", "开关锁: code = " + code);
                LogUtils.i(TAG + "lpq", "开关锁: message = " + message);
                LogUtils.i(TAG + "lpq", "开关锁: obj = " + obj);
            }
        });
    }

    /**
     * 测试开关服役中的锁
     */
    public void btnLockOrUnlockInServiceLockTest(View view) {
        mDncpControl.lockOrUnlock(testOrgKey, new OnDncpOperateResult<Integer>() {
            @Override
            public void onResult(int code, String message, Integer obj) {
                LogUtils.i(TAG + "lpq", "开关锁: code = " + code);
                LogUtils.i(TAG + "lpq", "开关锁: message = " + message);
                LogUtils.i(TAG + "lpq", "开关锁: obj = " + obj);
            }
        });
    }

    /**
     * 锁具注册
     */
    public void btnLockRegister(View v) {
        mDncpControl.registerLock(new RegisterLockParameter((short) 1, testOrgKey, testOperateKey), new OnDncpOperateResult() {
            @Override
            public void onResult(int code, String message, Object obj) {
                LogUtils.i(TAG + "lpq", "注册: code = " + code);
                LogUtils.i(TAG + "lpq", "注册: message = " + message);
                LogUtils.i(TAG + "lpq", "注册: obj = " + obj);
            }
        });
    }

    /**
     * 清空锁具（恢复出厂设置）
     */
    public void btnLockClear(View v) {
        mDncpControl.resetLock(testOrgKey, new OnDncpOperateResult() {
            @Override
            public void onResult(int code, String message, Object obj) {
                LogUtils.i(TAG + "lpq", "清空: code = " + code);
                LogUtils.i(TAG + "lpq", "清空: message = " + message);
                LogUtils.i(TAG + "lpq", "清空: obj = " + obj);
            }
        });
    }

    /**
     * 获取锁具状态
     */
    public void getLockStatus(View v) {
        LogUtils.i(TAG + "lpq", "getLockStatus: ");
        mDncpControl.getSensorStatus(new OnDncpOperateResult<SensorStatus>() {
            @Override
            public void onResult(int code, String message, SensorStatus obj) {
                LogUtils.i(TAG + "lpq", "获取锁具状态: code = " + code);
                LogUtils.i(TAG + "lpq", "获取锁具状态: message = " + message);
                LogUtils.i(TAG + "lpq", "获取锁具状态: obj = " + obj);
            }
        });
    }

    /**
     * 获取锁具信息
     */
    public void getLockInfo(View v) {
        LogUtils.i(TAG + "lpq", "getLockInfo: ");
        mDncpControl.getLockInfo(new OnDncpOperateResult<LockInfo>() {
            @Override
            public void onResult(int code, String message, LockInfo obj) {
                LogUtils.i(TAG + "lpq", "获取锁具信息: code = " + code);
                LogUtils.i(TAG + "lpq", "获取锁具信息: message = " + message);
                LogUtils.i(TAG + "lpq", "获取锁具信息: obj = " + obj);
            }
        });
    }

    /**
     * 获取钥匙信息
     */
    public void getKeyInfo(View v) {
        LogUtils.i(TAG + "lpq", "getKeyInfo: ");
        mDncpControl.getKeyInfo(new OnDncpOperateResult<KeyInfo>() {
            @Override
            public void onResult(int code, String message, KeyInfo obj) {
                LogUtils.i(TAG + "lpq", "获取钥匙信息: code = " + code);
                LogUtils.i(TAG + "lpq", "获取钥匙信息: message = " + message);
                LogUtils.i(TAG + "lpq", "获取钥匙信息: obj = " + obj);
            }
        });
    }

    /**
     * —————————————————设备主动上报事件———————————————————
     */
    @Override
    public void onDncpLockConnect() {
        LogUtils.i(TAG + "lpq", "onDncpLockConnect: 钥匙与锁具连接上报");
    }

    @Override
    public void onDncpLockDisconnect() {
        LogUtils.i(TAG + "lpq", "onDncpLockDisconnect: 钥匙与锁具断开上报");
    }

    @Override
    public void onDncpLowPower() {
        LogUtils.i(TAG + "lpq", "onDncpLowPower: 钥匙低电量上报");
    }

    @Override
    public void onDncpLock() {
        LogUtils.i(TAG + "lpq", "onDncpLock: 锁具上锁上报");
    }

    @Override
    public void onDncpUnlock() {
        LogUtils.i(TAG + "lpq", "onDncpUnlock: 锁具开锁上报");
    }


    @Override
    public void onBluetoothOpen() {
        LogUtils.i(TAG + "lpq", "onBluetoothOpen: 蓝牙打开");
    }

    @Override
    public void onBluetoothClose() {
        LogUtils.i(TAG + "lpq", "onBluetoothClose: 蓝牙关闭");
    }

    @Override
    public void onBluetoothConnect(BluetoothDevice device, boolean isSuccess) {
        if (isSuccess) {
            mController.setProtocol(new DscpProtocol(this, null));
            LogUtils.i(TAG + "lpq", "onBluetoothConnect: 蓝牙已连接");
        } else {
            LogUtils.i(TAG + "lpq", "onBluetoothConnect: 蓝牙连接失败");
        }
    }

    @Override
    public void onBluetoothDisconnect(BluetoothDevice device) {
        LogUtils.i(TAG + "lpq", "onBluetoothDisconnect: 蓝牙已断开");
    }
}
