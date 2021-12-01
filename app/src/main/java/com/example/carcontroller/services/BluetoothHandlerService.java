package com.example.carcontroller.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.carcontroller.MainActivity;
import com.example.carcontroller.R;
import com.example.carcontroller.globals.AppConfig;
import com.example.carcontroller.globals.BtEventTypes;
import com.example.carcontroller.globals.Controls;
import com.example.carcontroller.utils.AudioUtils;
import com.example.carcontroller.utils.SharedPerefrencesUtils;
import com.example.carcontroller.utils.SpotifyHandler;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

public class BluetoothHandlerService extends Service {

    private final int NOTIFICATION_ID = 1;
    private static boolean shouldRestart;
    private Handler Handler;
    private final int SCAN_PERIOD = 30 * 1000;
    private final String NAME_TO_SEARCH = "OttoBT";
    private volatile NotificationCompat.Builder notificationBuilder;

    /**
     * Bluetooth
     */
    private volatile ScanCallback scanCallback;
    private volatile BluetoothGattCallback gattCallback;
    private UUID SERVICE_UUID;
    private UUID CHAR_UUID;
    final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);
    private volatile BluetoothDevice device;
    private volatile BluetoothGatt bluetoothGatt;
    private volatile BluetoothGattCharacteristic characteristic;
    private volatile static BluetoothLeScanner bluetoothLeScanner;
    private volatile static BluetoothAdapter bluetoothAdapter;

    /**
     * Spotify
     */
    private volatile static boolean shouldUseSpotify;
    private volatile static SpotifyHandler spotifyHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        Handler = new Handler();
        initScanCallback();
        initGattCallback();
        startNotification();
        setShouldRestart(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scanLeDevice(true);
        if (getSpotifyHandler() != null) {
            getSpotifyHandler().initSpotifyConnection();
        }
        return START_STICKY;
    }

    private void scanLeDevice(boolean enable) {
        if (enable) {
            // stops scanning after pre defined delay
            Handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (device == null) {
                        bluetoothLeScanner.stopScan(scanCallback);
                        setShouldRestart(false);
                        stopSelf();
                    }
                }
            }, SCAN_PERIOD);
            bluetoothLeScanner.startScan(scanCallback);
        } else {
            bluetoothLeScanner.stopScan(scanCallback);
        }
    }

    private void initScanCallback() {
        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                try {

                    if (result.getDevice() == null || result.getDevice().getName() == null) return;
                    if (!result.getDevice().getName().equalsIgnoreCase(NAME_TO_SEARCH)) {
                        Log.d("MyBlueTooth", result.getDevice().getName() + " NAMEEEE");
                        return;
                    }
                    Log.d("MyBlueTooth", result.getDevice().getName() + " NAMEEEE");
                    if (result.getScanRecord() == null || result.getScanRecord().getServiceUuids() == null) {
                        Log.d("MyBlueTooth", "no uuid");
                        return;
                    }
                    Log.d("MyBlueTooth", result.getScanRecord().getServiceUuids().get(0).getUuid() + " UUID");

                    SERVICE_UUID = result.getScanRecord().getServiceUuids().get(0).getUuid();
                    scanLeDevice(false);
                    device = result.getDevice();
                    bluetoothGatt = device.connectGatt(getApplicationContext(), false, gattCallback);
//                BluetoothGattService gattService = bluetoothGatt.getService(uuid);


                } catch (Exception e) {
                    Log.d("MyBlueTooth", "CATCH " + e.getLocalizedMessage());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d("MyBlueTooth", "onScanFailedddd");
            }
        };
    }

    private void initGattCallback() {
        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d("MyBlue","onConnectionStateChange status = " + status + ", newState = " + newState);
                if (status == BluetoothGatt.GATT_SUCCESS
                        && newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();

                } else if (status == BluetoothGatt.GATT_SUCCESS
                        && newState == BluetoothProfile.STATE_DISCONNECTED) {
                    //Handle a disconnect event
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            super.onServicesDiscovered(gatt, status);
                CHAR_UUID = gatt.getService(SERVICE_UUID).getCharacteristics().get(0).getUuid();
                BluetoothGattDescriptor descriptor =
                        gatt.getService(SERVICE_UUID).getCharacteristics().get(0).getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);

                descriptor.setValue(
                        BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);

                startForeground(NOTIFICATION_ID, notificationBuilder.setContentTitle("Bluetooth Connected").build());
                updateActivity(true);

            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                characteristic =
                        gatt.getService(SERVICE_UUID)
                                .getCharacteristic(CHAR_UUID);
                //Enabling char notifications IMPORTENT
                bluetoothGatt.setCharacteristicNotification(characteristic, true);
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            byte[] c = characteristic.getValue();
//            Log.d("MyBlueTooth", "READ 2 bytes " + Arrays.toString(c));
//            String str = new String(c, StandardCharsets.UTF_8);
//            Log.d("MyBlueTooth", "READ 2" + bytesToString(c));
                super.onCharacteristicRead(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d("MyBlueTooth", "READ 3");
                super.onCharacteristicWrite(gatt, characteristic, status);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] c = characteristic.getValue();
                String str = new String(c, StandardCharsets.UTF_8);
                Log.d("MyBlueTooth", "READ 4: " + str);

//                Intent intent = new Intent(AppConfig.BT_EVENT);
                // You can also include some extra data.
//                intent.putExtra("type", BtEventTypes.MSG);
//                intent.putExtra("msg", str);
//                LocalBroadcastManager.getInstance(BluetoothHandlerService.this).sendBroadcast(intent);

                str = str.replace("\n", "");
                str = str.replace("\r", "");
                onReceiveAction(str);
                super.onCharacteristicChanged(gatt, characteristic);
            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.d("MyBlueTooth", "READ 1");
                super.onDescriptorRead(gatt, descriptor, status);
            }

        };
    }

    private void startNotification() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        chan.setDescription("Working");

        NotificationManager manager = (NotificationManager) getSystemService(NotificationManager.class);
        if (manager == null) {
            Log.d("Service", "manager is null");
            return;
        }
        manager.createNotificationChannel(chan);

        /**
         * Activity intent
         */
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        /**
         * Action intent
         */
        Intent closeServiceIntent = new Intent(this, BluetoothBroadcastReciver.class);
        closeServiceIntent.setAction("Close Service");
        closeServiceIntent.putExtra(Intent.EXTRA_INDEX, 1);
        PendingIntent closeServicePendingIntent =
                PendingIntent.getBroadcast(this, 0, closeServiceIntent, 0);

        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Searching for HMsoft")
                .setSmallIcon(R.drawable.bluetooth_connect_icon)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(null)
                .addAction(R.drawable.close_icon, "Close", closeServicePendingIntent)
                .build();
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        /**
         * Important for foreground service
         */
        startForeground(NOTIFICATION_ID, notification);
    }

    private void updateActivity(boolean btOn) {
        try {
            Log.d("sender", "Broadcasting message");
            Intent intent = new Intent(AppConfig.BT_EVENT);
            // You can also include some extra data.
            intent.putExtra("type", BtEventTypes.CONNECTION_CHANGE);
            intent.putExtra("isBtOn", btOn);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        } catch (Exception e) {

        }
    }

    private void onReceiveAction(String action) {
        try {
            int actionInt = Integer.parseInt(action);
            if (actionInt > 3) return;

            String controlPicked = SharedPerefrencesUtils.getPickedAction(getApplicationContext(), actionInt);

            switch (controlPicked) {
                case Controls.SPOTIFY_LIKE_SONG:
                    getSpotifyHandler().likeSong();
                    break;
                case Controls.SPOTIFY_TOGGLE_SHUFFLE:
                    getSpotifyHandler().toggleShuffle();
                    break;
                case Controls.NEXT_SONG:
                    AudioUtils.skipSong(getApplicationContext());
                    break;
                case Controls.PREV_SONG:
                    AudioUtils.goBackSong(getApplicationContext());
                    break;
                case Controls.PAUSE_PLAY:
                    break;
            }


        } catch (Exception e) {

        }
    }

    private UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

    public static boolean isShouldRestart() {
        return shouldRestart;
    }

    public static void setShouldRestart(boolean shouldRestart) {
        BluetoothHandlerService.shouldRestart = shouldRestart;
    }

    public static BluetoothLeScanner getBluetoothLeScanner() {
        return bluetoothLeScanner;
    }

    public static void setBluetoothLeScanner(BluetoothLeScanner bluetoothLeScanner) {
        BluetoothHandlerService.bluetoothLeScanner = bluetoothLeScanner;
    }

    public static BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public static void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        BluetoothHandlerService.bluetoothAdapter = bluetoothAdapter;
    }

    public static SpotifyHandler getSpotifyHandler() {
        return spotifyHandler;
    }

    public static void setSpotifyHandler(SpotifyHandler spotifyHandler) {
        BluetoothHandlerService.spotifyHandler = spotifyHandler;
    }

    public static boolean isShouldUseSpotify() {
        return shouldUseSpotify;
    }

    public static void setShouldUseSpotify(boolean shouldUseSpotify) {
        BluetoothHandlerService.shouldUseSpotify = shouldUseSpotify;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) bluetoothGatt.close();
        scanLeDevice(false);
        scanCallback = null;
        gattCallback = null;
        spotifyHandler.disconnectSpotifyConnection();
        updateActivity(false);

//        if (isShouldRestart()) {
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction("restartservice");
//            broadcastIntent.setClass(this, Restarter.class);
//            this.sendBroadcast(broadcastIntent);
//        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
