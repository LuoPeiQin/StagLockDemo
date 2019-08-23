# 施泰蓝牙锁SDK使用范例

## 如何使用

### 导入包

```java
implementation 'com.stag:bluetooth:1.0.2'
implementation 'com.stag:bluetoothbusiness:1.0.2'
```

### 生成包需要的so库

```java
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DncpControl.loadLibraries();
    }
}
```

您需要在使用该库前执行该命令：**DncpControl.loadLibraries()**

### 其它

其它请参考下面的文档和Demo示例。

## 基础概念

### 锁具基本信息

1. 锁具ID：使用UUID表示，是锁具的唯一识别标志；
2. 门磁：锁具连接门的传感器，表示门的开关（并不是所有锁具都有）；
3. 把手：判断把手的开关状态；

### 锁具状态

1. 未注册（空锁）：此时锁具未注册，可以直接进行开关；
2. 已注册：此时锁具已经注册，需要使用正确的密钥才能开锁；

### 注意事项

1. Android6.0以后，大部分Android手机需要申请定位权限才能正常使用蓝牙功能；

## 蓝牙操作相关接口说明

> **调用的类为com.stag.bluetooth.BluetoothControl.java**

### 获取单例对象

```java
BluetoothController mController = BluetoothController.getController(this);
```

### 注册蓝牙状态监听

```java
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
```

### 注册蓝牙连接状态变化监听

```java
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
```

### 设置蓝牙传输协议（该方法是与设备操作相关的方法，而且必须在连接蓝牙设备之前设置）

```java
mController.setProtocol(new DncpProtocol(this, this));
```

第二个参数是一个接口，用于返回连接蓝牙设备后，设备主动上报的事件：

```java
/**
 * Dncp蓝牙设备抓到上报事件回调
 */
public interface OnDncpEventListener extends OnEventListener {

    /**
     * 钥匙连上锁具
     * */
    void onDncpLockConnect();

    /**
     * 钥匙与锁具分开
     * */
    void onDncpLockDisconnect();

    /**
     * 钥匙低电量
     * */
    void onDncpLowPower();

    /**
     * 上锁事件
     * 主要用于蓝牙锁等手动上锁的情况
     * */
    void onDncpLock();

    /**
     * 开锁事件
     * 主要用于蓝牙锁等手动开锁的情况
     * */
    void onDncpUnlock();

}
```

### 搜索蓝牙

```java
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
```

### 停止搜索蓝牙

```java
mController.stopScan();
```

### 连接蓝牙与断开蓝牙连接

```java
mController.connect("0C:B2:B7:3E:23:60");	//连接蓝牙，参数为蓝牙MAC地址
mController.disconnect();	//断开连接
```

### 取消注册监听回调

```java
	@Override
    protected void onDestroy() {
        super.onDestroy();
        if (mController != null) {
            mController.unregisterBluetoothStateChangeListener();
            mController.unregisterConnectStateChangeListener();
        }
    }
```

## 设备操作相关接口说明

> **操作类为：com.staginfo.segs.sterm.DncpControl.java**

> 说明：
>
> 1. 以下接口都需要通过BluetoothControl类连接了蓝牙设备才能使用；
> 2. 以下接口均为异步接口；
> 3. 返回值由code，message，obj三个参数组成，**code为0是表示操作成功**，其它结果请参照message进行查看（message是对code的扩展说明），obj可能为空。需要返回有效信息（如获取锁具信息，获取锁具状态）时才会返回对应的obj；

### 开关锁

```java
    /**
     * 开关锁
     */
    public void btnLockOrUnlock(View v) {
        mDncpControl.lockOrUnlock(operateKey, new OnDncpOperateResult<Integer>() {
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
```

**lockOrUnlock**方法参数说明：

**参数1**：UUID类型的操作密钥，在注册锁具之后，必须使用注册时写入的唯一的操作密钥才能开锁。锁具未注册时，传入参数为空即可开锁；

**参数2**：开锁结果的异步回调接口，code为0时表示开锁或者上锁成功；

### 注册锁具

```java
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
```

**registerLock**方法参数说明：

**参数1**：RegisterLockParameter

```java
public class RegisterLockParameter {
    protected short organizationId; // 组织ID，可以设置任何short型的自己想要的值
    protected UUID organizationKey; // 组织密钥，用于将已注册的锁恢复出厂设置
    protected UUID operateKey; //操作密钥，用于开关锁具
}
```

> 注意：请妥善保管锁具的组织密钥和操作密钥，一但丢失，将无法对锁具进行开关锁和清空操作

**参数2**：注册结果的异步回调接口，code为0时表示注册成功；

### 清空锁具

```java
    /**
     * 清空锁具（恢复出厂设置，预置的锁具参数不会清空，但是会将锁具由注册状态变为未注册状态）
     */
    public void btnLockClear(View v) {
        mDncpControl.resetLock(organizationKey, new OnDncpOperateResult() {
            @Override
            public void onResult(int code, String message, Object obj) {
                LogUtils.i(TAG + "lpq", "清空: code = " + code);
                LogUtils.i(TAG + "lpq", "清空: message = " + message);
                LogUtils.i(TAG + "lpq", "清空: obj = " + obj);
            }
        });
    }
```

**btnLockClear**方法参数说明：

**参数1**：注册锁具时写入的组织密钥

**参数2**：清空结果的异步回调接口，code为0时表示清空成功；

### 获取锁具状态

```java
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
```

SensorStatus类说明：

```java
public class SensorStatus {
    private boolean isHandleOn;//把手开关状态 true 表示开 false 表示关
    private boolean isLockTongueOn;//锁舌 true 表示开 false 表示关
    private boolean isDoorContactOn;//门磁 true 表示开 false 表示关
}
```

### 获取锁具信息

```java
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
```

LockInfo类说明：

```java
public class LockInfo {
    private UUID uuid; // 锁具唯一标识
    private String type; // 锁具类型
    private String model; // 型号
    private String sn; // 序列号
    private String hardwareVersion; // 硬件版本
    private String softwareVersion; // 软件版本
}
```
