package com.example.ichhabschonmal.server_client_communication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ServerSocketEndPoint extends SocketEndPoint {
    private final Activity activity;
    private final Context context;

    private final ServerSocket serverSocket;

    private final LinkedList<Client> clients = new LinkedList<>();
    private int countOfRequestedClients = 0;
    private final Semaphore semCountOfRequestedClients = new Semaphore(1);

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


    public String getClientsIPAddress(int index) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client (get IP) found, invalid index: " + index);

        return clients.get(index).getIPAddress();
    }

    public int getClientsPlayerNumber(int index) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client (get Player-Number) found, invalid index: " + index);

        return clients.get(index).getPlayerNumber();
    }

    public String getClientsDeviceName(int index) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client (get Device) found, invalid index: " + index);

        return clients.get(index).getDeviceName();
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

    public void setClientsDeviceName(int index, String deviceName) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client (set Device) found, invalid index: " + index);

        clients.get(index).setDeviceName(deviceName);
    }

    public void setClientsIPAddress(int index, String IPAddress) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client (set IP) found, invalid index: " + index);

        clients.get(index).setIPAddress(IPAddress);
    }

    public void setClientsPlayerNumber(int index, int playerNumber) {
        if (index < 0 || index >= clients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client (set Player-Number) found, invalid index: " + index);

        clients.get(index).setPlayerNumber(playerNumber);
    }

    public int sizeOfClients() {
        return clients.size();
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
    public void createConnection(int countOfRequestedClients, SocketCommunicator.Receiver receiverAction) {

        this.countOfRequestedClients = countOfRequestedClients;
        this.receiverAction = receiverAction;

        try {
            semCountOfRequestedClients.acquire();

            if (connectionThread != null && connectionThread.getState() != Thread.State.TERMINATED) {// Extend existing "<Socket>.accept()'s"
                this.countOfRequestedClients += countOfRequestedClients;
                Log.e("Test123", "1");
            }else {  // Create new connection with new "<Socket>.accept()"
                Log.e("Test123", "12");
                connectionThread = new Thread(new ServerConnector()); // Thread is running until connection is canceled
                connectionThread.start();
            }

            semCountOfRequestedClients.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
            throw new NullPointerException("Class ServerSocketEndPoint, during \"receiveMessages(...)\": No Receiver-Action defined: " + this.receiverAction);
        }
    }

    /*
     *
     * Stops receiving messages from all clients.
     *
     */
    public void stopReceivingMessages() {
        for (Client client : clients)
            if (client != null)
                client.stopReceivingMessages();

        if (receiverAction != null)
            receiverAction.setDoneReading(true);
        else
            throw new NullPointerException("Class ServerSocketEndPoint, during \"stopReceivingMessages(...)\": No Receiver-Action defined: " + null);
    }

    /*
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

    /*
     *
     * Send messages to all clients. Return indices of clients for failed sending.
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public LinkedList<Integer> sendMessage(String message) {
        LinkedList<Integer> responses = new LinkedList<>();

        for (int i = 0; i < clients.size(); i++)
            if (!clients.get(i).sendMessage(message))
                responses.add(i);

        return responses; // Works not on every device!!!
    }

    /*
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

    /*
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
                for (int i = 0; i < getCountOfRequestedClients(); i++) {
                    BufferedReader input;
                    PrintWriter output;
                    Socket serverEndPoint;

                    serverEndPoint = serverSocket.accept(); // Searches for clients always
                    input = new BufferedReader(new InputStreamReader(serverEndPoint.getInputStream()));
                    output = new PrintWriter(serverEndPoint.getOutputStream());
                    clients.add(new Client(activity, context, serverEndPoint, input, output));
                    Log.e("ServerConnector", clients.toString());

                    if (receiverAction != null)
                        clients.getLast().receiveMessages(receiverAction);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}