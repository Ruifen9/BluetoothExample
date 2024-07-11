package com.bluexmicro.bluetoothexample.environment;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class BluetoothEnvironment {
    private boolean TOGGLE_BLUETOOTH = true;
    private boolean TOGGLE_LOCATION = true;
    private boolean PERMISSION_LOCATION = true;
    private boolean PERMISSION_BLUETOOTH_SCAN = true;
    private boolean PERMISSION_BLUETOOTH_CONNECT = true;

    public boolean isScanReady() {
        return TOGGLE_BLUETOOTH && TOGGLE_LOCATION && PERMISSION_LOCATION && PERMISSION_BLUETOOTH_SCAN;
    }

    public boolean isConnectReady() {
        return TOGGLE_BLUETOOTH && PERMISSION_BLUETOOTH_CONNECT;
    }

    public boolean isSanAndConnectReady() {
        return TOGGLE_BLUETOOTH && TOGGLE_LOCATION && PERMISSION_LOCATION && PERMISSION_BLUETOOTH_SCAN && PERMISSION_BLUETOOTH_CONNECT;
    }

    boolean isBluetoothEnable() {
        return TOGGLE_BLUETOOTH;
    }

    boolean isLocationEnable() {
        return TOGGLE_LOCATION;
    }

    boolean isScanPermissionGranted() {
        return PERMISSION_BLUETOOTH_SCAN;
    }

    boolean isConnectPermissionGranted() {
        return PERMISSION_BLUETOOTH_CONNECT;
    }

    boolean isLocationPermissionGranted() {
        return PERMISSION_LOCATION;
    }

    public void checkEnv(Context context) {
        BluetoothAdapter adapter = null;
        try {
            BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            adapter = manager.getAdapter();
        } catch (Exception ignore) {
        }
        if (adapter == null) {
            TOGGLE_BLUETOOTH = false;
        } else {
            TOGGLE_BLUETOOTH = adapter.isEnabled();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PERMISSION_BLUETOOTH_SCAN = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED;
            PERMISSION_BLUETOOTH_CONNECT = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED;
        }
        boolean l1 = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        boolean l2 = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
        PERMISSION_LOCATION = l1 || l2;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            TOGGLE_LOCATION = locationManager != null && locationManager.isLocationEnabled();
        } else {
            try {
                int state = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
                TOGGLE_LOCATION = state != Settings.Secure.LOCATION_MODE_OFF;
            } catch (Exception e) {
                TOGGLE_LOCATION = false;
            }
        }
    }

    public static List<String> necessaryBluetoothPermissions() {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT);
        }
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissions;
    }



    @Override
    public String toString() {
        return "BluetoothEnvironment{" +
                "TOGGLE_BLUETOOTH=" + TOGGLE_BLUETOOTH +
                ", TOGGLE_LOCATION=" + TOGGLE_LOCATION +
                ", PERMISSION_LOCATION=" + PERMISSION_LOCATION +
                ", PERMISSION_BLUETOOTH_SCAN=" + PERMISSION_BLUETOOTH_SCAN +
                ", PERMISSION_BLUETOOTH_CONNECT=" + PERMISSION_BLUETOOTH_CONNECT +
                '}';
    }
}
