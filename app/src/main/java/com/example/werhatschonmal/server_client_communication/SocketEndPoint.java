package com.example.werhatschonmal.server_client_communication;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author Patrick
 *
 * <strong>
 *     Super class for ServerSocketEndPoint and ClientSocketEndPoint.<br>
 *     -> Communication between client and server.<br>
 * </strong>
 */
public abstract class SocketEndPoint {
    protected Activity activity;
    protected Context context;

    protected String serverIP;
    protected Thread connectionThread;
    protected SocketCommunicator.Receiver receiverAction;

    // Constants (condition: separators are not used by users, who play the game)
    /**
     *
     * <strong>Data field for the communication port.</strong><br>
     */
    public static final int SERVER_PORT = 8080;

    /**
     *
     * <strong>Data field for separating parts of a message inside a message.</strong><br>
     */
    public static final String SEPARATOR = "SEPARATOR";

    /**
     *
     * <strong>Data field for informing a client to close connection.</strong><br>
     */
    public static final String CLOSE_CONNECTION = "CLOSE_CONNECTION";

    /**
     *
     * <strong>Data field for telling a client to create a player.</strong><br>
     */
    public static final String CREATE_PLAYER = "CREATE_PLAYER";

    /**
     *
     * <strong>Data field for sending host the created player of the client.</strong><br>
     */
    public static final String CREATED_PLAYER = "CREATED_PLAYER";

    /**
     *
     * <strong>Data field for telling clients to start the game.</strong><br>
     */
    public static final String PLAY_GAME_CLIENTS = "PLAY_GAME_CLIENTS"; // Clients can go to PlayGame-intent

    /**
     *
     * <strong>Data field informing host to start the game too.</strong><br>
     * The host should wait, until he received this message from all clients.
     */
    public static final String PLAY_GAME_HOST = "PLAY_GAME_HOST"; // Host can go to PlayGame-intent

    /**
     *
     * <strong>Data field for informing clients over preconditions of the actual game.</strong><br>
     */
    public static final String SET_PRE_CONDITIONS_OF_PLAYING_GAME = "SET_PRE_CONDITIONS_OF_PLAYING_GAME";

    /**
     *
     * <strong>Data field for informing a client that the game has changed.</strong><br>
     * A new round can be started.
     */
    public static final String GAME_STATUS_CHANGED = "GAME_STATUS_CHANGED";

    /**
     *
     * <strong>Data field for informing a client to guess.</strong><br>
     */
    public static final String YOUR_TURN = "YOUR_TURN";

    /**
     *
     * <strong>
     *     Data field for receiving the results of choosing from the client, who guessed.<br>
     * </strong>
     */
    public static final String PLAYER_CHOSEN = "PLAYER_CHOSEN";

    /**
     *
     * <strong>Data field for informing a client about guessing results.</strong><br>
     * Some client or the host itself has guessed and all clients can receive the results of the
     * host.
     */
    public static final String RESULT_OF_GUESSING = "RESULT_OF_GUESSING";

    /**
     *
     * <strong>Data field for informing the host that a client view the actual score.</strong><br>
     * The host waits for receiving this message from all clients. Then he continues.
     */
    public static final String VIEWED_ACTUAL_SCORE = "VIEWED_ACTUAL_SCORE";

    /**
     *
     * @param activity Pass actual activity for running messages on UI-Thread.
     * @param context Pass actual context for making and showing Toasts.
     * @param serverIP Pass the IP-address of the server (to connect as client or the own IP as
     *                 server).
     */
    public SocketEndPoint(Activity activity, Context context, String serverIP) {
        this.activity = activity;
        this.context = context;
        this.serverIP = serverIP;
    }

    /**
     *
     * @return Return actual activity.
     */
    public Activity getActivity() {
        return activity;
    }

    /**
     *
     * @return Return actual context.
     */
    public Context getContext() {
        return context;
    }

    /**
     *
     * @return Return actual server IP-address.
     */
    public String getServerIP() {
        return serverIP;
    }

    /**
     *
     * @param activity Set actual activity.
     */
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /**
     *
     * @param context Set actual context.
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     *
     * @param serverIP Set actual server IP-address.
     */
    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    /**
     *
     * @return Returns the IP-address (in WLAN) of the actual device as a String.
     * @throws UnknownHostException
     */
    protected String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();

        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(ipInt).array()).getHostAddress();
    }

    /**
     *
     * @return Returns the device name of the actual device as a String.
     */
    public String getNameOfDevice() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     *
     * @return Return true, if a connection Thread is running, else return false.
     */
    public boolean isAConnectionThreadRunning() {
        return connectionThread != null && connectionThread.getState() != Thread.State.TERMINATED;
    }

    /**
     *
     * @param s Input a String that will be turned to uppercase.
     * @return Return the capitalized String.
     */
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
