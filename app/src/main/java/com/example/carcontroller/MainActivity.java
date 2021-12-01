package com.example.carcontroller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.Lottie;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieConfig;
import com.airbnb.lottie.LottieDrawable;
import com.example.carcontroller.globals.AppConfig;
import com.example.carcontroller.globals.BtEventTypes;
import com.example.carcontroller.globals.Controls;
import com.example.carcontroller.services.BluetoothHandlerService;
import com.example.carcontroller.services.HttpServerService;
import com.example.carcontroller.services.Restarter;
import com.example.carcontroller.utils.AudioUtils;
import com.example.carcontroller.utils.GlobalUtils;
import com.example.carcontroller.utils.SharedPerefrencesUtils;
import com.example.carcontroller.utils.SpotifyHandler;
import com.example.carcontroller.utils.UserState;
import com.example.carcontroller.views.BtActionView;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;

import java.util.List;

public class MainActivity extends Activity {
    BluetoothHandlerService mYourService;
    Intent mServiceIntent;
    private int REQUEST_LOCATION_CODE = 2;
    private static final int REQUEST_ENABLE_BT_CODE = 31;
    private BluetoothAdapter mBluetoothAdapter;
    private Button btConnectBtn;
    private ImageView menuBtn, deleteBtn;
    private Handler Handler;
    private final int SCALE_VIEW_DURATION = 500;
    private boolean isMenuOpened = false;
    private RelativeLayout menuWrapper, menuBottomSide, btConnectLayout;
    private LottieAnimationView btSearchingView;
    private LinearLayout layoutHider;

    private ListView btActionsList;
    private List<String> finalOptions;
    private Button set1, set2, set3, callSet;

    private BroadcastReceiver broadcastReceiver;
    private boolean isBtOnGlobal;
    private final String BT_DISCONNECTED = "btDisconnected";
    private final String BT_CONNECTING = "btConnecting";
    private final String BT_CONNECTED = "btConnected";

    private RelativeLayout redControl, blueControl, greenControl, purpleControl;
    private int controlPickedGlobal = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Handler = new Handler();
        initViewItems();

        initReciver();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(AppConfig.BT_EVENT));

        isBtOnGlobal = GlobalUtils.isMyServiceRunning(getApplicationContext(), BluetoothHandlerService.class);
        if (isBtOnGlobal) {
            changeBtStatus(BT_CONNECTED);
        }

        BluetoothHandlerService.setSpotifyHandler(new SpotifyHandler(getApplicationContext()));
    }

    private void initViewItems() {
        btConnectBtn = findViewById(R.id.bt_connect_btn);
        menuBtn = findViewById(R.id.menu_btn);
        deleteBtn = findViewById(R.id.delete_btn);
        menuWrapper = findViewById(R.id.menu_wrapper);
        menuBottomSide = findViewById(R.id.menu_bottom_side);
        layoutHider = findViewById(R.id.layout_hider);
        btConnectLayout = findViewById(R.id.bt_connect_layout);
        btSearchingView = findViewById(R.id.bt_searching);


        //For options list
        initControlViews();

        initBtCotrolsList();
    }

    private void initBtCotrolsList() {
        btActionsList = findViewById(R.id.bt_actions_list);
        finalOptions = new ArrayList<>();

        List<String> finalOptionsDisplay = new ArrayList<>();
        if (SpotifyAppRemote.isSpotifyInstalled(getApplicationContext())) {
            finalOptionsDisplay.addAll(Controls.spotifyOptionsDisplay);
            finalOptions.addAll(Controls.spotifyOptions);
        }
        finalOptionsDisplay.addAll(Controls.phoneOptionsDisplay);
        finalOptions.addAll(Controls.phoneOptions);

        //TODO use more prettier list
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, finalOptionsDisplay);
        btActionsList.setAdapter(adapter);
        btActionsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String value = finalOptions.get(position);
//                adapter.getItem(position);
                handleControlPick(value);
