package com.welie.blessed.misc;

import com.welie.blessed.BluetoothPeripheral;

public interface Observable {

    void addObserver(Observer obj);

    void deleteObserver(Observer obj);

    void notifyObserverDeviceDisconnected(BluetoothPeripheral device);

    void notifyDataReceived(BluetoothPeripheral device, int cadence, double power, double torque);
}
