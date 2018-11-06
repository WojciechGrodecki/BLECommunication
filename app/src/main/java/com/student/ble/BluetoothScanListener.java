package com.student.ble;

import android.support.annotation.NonNull;

import com.student.ble.BluetoothDeviceListItem;

import java.util.List;

public interface BluetoothScanListener {
    void onScanCompleted(@NonNull List<BluetoothDeviceListItem> list);
}
