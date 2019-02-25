package com.student.model;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.student.utill.Crc16;

import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MessagePacket {
    @IntDef({Payload.READ, Payload.WRITE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Payload {
        int READ = 0;
        int WRITE = 1;
    }

    private int mPayload;
    private String mData;
    private String mCrc16;

    public MessagePacket(@Payload int payload, byte[] data, String crc16) throws UnsupportedEncodingException,
            InvalidObjectException {
        mPayload = payload;
        mData = new String(data, "UTF-8");
        mCrc16 = crc16;
        if (!isPacketValid(data)) {
            throw new InvalidObjectException("Wrong crc");
        }
    }

    public MessagePacket(@NonNull String data) {
        mPayload = 0;
        mData = data;
        mCrc16 = Crc16.encrypt(mData.getBytes());
    }

    public @NonNull LatLng getCoordinatesFromData() {
        String[] split = mData.split("_");
        return new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));
    }

    public int getPayload() {
        return mPayload;
    }

    public @NonNull String getData() {
        return mData;
    }

    public @NonNull String getCrc16() {
        return mCrc16;
    }

    public boolean isReadOperation() {
        return mPayload == Payload.READ;
    }

    private boolean isPacketValid(byte[] buffer) {
        return mCrc16.equalsIgnoreCase(Crc16.encrypt(buffer));
    }
}