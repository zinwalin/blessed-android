package com.welie.blessed.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.ConnectionState;
import com.welie.blessed.R;
import java.util.ArrayList;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<BluetoothPeripheral> devicesList;
    private List<BluetoothPeripheral> connectedDeviceList;
    private OnClickDeviceListener deviceClickListener;

    public DeviceAdapter(Context context) {
        this.context = context;
        devicesList = new ArrayList<>();
    }

    public void addDevice(BluetoothPeripheral device) {
        removeDevice(device); // 扫描的设备转为连接设备，先删除再添加，为了刷新设备的单行界面
        devicesList.add(device);
    }

    public void removeDevice(BluetoothPeripheral device) {
        for (int i = 0; i < devicesList.size(); i++) {
            BluetoothPeripheral item = devicesList.get(i);
            if (device.getAddress().equals(item.getAddress())) {
                devicesList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        for (int i = 0; i < devicesList.size(); i++) {
            BluetoothPeripheral device = devicesList.get(i);
            if (device.getState() == ConnectionState.CONNECTED) {
                devicesList.remove(i);
            }
        }
    }

    public void clearScannedDevices() {
        for (int i = 0; i < devicesList.size(); i++) {
            BluetoothPeripheral device = devicesList.get(i);
            if (!(device.getState() == ConnectionState.CONNECTED)) {
                devicesList.remove(i);
            }
        }
    }

    @Override
    public int getCount() {
        return devicesList.size();
    }

    @Override
    public BluetoothPeripheral getItem(int position) {
        if (position >= devicesList.size())
            return null;
        return devicesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.adapter_device, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.txt_name = convertView.findViewById(R.id.txt_name);
            holder.txt_mac = convertView.findViewById(R.id.txt_mac);
            holder.layout_idle = convertView.findViewById(R.id.layout_idle);
            holder.layout_connected = convertView.findViewById(R.id.layout_connected);
            holder.btn_connect = convertView.findViewById(R.id.btn_connect);
            holder.btn_disconnect = convertView.findViewById(R.id.btn_disconnect);
            holder.btn_details = convertView.findViewById(R.id.btn_details);
        }

        final BluetoothPeripheral device = getItem(position);
        if (device != null) {
            boolean isConnected = device.getState() == ConnectionState.CONNECTED;
            String name = device.getName();
            String mac = device.getAddress();
            holder.txt_name.setText(name);
            holder.txt_mac.setText(mac);

            if (isConnected) {
                // 连接设备的界面
                holder.layout_idle.setVisibility((View.GONE));
                holder.layout_connected.setVisibility((View.VISIBLE));
            } else {
                // 扫描的设备界面
                holder.layout_idle.setVisibility((View.VISIBLE));
                holder.layout_connected.setVisibility((View.GONE));
            }
        }

        holder.btn_connect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (deviceClickListener != null) {
                    deviceClickListener.onConnect(device);
                }
            }

        });

        holder.btn_disconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (deviceClickListener != null) {
                    deviceClickListener.onDisconnect(device);
                }
            }

        });

        holder.btn_details.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (deviceClickListener != null) {
                    deviceClickListener.onDeviceDetails(device);
                }
            }

        });

        return convertView;
    }

    public void setOnClickDeviceListener(OnClickDeviceListener listener) {
        this.deviceClickListener = listener;
    }

    class ViewHolder {
        TextView txt_name;
        TextView txt_mac;
        LinearLayout layout_idle;
        LinearLayout layout_connected;
        Button btn_connect;
        Button btn_disconnect;
        Button btn_details;
    }

    public interface OnClickDeviceListener {
        void onConnect(BluetoothPeripheral device);

        void onDisconnect(BluetoothPeripheral device);

        void onDeviceDetails(BluetoothPeripheral device);
    }
}
