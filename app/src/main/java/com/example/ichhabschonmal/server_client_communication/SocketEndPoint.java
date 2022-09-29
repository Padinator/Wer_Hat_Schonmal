package com.example.ichhabschonmal.server_client_communication;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class SocketEndPoint {
    protected Activity activity;
    protected Context context;

    protected String serverIP;
    protected Thread connectionThread;
    protected SocketCommunicator.Receiver receiverAction;

    // Constants
    public static final int SERVER_PORT = 8080;
    public static final String CLOSE_CONNECTION = "CLOSE_CONNECTION";
    public static final String CREATE_PLAYER = "CREATE_PLAYER";
    public static final String CREATED_PLAYER = "CREATED_PLAYER";
    public static final String START_OF_A_STORY = "START_OF_A_STORY";
    public static final String PLAY_GAME = "PLAY_GAME";

    public SocketEndPoint(Activity activity, Context context, String serverIP) {
        this.activity = activity;
        this.context = context;
        this.serverIP = serverIP;
    }

    public Activity getActivity() {
        return activity;
    }

    public Context getContext() {
        return context;
    }

    public String getServerIP() {
        return serverIP;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /*
     *
     * Returns the local IP-address of the actual device as a String.
     *
     */
    protected String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();

        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(ipInt).array()).getHostAddress();
    }

    public String getNameOfDevice() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public boolean isAConnectionThreadRunning() {
        return connectionThread != null && connectionThread.getState() != Thread.State.TERMINATED;
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
