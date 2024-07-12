package com.bluexmicro.bluetoothexample.ota.entity;

import java.nio.ByteBuffer;

public class Segment {
    private final int index;
    private final byte[] data;

    public Segment(int index, byte[] bytes) {
        this.index = index;
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length + 1);
        buffer.put((byte) index)
                .put(bytes);
        this.data = buffer.array();
    }

    public int getIndex() {
        return index;
    }

    public byte[] getData() {
        return data;
    }
}
