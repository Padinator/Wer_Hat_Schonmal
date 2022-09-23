package com.example.ichhabschonmal.server_client_communication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Client extends SocketCommunicator {

    private String deviceName, IPAddress;
    private int playerNumber = -1;

    public Client(Activity activity, Context context, Socket endPoint, BufferedReader input, PrintWriter output) {
        this(activity, context, endPoint, input, output, "", "");
    }

    public Client(Activity activity, Context context, Socket endPoint, BufferedReader input, PrintWriter output, String deviceName, String IPAddress) {
        super(activity, context, endPoint, input, output);

        this.deviceName = deviceName;
        this.IPAddress = IPAddress;
    }

    // Getter and setter
    public String getDeviceName() {
        return deviceName;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public int getPlayerNumber() { return playerNumber; }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public void setPlayerNumber(int playerNumber) { this.playerNumber = playerNumber; }

    // @Override methods
    @Override
    public String toString() {
        return "Client: Player-Number: " + playerNumber + ", Device: " + deviceName + ", IP-Address: " + IPAddress;
    }
}
