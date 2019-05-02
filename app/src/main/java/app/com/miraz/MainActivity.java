package app.com.miraz;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private int MY_PERMISSIONS_REQUEST_BLUETOOTH = 1;
    private int MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN = 1;
    private int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;


    RecyclerView recycleView;
    RecycleViewAdapter mAdapter;
    LinearLayoutManager mManager;

    Button btnStartScan;
    Button btnStopService;


    ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();


    private static final long SCAN_PERIOD = 3000;
    private boolean mScanning;
    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check for blutooth permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH);
            }
        }
        //check for blutooth admin permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_ADMIN)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN},
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_ADMIN);
            }
        }
        //check for location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }

        if(!isNotificationServiceEnabled()){
            startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }

        //initialize bluetooth
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
//        scanBleDevice(true);

        btnStartScan = (Button) findViewById(R.id.btnStartScan);
        btnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanBleDevice(true);
            }
        });

        btnStopService = (Button) findViewById(R.id.btnStopService);
        /*btnStopService.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent stopNotifierServiceIntent = new Intent(MainActivity.this,DeviceControlService.class);
                stopService(stopNotifierServiceIntent);
            }
        });*/


        recycleView = (RecyclerView) findViewById(R.id.recycleView);
        mManager = new LinearLayoutManager(MainActivity.this);
        recycleView.setLayoutManager(mManager);

        mAdapter = new RecycleViewAdapter(MainActivity.this, mLeDevices);
        recycleView.setAdapter(mAdapter);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void scanBleDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);

                    Toast.makeText(getApplication(), "Scanning Stop", Toast.LENGTH_SHORT).show();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
            Toast.makeText(getApplication(), "Scanning Start", Toast.LENGTH_SHORT).show();
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
            Toast.makeText(getApplication(), "Scanning Stop", Toast.LENGTH_SHORT).show();

        }
    }


    //        // Device scan callback.
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!mLeDevices.contains(device)) {
                        mLeDevices.add(device);
                    }

                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }

                }

            });
        }
    };

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
