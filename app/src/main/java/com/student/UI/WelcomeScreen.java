package com.student.UI;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.student.R;
import com.student.ble.BluetoothActivity;

public class WelcomeScreen  extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findViewById(R.id.angry_btn).setOnClickListener(v -> bluetoothActivityStart());
    }

    public void bluetoothActivityStart(){

        Intent intent = new Intent(this, BluetoothActivity.class);
        startActivity(intent);
    }



}