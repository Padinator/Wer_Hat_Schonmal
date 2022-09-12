package com.example.ichhabschonmal.server_client_communication;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SocketEndPoint {
    protected final Activity activity;
    protected final Context context;

    protected String serverIP;
    public static final int SERVER_PORT = 8080;

    public SocketEndPoint(Activity activity, Context context, String serverIP) {
        this.activity = activity;
        this.context = context;
        this.serverIP = serverIP;
    }

    public String getServerIP() {
        return serverIP;
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
}
