package com.bluexmicro.bluetoothexample.scan;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bluexmicro.bluetoothexample.environment.BluetoothEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class ScanViewModel extends AndroidViewModel {

    public ScanViewModel(@NonNull Application application) {
        super(application);
    }

    private boolean firstIn = true;
    private final List<String> addressList = new ArrayList<>();
    private final Map<String, ScanResult> resultMap = new HashMap<>();
    private final MutableLiveData<List<ScanResult>> _results = new MutableLiveData<>();
    final LiveData<List<ScanResult>> results = _results;

    private final BluetoothEnvironment env = new BluetoothEnvironment();
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
            if (result.getRssi() > -100) {
                String macAddress = result.getDevice().getAddress();
                if (!addressList.contains(macAddress)) {
                    addressList.add(macAddress);
                }
                resultMap.put(macAddress, result);
            }
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                if (result.getRssi() > -100) {
                    String macAddress = result.getDevice().getAddress();
                    if (!addressList.contains(macAddress)) {
                        addressList.add(macAddress);
                    }
                    resultMap.put(macAddress, result);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("TAG", "onScanFailed() called with: errorCode = [" + errorCode + "]");
        }
    };

    private final MutableLiveData<Boolean> _bluetoothReady = new MutableLiveData<>(true);
    LiveData<Boolean> bluetoothReady = _bluetoothReady;

    private boolean handleTask = false;
    private Thread thread = null;
    /**
     * Three actions were taken here
     * 1. Check the Bluetooth environment every second
     * 2. start Scan and stop Bluetooth
     * 3. Update data associated with View
     */
    private final Runnable checkRunnable = () -> {
        while (handleTask) {
            env.checkEnv(getApplication());
            boolean ready = env.isSanAndConnectReady();

            if (ready != Boolean.TRUE.equals(_bluetoothReady.getValue()) || firstIn) {
                firstIn = false;
                _bluetoothReady.postValue(ready);
                // Android system has limitations: frequent on/off scanning within 30 seconds is not allowed
                BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
                if (ready) {
                    ScanSettings settings = new ScanSettings.Builder()
                            .setLegacy(false)
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setReportDelay(5000)
                            .setUseHardwareBatchingIfSupported(true)
                            .build();
                    List<ScanFilter> filters = new ArrayList<>();
                    scanner.startScan(filters, settings, scanCallback);
                } else {
                    scanner.stopScan(scanCallback);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {

            }
            updateData();
        }
    };

    public void setHandleTask(boolean handleTask) {
        this.handleTask = handleTask;
        if (handleTask) {
            thread = new Thread(checkRunnable);
            thread.start();
        } else {
            BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = null;
        }
    }

    private void updateData() {
        List<ScanResult> tmp = new ArrayList<>();
        for (String address : addressList) {
            ScanResult res = resultMap.get(address);
            if (res != null) {
                tmp.add(res);
            }
        }
        _results.postValue(tmp);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        setHandleTask(false);
    }
}
