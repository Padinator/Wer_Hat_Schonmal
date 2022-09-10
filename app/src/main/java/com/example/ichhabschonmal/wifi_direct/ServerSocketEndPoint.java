package com.example.ichhabschonmal.wifi_direct;

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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ServerSocketEndPoint {
    private final Activity activity;
    private final Context context;

    private ServerSocket serverSocket;
    private Socket serverEndPoint;
    private Socket clientEndPoint;
    private final String SERVER_IP;
    public static final int SERVER_PORT = 8080;

    private PrintWriter output;
    private BufferedReader input;
    private String message;

    private Thread connectionThread;
    private Thread receiverThread;
    private Thread senderThread;
    private Receiver receiverAction;

    public ServerSocketEndPoint(Activity activity, Context context) throws IOException {
        this.activity = activity;
        this.context = context;
        SERVER_IP = getLocalIpAddress();
        serverSocket = new ServerSocket(SERVER_PORT);
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

    public void startConnection(int countOfClients) {
        startConnection(countOfClients, null);
    }

    public void startConnection(int countOfClients, Receiver receiverAction) {
        this.receiverAction = receiverAction;

        for (int i = 0; i < countOfClients; i++) {
            connectionThread = new Thread(new Connector(receiverAction));
            connectionThread.start();
        }
    }

    public void receiveMessages(Receiver receiverAction) {
        try {
            this.receiverAction = receiverAction;
            this.receiverAction.setDoneReading(false);
            receiverThread = new Thread(this.receiverAction);
            receiverThread.start();
        } catch (NullPointerException e) {
            throw new NullPointerException("\"During receiveMessages(...)\": No Receiver-Action defined: " + this.receiverAction);
        }
    }

    public void stopReceivingMessages() {
        if (receiverAction != null)
            receiverAction.setDoneReading(true);
        else
            throw new NullPointerException("During \"stopReceivingMessages(...)\": No Receiver-Action defined: " + null);
    }

    public void continueReceivingMessages() {
        //Log.e("ReceiverTrhead: ", receiverThread.getState().toString());
        if (receiverThread == null || receiverThread.isInterrupted()) {
            receiveMessages(receiverAction);
            receiverAction.setDoneReading(false);
            Log.e("Continue receiving", "continue receiving");
        }
    }

    public void sendMessage(String message) {
        if (message != null) {
            senderThread = new Thread(new Sender(message));
            senderThread.start();
        } else
            throw new NullPointerException("During \"sendMessage(...)\": No message defined: " + null);
    }

    public void disconnectSocket() throws IOException {
        // Cancel connection
        if (connectionThread != null && connectionThread.getState() != Thread.State.TERMINATED)
            connectionThread.interrupt();

        // Stop receiving messages
        receiverAction.setDoneReading(true);

        if (serverSocket != null && !serverSocket.isClosed())
            serverSocket.close();

        if (serverEndPoint != null && !serverEndPoint.isClosed())
            serverEndPoint.close();
        //serverEndPoint.bind(new InetSocketAddress(9999));

        if (receiverThread != null && receiverThread.getState() != Thread.State.TERMINATED)
            receiverThread.interrupt();

        input.close();
        output.close();

        // Stop sending messages
        if (senderThread != null && senderThread.getState() != Thread.State.TERMINATED)
            senderThread.interrupt();

        receiverAction = null;
    }

    private class Connector implements Runnable {

        public Connector(Receiver actionThread) {
            ServerSocketEndPoint.this.receiverAction = actionThread;
        }

        @Override
        public void run() {
            try {
                //serverSocket.setReuseAddress(true);
                serverEndPoint = serverSocket.accept();
                //serverEndPoint.setReuseAddress(true);
                output = new PrintWriter(serverEndPoint.getOutputStream());
                input = new BufferedReader(new InputStreamReader(serverEndPoint.getInputStream()));

                if (receiverAction != null)
                    new Thread(receiverAction).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public abstract class Receiver implements Runnable {

        private boolean doneReading = false;

        public Receiver(String message) {
            ServerSocketEndPoint.this.message = message;
        }

        private void setDoneReading(boolean doneReading) {
            this.doneReading = doneReading;
        }

        public abstract void action(); // Define actions of this Thread

        @Override
        public void run() {
            if (serverEndPoint != null && !serverEndPoint.isClosed()) {
                ServerSocketEndPoint.this.message = ""; // Reset message

                try {
                    while (!doneReading) {
                        if (input != null && input.ready()) {
                            final String message = input.readLine();
                            Log.e("Client sent to server", "Ready");

                            if (message != null) {
                                ServerSocketEndPoint.this.message = message;
                                action(); // Do defined action
                            }
                        }
                    }
                    Log.e("Receiving stopped", "stopped or failed reading1");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                activity.runOnUiThread(() -> Toast.makeText(context, "Cannot receive: No connection to end point", Toast.LENGTH_SHORT).show());
        }

    }

    private class Sender implements Runnable {

        public Sender(String message) {
            ServerSocketEndPoint.this.message = message;
        }

        @Override
        public void run() {
            if (output != null && !serverEndPoint.isClosed()) {
                output.println(message);
                output.flush();
            } else
                activity.runOnUiThread(() -> Toast.makeText(context, "Cannot send: No connection to end point, server end point is closed: " + serverEndPoint.isClosed(), Toast.LENGTH_SHORT).show());
        }


    }
}