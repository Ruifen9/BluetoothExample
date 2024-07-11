package com.bluexmicro.bluetoothexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bluexmicro.bluetoothexample.environment.BluetoothEnvironment;
import com.bluexmicro.bluetoothexample.environment.EnvActivity;

public class MainActivity extends AppCompatActivity {

    private final BluetoothEnvironment env = new BluetoothEnvironment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        env.checkEnv(this);
        Log.e("MainActivity", "onResume: " + env);
        if (!env.isSanAndConnectReady()) {
            startActivity(new Intent(this, EnvActivity.class));
        }
    }
}