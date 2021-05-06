package com.welie.blessed.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.welie.blessed.BluetoothPeripheral;
import com.welie.blessed.R;
import com.welie.blessed.misc.Observer;
import com.welie.blessed.misc.ObserverManager;
//
import java.util.ArrayList;
import java.util.List;

public class DeviceDetailsActivity extends AppCompatActivity implements Observer {

    public static final String DETAILS_INTENT_KEY = "DETAILS_INTENT_KEY";

    private BluetoothPeripheral gimKitDevice;
    private Toolbar toolbar;
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_details);
        initData();
        initView();

        ObserverManager.getInstance().addObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().deleteObserver(this);
    }

    @Override
    public void onDisconnected(BluetoothPeripheral device) {
        if (device != null && gimKitDevice != null && device.getAddress().equals(gimKitDevice.getAddress())) {
            finish();
        }
    }

    @Override
    public void onDataReceived(BluetoothPeripheral device, int cadence, double power, double torque) {
        if (device != null && gimKitDevice != null && device.getAddress().equals(gimKitDevice.getAddress())) {
            if (fragments.size() < 1) return;
            Fragment controlFragment = fragments.get(0);
            if (controlFragment instanceof DeviceControlFragment) {
                ((DeviceControlFragment)controlFragment).setRTData(cadence, power, torque);
            }
        }
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(gimKitDevice.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // init fragments
        fragments.add(new DeviceControlFragment());
        for (Fragment fragment : fragments) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, fragment).hide(fragment).commit();
        }

        updateFragment(0); // 暂时只有DeviceControlFragment
    }

    private void initData() {
        gimKitDevice = getIntent().getParcelableExtra(DETAILS_INTENT_KEY);
        if (gimKitDevice == null)
            finish();
    }

    private void updateFragment(int position) {
        if (position > fragments.size() - 1) {
            return;
        }
        for (int i = 0; i < fragments.size(); i++) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = fragments.get(i);
            if (i == position) {
                transaction.show(fragment);
            } else {
                transaction.hide(fragment);
            }
            transaction.commit();
        }
    }

    public BluetoothPeripheral getGimKitDevice() {
        return gimKitDevice;
    }
}
