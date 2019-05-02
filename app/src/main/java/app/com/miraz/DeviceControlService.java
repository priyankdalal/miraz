package app.com.miraz;

import android.annotation.TargetApi;
import android.app.Service;
import android.arch.lifecycle.OnLifecycleEvent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class DeviceControlService extends Service {
    private final static String TAG = "+++++++++++++++++";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static boolean isServiceRunning = false;

    private String mDeviceName;
    private String mDeviceAddress;
    private String mBluetoothDeviceAddress;

    //bluetooth gatt
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mbluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private int mConnectionState = STATE_DISCONNECTED;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    //my gattservice
    private static final String notifierServiceUUID = "3db02924-b2a6-4d47-be1f-0f90ad62a048";
    private static final String notifierMessageCharactersticUUID = "8d8218b6-97bc-4527-a8db-13094ac06b1d";
    private static final String notifierTimeCharactersticUUID = "b7b0a14b-3e94-488f-b262-5d584a1ef9e1";

    private static final String NOTIFICATION_CODE_UUID = "d6afa6d2-b79e-4d80-a517-47e898abb113";
    private static final String NOTIFICATION_TITLE_UUID = "bbb19eb8-17ae-4345-b038-2964a44c30b6";
    private static final String NOTIFICATION_CONVERSATION_TITLE_UUID = "8f1d743d-edf3-4b31-b70a-0781ce7395d3";
    private static final String NOTIFICATION_SUBTEXT_UUID = "944511fb-c208-4922-8e1e-66347cf695d8";
    private static final String NOTIFICATION_TEXT_UUID = "ef9edced-ff56-481e-9ac1-e30a3e731ec1";
    private static final String NOTIFICATION_SELF_DISPLAY_NAME_UUID = "a2fa4dac-891b-499d-8f23-f559fc41f602";
    private static final String NOTIFICATION_INFO_TEXT_UUID = "99c56b26-6b57-4919-8b35-e2ac42309546";
    private static final String NOTIFICATION_ICON_UUID = "93acaf3e-6112-4904-97b1-0602eaf93994";
    private static final String DEVICE_TIME_UUID = "efd6ec74-388b-4f48-9b54-78aa70b85fca";
    private static final String DEVICE_BATTERY_UUID = "9960f7e4-4803-47e2-b97c-2a6bdb409e0f";

    private BluetoothGattService notifierService;

    private BluetoothGattCharacteristic notifierCodeCharacterstic;
    private BluetoothGattCharacteristic notifierTitleCharacterstic;
    private BluetoothGattCharacteristic notifierConversationTitleCharacterstic;
    private BluetoothGattCharacteristic notifierSubTextCharacterstic;
    private BluetoothGattCharacteristic notifierTextCharacterstic;
    private BluetoothGattCharacteristic notifierSelfDisplayNameCharacterstic;
    private BluetoothGattCharacteristic notifierInfoTextCharacterstic;
    private BluetoothGattCharacteristic notifierTimeCharacterstic;
    private BluetoothGattCharacteristic notifierBatteryCharacterstic;

    private NotificationReciever notificationReciever;

    public DeviceControlService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        isServiceRunning = true;
        //startServiceWithNotification();
        Log.i(TAG,"registering notification service");
        notificationReciever = new NotificationReciever();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("app.com.miraz");
        registerReceiver(notificationReciever,intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        isServiceRunning = true;
        //Set<String> keys = intent.getExtras().keySet();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        Log.d(TAG, ""+mDeviceName);
        //showToast("device name "+ mDeviceName);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        Log.d(TAG, ""+mDeviceAddress);
        //showToast("device address "+ mDeviceAddress);

        connect(mDeviceAddress);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        isServiceRunning = false;
        super.onDestroy();
    }

    private void showToast(final String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Ble services part start here
    @TargetApi(Build.VERSION_CODES.M)
    public boolean connect(final String address){
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        if(mBluetoothAdapter == null && address == null){
            Log.d(TAG, "BluetoothAdapter not initialized or unspecified address.");
            this.showToast("BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        if(mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mbluetoothGatt != null){
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            this.showToast("Trying to use an existing mBluetoothGatt for connection.");
            if(mbluetoothGatt.connect()){
                mConnectionState = STATE_CONNECTING;
                return true;
            }else{
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if(device == null){
            Log.w(TAG, "Device not found.  Unable to connect.");
            this.showToast("Device not found.  Unable to connect.");
            return false;
        }

        mbluetoothGatt = device.connectGatt(this, false,mGattCallback,2);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(newState == BluetoothProfile.STATE_CONNECTED){
                mConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server.");
                showToast("connected to gatt server");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mbluetoothGatt.discoverServices());
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i(TAG, "Disconnected from GATT server.");
                showToast("Disconnected from GATT server.");
                mConnectionState = STATE_DISCONNECTED;
                gatt.close();
                gatt.disconnect();
                gatt = null;
                //mbluetoothGatt.close();
                //mbluetoothGatt.disconnect();
                //mbluetoothGatt = null;
                //stopSelf();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            String uuid;
            String charuuid;
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //services has been discoverd
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                List<BluetoothGattService> gattServices = mbluetoothGatt.getServices();
                for(BluetoothGattService gattService : gattServices){
                    uuid = gattService.getUuid().toString();
                    if(uuid.equals(notifierServiceUUID)){
                        notifierService = gattService;
                        showToast("notifier uuid matched");
                        List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                            charuuid = gattCharacteristic.getUuid().toString();
                            if(charuuid.equals(NOTIFICATION_CODE_UUID)){
                                notifierCodeCharacterstic = gattCharacteristic;
                            }else if(charuuid.equals(NOTIFICATION_TITLE_UUID)){
                                notifierTitleCharacterstic = gattCharacteristic;
                            }else if(charuuid.equals(NOTIFICATION_CONVERSATION_TITLE_UUID)){
                                notifierConversationTitleCharacterstic = gattCharacteristic;
                            }else if(charuuid.equals(NOTIFICATION_SUBTEXT_UUID)){
                                notifierSubTextCharacterstic = gattCharacteristic;
                            }else if(charuuid.equals(NOTIFICATION_TEXT_UUID)){
                                notifierTextCharacterstic = gattCharacteristic;
                            }else if(charuuid.equals(NOTIFICATION_SELF_DISPLAY_NAME_UUID)){
                                notifierSelfDisplayNameCharacterstic = gattCharacteristic;
                            }else if(charuuid.equals(NOTIFICATION_INFO_TEXT_UUID)){
                                notifierInfoTextCharacterstic = gattCharacteristic;
                            }else if(charuuid.equals(DEVICE_TIME_UUID)){
                                notifierTimeCharacterstic = gattCharacteristic;
                            }else if(charuuid.equals(DEVICE_BATTERY_UUID)){
                                notifierBatteryCharacterstic = gattCharacteristic;
                            }
                        }
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                //stopSelf();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }
    };

    // send messages to BLE
    public void sendMessageToBle(BluetoothGattCharacteristic bleCharacterstic,String message){
        if(mbluetoothGatt != null && bleCharacterstic != null){
            bleCharacterstic.setValue(message);
            mbluetoothGatt.writeCharacteristic(bleCharacterstic);
        }
    }

    //notification broadcast reciever
    public class NotificationReciever extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            int recievedNotificationCode = intent.getIntExtra("notificationCode", -1);
            Log.i("+++++++++++","notification recieved"+recievedNotificationCode);
            if(recievedNotificationCode ==1){
                showToast(intent.getStringExtra("notificationText"));
            }else if(recievedNotificationCode == 2){
                showToast(intent.getStringExtra("notificationText"));
            }else if(recievedNotificationCode == 3){
                showToast(intent.getStringExtra("notificationText"));
            }else if(recievedNotificationCode == 4){
                showToast(intent.getStringExtra("notificationText"));
            }else if(recievedNotificationCode == 5){
                showToast(intent.getStringExtra("notificationTitle"));
            }else{
                showToast("Other Notifications");
            }
            if(notifierTextCharacterstic != null){
                sendMessageToBle(notifierTextCharacterstic, intent.getStringExtra("notificationIcon"));
            }
            //do further things
        }
    }
}
