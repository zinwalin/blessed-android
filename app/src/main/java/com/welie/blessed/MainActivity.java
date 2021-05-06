package com.welie.blessed;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.welie.blessed.adapter.DeviceAdapter;
import com.welie.blessed.detail.DeviceDetailsActivity;
import com.welie.blessed.misc.ObserverManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 *
 * android.permission.BLUETOOTH : 这个权限允许程序连接到已配对的蓝牙设备, 请求连接/接收连接/传输数据需要此权限, 主要用于对配对后进行操作;  https://developer.android.com/reference/android/Manifest.permission
 * android.permission.BLUETOOTH_ADMIN : 这个权限允许程序发现和配对蓝牙设备, 该权限用来管理蓝牙设备, 有了这个权限, 应用才能使用本机的蓝牙设备, 主要用于对配对前的操作;  https://developer.android.com/reference/android/Manifest.permission
 * android.permission.ACCESS_COARSE_LOCATION 和 android.permission.ACCESS_FINE_LOCATION：Android 6.0(MarshMallow API 21)以后，这两个权限是必须的，蓝牙扫描周围的设备需要获取模糊的位置信息。这两个权限属于同一组隐私敏感权限，在清单文件中声明之后，还需要再运行时动态获取。 https://developer.android.com/training/location/permissions
 *
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String[] NEXGIM_FITNESS_DEVICES = { "MG03", "RQ002", "QB-C01-1" };

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int ACCESS_LOCATION_REQUEST = 2;

    private Button btn_scan;
    private Button enable_bt;
    private ImageView img_loading;
    private Animation loadingAnimation;

    private DeviceAdapter deviceAdapter;

    private BluetoothCentralManager central;
    private final Handler handler = new Handler();
    private BluetoothCentralManagerCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerReceiver(btStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        callback = new BluetoothCentralManagerCallback() {
            @Override
            public void onConnectedPeripheral(@NotNull BluetoothPeripheral peripheral) {
                super.onConnectedPeripheral(peripheral);

                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                deviceAdapter.addDevice(peripheral);

                BluetoothGattCharacteristic characteristic = peripheral.getCharacteristic(UUID.fromString(BleConstants.UUID_GMS_SERVICE_GIMKIT_MAIN_SERVICE), UUID.fromString(BleConstants.UUID_GMS_CHARACTERISTIC_DEVICE_COMMAND_NOTIFY));

                boolean isNotifyEnabled = peripheral.setNotify(characteristic, true);
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onConnectionFailed(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
                super.onConnectionFailed(peripheral, status);
            }

            @Override
            public void onDisconnectedPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull HciStatus status) {
                super.onDisconnectedPeripheral(peripheral, status);

                Toast.makeText(MainActivity.this, "连接断开了", Toast.LENGTH_SHORT).show();
                deviceAdapter.removeDevice(peripheral);
                deviceAdapter.notifyDataSetChanged();
                ObserverManager.getInstance().notifyObserverDeviceDisconnected(peripheral);
            }

            @Override
            public void onDiscoveredPeripheral(@NotNull BluetoothPeripheral peripheral, @NotNull ScanResult scanResult) {
                super.onDiscoveredPeripheral(peripheral, scanResult);

                Log.d(TAG, "blessed-android...onDiscoveredPeripheral..." + peripheral);

//                deviceAdapter.clearScannedDevices();
                deviceAdapter.addDevice(peripheral);
                deviceAdapter.notifyDataSetChanged();

                if (deviceAdapter.getCount() > 2) {
                    // 搜索结束
                    central.stopScan();
                    img_loading.clearAnimation();
                    img_loading.setVisibility(View.INVISIBLE);
                    btn_scan.setText(getString(R.string.start_scan));
                }
            }

            @Override
            public void onScanFailed(@NotNull ScanFailure scanFailure) {
                super.onScanFailed(scanFailure);

                // 搜索结束
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
            }

            @Override
            public void onBluetoothAdapterStateChanged(int state) {
                super.onBluetoothAdapterStateChanged(state);
            }
        };


        central = new BluetoothCentralManager(this, callback, handler);

        initUIView();


        if (BluetoothAdapter.getDefaultAdapter() != null) {
            if (!isBluetoothEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        } else {
            Log.e(TAG,"This device has no Bluetooth hardware");
        }


        enable_bt = findViewById(R.id.btn_enable);
        enable_bt.setOnClickListener(this);

        enable_bt.setEnabled(!isBluetoothEnabled());

    }

    @Override
    protected void onResume() {
        super.onResume();
//    doScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btStateReceiver);

//    GimKitManager.getInstance().disconnectAllDevices();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    checkPermissions();
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    central.stopScan();
                }
                break;


            case R.id.btn_enable:
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                break;
        }
    }

    private boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) return false;

        return bluetoothAdapter.isEnabled();
    }


    private final BroadcastReceiver btStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) return;


            switch(action){


                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                    Log.d(TAG,"bluetooth adapter changed state to "+ state);

                    //根据当前系统蓝牙的打开、关闭状态实时更新“打开蓝牙”按钮可否被点击
                    if (state == BluetoothAdapter.STATE_ON) {
                        enable_bt.setEnabled(false);

                    } else if (state == BluetoothAdapter.STATE_OFF) {
                        enable_bt.setEnabled(true);
                    }

                    break;

                default:
                    Log.d(TAG,"some message received but needn't handle.");

                    break;

            }

        }
    };


    private void initUIView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);

        img_loading = findViewById(R.id.img_loading);
        loadingAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        loadingAnimation.setInterpolator(new LinearInterpolator());

        deviceAdapter = new DeviceAdapter(this);
        ListView listView = findViewById(R.id.list_device);
        listView.setAdapter(deviceAdapter);

    deviceAdapter.setOnClickDeviceListener(new DeviceAdapter.OnClickDeviceListener() {
        @Override
        public void onConnect(BluetoothPeripheral device) {
            // TODO check isConnected or not
            doConnect(device);
        }

        @Override
        public void onDisconnect(BluetoothPeripheral device) {
            central.cancelConnection(device);
        }

        @Override
        public void onDeviceDetails(BluetoothPeripheral device) {
            BluetoothGattCharacteristic characteristic = device.getCharacteristic(UUID.fromString(BleConstants.UUID_GMS_SERVICE_GIMKIT_MAIN_SERVICE), UUID.fromString(BleConstants.UUID_GMS_CHARACTERISTIC_DEVICE_COMMAND_NOTIFY));
            for (int i = 0; i < 1000; i++) {
                device.writeCharacteristic(characteristic, new byte[]{ 0x0, 0x44, (byte)0x39, (byte)0xF4, (byte)0x3B, (byte)0xC0 }, WriteType.WITH_RESPONSE );
            }
            return;

//            if (device.getState() == ConnectionState.CONNECTED) {
//                Intent intent = new Intent(MainActivity.this, DeviceDetailsActivity.class);
//                intent.putExtra(DeviceDetailsActivity.DETAILS_INTENT_KEY, device);
//                startActivity(intent);
//            }
        }
    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == REQUEST_ENABLE_BT){
            Log.d(TAG,"got result from REQUEST_ENABLE_BT: " + resultCode);//resultCode=0(拒绝)/-1(允许)
        }
    }

    private void doScan() {
        deviceAdapter.clearScannedDevices();
        deviceAdapter.notifyDataSetChanged();
        img_loading.startAnimation(loadingAnimation);
        img_loading.setVisibility(View.VISIBLE);
        btn_scan.setText(getString(R.string.stop_scan));

        central.scanForPeripheralsWithNames( NEXGIM_FITNESS_DEVICES );
    }

    private void doConnect(BluetoothPeripheral device) {
        central.connectPeripheral(device, new BluetoothPeripheralCallback() {
            @Override
            public void onServicesDiscovered(@NotNull BluetoothPeripheral peripheral) {
                super.onServicesDiscovered(peripheral);
            }

            @Override
            public void onNotificationStateUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
                super.onNotificationStateUpdate(peripheral, characteristic, status);
            }

            @Override
            public void onCharacteristicUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
                super.onCharacteristicUpdate(peripheral, value, characteristic, status);

                ObserverManager.getInstance().notifyDataReceived(peripheral, 8, 10.0, 20.0);
            }

            @Override
            public void onCharacteristicWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattCharacteristic characteristic, @NotNull GattStatus status) {
                super.onCharacteristicWrite(peripheral, value, characteristic, status);
                Log.d("blessed-android", "onCharacteristicWrite");
            }

            @Override
            public void onDescriptorRead(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattDescriptor descriptor, @NotNull GattStatus status) {
                super.onDescriptorRead(peripheral, value, descriptor, status);
            }

            @Override
            public void onDescriptorWrite(@NotNull BluetoothPeripheral peripheral, @NotNull byte[] value, @NotNull BluetoothGattDescriptor descriptor, @NotNull GattStatus status) {
                super.onDescriptorWrite(peripheral, value, descriptor, status);
            }

            @Override
            public void onBondingStarted(@NotNull BluetoothPeripheral peripheral) {
                super.onBondingStarted(peripheral);
            }

            @Override
            public void onBondingSucceeded(@NotNull BluetoothPeripheral peripheral) {
                super.onBondingSucceeded(peripheral);
            }

            @Override
            public void onBondingFailed(@NotNull BluetoothPeripheral peripheral) {
                super.onBondingFailed(peripheral);
            }

            @Override
            public void onBondLost(@NotNull BluetoothPeripheral peripheral) {
                super.onBondLost(peripheral);
            }

            @Override
            public void onReadRemoteRssi(@NotNull BluetoothPeripheral peripheral, int rssi, @NotNull GattStatus status) {
                super.onReadRemoteRssi(peripheral, rssi, status);
            }

            @Override
            public void onMtuChanged(@NotNull BluetoothPeripheral peripheral, int mtu, @NotNull GattStatus status) {
                super.onMtuChanged(peripheral, mtu, status);
            }

            @Override
            public void onPhyUpdate(@NotNull BluetoothPeripheral peripheral, @NotNull PhyType txPhy, @NotNull PhyType rxPhy, @NotNull GattStatus status) {
                super.onPhyUpdate(peripheral, txPhy, rxPhy, status);
            }

            @Override
            public void onConnectionUpdated(@NotNull BluetoothPeripheral peripheral, int interval, int latency, int timeout, @NotNull GattStatus status) {
                super.onConnectionUpdated(peripheral, interval, latency, timeout, status);
            }
        });