//                Toast.makeText(getApplicationContext(),value,Toast.LENGTH_SHORT).show();
                toggleMenu(null);
            }
        });
    }

    private void initControlViews() {
        redControl = findViewById(R.id.red_control);
        blueControl = findViewById(R.id.blue_control);
        greenControl = findViewById(R.id.green_control);
        purpleControl = findViewById(R.id.purple_control);

        refreshControlViews();

        set1 = findViewById(R.id.set_1);
        set2 = findViewById(R.id.set_2);
        set3 = findViewById(R.id.set_3);
        callSet = findViewById(R.id.call_set);
    }

    private void refreshControlViews() {
        String[] currentControlValues = SharedPerefrencesUtils.getCurrentControlsState(getApplicationContext());
        for (int i = 0; i < currentControlValues.length; i++) {
            switch (i) {
                case Controls.RED_CONTROL:
                    ((BtActionView) redControl.getChildAt(0)).setIcons(currentControlValues[i]);
                    break;
                case Controls.BLUE_CONTROL:
                    ((BtActionView) blueControl.getChildAt(0)).setIcons(currentControlValues[i]);
                    break;
                case Controls.GREEN_CONTROL:
                    ((BtActionView) greenControl.getChildAt(0)).setIcons(currentControlValues[i]);
                    break;
                case Controls.PURPLE_CONTROL:
                    ((BtActionView) purpleControl.getChildAt(0)).setIcons(currentControlValues[i]);
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        View decorView = getWindow().getDecorView();

        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT_CODE && resultCode == RESULT_CANCELED)  {
            Log.d("PERMISSIONS", "BT + NO");
            Toast.makeText(getApplicationContext(), "You need to enable bluetooth", Toast.LENGTH_SHORT).show();
        } else if (requestCode == REQUEST_ENABLE_BT_CODE && resultCode == RESULT_OK)  {
            Log.d("PERMISSIONS", "BT + OK");
            handleBluetooth();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSIONS", "LOC + OK");
            // init bluetooth adapter
            handleBluetooth();
        } else if (requestCode == REQUEST_LOCATION_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Log.d("PERMISSIONS", "LOC + NO");
            Toast.makeText(getApplicationContext(), "You need to grand permission location", Toast.LENGTH_SHORT).show();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void changeSet(View v) {
        if (set1.equals(v)) {
            SharedPerefrencesUtils.setSelectedSet(getApplicationContext(), SharedPerefrencesUtils.SET_1);
        } else if (set2.equals(v)) {
            SharedPerefrencesUtils.setSelectedSet(getApplicationContext(), SharedPerefrencesUtils.SET_2);
        } else if (set3.equals(v)) {
            SharedPerefrencesUtils.setSelectedSet(getApplicationContext(), SharedPerefrencesUtils.SET_3);
        } else if (callSet.equals(v)) {
            SharedPerefrencesUtils.setSelectedSet(getApplicationContext(), SharedPerefrencesUtils.CALL_SET);
        }
        refreshControlViews();
    }

    public void toggleMenu(View v) {
        if (!isMenuOpened) { //if menu is closed
            redControl.setClickable(false);
            blueControl.setClickable(false);
            greenControl.setClickable(false);
            purpleControl.setClickable(false);

            if (v.getId() == redControl.getId()) {
               controlPickedGlobal = Controls.RED_CONTROL;
            } else if (v.getId() == blueControl.getId()) {
                controlPickedGlobal = Controls.BLUE_CONTROL;
            } else if (v.getId() == greenControl.getId()) {
                controlPickedGlobal = Controls.GREEN_CONTROL;
            } else if (v.getId() == purpleControl.getId()) {
                controlPickedGlobal = Controls.PURPLE_CONTROL;
            }

            layoutHider.setVisibility(View.VISIBLE);
            layoutHider.bringToFront();
            scaleView(menuWrapper, 3, true);
            scaleView(menuBottomSide, 5 ,true);
            menuWrapper.bringToFront();
            btActionsList.setVisibility(View.VISIBLE);

            menuBtn.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);

            isMenuOpened = !isMenuOpened;
        } else {
            controlPickedGlobal = -1;
            layoutHider.setVisibility(View.GONE);
            scaleView(menuWrapper, 3, false);
            scaleView(menuBottomSide, 5 ,false);
            btActionsList.setVisibility(View.GONE);

            Handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            redControl.setClickable(true);
                            blueControl.setClickable(true);
                            greenControl.setClickable(true);
                            purpleControl.setClickable(true);
                        }
                    });
                }
            },  SCALE_VIEW_DURATION + 100);

            menuBtn.setVisibility(View.GONE);
            deleteBtn.setVisibility(View.GONE);

            isMenuOpened = !isMenuOpened;
        }
    }

    public void cancelScan(View v) {
        BluetoothHandlerService.setShouldRestart(false);
        Intent stopIntent = new Intent(getApplicationContext(), BluetoothHandlerService.class);
        stopService(stopIntent);
    }

    public void checkIfBluetoothIsOn(View v) {
        /**
         *
         */
        // check whether BLE is supported
        if (!isBtOnGlobal) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
                this.finish();
            }

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                handleBluetooth();
//            turnGPSOn();
                //TODO ask to turn on gps
            }
        } else {
            BluetoothHandlerService.setShouldRestart(false);
            Intent stopIntent = new Intent(this, BluetoothHandlerService.class);
            stopService(stopIntent);
        }
    }

    private void turnGPSOn(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    public void handleBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
//        BluetoothHandlerService.setBluetoothAdapter(mBluetoothAdapter);
        // check if bluetooth is supported in device
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT_CODE);
        } else {
            changeBtStatus(BT_CONNECTING);

            BluetoothHandlerService.setBluetoothLeScanner(mBluetoothAdapter.getBluetoothLeScanner());
            mYourService = new BluetoothHandlerService();
            mServiceIntent = new Intent(this, mYourService.getClass());
            if (!GlobalUtils.isMyServiceRunning(getApplicationContext(), mYourService.getClass())) {
                startForegroundService(mServiceIntent);
            }
        }
    }

    private void changeBtStatus(String status) {
        switch (status) {
            case BT_CONNECTED:
                if (btSearchingView.isAnimating()) btSearchingView.cancelAnimation();
                btSearchingView.setVisibility(View.GONE);
                btConnectBtn.setBackgroundResource(R.drawable.bluetooth_disconnect_icon);
                btConnectBtn.setVisibility(View.VISIBLE);
                GradientDrawable connectedDrawable = (GradientDrawable) btConnectLayout.getBackground();
                connectedDrawable.setColor(getResources().getColor(R.color.bt_connected, null));
                btConnectBtn.setClickable(true);
                break;
            case BT_CONNECTING:
                btConnectBtn.setClickable(false);
                btConnectBtn.setVisibility(View.GONE);
                btSearchingView.setProgress(0);
                btSearchingView.setVisibility(View.VISIBLE);
                btSearchingView.playAnimation();
                GradientDrawable connectingDrawable = (GradientDrawable) btConnectLayout.getBackground();
                connectingDrawable.setColor(getResources().getColor(R.color.bt_connecting, null));
                break;
            case BT_DISCONNECTED:
                if (btSearchingView.isAnimating()) btSearchingView.cancelAnimation();
                btSearchingView.setVisibility(View.GONE);
                btConnectBtn.setBackgroundResource(R.drawable.bluetooth_connect_icon);
                btConnectBtn.setVisibility(View.VISIBLE);
                GradientDrawable disconnectedDrawable = (GradientDrawable) btConnectLayout.getBackground();
                disconnectedDrawable.setColor(getResources().getColor(R.color.bt_not_connected, null));
                btConnectBtn.setClickable(true);
                break;
        }
    }

    public void handleControlPick(String value) {
        switch (controlPickedGlobal) {
            case Controls.RED_CONTROL:
                ((BtActionView) redControl.getChildAt(0)).setIcons(value);
                SharedPerefrencesUtils.setControlValue(getApplicationContext(), controlPickedGlobal, value);
                break;
            case Controls.BLUE_CONTROL:
                ((BtActionView) blueControl.getChildAt(0)).setIcons(value);
                SharedPerefrencesUtils.setControlValue(getApplicationContext(), controlPickedGlobal, value);
                break;
            case Controls.GREEN_CONTROL:
                ((BtActionView) greenControl.getChildAt(0)).setIcons(value);
                SharedPerefrencesUtils.setControlValue(getApplicationContext(), controlPickedGlobal, value);
                break;
            case Controls.PURPLE_CONTROL:
                ((BtActionView) purpleControl.getChildAt(0)).setIcons(value);
                SharedPerefrencesUtils.setControlValue(getApplicationContext(), controlPickedGlobal, value);
                break;
        }
    }

    public void deleteControlValue(View v) {
        handleControlPick("");
        toggleMenu(null);
    }

    private void initReciver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //TODO add type string
                String type = intent.getStringExtra("type");
                if (type == null) return;

                switch (type) {
                    case BtEventTypes
                            .MSG:
                        String msg = intent.getStringExtra("msg");
                        if (msg != null) Toast.makeText(MainActivity.this, "MSG: " + msg, Toast.LENGTH_SHORT).show();
                        break;
                    case BtEventTypes.CONNECTION_CHANGE:
                        boolean isBtOn = intent.getBooleanExtra("isBtOn", false);
                        if (isBtOn) {
                            changeBtStatus(BT_CONNECTED);
                        } else {
                            changeBtStatus(BT_DISCONNECTED);
                        }
                        isBtOnGlobal = isBtOn;
                        break;
                }
            }
        };
    }

    public void scaleView(View v, float multiply, boolean up) {
        ValueAnimator anim;
        if (up) {
            anim = ValueAnimator.ofInt(v.getMeasuredHeight(), (int) (v.getMeasuredHeight() * multiply));
        }  else {
            anim = ValueAnimator.ofInt(v.getMeasuredHeight(), (int) (v.getMeasuredHeight() / multiply));
        }
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.height = val;
                v.setLayoutParams(layoutParams);
            }
        });
        anim.setDuration(SCALE_VIEW_DURATION);
        anim.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        broadcastReceiver = null;
        /**
         * Sticky Service
         */
        if (!isBtOnGlobal) {
            BluetoothHandlerService.setShouldRestart(false);
            Intent stopIntent = new Intent(getApplicationContext(), BluetoothHandlerService.class);
            stopService(stopIntent);
        }

//        if (BluetoothHandlerService.isShouldRestart() && !GlobalUtils.isMyServiceRunning(getApplicationContext(), BluetoothHandlerService.class)) {
//            Intent broadcastIntent = new Intent();
//            broadcastIntent.setAction("restartservice");
//            broadcastIntent.setClass(this, Restarter.class);
//            this.sendBroadcast(broadcastIntent);
//        }
    }

}