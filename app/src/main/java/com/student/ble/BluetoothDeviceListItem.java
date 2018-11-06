package com.student.ble;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

public class BluetoothDeviceListItem  {
    private String mName;
        private String mAddress;
        private BluetoothDevice mDevice;

        public BluetoothDeviceListItem(@NonNull BluetoothDevice device) {
            mDevice = device;
            mName = device.getName();
            mAddress = device.getAddress();
        }

        public @NonNull String getName() {
            return mName;
        }

        public @NonNull String getAddress() {
            return mAddress;
        }

        public @NonNull BluetoothDevice getDevice() {
            return mDevice;
        }

}
