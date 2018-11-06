package com.student.ble;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.student.R;

import java.util.List;

public class BluetoothDevicesAdapter extends RecyclerView.Adapter <BluetoothDevicesAdapter.DeviceItemViewHolder> {
    private final List<BluetoothDeviceListItem> mDataList;
    private OnItemClickListener mOnItemClickListener;

    public BluetoothDevicesAdapter(@NonNull List<BluetoothDeviceListItem> dataList) {
        mDataList = dataList;
    }

    public void setListener(@NonNull OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public @NonNull DeviceItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_bluetooth_device, parent, false);
        return new DeviceItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceItemViewHolder holder, int position) {
        holder.mTxtvName.setText(mDataList.get(position).getName());
        holder.mTxtvAddress.setText(mDataList.get(position).getAddress());
        holder.mView.setOnClickListener(view -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onDeviceSelected(mDataList.get(position).getDevice());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    class DeviceItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mTxtvName;
        private TextView mTxtvAddress;
        private View mView;

        DeviceItemViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mTxtvName = itemView.findViewById(R.id.txtvName);
            mTxtvAddress = itemView.findViewById(R.id.txtvAddress);
        }
    }

    public interface OnItemClickListener {
        void onDeviceSelected(@NonNull BluetoothDevice device);
    }
}