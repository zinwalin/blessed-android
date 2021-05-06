package com.welie.blessed.misc;

import com.welie.blessed.BluetoothPeripheral;

import java.util.ArrayList;
import java.util.List;

public class ObserverManager implements Observable {

    public static ObserverManager getInstance() {
        return ObserverManagerHolder.sObserverManager;
    }

    private static class ObserverManagerHolder {
        private static final ObserverManager sObserverManager = new ObserverManager();
    }

    private List<Observer> observers = new ArrayList<>();

    @Override
    public void addObserver(Observer obj) {
        observers.add(obj);
    }

    @Override
    public void deleteObserver(Observer obj) {
        int i = observers.indexOf(obj);
        if (i >= 0) {
            observers.remove(obj);
        }
    }

    @Override
    public void notifyObserverDeviceDisconnected(BluetoothPeripheral device) {
        for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onDisconnected(device);
        }
    }

    @Override
    public void notifyDataReceived(BluetoothPeripheral device, int cadence, double power, double torque) {
        for (int i = 0; i < observers.size(); i++) {
            Observer o = observers.get(i);
            o.onDataReceived(device, cadence, power, torque);
        }
    }
}
