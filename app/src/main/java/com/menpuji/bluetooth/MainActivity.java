package com.menpuji.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jinoux.android.jobluetooth.BluetoothLeService;
import com.menpuji.bluetooth.util.BleHelper;
import com.menpuji.bluetooth.util.BluetoothHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private BluetoothLeService mBluetoothLeService;
    private BluetoothGattCharacteristic mWrite;
    private BluetoothGattCharacteristic mRead;
    private BluetoothGattCharacteristic mReceive;
    private BluetoothGatt bluetoothGatt;
    private String mMaxPacketSizeUUID = "0000b353-0000-1000-8000-00805f9b34fb";
    private String mWriteUUID = "0000b352-0000-1000-8000-00805f9b34fb";
    private String mReadUUID = "0000b351-0000-1000-8000-00805f9b34fb";
    private String mReceiveUUID = "0000b356-0000-1000-8000-00805f9b34fb";
    private String mAddress = "00:1B:35:0E:82:7A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("onCreate");
        setContentView(R.layout.activity_main);
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
                mBluetoothLeService.initialize();
                mBluetoothLeService.connect(mAddress, 15);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(new Intent(this, BluetoothLeService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect:
                startBle();
                break;
            case R.id.btn_write:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothLeService.wirte(hexStringToBytes("100402"));
                    }
                }).start();
