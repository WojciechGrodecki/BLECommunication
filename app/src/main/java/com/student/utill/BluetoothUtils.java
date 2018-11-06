package com.student.utill;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.student.ble.BluetoothConnectionListener;
import com.student.ble.BluetoothDeviceListItem;
import com.student.ble.BluetoothScanListener;
import com.student.model.MessagePacket;

import java.io.InvalidObjectException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
public class BluetoothUtils {
    private static final int SCAN_DELAY = 2000;
    private static final int TIMEOUT = 500;
    private final UUID SERVICE_UUID = UUID.fromString("65333333-A115-11E2-9E9A-0800200CA100");
    private final UUID UNACK_UUID = UUID.fromString("65333333-A115-11E2-9E9A-0800200CA102");
    private final UUID UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothScanListener mDeviceListener;
    private BluetoothConnectionListener mConnectionListener;
    private final Map<String, BluetoothDevice> mDiscoveredDevices = new HashMap<>();
    private BluetoothDevice mCurrentDevice;
    private BluetoothGattService mCurrentGattService;
    private BluetoothGattCharacteristic mCurrentCharacteristics;
    private PacketUtils mPacketUtils = new PacketUtils();
    private Handler mTimeoutHandler = new Handler();

    private static BluetoothUtils sInstance;
    private boolean mIsDescriptorSet;
    private String mCurrentMessage = "";

    private final Runnable mTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mConnectionListener != null) {
                mConnectionListener.onMessageParseError();
            }
        }
    };

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                mBluetoothGatt.connect();
            } else if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                connectToService();
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            mIsDescriptorSet = true;
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String message = characteristic.getStringValue(0).trim();
            mCurrentMessage += message;
            if (message.length() > 0 &&
                    message.substring(message.length() - 1).equals(PacketUtils.PACKET_END)) {
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
                tryParsingMessage();
                mCurrentMessage = "";
            } else {
                mTimeoutHandler.postDelayed(mTimeoutRunnable, TIMEOUT);
            }
        }
    };

    private final ScanCallback mScanCallback = new ScanCallback() {
        @SuppressLint("NewApi")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            mDiscoveredDevices.put(result.getDevice().getAddress(), result.getDevice());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            stopAndReport();
        }
    };

    private final BluetoothAdapter.LeScanCallback mLeScanCallback =
            (bluetoothDevice, i, bytes) -> mDiscoveredDevices.put(bluetoothDevice.getAddress(),
                    bluetoothDevice);

    public static @NonNull BluetoothUtils getInstance() {
        if (sInstance == null) {
            sInstance = new BluetoothUtils();
        }

        return sInstance;
    }

    public void connectToDevice(@NonNull Context context, @NonNull BluetoothDevice device) {
        mCurrentDevice = device;
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mBluetoothGatt = mCurrentDevice.connectGatt(context, false,
                        mGattCallback, BluetoothDevice.TRANSPORT_LE);
            } else {
                mBluetoothGatt = mCurrentDevice.connectGatt(context, false,
                        mGattCallback);
            }
        });
    }

    public void disconnect() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
        }
        mConnectionListener = null;
        mDeviceListener = null;
        mIsDescriptorSet = false;
        mCurrentCharacteristics = null;
        mCurrentGattService = null;
    }

    private void setCharacteristicsNotification() {
        mCurrentCharacteristics = mCurrentGattService.getCharacteristic(UNACK_UUID);
        mBluetoothGatt.setCharacteristicNotification(mCurrentCharacteristics, true);
        BluetoothGattDescriptor descriptor = mCurrentCharacteristics.getDescriptor(UUID_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
    }


    public boolean sendMessage(@NonNull MessagePacket messagePacket) {
        if (mIsDescriptorSet) {
            mCurrentCharacteristics.setValue(mPacketUtils.parse(messagePacket));
            mBluetoothGatt.writeCharacteristic(mCurrentCharacteristics);
        }

        return mIsDescriptorSet;
    }
    private void connectToService() {
        mCurrentGattService = mBluetoothGatt.getService(SERVICE_UUID);
        setCharacteristicsNotification();
        if (mConnectionListener != null) {
            mConnectionListener.connectedToService();
        }
    }

    public boolean isBluetoothEnabled() {
        return mAdapter.isEnabled();
    }

    public boolean isBleSupported(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void registerDevicesListener(@NonNull BluetoothScanListener listener) {
        mDeviceListener = listener;
    }

    public void registerConnectionListener(@NonNull BluetoothConnectionListener listener) {
        mConnectionListener = listener;
    }

    public void startScan() {
        mDiscoveredDevices.clear();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = mAdapter.getBluetoothLeScanner();
            scanner.startScan(mScanCallback);
        } else {
            mAdapter.startLeScan(mLeScanCallback);
        }

        Handler handler = new Handler();
        handler.postDelayed(this::stopAndReport, SCAN_DELAY);
    }

    private void stopAndReport() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            BluetoothLeScanner scanner = mAdapter.getBluetoothLeScanner();
            scanner.stopScan(mScanCallback);
        } else {
            mAdapter.stopLeScan(mLeScanCallback);
        }

        if (mDeviceListener != null) {
            List<BluetoothDeviceListItem> items = new ArrayList<>();
            for (BluetoothDevice device : mDiscoveredDevices.values()) {
                items.add(new BluetoothDeviceListItem(device));
            }
            mDeviceListener.onScanCompleted(items);
        }
    }

    private void tryParsingMessage() {
        try {
            MessagePacket packet = mPacketUtils.parse(mCurrentMessage);
            if (mConnectionListener != null) {
                mConnectionListener.onMessageParsed(packet.getCoordinatesFromData());
            }
        } catch (UnsupportedEncodingException e) {
            if (mConnectionListener != null) {
                mConnectionListener.onMessageParseError();
            }
        } catch (InvalidObjectException e) {
            if (mConnectionListener != null) {
                mConnectionListener.onMessageCrcError();
            }
        }
    }
}