package com.bluexmicro.bluetoothexample.ota;

public interface BasicProcessCallback {
    void onProgress(float progress);

    void onDone();

    void onFailed(int status);
}
