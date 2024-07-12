package com.bluexmicro.bluetoothexample.connection;

import static kotlin.io.ConstantsKt.DEFAULT_BUFFER_SIZE;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MutableLiveData;

import com.bluexmicro.bluetoothexample.R;
import com.bluexmicro.bluetoothexample.databinding.ActivityConnectionBinding;
import com.bluexmicro.bluetoothexample.ota.OtaManager;
import com.bluexmicro.bluetoothexample.ota.entity.OtaTask;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class ConnectionActivity extends AppCompatActivity {

    private final MutableLiveData<List<OtaTask>> tasks = new MutableLiveData<>();
    private final AtomicBoolean updating = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityConnectionBinding binding = ActivityConnectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        ScanResult target = intent.getParcelableExtra("device");

        /*
         * In production environments, this data source is usually downloaded from cloud servers
         */
        loadFirmware(this);

        binding.startBtn.setOnClickListener(v -> {
            if (tasks.getValue() != null && target != null) {
                if (updating.compareAndSet(false, true)) {
                    startOta(target.getDevice());
                }
            }
        });
    }

    private void loadFirmware(Context context) {
        new Thread(() -> {
            try (InputStream is = context.getAssets().open("firmware.bin")) {
                ByteArrayOutputStream out = new ByteArrayOutputStream(Math.max(DEFAULT_BUFFER_SIZE, is.available()));
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int bytes = is.read(buffer);
                while (bytes >= 0) {
                    out.write(buffer, 0, bytes);
                    bytes = is.read(buffer);
                }
                byte[] rawData = out.toByteArray();
                OtaTask task = new OtaTask(rawData);
                List<OtaTask> list = new ArrayList<>();
                list.add(task);
                tasks.postValue(list);
            } catch (Exception ignore) {

            }
        }).start();
    }

    /**
     * In a production environment, usually the app has already been connected to a Bluetooth device,
     * so you don't need to disconnect, just use OtaManager directly
     */
    private void startOta(@NonNull BluetoothDevice target) {
        OtaManager mgr = new OtaManager(ConnectionActivity.this, target);
        mgr.startFastOta(tasks.getValue(), new OtaManager.ProcessCallback() {

            @Override
            public void onProgress(int taskIndex, float progress) {
                // Because multiple tasks are being transmitted at once, the progress of each task will be printed sequentially
                Log.d("TAG", "onProgress() called with: taskIndex = [" + taskIndex + "], progress = [" + progress + "]");
            }

            @Override
            public void onDone() {
                updating.set(false);
            }

            @Override
            public void onFailed(int status) {
                updating.set(false);
            }
        });
    }

}