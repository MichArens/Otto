package com.example.carcontroller.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.carcontroller.utils.AudioUtils;

public class BluetoothBroadcastReciver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BluetoothBroadcastReciver", "GOT IT");

        //TODO close service
        BluetoothHandlerService.setShouldRestart(false);
        Intent stopIntent = new Intent(context, BluetoothHandlerService.class);
        context.stopService(stopIntent);
    }
}
