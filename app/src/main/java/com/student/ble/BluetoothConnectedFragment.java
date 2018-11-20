package com.student.ble;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.student.R;
import com.student.model.MessagePacket;
import com.student.utill.BluetoothUtils;

public class BluetoothConnectedFragment  extends Fragment implements OnMapReadyCallback {
    private static final int ZOOM_LEVEL = 16;
    private GoogleMap mMap;
    private BluetoothUtils mBluetoothUtils = BluetoothUtils.getInstance();
    private EditText mEtText;
    private Marker mCurrentMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_connected, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.angry_btn).setOnClickListener(v -> end());
        setupMap();
        mEtText = view.findViewById(R.id.etText);
        view.findViewById(R.id.btnSend).setOnClickListener(v ->
                sendMessage(mEtText.getText().toString()));
    }

    private void end (){
        System.exit(0);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    private void sendMessage(@NonNull String message) {
        if (!Strings.isEmptyOrWhitespace(message)) {
            MessagePacket messagePacket = new MessagePacket(message);
            boolean isSend = mBluetoothUtils.sendMessage(messagePacket);
            if (isSend) {
                mEtText.getText().clear();
            } else {
                Toast.makeText(getContext(), R.string.problem_with_sending,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            mEtText.setError(getString(R.string.et_error));
        }
    }

    public void addMapPin(@NonNull LatLng coordinates) {
        if (isAdded()) {
            if (mCurrentMarker != null) {
                mCurrentMarker.remove();
            }
            mCurrentMarker = mMap.addMarker(new MarkerOptions().position(coordinates));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, ZOOM_LEVEL));
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
    }

}
