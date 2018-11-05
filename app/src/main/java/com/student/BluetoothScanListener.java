package com.student;

import android.support.annotation.NonNull;

import java.util.List;

public interface BluetoothScanListener {
    void onScanCompleted(@NonNull List<BluetoothDeviceListItem> list);
}
