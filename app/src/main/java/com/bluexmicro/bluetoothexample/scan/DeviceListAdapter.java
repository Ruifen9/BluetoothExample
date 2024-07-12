package com.bluexmicro.bluetoothexample.scan;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bluexmicro.bluetoothexample.connection.ConnectionActivity;
import com.bluexmicro.bluetoothexample.databinding.ListItemBinding;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class DeviceListAdapter extends ListAdapter<ScanResult, DeviceListAdapter.MyViewHolder> {

    public DeviceListAdapter() {
        super(new DiffUtil.ItemCallback<ScanResult>() {
            @Override
            public boolean areItemsTheSame(@NonNull ScanResult oldItem, @NonNull ScanResult newItem) {
                return oldItem.getDevice().getAddress().equals(newItem.getDevice().getAddress());
            }

            @Override
            public boolean areContentsTheSame(@NonNull ScanResult oldItem, @NonNull ScanResult newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(
                ListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ScanResult result = getItem(position);
        holder.bindTo(result);
        holder.connectBtn.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ConnectionActivity.class);
            intent.putExtra("device", result);
            v.getContext().startActivity(intent);
        });
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        @NonNull
        TextView nameTx, addressTx;
        Button connectBtn;

        public MyViewHolder(@NonNull ListItemBinding binding) {
            super(binding.getRoot());
            nameTx = binding.nameTx;
            addressTx = binding.addressTx;
            connectBtn = binding.connectBtn;

        }

        void bindTo(ScanResult result) {
            String name = null;
            if (ActivityCompat.checkSelfPermission(nameTx.getContext(), android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                name = result.getDevice().getName();
            }
            if (name == null && result.getScanRecord() != null) {
                name = result.getScanRecord().getDeviceName();
            }
            if (name == null) name = "Null";
            nameTx.setText(name+" [RSSI: "+result.getRssi()+"dbm]");
            addressTx.setText(result.getDevice().getAddress());
        }
    }

}
