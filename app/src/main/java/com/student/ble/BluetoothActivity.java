package com.student.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.student.R;
import com.student.utill.BluetoothUtils;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 101;
    private final BluetoothUtils mBluetoothUtils = BluetoothUtils.getInstance();
    private final List<BluetoothDeviceListItem> mDiscoveredDevices = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BluetoothDevicesAdapter mAdapter;
    private BluetoothConnectedFragment mBluetoothConnectedFragment = new BluetoothConnectedFragment();
    private Handler mUiThreadHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        setAdapter();
        enableBluetoothIfNecessary();
        setupWindowAnimations();
    }

    @Override
    protected void onStop() {
        mBluetoothUtils.disconnect();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setExitTransition(slide);
    }

    private void setAdapter() {
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this::scanNearbyDevices);
        mSwipeRefreshLayout.setRefreshing(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new BluetoothDevicesAdapter(mDiscoveredDevices);
        mAdapter.setListener(this::deviceSelected);
        mRecyclerView.setAdapter(mAdapter);

    }

    private void deviceSelected(@NonNull BluetoothDevice device) {
        //todo connect to ble device
        mBluetoothUtils.registerConnectionListener(new BluetoothConnectionListener() {
            @Override
            public void connectedToService() {
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(BluetoothConnectedFragment.class.getName())
                        .add(R.id.flContainer, mBluetoothConnectedFragment)
                        .commit();
            }

            @Override
            public void onMessageParsed(@NonNull LatLng latLng) {
                mUiThreadHandler.post(() -> mBluetoothConnectedFragment.addMapPin(latLng));
                mUiThreadHandler.post(() ->
                        Toast.makeText(BluetoothActivity.this, R.string.Lokalizacja,
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onMessageParseError() {
                mUiThreadHandler.post(() ->
                        Toast.makeText(BluetoothActivity.this, R.string.error_parse_message,
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onMessageCrcError() {
                mUiThreadHandler.post(() -> Toast.makeText(BluetoothActivity.this,
                        R.string.error_crc, Toast.LENGTH_SHORT).show());
            }
        });
        mBluetoothUtils.connectToDevice(BluetoothActivity.this, device);
    }

    private void enableBluetoothIfNecessary() {
        if (!mBluetoothUtils.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            scanNearbyDevices();
        }
    }

    private void scanNearbyDevices() {
        if (mBluetoothUtils.isBleSupported(this)) {
            mBluetoothUtils.registerDevicesListener(list -> {
                mDiscoveredDevices.clear();
                mDiscoveredDevices.addAll(list);
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            });
            mBluetoothUtils.startScan();
        } else {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
                    .show();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                scanNearbyDevices();
            } else {
                enableBluetoothIfNecessary();
            }
        }
    }
}
