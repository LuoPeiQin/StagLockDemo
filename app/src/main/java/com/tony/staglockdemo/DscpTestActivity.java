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
import com.staginfo.segs.sterm.bluetooth.protocol.dscp.DscpProtocol;
import com.staginfo.segs.sterm.bluetooth.protocol.dscp.DscpUtil;
import com.staginfo.segs.sterm.bluetooth.protocol.dscp.OnDscpEventListener;
import com.staginfo.segs.sterm.bluetooth.protocol.dscp.model.BleManufacturerData;
import com.staginfo.segs.sterm.bluetooth.protocol.dscp.model.DeviceInfo;
import com.staginfo.segs.sterm.bluetooth.protocol.dscp.model.DeviceStatus;
import com.staginfo.segs.sterm.bluetooth.protocol.dscp.model.SecureInfo;
import com.staginfo.segs.sterm.callback.OnTimeoutResult;
import com.staginfo.segs.sterm.utils.ByteUtils;
import com.tony.staglockdemo.utils.LogUtils;

import java.util.Date;

public class DscpTestActivity extends AppCompatActivity implements OnDscpEventListener, OnBluetoothStateChangeListener, OnBluetoothConnectStateChangeListener {
    private static final String TAG = "Dscp-";
    // 蓝牙相关
    private BluetoothController mController;
    // 测试数据
    public String testOperateKey = "b2cce9be-1eff-4ae7-bd69-ed09a77f5f89";
    DscpUtil dscpUtil = new DscpUtil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dscp_test);
        initBluetooth();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mController != null) {
            mController.unregisterBluetoothStateChangeListener(this);
            mController.unregisterConnectStateChangeListener(this);
        }
    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        mController = BluetoothController.getController(this);
        mController.registerBluetoothStateChangeListener(this);
        mController.registerConnectStateChangeListener(this);
        mController.setProtocol(new DscpProtocol(this, this));
        mController.sendData(new byte[]{});
    }


    /***************************** 事件回调 *******************************/

    @Override
    public void onRecvDeviceStatus(byte[] bytes) {
        LogUtils.d(TAG + "lpq", "onRecvDeviceStatus: ");
//        let deviceStatusInfo = new DeviceStatusInfo();
//        deviceStatusInfo.praseNewByRecvCmdData(recvCmdData);
//        if (deviceStatusInfo.receiveReason == 0x0101) {
//            // this._showToast("设备已开锁");
//            // 上传蓝牙开锁日志(由触碰指纹触发)
//            this._createLogAndUpload(this.data.curDevice, OperationCode.ACCESS_BLE, 0x030160, "指纹触碰开锁", this.data.fpTouchUnlockAuthUuid);
//        } else if (deviceStatusInfo.receiveReason = 0x0201) {
//            // 只有在tab页面才显示设备上锁和开锁的提示，避免影响流程
//            if (pageUtils.isTabShow) {
//                if (deviceStatusInfo.isLocked) {
//                    this._showToast("设备已上锁");
//                } else {
//                    this._showToast("设备已开锁");
//                }
//            }
//        } else {
//            console.log("没有匹配的激活类型：" + deviceStatusInfo.receiveReason);
//        }
    }

    @Override
    public void onRecvAddFpEvent(byte[] bytes) {
        LogUtils.d(TAG + "lpq", "onRecvAddFpEvent: ");
    }

    @Override
    public void onRecvIcCardEvent(byte[] bytes) {
        LogUtils.d(TAG + "lpq", "onRecvIcCardEvent: ");
    }

    /*************************** 锁具操作 *******************************/
    /**
     * 搜索蓝牙
     */
    public void searchBle(View v) {
        LogUtils.i(TAG + "lpq", "onClick: 开始搜蓝牙");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        LogUtils.i(TAG + "lpq", "bluetoothEnable: " + bluetoothAdapter.isEnabled());
        mController.setScanRemoveRepeat(true);
        mController.startScan(new OnBluetoothScanListener() {
            @Override
            public void onBluetoothScanFindDevice(BluetoothDevice device, int rssi, byte[] bytes) {
                LogUtils.d(TAG + "lpq", "onFindDevice: Name = " + device.getName() + " RSSI = " + rssi + " address = " + device.getAddress());
                LogUtils.d(TAG + "lpq", "onFindDevice: size = " + bytes.length + "bytes = " + ByteUtils.toString(bytes));
                int index = 0;
                byte[] ad;
                BleAdPart bleAdPart = new BleAdPart();
                while (bytes[index] != 0) {
                    bleAdPart.setLength(bytes[index]);
                    bleAdPart.setType(bytes[index + 1]);
                    bleAdPart.setValues(ByteUtils.subBytes(bytes, index + 2, bytes[index] - 1));
                    LogUtils.d(TAG + "lpq", "bleAdPart: " + bleAdPart.toString());
                    if (bleAdPart.getType() == (byte) 0xff) {
                        break;
                    }
                    index += bytes[index] + 1;
                }
                if ("SEL-ANSI-1851".equals(device.getName())) {
                    LogUtils.d(TAG + "lpq", "onFindDevice: " + ByteUtils.toString(bleAdPart.getValues()));
                    BleManufacturerData manufacturerData = BleManufacturerData.createByAdData(bleAdPart.getValues());
                    LogUtils.d(TAG + "lpq", "onFindDevice: " + manufacturerData.toString());
                }
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
//        mController.connect("C4:75:2E:58:4A:E0", new DscpProtocol(this, this));
//        mController.connect("C6:62:C5:7D:86:EF", new DscpProtocol(this, this));
//        mController.connect("C6:62:C5:7D:86:EF", new DscpProtocol(this, this));
//        mController.connect("FB:58:10:40:5B:7C", new DscpProtocol(this, this));
        mController.connect("C6:62:C5:7D:86:EF", new DscpProtocol(this, this));
    }

    /**
     * 断开连接
     */
    public void disConnectBle(View v) {
        mController.disconnect();
    }

    /**
     * 获取锁具安全信息
     */
    public void getLockSecureInfo(View v) {
        dscpUtil.setSessionCode(1, new OnTimeoutResult<SecureInfo>() {
            @Override
            public void onResult(boolean isTimeout, SecureInfo result) {
                LogUtils.d(TAG + "lpq", "onResult: " + result);
            }
        }, true);
    }

    /**
     * 锁具注册
     */
    public void btnLockRegister(View v) {
        dscpUtil.registerDevice(new Date(), testOperateKey, 1, 80, "nono",
                new OnTimeoutResult<Boolean>() {
                    @Override
                    public void onResult(boolean isTimeout, Boolean result) {
                        LogUtils.d(TAG + "lpq", "onResult: 注册结果 = " + result);
                    }
                }, true);
    }

    /**
     * 清空锁具（恢复出厂设置）
     */
    public void btnLockClear(View v) {
        dscpUtil.operateDevice(new Date(), (byte) 3, 1, (byte) 1, new OnTimeoutResult<Boolean>() {
            @Override
            public void onResult(boolean isTimeout, Boolean result) {
                LogUtils.d(TAG + "lpq", "onResult: 清空结果 = " + result);
            }
        }, true);
    }

    /**
     * 开锁
     */
    public void btnUnLock(View view) {
        dscpUtil.operateDevice(new Date(), (byte) 1, 1, (byte) 1, new OnTimeoutResult<Boolean>() {
            @Override
            public void onResult(boolean isTimeout, Boolean result) {
                LogUtils.d(TAG + "lpq", "onResult: 开锁结果 = " + result);
            }
        }, true);
    }

    /**
     * 上锁
     */
    public void btnLock(View view) {
        dscpUtil.operateDevice(new Date(), (byte) 2, 1, (byte) 1, new OnTimeoutResult<Boolean>() {
            @Override
            public void onResult(boolean isTimeout, Boolean result) {
                LogUtils.d(TAG + "lpq", "onResult: 上锁结果 = " + result);
            }
        }, true);
    }

    /**
     * 设备测试
     */
    public void btnDeviceTest(View view) {
        dscpUtil.deviceTest(new Date(), 1, 244, new OnTimeoutResult<SecureInfo>() {
            @Override
            public void onResult(boolean isTimeout, SecureInfo result) {
                LogUtils.d(TAG + "lpq", "onResult: " + result);
            }
        }, true);
    }

    /**
     * 获取锁具状态
     */
    public void getLockStatus(View v) {
        LogUtils.i(TAG + "lpq", "getLockStatus: 开始");
        dscpUtil.getDeviceStatus(new Date(), new OnTimeoutResult<DeviceStatus>() {
            @Override
            public void onResult(boolean isTimeout, DeviceStatus result) {
                LogUtils.d(TAG + "lpq", "getLockStatus: 结束");
            }
        }, true);
    }

    /**
     * 获取锁具信息
     */
    public void getLockInfo(View v) {
        LogUtils.i(TAG + "lpq", "getLockInfo: 开始");
        dscpUtil.getDeviceInfo(new OnTimeoutResult<DeviceInfo>() {
            @Override
            public void onResult(boolean isTimeout, DeviceInfo result) {
                LogUtils.d(TAG + "lpq", "getLockInfo: 结束");
            }
        }, true);
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
            DscpUtil.setCurKey(testOperateKey);
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
