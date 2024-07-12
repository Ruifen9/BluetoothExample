package com.bluexmicro.bluetoothexample.ota;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bluexmicro.bluetoothexample.ota.entity.Block;
import com.bluexmicro.bluetoothexample.ota.entity.OtaTask;
import com.bluexmicro.bluetoothexample.ota.entity.Segment;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.ConnectionPriorityRequest;
import no.nordicsemi.android.ble.callback.FailCallback;

public class OtaManager extends BleManager {

    @NonNull
    private final BluetoothDevice device;

    @Nullable
    private BluetoothGattCharacteristic mCtrl, mData;

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return true;
    }

    @Override
    protected void initialize() {
        super.initialize();
        beginAtomicRequestQueue()
                .add(enableIndications(mCtrl).fail((device, status) -> {
                    disconnect().enqueue();
                }))
                .add(requestMtu(251))
                .enqueue();
    }

    @Override
    protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
        BluetoothGattService service = gatt.getService(Constant.UUID_BX_DEFAULT_OTA_SERVICE);
        if (service == null) return false;
        BluetoothGattCharacteristic ctrl = service.getCharacteristic(Constant.UUID_BX_DEFAULT_CTRL);
        BluetoothGattCharacteristic data = service.getCharacteristic(Constant.UUID_BX_DEFAULT_DATA);
        boolean flag1 = ctrl != null && (ctrl.getProperties() & Constant.CTRL_REQUIRED_PROPERTIES) > 0;
        boolean flag2 = data != null && (data.getProperties() & Constant.DATA_REQUIRED_PROPERTIES) > 0;
        if (flag1 && flag2) {
            mCtrl = ctrl;
            mData = data;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onServicesInvalidated() {
        super.onServicesInvalidated();
        mCtrl = null;
        mData = null;
    }

    public OtaManager(@NonNull Context context, @NonNull BluetoothDevice device) {
        super(context);
        this.device = device;
    }

    private class BasicProcess {
        @NonNull
        OtaTask task;
        @NonNull
        BluetoothGattCharacteristic _ctrl;
        @NonNull
        BluetoothGattCharacteristic _data;
        @NonNull
        BasicProcessCallback callback;
        @NonNull
        private final List<Block> pendingBlocks = new ArrayList<>();
        private final Handler handler = new Handler();

        private final static long TIMEOUT_START_RESPONSE = 5000L;
        private final Runnable onRequestStartTimeout = () -> callback.onFailed(FailCallback.REASON_TIMEOUT);

        public BasicProcess(@NonNull OtaTask task, @NonNull BluetoothGattCharacteristic _ctrl, @NonNull BluetoothGattCharacteristic _data, @NonNull BasicProcessCallback cb) {
            this.task = task;
            this._ctrl = _ctrl;
            this._data = _data;
            this.callback = cb;
            setIndicationCallback(_ctrl).with((device, data) -> {
                //next
                if (data.getValue() != null) {
                    handleStartResponse(data.getValue());
                }
            });
        }

        void execute() {
            byte[] cmd = task.getRequestStartCommand();
            writeCharacteristic(_ctrl, cmd, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
//                    .done(device -> requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH).enqueue())
                    .fail((device, status) -> callback.onFailed(status))
                    .enqueue();
            handler.postDelayed(onRequestStartTimeout, TIMEOUT_START_RESPONSE);

        }

        private void handleStartResponse(byte[] bytes) {
            //todo 触发timeout但消息又进来了
            if (bytes.length >= 3 && bytes[0] == Constant.RESPONSE_CTRL_OTA_START) {
                requestConnectionPriority(ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH).enqueue();
                handler.removeCallbacks(onRequestStartTimeout);
                int maxSegmentNumInBlock = (bytes[2] & 0xFF) * 8;
                int status = bytes[1] & 0xff;
                if (status == 0) {
                    int chuckSize = getMtu();
                    /*
                    抓包发现如果内容长度和MTU载体长度一样时，发现有些手机（大于android12）会擅自修改内容长度，导致内容错乱。从而导致OTA失败或者内容的重新发送。
                    当内容长度低于MTU阈值时会降低此现象的发生，猜测是蓝牙芯片吞吐量低或者波动导致的异常。
                    */
                    /*
                        实测存在异常的手机在减低最大长度后成功率恢复正常,
                        如果仍存在部分手机出现成功率很低的情况，请继续递减mtu的值
                        对应出现的问题的手机来说，看似减低速度，实际上降低了错误重发的概率，因此对于问题手机手机来说是提速
                        要保证MTU的值不能低于23
                     */

                    /** 增加faultToleranceValue的值可以提高成功率，但速度会下降*/
                    int faultToleranceValue = 50;
                    chuckSize = Math.max(23, chuckSize - faultToleranceValue);

//                    int mtu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? getMtu() - 13 : getMtu();
                    task.splitData(maxSegmentNumInBlock, chuckSize);
                    pendingBlocks.addAll(task.getBlocks());
                    requestBlock();
                }
            }
        }

        private void requestBlock() {
            onProgress();
            if (pendingBlocks.isEmpty()) {
                transportEnd();
            } else {
                Block block = pendingBlocks.remove(0);
                writeCharacteristic(_ctrl, block.getRequestNewBlockCommand(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                        .done(device -> sendSegmentsByBlock(block))
                        .fail((device, status) -> callback.onFailed(status))
                        .enqueue();
            }
        }

        private void sendSegmentsByBlock(Block block) {
            List<Segment> segments = block.getSegments();
            writeCharacteristic(_data, block.getAllSegmentData(), BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
                    .split((message, index, maxLength) -> {
                        if (index < segments.size()) {
                            return segments.get(index).getData();
                        } else {
                            return null;
                        }
                    })
                    .fail((device, status) -> callback.onFailed(status))
                    .done(device -> requestBlock())
                    .enqueue();
        }

        private void transportEnd() {
            writeCharacteristic(_ctrl, task.getRequestTransportEndCommand(), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                    .done(device1 -> apply())
                    .fail((device1, status) -> callback.onFailed(status))
                    .enqueue();
        }

        private void apply() {
            writeCharacteristic(_ctrl, new byte[]{Constant.PARAMS_REQUEST_APPLY}, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                    .done(device1 -> callback.onDone())
                    .fail((device1, status) -> callback.onFailed(status))
                    .enqueue();
        }

        private void onProgress() {
            float progress;
            if (task.getBlocks().isEmpty()) {
                progress = 0f;
            } else {
                float sentSize = task.getAllBlockSize() - pendingBlocks.size();
                progress = sentSize / task.getAllBlockSize();
            }
            callback.onProgress(progress);
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////


    public interface ProcessCallback {
        void onProgress(int taskIndex, float progress);

        void onDone();

        void onFailed(int status);
    }

    private List<OtaTask> pendingTasks = null;
    private int index = 0;

    public void startFastOta(List<OtaTask> tasks, ProcessCallback callback) {
        connect(device).retry(3, 100)
                .timeout(15_000)
//                .usePreferredPhy(PhyRequest.PHY_LE_1M_MASK | PhyRequest.PHY_LE_2M_MASK | PhyRequest.PHY_LE_CODED_MASK)
                .done(device1 -> {
                    pendingTasks = new ArrayList<>(tasks);
                    index = 0;
                    executeTasks(callback);
                }).fail((device1, status) -> {
                    pendingTasks = null;
                    callback.onFailed(-1);
                }).enqueue();
    }

    @Override
    public int getMinLogPriority() {
        return Log.ERROR;
    }

    private void executeTasks(ProcessCallback callback) {
        if (pendingTasks == null) {
            callback.onFailed(-1);
        } else {
            if (index >= pendingTasks.size()) {
                writeCharacteristic(mCtrl, new byte[]{Constant.PARAMS_REQUEST_REBOOT}, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                        .then(device -> disconnect().then(device1 -> callback.onDone()).enqueue())
                        .enqueue();
            } else {
                OtaTask current = pendingTasks.get(index);
                execute(current, new BasicProcessCallback() {
                    @Override
                    public void onProgress(float progress) {
                        callback.onProgress(index, progress);
                    }

                    @Override
                    public void onDone() {
                        index++;
                        executeTasks(callback);
                    }

                    @Override
                    public void onFailed(int status) {
                        writeCharacteristic(mCtrl, new byte[]{Constant.PARAMS_REQUEST_REBOOT}, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                                .then(device -> disconnect().then(device1 -> callback.onFailed(status)).enqueue())
                                .enqueue();
                    }
                });
            }
        }
    }

    private void execute(OtaTask task, BasicProcessCallback callback) {
        if (mCtrl == null || mData == null) {
            callback.onFailed(-1);
            return;
        }
        BasicProcess basicProcess = new BasicProcess(task, mCtrl, mData, new BasicProcessCallback() {
            @Override
            public void onProgress(float progress) {
                callback.onProgress(progress);
            }

            @Override
            public void onDone() {
                callback.onDone();
            }

            @Override
            public void onFailed(int status) {
                callback.onFailed(status);
            }
        });
        basicProcess.execute();
    }

}
