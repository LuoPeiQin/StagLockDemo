
# 施泰蓝牙锁SDK使用范例 - BSCP协议
## 如何使用
### 导入包
```java
implementation 'com.stag:bluetooth:1.0.4'
implementation 'com.stag:bluetoothbusiness:1.0.9'
```
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
mController.setProtocol(new DscpProtocol(this, this));
```

第二个参数是一个接口，用于返回连接蓝牙设备后，设备主动上报的事件：

```java
/**
 * Dscp蓝牙设备抓到上报事件回调
 */
public interface OnDscpEventListener extends OnEventListener {
	/**
	  * 接收到设备状态变化事件
	  */
    void onRecvDeviceStatus(byte[] bytes);
    
    /**
	  * 接收到指纹录入事件
	  */
    void onRecvAddFpEvent(byte[] bytes);

	/**
	  * 接收到IC卡录入事件
	  */
    void onRecvIcCardEvent(byte[] bytes);
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
> 3. **连接之后，必须先设置会话码，并在成功之后才能发送其它命令**
> 4. 设备注册以后调用开关锁才能生效，否则设备不会有回应；

### 设置会话码
```java
    /**
     * 获取锁具安全信息
     */
    public void getLockSecureInfo(View v) {
        int userId = 1; // 用户ID
        dscpUtil.setSessionCode(userId, new OnTimeoutResult<SecureInfo>() {
            @Override
            public void onResult(boolean isTimeout, SecureInfo result) {
                LogUtils.d(TAG + "lpq", "onResult: " + result);
            }
        }, true);
    }
```
SecureInfo类说明：

```java
public class SecureInfo {
    private boolean isSetSucceed; // 0 设置成功
    private boolean isLocked; // 0 关 1 开
    private int userId; // 用户id
    private byte[] sessionCode; // 会话码
    private int lastDisconnectCode; // 上一次的蓝牙断开原因
    private int bleMTU; // 蓝牙MTU
}
```
### 注册锁具
```java
    /**
     * 锁具注册
     */
    public void btnLockRegister(View v) {
        Date curDate = new Date(); // 当前事件
        UUID operateKey = testOperateKey; // 操作密钥，保护锁具安全，不能丢失！
        int userId = 1; // 用户ID
        int ipPort = 80; // 联网ip端口号：设备是NB联网设备时，该参数有效
        String ip = "no"; // 联网ip：设备是NB联网设备时，该参数有效
        dscpUtil.registerDevice(curDate, operateKey, userId, ipPort, ip,
                new OnTimeoutResult<Boolean>() {
                    @Override
                    public void onResult(boolean isTimeout, Boolean result) {
                        LogUtils.d(TAG + "lpq", "onResult: 注册结果 = " + result);
                    }
                }, true);
    }
```
### 开锁
```java
    /**
     * 开锁
     */
    public void btnUnLock(View view) {
        Date curDate = new Date(); // 当前事件
        int userId = 1; // 用户ID
        byte lockIndex = 1; // 锁具下标（从1开始计数）
        dscpUtil.unlock(curDate, userId, lockIndex, new OnTimeoutResult<Boolean>() {
            @Override
            public void onResult(boolean isTimeout, Boolean result) {
                LogUtils.d(TAG + "lpq", "onResult: 开锁结果 = " + result);
            }
        }, true);
    }
```
### 上锁
```java
    /**
     * 上锁
     */
    public void btnLock(View view) {
        Date curDate = new Date(); // 当前事件
        int userId = 1; // 用户ID
        byte lockIndex = 1; // 锁具下标（从1开始计数）
        dscpUtil.lock(curDate, userId, lockIndex, new OnTimeoutResult<Boolean>() {
            @Override
            public void onResult(boolean isTimeout, Boolean result) {
                LogUtils.d(TAG + "lpq", "onResult: 上锁结果 = " + result);
            }
        }, true);
    }
```
### 清空
```java
    /**
     * 清空锁具（恢复出厂设置）
     */
    public void btnLockClear(View v) {
        Date curDate = new Date(); // 当前事件
        int userId = 1; // 用户ID
        byte lockIndex = 1; // 锁具下标（从1开始计数）
        dscpUtil.clear(curDate, userId, lockIndex, new OnTimeoutResult<Boolean>() {
            @Override
            public void onResult(boolean isTimeout, Boolean result) {
                LogUtils.d(TAG + "lpq", "onResult: 清空结果 = " + result);
            }
        }, true);
    }
```
### 获取锁具状态

```java
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
```

DeviceStatus类说明：

```java
public class DeviceStatus {
    private double electricity; // 电量 V
    private int rsrp; // NB信号
    private int sinr; // 信噪比
    private boolean isLocked; // 是否处于上锁状态
    private byte bolt;  //	主锁锁舌开关状态	0缩回，1伸出
    private byte antiLock; //	反锁锁体开关状态	0.缩回，1.伸出
    private byte doorMagnet; //  门磁开关状态	0关，1开
    private byte fingerprintUsed; // 指纹已使用个数
    private byte fingerprintLeft; //Int	指纹剩余可用个数
    private byte passwordUsed; //Int	密码已使用个数
    private byte passwordLeft; //Int	密码剩余可用个数
    private byte icCardUsed; //Int	IC卡已使用个数
    private byte icCardLeft; //Int	IC卡剩余可用个数
    private int logMaxNumber; // 日志最大能存个数
    private int logIndex; // 最新日志下标
    private int ungetLogNumber; // 未获取日志数
    private int ungetLogIndex; // 未获取日志下标
    private Date logTime; // 最新日志时间
    private byte lockMode; // 回锁模式：1：按时间回锁 2：不自动回锁
    private byte lockTime; // 自动回锁时间：只有回锁模式为1时有效，时间单位s秒
}
```

### 获取锁具信息

```java
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
```

DeviceInfo类说明：

```java
public class DeviceInfo {
    private String hardwareVersion; // 硬件版本
    private String softwareVersion; // 软件版本
    private int typeCode; // 设备类型编码
    private byte supportFunctionBase; // 功能支持
    private byte supportFunctionOther; // 其它功能支持
    private int userId; // 注册设备用户ID
    private boolean isLock; // 是否上锁
    private Date deviceDate; // 设备时间
    private byte[] sn; // 设备sn号
    private int broadcastInterval; // 蓝牙关闭间隔
    private int deviceSvnVersion; // 设备svn版本
    private int spare2; // 备用字段
    private int ipPort; // 备用字段
    private int ipLength; // ip长度
    private String ip; // ip
    private String fpModel; // 指纹型号
}
```

