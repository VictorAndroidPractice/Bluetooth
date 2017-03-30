package com.menpuji.bluetooth.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.List;

public class BleHelper {
    private static BleHelper instance;
    private BluetoothAdapter mBluetoothAdapter;

    public static BleHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BleHelper(context);
        }
        return instance;
    }

    private BleHelper(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }

    public boolean isSupport(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && packageManager
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) && mBluetoothAdapter != null;
    }

    public boolean isEnabled(Context context) {
        return isSupport(context) && mBluetoothAdapter.isEnabled();
    }

    public void startLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter.startLeScan(leScanCallback);
        }
    }

    public void stopLeScan(BluetoothAdapter.LeScanCallback leScanCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    public BluetoothGatt connect(Context context, String address, BluetoothGattCallback bluetoothGattCallback) {
        if (mBluetoothAdapter == null || address == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            return device.connectGatt(context, false, bluetoothGattCallback);
        }
        return null;
    }

    public BluetoothGattCharacteristic getBluetoothGattCharacteristic(BluetoothGatt bluetoothGatt, String uuid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            List<BluetoothGattService> bluetoothGattServices = bluetoothGatt.getServices();
            for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
                List<BluetoothGattCharacteristic> bluetoothGattCharacteristics = bluetoothGattService.getCharacteristics();
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristics) {
                    if (uuid.equalsIgnoreCase(bluetoothGattCharacteristic.getUuid().toString())) {
                        return bluetoothGattCharacteristic;
                    }
                }
            }
        }
        return null;
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGatt bluetoothGatt) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGatt bluetoothGatt) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothGatt.readCharacteristic(characteristic);
        }
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              BluetoothGatt bluetoothGatt, boolean enabled) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getUuid());
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                bluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }
}
