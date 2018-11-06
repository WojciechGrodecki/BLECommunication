package com.student.ble;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public interface BluetoothConnectionListener {
    void connectedToService();
    void onMessageParsed(@NonNull LatLng latLng);
    void onMessageParseError();
    void onMessageCrcError();
}
