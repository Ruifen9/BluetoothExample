package com.bluexmicro.bluetoothexample.ota.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class OtaState {

    public int index = 0;

    public long startTimeMillis = 0L;
    public long endTimeMillis = 0L;

    public float progress = 0f;

    public boolean result = false;
    @Nullable
    public String errorMessage;//当发生错误时的错误信息，endTimeMillis > 0L

    /**
     * 判断是否正在升级
     */
    public boolean inProgress() {
        return startTimeMillis > 0 && endTimeMillis == 0L;
    }

    /**
     * 判断传输是否结束
     */
    public boolean hasResult() {
        return startTimeMillis > 0 && endTimeMillis > 0L;
    }

    @NonNull
    @Override
    public String toString() {
        return "OtaState{" +
                "index=" + index +
                ", startTimeMillis=" + startTimeMillis +
                ", endTimeMillis=" + endTimeMillis +
                ", progress=" + progress +
                ", result=" + result +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}

