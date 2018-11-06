package com.student;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.student.ble.BluetoothConnectedFragment;


public class BeforeMap extends Fragment {
private static final int DELAY = 1100;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HandleLoad();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void LoadMap(){
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(BluetoothConnectedFragment.class.getName())
                .add(R.id.flContainer, new BluetoothConnectedFragment())
                .commit();

    }

    private void HandleLoad(){
        Handler handler = new Handler();
        handler.postDelayed(this::LoadMap, DELAY);
        }
    }

