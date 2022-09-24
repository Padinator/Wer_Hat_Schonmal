package com.example.ichhabschonmal.server_client_communication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ClientSocketEndPoint extends SocketEndPoint {
    private final Activity activity;
    private final Context context;

    private Client client;
    private final Semaphore semConnection = new Semaphore(0); // Connect client with host

    public static final String STATUS_CONNECTED = "Warten auf Host";
    public static final String STATUS_NOT_CONNECTED = "Nicht verbunden";


    public ClientSocketEndPoint(Activity activity, Context context, String serverIP) {
        super(activity, context, serverIP);

        this.activity = activity;
        this.context = context;
        this.serverIP = serverIP;
    }


    public void setServerIP(String serverIP) {
        this.serverIP = serverIP;
    }

    /*
     *
     * Returns the client's ip address.
     *
     */
    public String getClientIpAddress() {
        return client.getIPAddress();
    }

    /*
     *
     * Returns the message, the client has received.
     *
     */
    public String getClientsMessage() {
        if (client != null)
            return client.getMessage();
        else
            throw new NullPointerException("Cannot read message from client, no client was created!");
    }

    /*
     *
     * Returns the client's connection status.
     *
     */
    @SuppressLint("LongLogTag")
    public boolean isConnected() { // Undecided about exception handling
        /*
        if (client != null)
            return client.isConnected();
        else
            throw new NullPointerException("Cannot check connection status of client, when no one was created!");
         */

        if (client != null)
            return client.isConnected();

        Log.e("ClientSocketEndPoint", "Cannot check connection status of client, when no one was created");
        return false;
    }

    /*
     *
     * Creates connection between host and client.
     *
     */
    public boolean createConnection() throws InterruptedException {
        return createConnection(null);
    }

    /*
     *
     * Creates connection between host and client and starts receiving messages.
     * Returns, if connection could be created -> automatically synchronization with return value
     *
     */
    public boolean createConnection(SocketCommunicator.Receiver receiverAction) throws InterruptedException {
        boolean isConnected = false;

        if (client != null)
            isConnected = isConnected();

        if (!isConnected) {
            this.receiverAction = receiverAction;

            connectionThread = new Thread(new ClientConnector());
            connectionThread.start();
            semConnection.acquire();
        }

        return !isConnected;
    }

    /*
     *
     * Start Receiving messages from server.
     *
     */
    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
        if (client != null)
            client.receiveMessages(receiverAction);
        else
            throw new NullPointerException("\"Class ClientSocketEndPoint, during receiveMessages(...)\": No client defined: null");
    }

    /*
     *
     * Stops receiving messages from server.
     *
     */
    public void stopReceivingMessages() {
        client.stopReceivingMessages();

        if (receiverAction != null)
            receiverAction.setDoneReading(true);
        else
            throw new NullPointerException("Class ClientSocketEndPoint, during \"stopReceivingMessages(...)\": No Receiver-Action defined: null");
    }

    /*
     *
     * Continue receiving messages from server.
     *
     */
    public void continueReceivingMessages() {
        if (client != null)
            client.continueReceivingMessages();
        else
            throw new NullPointerException("Class ClientSocketEndPoint, during \"continueReceivingMessages(...)\": No client defined: null");
    }

    /*
     *
     * Send messages to the server.
     *
     */
    public void sendMessage(String message) {
        if (client != null)
            client.sendMessage(message);
        else
            throw new NullPointerException("Class ClientSocketEndPoint, during \"sendMessage(...)\": No client defined: null");
    }

    public void disconnectClient() throws IOException {
        if (client != null)
            client.disconnectClientFromServer();
        else
            throw new NullPointerException("Class ClientSocketEndPoint, during \"disconnectClient(...)\": No client defined: null");
    }

    private class ClientConnector implements Runnable {

        @Override
        public void run() {
            try {
                BufferedReader input;
                PrintWriter output;
                Socket clientEndPoint;

                Log.e("Client sees Server", "Server-IP: " + serverIP + ", Server-Port: " + SERVER_PORT + "");

                clientEndPoint = new Socket(serverIP, SERVER_PORT);
                //clientEndPoint = new Socket("192.168.1.38", 8080);
                input = new BufferedReader(new InputStreamReader(clientEndPoint.getInputStream()));
                output = new PrintWriter(clientEndPoint.getOutputStream());
                client = new Client(activity, context, clientEndPoint, input, output, getNameOfDevice(), getLocalIpAddress());
                Log.e("ClientConnector", client.toString());

                if (receiverAction != null)
                    client.receiveMessages(receiverAction);
            } catch (NoRouteToHostException e) {
                Log.e("NoRouteToHostException", "No Host is waiting for a connection!");
                activity.runOnUiThread(() -> Toast.makeText(context, "Kein Host wartet auf andere Spieler!", Toast.LENGTH_SHORT).show());
            } catch (ConnectException e) {
                Log.e("ConnectException", "An error occurred, while attempting to connect a socket to a remote address and port!");
                activity.runOnUiThread(() -> Toast.makeText(context, "Verbindung zum Host fehlgeschlagen!", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                semConnection.release();
            }
        }
    }
}