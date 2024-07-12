package com.bluexmicro.bluetoothexample.environment;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bluexmicro.bluetoothexample.R;
import com.bluexmicro.bluetoothexample.databinding.ActivityEnvBinding;

/**
 * Add AndroidManifest
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
 * <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
 * <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 * <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 */
public class EnvActivity extends AppCompatActivity {

    ActivityEnvBinding binding;

    private final BluetoothEnvironment env = new BluetoothEnvironment();
    //Necessary Bluetooth Permissions
    private final String[] permissions = BluetoothEnvironment.necessaryBluetoothPermissions().toArray(new String[0]);
    private final static Handler handler = new Handler();
    private final Runnable checkTask = new Runnable() {
        @Override
        public void run() {
            env.checkEnv(EnvActivity.this);
            binding.bluetoothBtn.setEnabled(!env.isBluetoothEnable());
            binding.scanBtn.setEnabled(!env.isScanPermissionGranted());
            binding.connectBtn.setEnabled(!env.isConnectPermissionGranted());
            binding.locationBtn.setEnabled(!env.isLocationEnable());
            binding.locationPermissionBtn.setEnabled(!env.isLocationPermissionGranted());
            binding.readyBtn.setEnabled(env.isSanAndConnectReady());
            handler.postDelayed(this, 1000);
        }
    };
    Intent appSettingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_env);
        binding = ActivityEnvBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), res -> {
        });

        binding.readyBtn.setOnClickListener(v -> EnvActivity.this.finish());

        // try enable bluetooth
        binding.bluetoothBtn.setOnClickListener(v -> launcher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)));
        // go to settings page Granting permissions
        binding.scanBtn.setOnClickListener(v -> launcher.launch(appSettingIntent));
        // go to settings page Granting permissions
        binding.connectBtn.setOnClickListener(v -> launcher.launch(appSettingIntent));
        // try enable location
        binding.locationBtn.setOnClickListener(v -> launcher.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        // try enable location
        binding.locationPermissionBtn.setOnClickListener(v -> launcher.launch(appSettingIntent));

        // Dynamic request permission
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), map -> {
            // result
        }).launch(permissions);


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(checkTask, 100);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(checkTask);
    }

}