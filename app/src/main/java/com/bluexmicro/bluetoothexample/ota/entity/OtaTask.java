package com.bluexmicro.bluetoothexample.ota.entity;

import android.util.Log;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.bluexmicro.bluetoothexample.ota.Constant;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

public class OtaTask {

    @Nullable
    private final BigInteger address;
    @NonNull
    private final byte[] rawData;
    @NonNull
    private final List<Block> blocks = new ArrayList<>();
    //发送一条数据的长度,为segment的最大长度 默认 23（mtuSize） - 3（蓝牙底层） - 1（index） = 19
    private int maxSegmentSize = 19;
    private int allBlockSize = 0;

    public OtaTask(@NonNull byte[] rawData) {
        this.rawData = rawData;
        address = null;
    }

    /**
     * @param rawData 数据
     * @param address 指定写入的地址
     */
    public OtaTask(@NonNull byte[] rawData, @Nullable BigInteger address) {
        this.rawData = rawData;
        this.address = address;
    }

    /**
     * @param maxSegmentCountInBlock 一个Block中允许最多有多少个Segment
     */
    public void splitData(int maxSegmentCountInBlock, @IntRange(from = 23, to = 517) int mtuSize) {
        allBlockSize = 0;
        //发送一条数据的长度,为segment的最大长度 默认 23（mtuSize） - 3（蓝牙底层） - 1（index） = 19
        maxSegmentSize = mtuSize - 4;
        int maxBytesLenInBlock = maxSegmentCountInBlock * maxSegmentSize;
        Log.d("splitData: ", "maxSegmentCountInBlock:" + maxSegmentCountInBlock);
        Log.d("splitData: ", "maxSegmentSize:" + maxSegmentSize);
        int size = rawData.length / maxBytesLenInBlock;
        int remain = rawData.length % maxBytesLenInBlock;
        for (int i = 0; i < size; i++) {
            byte[] bytes = new byte[maxBytesLenInBlock];
            System.arraycopy(rawData, i * maxBytesLenInBlock, bytes, 0, maxBytesLenInBlock);
            Block block = new Block(i, bytes, maxSegmentSize);
            blocks.add(block);
        }
        if (remain > 0) {
            byte[] bytes = new byte[remain];
            System.arraycopy(rawData, size * maxBytesLenInBlock, bytes, 0, remain);
            Block block = new Block(size, bytes, maxSegmentSize);
            blocks.add(block);
        }
        allBlockSize = blocks.size();
    }

    public byte[] getRequestStartCommand() {
        //cmd 1 byte
        byte header = Constant.PARAMS_REQUEST_OTA_START;
        // size 2 bytes
        short maxSegmentSize = (short) getMaxSegmentSize();
        //crc32 4bytes
        CRC32 crc32 = new CRC32();
        crc32.update(rawData);
        int crc32Value = (int) crc32.getValue();
        Log.e("TAG", "crc32Value: " + crc32Value);
        // raw data len 4bytes
        int allDataLen = rawData.length;
        // type
        byte[] type;
        if (address == null) {
            type = new byte[]{0};
        } else {
            ByteBuffer typeBuffer = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
            typeBuffer.put((byte) 1).putInt(address.intValue());
            type = typeBuffer.array();
        }
        int size = 11 + type.length;
        ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(header).putShort(maxSegmentSize).putInt(crc32Value).putInt(allDataLen).put(type);
        return buffer.array();
    }

    public byte[] getRequestTransportEndCommand() {
        return new byte[]{
                Constant.PARAMS_REQUEST_CTRL_DATA_FINISH
        };
    }

    @NonNull
    public byte[] getRawData() {
        return rawData;
    }

    @Nullable
    public BigInteger getAddress() {
        return address;
    }

    public int getMaxSegmentSize() {
        return maxSegmentSize;
    }

    @NonNull
    public List<Block> getBlocks() {
        return blocks;
    }

    public int getAllBlockSize() {
        return allBlockSize;
    }
}
