package com.example.werhatschonmal.server_client_communication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ServerSocketEndPoint extends SocketEndPoint implements Serializable {
    private final ServerSocket serverSocket;

    private LinkedList<Client> clients = new LinkedList<>();
    private int countOfRequestedClients = 0;
    private final Semaphore semCountOfRequestedClients = new Semaphore(1);

    /**
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

    /**
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

    public Client getAClient(int index) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client found, invalid index: " + index);

        return clients.get(index);
    }

    public LinkedList<Client> getClients() {
        return clients;
    }

    public int getCountOfRequestedClients() {
        int tmp = 0;

        try {
            semCountOfRequestedClients.acquire();
            tmp = countOfRequestedClients;
            semCountOfRequestedClients.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return tmp;
    }

    public void setClients(LinkedList<Client> clients) {
        this.clients = clients;
    }

    public int sizeOfClients() {
        return clients.size();
    }

    /**
     *
     * Creates connection between host and clients.
     *
     */
    public void createConnection(int countOfClients) {
        createConnection(countOfClients, null);
    }

    /**
     *
     * Creates connection between host and clients and starts receiving messages.
     *
     */
    public void createConnection(int countOfRequestedClients, SocketCommunicator.Receiver receiverAction) {

        this.countOfRequestedClients = countOfRequestedClients;
        this.receiverAction = receiverAction;

        try {
            semCountOfRequestedClients.acquire();

            if (connectionThread != null && connectionThread.getState() != Thread.State.TERMINATED) // Extend existing "<Socket>.accept()'s"
                this.countOfRequestedClients += countOfRequestedClients;
            else { // Create new connection with new "<Socket>.accept()"
                connectionThread = new Thread(new ServerConnector()); // Thread is running until connection is canceled
                connectionThread.start();
            }

            semCountOfRequestedClients.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * Start Receiving messages from one client.
     *
     */
    public void receiveMessages(int index, SocketCommunicator.Receiver receiverAction) {
        try {
            this.receiverAction = receiverAction;

            if (index < 0 || index >= clients.size())
                throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client to receive from found, invalid index: " + index);

            if (clients.get(index) != null)
                clients.get(index).receiveMessages(receiverAction);
        } catch (NullPointerException e) {
            throw new NullPointerException("Class ServerSocketEndPoint, during \"receiveMessages(...)\": No Receiver-Action defined: " + this.receiverAction);
        }
    }

    /**
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
            throw new NullPointerException("Class ServerSocketEndPoint, during \"receiveMessages(...)\": No Receiver-Action defined: " + this.receiverAction);
        }
    }

    /**
     *
     * Stops receiving messages from one client.
     *
     */
    public void stopReceivingMessages(int index) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client to receive from found, invalid index: " + index);

        if (clients.get(index) != null)
            clients.get(index).stopReceivingMessages();

        /*
        if (receiverAction != null)
            receiverAction.setDoneReading(true);
        else
            throw new NullPointerException("Class ServerSocketEndPoint, during \"stopReceivingMessages(...)\": No Receiver-Action defined: " + null);
        */
    }

    /**
     *
     * Stops receiving messages from all clients.
     *
     */
    public void stopReceivingMessages() {
        for (Client client : clients)
            if (client != null)
                client.stopReceivingMessages();

        /*
        if (receiverAction != null)
            receiverAction.setDoneReading(true);
        else
            throw new NullPointerException("Class ServerSocketEndPoint, during \"stopReceivingMessages(...)\": No Receiver-Action defined: " + null);
        */
    }

    /**
     *
     * Continue receiving messages from one client.
     *
     */
    public void continueReceivingMessages(int index) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client to receive from found, invalid index: " + index);

        if (clients.get(index) != null)
            clients.get(index).continueReceivingMessages();
    }

    /**
     *
     * Continue receiving messages from all clients.
     *
     */
    public void continueReceivingMessages() {
        for (Client client : clients)
            if (client != null)
                client.continueReceivingMessages();
    }

    public boolean sendMessageToClient(int index, String message) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client to sent found, invalid index: " + index);

        return clients.get(index).sendMessage(message);
    }

    /**
     *
     * Send messages to all clients. Return indices of clients for failed sending.
     *
     */
    public LinkedList<Integer> sendMessage(String message) {
        LinkedList<Integer> responses = new LinkedList<>();

        for (int i = 0; i < clients.size(); i++)
            if (!clients.get(i).sendMessage(message))
                responses.add(i);

        return responses; // Works not on every device!!!
    }

    /**
     *
     * Disconnect the connection from server to client, serverside disconnection.
     *
     */
    @SuppressLint("LongLogTag")
    public void disconnectClientFromServer(int index) throws IOException {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("ServerSocketEndPoint, no client (disconnect with index) found, invalid index: " + index);

        if (clients.get(index) != null)
            clients.get(index).disconnectClientFromServer();

        clients.remove(index);
    }

    /**
     *
     * Disconnect all connections from server to clients, serverside disconnection.
     *
     */
    public void disconnectClientsFromServer() throws IOException {
        for (Client client : clients)
            if (client != null)
                client.disconnectClientFromServer();

        clients.clear();
    }

    @SuppressLint("LongLogTag")
    public void disconnectServerSocket() throws IOException {
        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close(); // All "<Socket>.accept(...)" will be closed automatically with an Exception
            Log.e("Server Socket closed", serverSocket.isClosed() + "");
        }
    }

    private class ServerConnector implements Runnable {

        @Override
        public void run() {
            try {
                for (int i = 0; i < getCountOfRequestedClients(); i++) {
                    BufferedReader input;
                    PrintWriter output;
                    Socket serverEndPoint;

                    Log.e("Test-accept", "1");
                    serverEndPoint = serverSocket.accept(); // Searches for clients always
                    Log.e("Test-accept", "2");
                    input = new BufferedReader(new InputStreamReader(serverEndPoint.getInputStream()));
                    Log.e("Test-accept", "3");
                    output = new PrintWriter(serverEndPoint.getOutputStream());
                    Log.e("Test-accept", "4");
                    clients.add(new Client(activity, context, serverEndPoint, input, output));
                    Log.e("ServerConnector", clients.toString());

                    if (receiverAction != null)
                        clients.get(clients.size() - 1).receiveMessages(receiverAction);
                }
            } catch (SocketException e) {
                Log.e("ServerSocket was closed", "Socket was closed without sing all \"<Socket>.accepts(...)\"'s");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}