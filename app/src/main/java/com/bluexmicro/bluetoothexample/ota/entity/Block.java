package com.bluexmicro.bluetoothexample.ota.entity;


import com.bluexmicro.bluetoothexample.ota.Constant;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class Block {
    private final int index;
    private final byte[] allSegmentData;
    private final List<Segment> segments = new ArrayList<>();

    /**
     * @param maxSegmentDataLen 就是 MtuSize - 4
     */
    public Block(int index, byte[] allSegmentData, int maxSegmentDataLen) {
        this.index = index;
        this.allSegmentData = allSegmentData;
        int size = allSegmentData.length / maxSegmentDataLen;
        int remain = allSegmentData.length % maxSegmentDataLen;
        for (int i = 0; i < size; i++) {
            byte[] bytes = new byte[maxSegmentDataLen];
            System.arraycopy(allSegmentData, i * maxSegmentDataLen, bytes, 0, maxSegmentDataLen);
            Segment segment = new Segment(i, bytes);
            segments.add(segment);
        }
        if (remain > 0) {
            byte[] bytes = new byte[remain];
            System.arraycopy(allSegmentData, size * maxSegmentDataLen, bytes, 0, remain);
            Segment segment = new Segment(size, bytes);
            segments.add(segment);
        }
    }

    public byte[] getRequestNewBlockCommand() {
        ByteBuffer buffer = ByteBuffer.allocate(3).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(Constant.REQUEST_CTRL_NEW_BLOCK).putShort((short) index);
        return buffer.array();
    }

    public int getIndex() {
        return index;
    }

    public byte[] getAllSegmentData() {
        return allSegmentData;
    }

    public List<Segment> getSegments() {
        return segments;
    }
}
