package de.android.werhatschonmal.server_client_communication;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * <strong>
 *     Sub class of SocketCommunicator, can communicate with other clients.<br>
 *     Difference to SocketCommunicator: client-properties (getter and setter)
 * </strong>
 */
public class Client extends SocketCommunicator {

    /**
     *
     * <strong>
     *     Data field the name of a device.<br>
     *  </strong>
     */
    private String deviceName;

    /**
     *
     * <strong>
     *     Data field the IP-address of a device.<br>
     *  </strong>
     */
    private String IPAddress;

    /**
     *
     * <strong>
     *     Data field saving a client's/"device's" player number.<br>
     *  </strong>
     */
    private int playerNumber = -1;

    /**
     *
     * @param activity Pass actual activity for showing Toasts.
     * @param context Pass actual context for making Toasts.
     * @param endPoint Pass a Socket for sending and receiving messages.
     * @param input Pass the BufferedReader to the Socket.
     * @param output Pass the PrintWriter to the Socket.
     */
    public Client(Activity activity, Context context, Socket endPoint, BufferedReader input, PrintWriter output) {
        this(activity, context, endPoint, input, output, "", "");
    }

    /**
     *
     * @param activity Pass actual activity for showing Toasts.
     * @param context Pass actual context for making Toasts.
     * @param endPoint Pass a Socket for sending and receiving messages.
     * @param input Pass the BufferedReader to the Socket.
     * @param output Pass the PrintWriter to the Socket.
     * @param deviceName Pass the nae of the actual device.
     * @param IPAddress Pass the IP-address of the device in WLAN.
     */
    public Client(Activity activity, Context context, Socket endPoint, BufferedReader input, PrintWriter output, String deviceName, String IPAddress) {
        super(activity, context, endPoint, input, output);

        this.deviceName = deviceName;
        this.IPAddress = IPAddress;
    }

    /**
     *
     * @return The the name of the actual device.
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     *
     * @return Return the IP-adress of the actual device.
     */
    public String getIPAddress() {
        return IPAddress;
    }

    /**
     *
     * @return Return the actual player number of the actual game.
     */
    public int getPlayerNumber() {
        return playerNumber;
    }

    /**
     *
     * @param deviceName Pass and set name of the actual device.
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    /**
     *
     * @param IPAddress Pass and set IP-address of the actual device.
     */
    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    /**
     *
     * @param playerNumber Pass and set player number in game of the actual device.
     */
    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    /**
     *
     * @return Return the String value of a Client.
     */
    @NonNull
    @Override
    public String toString() {
        return "Client: Player-Number: " + playerNumber + ", Device: " + deviceName + ", IP-Address: " + IPAddress;
    }
}
