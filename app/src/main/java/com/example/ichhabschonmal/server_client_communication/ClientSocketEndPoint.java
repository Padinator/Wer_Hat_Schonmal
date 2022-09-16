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
    * Returns client ip address
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
        this.receiverAction = receiverAction;

        connectionThread = new Thread(new ClientConnector());
        connectionThread.start();

        connection = new Semaphore(0); // Connect client with host
        connection.acquire();

        return client != null && client.isConnected();
    }

    /*
     *
     * Start Receiving messages from server.
     *
     */
    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
        try {
            this.receiverAction = receiverAction;

            client.receiveMessages(receiverAction);
        } catch (NullPointerException e) {
            throw new NullPointerException("\"During receiveMessages(...)\": No Receiver-Action defined: " + this.receiverAction);
        }
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
            throw new NullPointerException("During \"stopReceivingMessages(...)\": No Receiver-Action defined: " + null);
    }

    /*
     *
     * Continue receiving messages from server.
     *
     */
    public void continueReceivingMessages() {
        client.continueReceivingMessages();
    }

    /*
     *
     * Send messages to the server.
     *
     */
    public void sendMessage(String message) {
        client.sendMessage(message);
    }

    public void disconnectClient() throws IOException {
        client.disconnectClientFromServer();
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
                client = new Client(socketCommunicatorToServer, receiverAction, getNameOfDevice(),getLocalIpAddress());
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