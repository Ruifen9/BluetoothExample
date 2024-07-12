package com.bluexmicro.bluetoothexample.ota.entity;

import androidx.annotation.NonNull;

public interface OtaCallback {

    void onInitialError(@NonNull String errorMessage);

    /**
     * {@link OtaState#hasResult()}  and {@link OtaState#progress} ==1f
     */
    void onStateChanged(@NonNull OtaState state);

    void onFinish();
}
