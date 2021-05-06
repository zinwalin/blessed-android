package com.welie.blessed.misc;

import com.welie.blessed.BluetoothPeripheral;

public interface Observer {

    void onDisconnected(BluetoothPeripheral device);

    void onDataReceived(BluetoothPeripheral device, int cadence, double power, double torque);
}
