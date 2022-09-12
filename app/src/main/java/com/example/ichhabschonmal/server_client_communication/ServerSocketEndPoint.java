package com.example.ichhabschonmal.server_client_communication;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

public class ServerSocketEndPoint extends SocketEndPoint {
    private final Activity activity;
    private final Context context;

    private ServerSocket serverSocket;
    public static final int SERVER_PORT = 8080;

    private Thread connectionThread;
    private SocketCommunicator.Receiver receiverAction;

    private LinkedList<Client> clients = new LinkedList<>();
    private int countOfClients;

    /*
     *
     * Creates an end point for a Server-Socket
     *
     */
    public ServerSocketEndPoint(Activity activity, Context context) throws IOException {
        super(activity, context, "");

        this.activity = activity;
        this.context = context;
        this.serverIP = getLocalIpAddress();

        Log.e("Serverip", serverIP);
        serverSocket = new ServerSocket(SERVER_PORT);
        serverSocket.setReuseAddress(true);
    }

    /*
    *
    * Return a list of all clients' messages.
    *
     */
    public ArrayList<String> getClientsMessages() {
        ArrayList<String> messages = new ArrayList<>();

        for (Client client : clients)
            messages.add(client.getMessage());

        return messages;
    }

    /*
    *
    * Returns a client's message.
    *
     */
    public String getClientsMessage(int index) {
        return clients.get(index).getMessage();
    }

    /*
     *
     * Creates connection between host and clients.
     *
     */
    public void createConnection(int countOfClients) {
        createConnection(countOfClients, null);
    }

    /*
     *
     * Creates connection between host and clients and starts receiving messages.
     *
     */
    public void createConnection(int countOfClients, SocketCommunicator.Receiver receiverAction) {
        this.countOfClients = countOfClients;
        this.receiverAction = receiverAction;

        connectionThread = new Thread(new ServerConnector()); // Thread is running until connection is canceled
        connectionThread.start();
    }

    /*
     *
     * Start Receiving messages from all clients.
     *
     */
    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
        try {
            this.receiverAction = receiverAction;

            for (Client client : clients)
                client.receiveMessages(receiverAction);
        } catch (NullPointerException e) {
            throw new NullPointerException("\"During receiveMessages(...)\": No Receiver-Action defined: " + this.receiverAction);
        }
    }

    /*
     *
     * Stops receiving messages from all clients.
     *
     */
    public void stopReceivingMessages() {
        for (Client client : clients)
            client.stopReceivingMessages();

        if (receiverAction != null)
            receiverAction.setDoneReading(true);
        else
            throw new NullPointerException("During \"stopReceivingMessages(...)\": No Receiver-Action defined: " + null);
    }

    /*
     *
     * Continue receiving messages from all clients.
     *
     */
    public void continueReceivingMessages() {
        for (Client client : clients)
            client.continueReceivingMessages();
    }

    /*
     *
     * Send messages to all clients.
     *
     */
    public void sendMessage(String message) {
        for (Client client : clients)
            client.sendMessage(message);
    }

    public void disconnectServerFromClients() throws IOException {
        for (Client client : clients)
            client.disconnectClientFromServer();
    }

    public void disconnectServerSocket() throws IOException {
        // Cancel connection
        if (connectionThread != null && connectionThread.getState() != Thread.State.TERMINATED)
            connectionThread.interrupt();

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed())
            serverSocket.close();
    }

    private class ServerConnector implements Runnable {

        @Override
        public void run() {
            try {
                for (int i = 0; i < countOfClients; i++) {
                    BufferedReader input;
                    PrintWriter output;
                    Socket serverEndPoint;
                    SocketCommunicator socketEndPoint;

                    serverEndPoint = serverSocket.accept(); // Searches for clients always???
                    input = new BufferedReader(new InputStreamReader(serverEndPoint.getInputStream()));
                    output = new PrintWriter(serverEndPoint.getOutputStream());
                    socketEndPoint = new SocketCommunicator(activity, context, serverEndPoint, input, output);
                    clients.add(new Client(socketEndPoint, receiverAction));

                    if (clients.getLast().getReceiver() != null)
                        clients.getLast().receiveMessages(receiverAction);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}