//    GimKitManager.getInstance().stopDiscovery();
//    GimKitManager.getInstance().connect(device, new GimKitDeviceListener() {
//
//        @Override
//        public void onDataReceived(int cadence, double power, double torque) {
//            Log.d(TAG, "blessed-android...onDataReceived..." + device.getUniqueKey() + cadence + " | " + power + " | " + torque);
//
//            ObserverManager.getInstance().notifyDataReceived(device, cadence, power, torque);
//        }
//
//        @Override
//        public void onConnectionChanged(int status, int errorCause) {
//            Log.d(TAG, "blessed-android...onConnectionChanged..." + status + " | " + errorCause);
//
//            if (status == GimKitDeviceConnectionStatus.CONNECTED.getCode()) {
//                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
//                deviceAdapter.addDevice(device);
//                deviceAdapter.notifyDataSetChanged();
//            } else if (status == GimKitDeviceConnectionStatus.DISCONNECTED.getCode()) {
//                Toast.makeText(MainActivity.this, "连接断开了", Toast.LENGTH_SHORT).show();
//                deviceAdapter.removeDevice(device);
//                deviceAdapter.notifyDataSetChanged();
//                ObserverManager.getInstance().notifyObserverDeviceDisconnected(device);
//            } else if (status == GimKitDeviceConnectionStatus.CONNECTING.getCode()) {
//                Toast.makeText(MainActivity.this, "正在连接中...", Toast.LENGTH_SHORT).show();
//            } else if (status == GimKitDeviceConnectionStatus.FAILED.getCode()) {
//                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
//            }
//        }
//    });
    }



    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] missingPermissions = getMissingPermissions(getRequiredPermissions());
            if (missingPermissions.length > 0) {
                requestPermissions(missingPermissions, ACCESS_LOCATION_REQUEST);//->回调onRequestPermissionsResult()，requestCode==ACCESS_LOCATION_REQUEST
            } else {
                permissionsGranted();
            }
        }
    }


    private String[] getMissingPermissions(String[] requiredPermissions) {
        List<String> missingPermissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String requiredPermission : requiredPermissions) {
                if (getApplicationContext().checkSelfPermission(requiredPermission) != PackageManager.PERMISSION_GRANTED) {
                    missingPermissions.add(requiredPermission);
                }
            }
        }
        return missingPermissions.toArray(new String[0]);
    }

    private String[] getRequiredPermissions() {
        int targetSdkVersion = getApplicationInfo().targetSdkVersion;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSdkVersion >= Build.VERSION_CODES.Q)
            return new String[] {Manifest.permission.ACCESS_FINE_LOCATION};
        else return new String[] {Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    private void permissionsGranted() {
        // Check if Location services are on because they are required to make scanning work
        if (checkLocationServices()) {
            doScan();
        }
    }

    private boolean areLocationServicesEnabled() {
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Log.e(TAG,"could not get location manager");
            return false;
        }

        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return isGpsEnabled || isNetworkEnabled;
    }

    //检查手机的“位置服务”功能有没有开启
    private boolean checkLocationServices() {
        if (!areLocationServicesEnabled()) {
            new android.app.AlertDialog.Builder(MainActivity.this)
                    .setTitle("Location services are not enabled")
                    .setMessage("Scanning for Bluetooth peripherals requires locations services to be enabled.") // Want to enable?
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // if this button is clicked, just close
                            // the dialog box and do nothing
                            dialog.cancel();
                        }
                    })
                    .create()
                    .show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if all permission were granted
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            permissionsGranted();
        } else {


            boolean allItemsDoNotAskAgain = true;

            for (String permission : permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,permission) ) {
                    allItemsDoNotAskAgain = false;
                    break;
                }
            }



            if(allItemsDoNotAskAgain){
                Toast.makeText(MainActivity.this, "Please set location permission by your self.", Toast.LENGTH_SHORT).show();

            }
            else {

                new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Location permission is required for scanning Bluetooth peripherals")
                        .setMessage("Please grant permissions")
                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                                checkPermissions();
                            }
                        })
                        .create()
                        .show();
            }


        }
    }



}