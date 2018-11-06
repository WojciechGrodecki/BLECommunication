package com.student.UI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.student.ble.BluetoothActivity;
import com.student.R;

import java.util.ArrayList;
import java.util.List;

public class SplashScreen extends AppCompatActivity {
    private static final int DELAY =1700;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        checkPermissions();
    }

    private void checkPermissions() {
        List<String> missingPermissions = new ArrayList<>();

        for (final String permission : REQUIRED_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions,
                    REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            startTimeOutActivity();
        }
    }



    private void startWelcomeScreen(){
       Intent intent = new Intent(this, WelcomeScreen.class);
       startActivity(intent);
    }

    private void startTimeOutActivity(){
        Handler handler = new Handler();
        handler.postDelayed(this::startWelcomeScreen, DELAY);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int i = 0; i < permissions.length; ++i) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        finish();
                        return;
                    }
                }
                startTimeOutActivity();
                break;
        }
    }
}
