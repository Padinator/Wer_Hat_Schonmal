package com.example.ichhabschonmal.server_client_communication;

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
    private Thread connectionThread;
    private SocketCommunicator.Receiver receiverAction;
    Semaphore connection;


    public ClientSocketEndPoint(Activity activity, Context context, String serverIP) {
        super(activity, context, serverIP);

        this.activity = activity;
        this.context = context;
        this.serverIP = serverIP;
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
    public boolean isConnected() {
        if (client != null)
            return client.isConnected();
        else
            throw new NullPointerException("Cannot check connection status of client, when no one was created!");
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
     * Returns, if connection could be created -> automatically synchronization
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

            connection = new Semaphore(0); // Connect client with host
            connection.acquire();

            return client != null && client.isConnected();
        }

        return false;
    }

    /*
     *
     * Start Receiving messages from server.
     *
     */
    public void receiveMessages(SocketCommunicator.Receiver receiverAction) throws NullPointerException {
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
                SocketCommunicator socketCommunicatorToServer;

                Log.e("Server-Port", "Server-IP: " + serverIP + ", Server-Port: " + SERVER_PORT + "");

                clientEndPoint = new Socket(serverIP, SERVER_PORT);
                //clientEndPoint = new Socket("192.168.1.38", 8080);
                Log.e("Server-Port", "1");
                input = new BufferedReader(new InputStreamReader(clientEndPoint.getInputStream()));
                Log.e("Server-Port", "2");
                output = new PrintWriter(clientEndPoint.getOutputStream());
                Log.e("Server-Port", "3");
                socketCommunicatorToServer = new SocketCommunicator(activity, context, clientEndPoint, input, output);
                Log.e("Server-Port", "4");
                client = new Client(socketCommunicatorToServer, receiverAction, getNameOfDevice(), getLocalIpAddress());
                Log.e("Client", client.toString());

                if (client.getReceiver() != null)
                    client.receiveMessages(receiverAction);
            } catch (NoRouteToHostException | ConnectException e) {
                Log.e("NoRouteToHostException", "No Host is waiting for a connection!");
                activity.runOnUiThread(() -> Toast.makeText(context, "No Host is waiting for a connection!", Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                connection.release();
            }
        }
    }
}