//                List<byte[]> bytesArray = bytes2BytesArray(hexStringToBytes("100402"), 18);
//                for (int i = 0; i < bytesArray.size(); i++) {
//                    mWrite.setValue(bytes2SequenceBytes((byte) i, bytesArray.get(i)));
//                    BleHelper.getInstance(MainActivity.this).writeCharacteristic(mWrite, bluetoothGatt);
//                    try {
//                        Thread.sleep(8);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                BluetoothGattCharacteristic characteristic = BleHelper.getInstance(this).getBluetoothGattCharacteristic(bluetoothGatt, "0000b35c-0000-1000-8000-00805f9b34fb");
//                characteristic.setValue(new byte[]{0x0});
//                BleHelper.getInstance(MainActivity.this).writeCharacteristic(characteristic, bluetoothGatt);
                break;
            case R.id.btn_read:
                break;
            default:
                break;
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        System.out.println(device.getName() + ":" + device.getAddress());
    }

    private void startBluetooth() {
        if (BluetoothHelper.getInstance().isEnabled()) {
            BluetoothHelper.getInstance().startDiscovery(this, new BluetoothHelper.DiscoveryCallback() {
                @Override
                public void success(BluetoothDevice device) {
                    String name = device.getName();
                    String address = device.getAddress();
                    System.out.println(name + ":" + address);
                    BluetoothHelper.getInstance().connect(device);
                    BluetoothSocket bluetoothSocket = BluetoothHelper.getInstance().getBluetoothSockets().get(address);
                    try {
                        if (bluetoothSocket != null) {
                            OutputStream out = bluetoothSocket.getOutputStream();
                            out.write(new byte[]{0x10, 0x04, 0x02});
                            out.flush();
                            byte[] buffer = new byte[1024];
                            InputStream in = bluetoothSocket.getInputStream();
                            in.read(buffer);
                            System.out.println(buffer[0]);
                            out.write(hexStringToBytes("1B402020202020202020202020202020202020202020202020202020202020200AB9A7CFB2A3ACB4F2D3A1BBFAD2D1BEADC1ACBDD3A3A120202020202020200A2020202020202020202020202020202020202020202020202020202020200AD1A1D4F1D6BDD0CDA3BA73697A652D3538202020202020202020202020200AC7EBBCECB2E9CFC2C3E6B5C4BAE1CFDFCAC7B7F1CEAAD5FBD0D0A3ACC8E70AB9FBCEB4B3C5C2FAD4F2D1A1D4F1B4F2D3A1BBFABFEDB6C8B9FDD0A1A3AC0AD5DBD0D0D4F2B1EDC3F7D1A1D4F1B4F2D3A1BBFABFEDB6C8B9FDBFEDA1A30A2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D2D0A2020202020202020202020202020202020202020202020202020202020200A1B320A0A0A0A"));
                            out.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void error() {
                    System.out.println("没有搜索到蓝牙设备");
                }
            });
        } else {
            System.out.println("无可用蓝牙设备");
        }
    }

    private void startBle() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BleHelper.getInstance(MainActivity.this).stopLeScan(MainActivity.this);
                if (BleHelper.getInstance(getApplicationContext()).isEnabled(MainActivity.this)) {
                    BleHelper.getInstance(getApplicationContext()).connect(MainActivity.this, mAddress, new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                System.out.println("连接成功");
                                bluetoothGatt = gatt;
                                gatt.discoverServices();
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                System.out.println("连接失败");
                            }
                        }

                        @Override
                        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                System.out.println("发现服务");
                                mReceive = BleHelper.getInstance(MainActivity.this).getBluetoothGattCharacteristic(gatt, mReceiveUUID);
                                mWrite = BleHelper.getInstance(MainActivity.this).getBluetoothGattCharacteristic(gatt, mWriteUUID);
                                mWrite.setWriteType(WRITE_TYPE_NO_RESPONSE);
                                mRead = BleHelper.getInstance(MainActivity.this).getBluetoothGattCharacteristic(gatt, mReadUUID);
                                BleHelper.getInstance(MainActivity.this).setCharacteristicNotification(mRead, gatt, true);
                                BluetoothGattCharacteristic readState = BleHelper.getInstance(MainActivity.this).getBluetoothGattCharacteristic(gatt, mMaxPacketSizeUUID);
                                BleHelper.getInstance(MainActivity.this).readCharacteristic(readState, gatt);
                            } else {
                                System.out.println("发现服务失败");
                            }
                        }

                        @Override
                        public void onCharacteristicRead(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicRead(gatt, characteristic, status);
                            System.out.println("onCharacteristicRead: " + characteristic.getValue()[0] + " status: " + status);
                        }

                        @Override
                        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                            super.onCharacteristicWrite(gatt, characteristic, status);
                            System.out.println("onCharacteristicWrite: " + bytesToHexString(characteristic.getValue()) + " status: " + status);
                        }

                        @Override
                        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                            super.onCharacteristicChanged(gatt, characteristic);
                            System.out.println("onCharacteristicChanged: " + bytesToHexString(characteristic.getValue()));
                            byte[] data = characteristic.getValue();
                            mReceive.setValue(new byte[]{data[0]});
                            BleHelper.getInstance(MainActivity.this).writeCharacteristic(mReceive, gatt);
                        }

                        @Override
                        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                            super.onDescriptorRead(gatt, descriptor, status);
                            System.out.println("onDescriptorRead: " + bytesToHexString(descriptor.getValue()) + " status: " + status);
                        }

                        @Override
                        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                            super.onDescriptorWrite(gatt, descriptor, status);
                            System.out.println("onDescriptorWrite: " + bytesToHexString(descriptor.getValue()) + " status: " + status);
                        }

                        @Override
                        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                            super.onReadRemoteRssi(gatt, rssi, status);
                            System.out.println("onReadRemoteRssi: " + rssi + " status: " + status);
                        }
                    });
                } else {
                    System.out.println("无可用蓝牙设备");
                }
            }
        }, 5000);
        BleHelper.getInstance(this).startLeScan(this);
    }

    private byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte aSrc : src) {
            int v = aSrc & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private static List<byte[]> bytes2BytesArray(byte[] src, int size) {
        List<byte[]> bytesArray = new ArrayList<byte[]>();
        int divide = src.length / size;
        for (int i = 0; i < divide; i++) {
            byte[] dest = new byte[size];
            System.arraycopy(src, i * size, dest, 0, size);
            bytesArray.add(dest);
        }
        int remain = src.length % size;
        if (remain != 0) {
            byte[] dest = new byte[remain];
            System.arraycopy(src, src.length - remain, dest, 0, dest.length);
            bytesArray.add(dest);
        }
        return bytesArray;
    }

    private static byte[] bytes2SequenceBytes(byte sequence, byte[] src) {
        byte[] dest = new byte[src.length + 2];
        dest[0] = sequence;
        System.arraycopy(src, 0, dest, 1, src.length);
        for (int i = 0; i < dest.length - 1; ++i) {
            dest[dest.length - 1] += dest[i];
        }
        return dest;
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        System.out.println("onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        System.out.println("onConfigurationChanged");
    }
}
