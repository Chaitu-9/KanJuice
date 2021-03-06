package com.example.kanjuice;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.kanjuice.utils.TypedJsonString;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BluetoothReaderService extends Service implements BluetoothDataReader.SerialDataReceiver {

    public static final String TAG = "BluetoothReaderService";

    final BluetoothDataReader bluetoothDataReader;
    private boolean isConnected;
    public static final int MSG_FAILED_BLUETOOTH_CONNECTION = 103;
    public static final int MSG_DATA_RECEIVED = 102;
    public static final int MSG_DATA_RECEIVE_FAILED = 105;

    private static final int MSG_INITIALIZE_BLUETOOTH = 501;

    Handler H = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_INITIALIZE_BLUETOOTH:
                    isConnected = bluetoothDataReader.openConnection();
                    break;
            }
        }
    };

    private final IBinder mBinder = new LocalBinder();
    private boolean clientListening;
    private Handler clientHandler;

    public class LocalBinder extends Binder {
        public BluetoothReaderService getService() {
            return BluetoothReaderService.this;
        }
    }

    private BroadcastReceiver stateChangeListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getAction();
            if (state.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                bluetoothDataReader.closeConnection();
                isConnected = false;
            }
        }
    };

    public BluetoothReaderService() {
        bluetoothDataReader = new BluetoothDataReader(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        H.sendEmptyMessageDelayed(MSG_INITIALIZE_BLUETOOTH, 2000);
        registerForBluetoothConnectionChange();
    }

    private void registerForBluetoothConnectionChange() {
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        this.registerReceiver(stateChangeListener, filter1);
        this.registerReceiver(stateChangeListener, filter2);
        this.registerReceiver(stateChangeListener, filter3);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        bluetoothDataReader.closeConnection();
        unregisterReceiver(stateChangeListener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDataReceived(byte[] data) {
        Log.d(TAG, "onDataReceived: " + data);
        String dataString = new String(data);
        sendLogData("[BluetoothReaderService][onDataReceived] Data receieved from Bluetooth" + dataString);
        if (dataString.startsWith("$") && dataString.endsWith("*")) {
            sendCardNumber(extractCardNumber(dataString));
        }
    }

    private void sendCardNumber(Integer cardNumber) {
        if (cardNumber == 0) {
            return;
        }

        if (clientListening && clientHandler != null) {
            clientHandler.obtainMessage(MSG_DATA_RECEIVED, cardNumber).sendToTarget();
        }
    }

    @Override
    public void onDataReceiveFail(String message) {
        sendLogData("[BluetoothReaderService][onDataReceiveFail]" +
                "     Data receieved  Fail from Bluetooth :: " + message);

        if (clientListening && clientHandler != null) {
            sendLogData("[BluetoothReaderService][onDataReceiveFail]" +
                    " Data receieved client Listening from Bluetooth :: " + message);

            clientHandler.obtainMessage(MSG_DATA_RECEIVE_FAILED, message).sendToTarget();
        }
    }

    public void startListening(Handler clientHandler) {
        if (!isConnected) {
            isConnected = bluetoothDataReader.openConnection();
            if (!isConnected) {
                clientHandler.obtainMessage(MSG_FAILED_BLUETOOTH_CONNECTION).sendToTarget();
            }
        }

        this.clientHandler = clientHandler;
        clientListening = true;
    }

    public void stopListening() {
        clientListening = false;
        clientHandler = null;
    }

    private Integer extractCardNumber(String readString) {
        try {
            Log.d(TAG, "Card# " + readString);
            sendLogData("[BluetoothReaderService][extractCardNumber] Data receieved from Bluetooth" + readString);

            String cardDecNumber = readString.substring(readString.indexOf("$") + 1, readString.length() - 1).trim();
            String binaryNumber = Integer.toBinaryString(Integer.valueOf(cardDecNumber));
            int startIndex = binaryNumber.length() - 17;
            if (startIndex < 0) {
                startIndex = 0;
            }
            return Integer.valueOf(binaryNumber.substring(startIndex, binaryNumber.length() - 1), 2);
        } catch(Exception e) {
            return 0;
        }
    }

    private void sendLogData(String debugMessage) {
        getJuiceServer().saveLogData(new TypedJsonString("{error: " + debugMessage + "}"), new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private KanJuiceApp getApp() {
        return (KanJuiceApp) getApplication();
    }

    private JuiceServer getJuiceServer() {
        return getApp().getJuiceServer();
    }
}