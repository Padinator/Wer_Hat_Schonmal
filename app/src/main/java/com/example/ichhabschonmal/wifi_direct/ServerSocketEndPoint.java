package com.example.ichhabschonmal.wifi_direct;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ServerSocketEndPoint {
    private final Activity activity;
    private final Context context;

    private Socket serverSocket;
    private Socket clientSocket;
    private final String SERVER_IP;
    public static final int SERVER_PORT = 8080;

    private PrintWriter output;
    private BufferedReader input;
    private String message;

    public ServerSocketEndPoint(Activity activity, Context context) throws UnknownHostException {
        this.activity = activity;
        this.context = context;
        SERVER_IP = getLocalIpAddress();
    }

    public String getSERVER_IP() {
        return SERVER_IP;
    }

    public String getMessage() {
        return message;
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();

        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(ipInt).array()).getHostAddress();
    }

    public class Connector implements Runnable {

        private Receiver receiver;

        public Connector(Receiver actionThread) {
            this.receiver = actionThread;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void run() {

            try {
                serverSocket = new ServerSocket(SERVER_PORT).accept();
                output = new PrintWriter(serverSocket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));

                new Thread(receiver).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public abstract class Receiver implements Runnable {

        public abstract void action(); // Define actions of this Thread

        public Receiver(String message) {
            ServerSocketEndPoint.this.message = message;
        }

        @Override
        public void run() {
            ServerSocketEndPoint.this.message = ""; // Reset message
            boolean doneReading = false;

            try {
                while (true) {
                    final String message = input.readLine();
                    Log.e("Client sent to server", "Ready");

                    if (message != null) {
                        ServerSocketEndPoint.this.message += message;
                        action(); // Do defined action
                    } else
                        doneReading = true;
                }

                // input.close(); // Muss irgendwo mit dem wait oder was auch immer kommen
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class Sender implements Runnable {

        public Sender(String message) {
            ServerSocketEndPoint.this.message = message;
        }

        @Override
        public void run() {
            if (output != null) {
                try {
                    output = new PrintWriter(serverSocket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                output.println(message);
                output.flush();
                output.close();
            } else
                Toast.makeText(context, "Nothing to send", Toast.LENGTH_SHORT).show();
        }


    }
}