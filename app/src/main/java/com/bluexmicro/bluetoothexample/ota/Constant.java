package com.bluexmicro.bluetoothexample.ota;

import android.bluetooth.BluetoothGattCharacteristic;

import java.util.UUID;

public class Constant {
    public static final byte PARAMS_REQUEST_OTA_START = 0;
    public static final byte RESPONSE_CTRL_OTA_START = 1;
    public static final byte REQUEST_CTRL_NEW_BLOCK = 2;
    public static final byte PARAMS_REQUEST_CTRL_DATA_FINISH = 3;
    public static final byte PARAMS_REQUEST_APPLY = 0x0A;
    public static final byte PARAMS_REQUEST_REBOOT = 0X0B;

    public static final UUID UUID_BX_DEFAULT_OTA_SERVICE = UUID.fromString("00002600-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BX_DEFAULT_CTRL = UUID.fromString("00007000-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BX_DEFAULT_DATA = UUID.fromString("00007001-0000-1000-8000-00805f9b34fb");

    public static final int CTRL_REQUIRED_PROPERTIES = BluetoothGattCharacteristic.PROPERTY_INDICATE | BluetoothGattCharacteristic.PROPERTY_WRITE;
    public static final int DATA_REQUIRED_PROPERTIES = BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;

    public static final UUID UUID_CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

}
