package com.tony.dncpsdktest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.stag.bluetooth.BluetoothController;
import com.stag.bluetooth.OnBluetoothConnectStateChangeListener;
import com.stag.bluetooth.OnBluetoothScanListener;
import com.stag.bluetooth.OnBluetoothStateChangeListener;
import com.staginfo.segs.sterm.DncpControl;
import com.staginfo.segs.sterm.bluetooth.protocol.dncp.DncpProtocol;
import com.staginfo.segs.sterm.bluetooth.protocol.dncp.OnDncpEventListener;
import com.staginfo.segs.sterm.bluetooth.protocol.dncp.model.SensorStatus;
import com.staginfo.segs.sterm.callback.OnDncpOperateResult;
import com.staginfo.segs.sterm.entity.LockInfo;
import com.staginfo.segs.sterm.entity.RegisterLockParameter;
import com.tony.dncpsdktest.utils.LogUtils;

import java.util.UUID;

public class MainActivity extends AppCompatActivity implements OnDncpEventListener {

    private static final String TAG = "MainActivity";

    // 蓝牙相关
    private BluetoothController mController;
    private DncpControl mDncpControl;
    // 测试数据
    // 组织密钥，用于清空锁
    private final UUID testOrgKey = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffff11");
    // 错误的组织密钥，用于校验只有正确的组织密钥才能清空锁
    private final UUID errorTestOrgKey = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffff10");
    // 操作密钥，用于开关锁
    private final UUID testOperateKey = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffff22");
    // 错误的操作密钥
    private final UUID errorTestOperateKey = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffff20");
    // 要连接的蓝牙钥匙地址
    private final String testBleMacAddress = "0C:B2:B7:3E:23:69";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 检查并申请定位权限
        checkLocationPermission();
    }

    /**
     * 申请定位权限，保证蓝牙功能的正常运行
     */
    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "没有定位权限蓝牙功能将无法正常使用", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 不再使用蓝牙时，取消注册事件的监听
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mController != null) {
            mController.unregisterBluetoothStateChangeListener();
            mController.unregisterConnectStateChangeListener();
            mController = null;
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
        mController.registerBluetoothStateChangeListener(new OnBluetoothStateChangeListener() {
            @Override
            public void onBluetoothOpen() {
                LogUtils.i(TAG + "lpq", "onBluetoothOpen: 蓝牙打开");
            }

            @Override
            public void onBluetoothClose() {
                LogUtils.i(TAG + "lpq", "onBluetoothClose: 蓝牙关闭");
            }
        });
        mController.registerConnectStateChangeListener(new OnBluetoothConnectStateChangeListener() {
            @Override
            public void onBluetoothConnect(BluetoothDevice device, boolean isSuccess) {
                if (isSuccess) {
                    LogUtils.i(TAG + "lpq", "onBluetoothConnect: 蓝牙已连接");
                } else {
                    LogUtils.i(TAG + "lpq", "onBluetoothConnect: 蓝牙连接失败");
                }
            }

            @Override
            public void onBluetoothDisconnect(BluetoothDevice device) {
                LogUtils.i(TAG + "lpq", "onBluetoothDisconnect: 蓝牙已断开");
            }
        });
        mController.setProtocol(new DncpProtocol(this, this));
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
        LogUtils.i(TAG + "lpq", "onClick: " + bluetoothAdapter.isEnabled());
        mController.startScan(new OnBluetoothScanListener() {
            @Override
            public void onBluetoothScanFindDevice(BluetoothDevice device, int rssi) {
                LogUtils.i(TAG + "lpq", "onBluetoothScanFindDevice: " + device.getAddress());
            }

            @Override
            public void onBluetoothScanFinish() {
                LogUtils.i(TAG + "lpq", "onBluetoothScanFinish: ");
            }
        });
    }

    /**
     * 停止搜索蓝牙
     * 搜索蓝牙会大量的占用系统资源，找到需要连接的设备之后，即可停止蓝牙的搜索
     */
    public void stopSearchBle(View v) {
        mController.stopScan();
    }

    /**
     * 连接蓝牙
     */
    public void connectBle(View v) {
        mController.connect(testBleMacAddress);
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
        mDncpControl.lockOrUnlock(errorTestOperateKey, new OnDncpOperateResult<Integer>() {
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
     * 清空锁具测试
     */
    public void btnLockClearTest(View v) {
        mDncpControl.resetLock(errorTestOrgKey, new OnDncpOperateResult() {
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


}
