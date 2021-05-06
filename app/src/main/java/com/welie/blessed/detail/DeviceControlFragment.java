package com.welie.blessed.detail;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.welie.blessed.BleConstants;
import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.R;
import com.welie.blessed.WriteType;

import java.util.Random;
import java.util.UUID;

@TargetApi(Build.VERSION_CODES.M)
public class DeviceControlFragment extends Fragment {

    private static final String TAG = "DeviceControlFragment";
    private LinearLayout layout_container;
    private TextView powerTV;
    private TextView cadenceTV;
    private TextView torqueTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_device_control, null);
        initView(v);
        return v;
    }

    private void initView(View v) {
        final BluetoothPeripheral device = ((DeviceDetailsActivity) getActivity()).getGimKitDevice();

        layout_container = (LinearLayout) v.findViewById(R.id.layout_device_controls);

        powerTV = (TextView) v.findViewById(R.id.tv_power);
        cadenceTV = (TextView) v.findViewById(R.id.tv_cadence);
        torqueTV = (TextView) v.findViewById(R.id.tv_torque);

        Button btn_set_torque = v.findViewById(R.id.btn_torque);
        btn_set_torque.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        double min = 0.0, max = 40.0;
                        double torque = min + (max - min) * (new Random()).nextDouble();

                        BluetoothGattCharacteristic characteristic = device.getCharacteristic(UUID.fromString(BleConstants.UUID_GMS_SERVICE_GIMKIT_MAIN_SERVICE), UUID.fromString(BleConstants.UUID_GMS_CHARACTERISTIC_DEVICE_COMMAND_NOTIFY));
                        device.writeCharacteristic(characteristic, new byte[]{ 0x0, 0x44, 0x39, (byte)0xaa, (byte)0xee, (byte)0xc0 }, WriteType.WITH_RESPONSE );
                        Toast.makeText(getActivity(),
                                "扭矩设置成功\n" +
                                        "扭矩值为: " + torque
                                , Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        Button btn_device_info = v.findViewById(R.id.btn_info);
        btn_device_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        GimKitDeviceInfo deviceInfo = GimKitManager.getInstance().getDeviceInfo(device);
//                        Log.d(TAG,"Device Info: " + deviceInfo.getDashboardSN() + deviceInfo.getDashboardVersion()
//                                + deviceInfo.getMotorSN() + deviceInfo.getMotorVersion());
//                        Toast.makeText(getActivity(),
//                                "设备信息\n" +
//                                        "表盘序列号: " + deviceInfo.getDashboardSN() +
//                                        "\n表盘版本: " + deviceInfo.getDashboardVersion() +
//                                        "\n电机序列号: " + deviceInfo.getMotorSN() +
//                                        "\n电机版本: " + deviceInfo.getMotorVersion(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public void setRTData(int cadence, double power, double torque) {
        if (powerTV != null) powerTV.setText(String.format("%s", power));
        if (cadenceTV != null) cadenceTV.setText(String.format("%s", cadence));
        if (torqueTV != null) torqueTV.setText(String.format("%s", torque));
    }

    private void runOnUiThread(Runnable runnable) {
        if (isAdded() && getActivity() != null)
            getActivity().runOnUiThread(runnable);
    }
}
