package com.bluexmicro.bluetoothexample.scan;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bluexmicro.bluetoothexample.R;
import com.bluexmicro.bluetoothexample.databinding.ActivityScanBinding;
import com.bluexmicro.bluetoothexample.environment.EnvActivity;

public class ScanActivity extends AppCompatActivity {

    private ScanViewModel viewModel;
    private final DeviceListAdapter deviceListAdapter = new DeviceListAdapter();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityScanBinding binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);
        viewModel.bluetoothReady.observe(this, ready -> {
            if (!ready) {
                // When Bluetooth is abnormal, jump to the guidance interface to guide the user to repair Bluetooth
                ScanActivity.this.startActivity(new Intent(ScanActivity.this, EnvActivity.class));
            }
        });

        // setup RecyclerView
        binding.deviceListView.setLayoutManager(new LinearLayoutManager(this));
        binding.deviceListView.setAdapter(deviceListAdapter);
        viewModel.results.observe(this, deviceListAdapter::submitList);
    }


    @Override
    protected void onResume() {
        super.onResume();
        viewModel.setHandleTask(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.setHandleTask(false);
    }
}