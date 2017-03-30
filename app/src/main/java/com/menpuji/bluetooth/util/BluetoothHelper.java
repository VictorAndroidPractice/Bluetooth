package com.menpuji.bluetooth.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BluetoothHelper {
    private static BluetoothHelper instance;
    private Map<String, BluetoothSocket> mBluetoothSockets = new HashMap<String, BluetoothSocket>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (mDiscoveryCallback != null) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDiscoveryCallback.success(device);
                } else {
                    mDiscoveryCallback.error();
                }
            }
        }
    };
    private DiscoveryCallback mDiscoveryCallback;

    private BluetoothHelper() {

    }

    public static BluetoothHelper getInstance() {
        if (instance == null) {
            instance = new BluetoothHelper();
        }
        return instance;
    }

    public boolean isSupport() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public boolean isEnabled() {
        return isSupport() && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public Map<String, BluetoothDevice> getDevices() {
        Map<String, BluetoothDevice> devices = new HashMap<String, BluetoothDevice>();
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                devices.put(device.getAddress(), device);
            }
        }
        return devices;
    }

    public void connect(BluetoothDevice device) {
        if (mBluetoothSockets.containsKey(device.getAddress())) {
            if (!mBluetoothSockets.get(device.getAddress()).isConnected()) {
                try {
                    mBluetoothSockets.get(device.getAddress()).connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                BluetoothSocket bluetoothSocket;
                if (device.getUuids() != null) {
                    for (ParcelUuid uuid : device.getUuids()) {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid.getUuid());
                        if (bluetoothSocket == null) {
                            continue;
                        }
                        mBluetoothSockets.put(device.getAddress(), bluetoothSocket);
                        bluetoothSocket.connect();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(String address) {
        if (mBluetoothSockets.get(address) != null) {
            try {
                mBluetoothSockets.get(address).close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startDiscovery(Context context, DiscoveryCallback discoveryCallback) {
        mDiscoveryCallback = discoveryCallback;
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mReceiver, filter);
        BluetoothAdapter.getDefaultAdapter().startDiscovery();
    }

    private boolean cancelDiscovery() {
        return BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    }

    public Map<String, BluetoothSocket> getBluetoothSockets() {
        return mBluetoothSockets;
    }

    public interface DiscoveryCallback {
        void success(BluetoothDevice device);

        void error();
    }
}
