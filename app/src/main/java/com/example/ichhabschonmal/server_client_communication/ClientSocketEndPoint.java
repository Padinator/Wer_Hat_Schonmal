package com.example.ichhabschonmal.server_client_communication;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocketEndPoint extends SocketEndPoint {
    private final Activity activity;
    private final Context context;

    public static final int SERVER_PORT = 8080;

    private Client client;

    private Thread connectionThread;
    private SocketCommunicator.Receiver receiverAction;


    public ClientSocketEndPoint(Activity activity, Context context, String serverIP) throws IOException {
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
        return client.getMessage();
    }

    /*
     *
     * Creates connection between host and client.
     *
     */
    public void createConnection() {
        createConnection(null);
    }

    /*
     *
     * Creates connection between host and client and starts receiving messages.
     *
     */
    public void createConnection(SocketCommunicator.Receiver receiverAction) {
        this.receiverAction = receiverAction;

        connectionThread = new Thread(new ClientConnector());
        connectionThread.start();
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
                SocketCommunicator socketEndPoint;

                clientEndPoint = new Socket(serverIP, SERVER_PORT);
                input = new BufferedReader(new InputStreamReader(clientEndPoint.getInputStream()));
                output = new PrintWriter(clientEndPoint.getOutputStream());
                socketEndPoint = new SocketCommunicator(activity, context, clientEndPoint, input, output);
                client = new Client(socketEndPoint, receiverAction);

                if (client.getReceiver() != null)
                    client.receiveMessages(receiverAction);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}