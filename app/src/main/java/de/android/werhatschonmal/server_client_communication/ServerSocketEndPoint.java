package de.android.werhatschonmal.server_client_communication;

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

/**
 *
 * <string>
 *     Class connecting, receiving messages and sending messages client(s) as a serevr.<br>
 * </string>
 */
public class ServerSocketEndPoint extends SocketEndPoint implements Serializable {

    /**
     * Data field serverSocket represents the Socket (serverside) as ServerSocket.
     */
    private final ServerSocket serverSocket;

    /**
     *
     * Data field listOfClients contains all clients that the host knows and has
     * "~ServerSocket~.accept()"-ed.
     */
    private LinkedList<Client> listOfClients = new LinkedList<>();

    /**
     *
     * Data field contains the count of requested clients to connect with host.
     */
    private int countOfRequestedClients = 0;

    /**
     *
     * Semaphore "semCountOfRequestedClients" synchronizes the variable
     * "countOfRequestedClients".<br>
     * That is necessary in HostOnlineGame: All clients are/are not connected and some are
     * removed. <br>
     * -> Solution: raise synchronized the variable "countOfRequestedClients"<br>
     */
    private final Semaphore semCountOfRequestedClients = new Semaphore(1);

    /**
     *
     * <string>Creates an end point for a Server-Socket.</string><br>
     *
     * @param activity Pass actual activity for running messages on UI-Thread.
     * @param context Pass actual context for making and showing Toasts.
     * @throws IOException if getting IP-address of the host or creating a server socket fails.
     */
    public ServerSocketEndPoint(Activity activity, Context context) throws IOException {
        super(activity, context, "");

        this.activity = activity;
        this.context = context;
        this.serverIP = getLocalIpAddress();
        serverSocket = new ServerSocket(SERVER_PORT);
        serverSocket.setReuseAddress(true);
    }

    /**
     *
     * @return Return a list of all clients' messages.
     */
    public ArrayList<String> getClientsMessages() {
        ArrayList<String> messages = new ArrayList<>();

        for (Client client : listOfClients)
            messages.add(client.getMessage());

        return messages;
    }

    /**
     *
     * @param index Pass an index of a client to get from data element listOfClients.
     * @return Return client from listOfClients with given index.
     */
    public Client getAClient(int index) {
        if (index < 0 || index >= listOfClients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client found, invalid index: " + index);

        return listOfClients.get(index);
    }

    /**
     *
     * @return Return data element listOfClients.
     */
    public LinkedList<Client> getListOfClients() {
        return listOfClients;
    }

    /**
     *
     * @return Return data element countOfRequestedClients.
     */
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

    /**
     *
     * @param listOfClients Pass a list of Clients to set data element listOfClients.
     */
    public void setListOfClients(LinkedList<Client> listOfClients) {
        this.listOfClients = listOfClients;
    }

    /**
     *
     * @return Return size of the list listOfClients.
     */
    public int sizeOfClients() {
        return listOfClients.size();
    }

    /**
     *
     * <strong>Creates connection between host and clients.<br></strong>
     *
     * @param countOfRequestedClients Pass the count of requested clients to accept only a limit
     *                                number of clients.
     */
    public void createConnection(int countOfRequestedClients) {
        createConnection(countOfRequestedClients, null);
    }

    /**
     *
     * <strong>
     *     Creates connection between host and clients and starts receiving messages.<br>
     * </strong>
     *
     * @param countOfRequestedClients Pass the count of requested clients to accept only a limit
     *                                number of clients.
     * @param receiverAction Pass a receiverAction for defining an action, when receiving a message.
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
     * <strong>tart Receiving messages from one client in listOfClients.<br></strong>
     *
     * @param index Pass index of a client in listOfClients to receive messages from.
     * @param receiverAction Pass a receiverAction for defining an action when receiving a message.
     */
    public void receiveMessages(int index, SocketCommunicator.Receiver receiverAction) {
        try {
            this.receiverAction = receiverAction;

            if (index < 0 || index >= listOfClients.size())
                throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client to receive from found, invalid index: " + index);

            if (listOfClients.get(index) != null)
                listOfClients.get(index).receiveMessages(receiverAction);
        } catch (NullPointerException e) {
            throw new NullPointerException("Class ServerSocketEndPoint, during \"receiveMessages(...)\": No Receiver-Action defined: " + this.receiverAction);
        }
    }

    /**
     *
     * <strong>Start Receiving messages from all clients with a new receiverAction.<br></strong>
     *
     * @param receiverAction Pass a receiverAction for defining an action when receiving a message.
     */
    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
        try {
            this.receiverAction = receiverAction;

            for (Client client : listOfClients)
                client.receiveMessages(receiverAction);
        } catch (NullPointerException e) {
            throw new NullPointerException("Class ServerSocketEndPoint, during \"receiveMessages(...)\": No Receiver-Action defined: " + this.receiverAction);
        }
    }

    /**
     *
     * <strong>Stops receiving messages from one client.<br></strong>
     *
     * @param index Pass index of a client in listOfClients to stop receiving messages from.
     */
    public void stopReceivingMessages(int index) {
        if (index < 0 || index >= listOfClients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client to receive from found, invalid index: " + index);

        if (listOfClients.get(index) != null)
            listOfClients.get(index).stopReceivingMessages();

        /*
        if (receiverAction != null)
            receiverAction.setDoneReading(true);
        else
            throw new NullPointerException("Class ServerSocketEndPoint, during \"stopReceivingMessages(...)\": No Receiver-Action defined: " + null);
        */
    }

    /**
     *
     * <strong>Stops receiving messages from all clients.<br></strong>
     */
    public void stopReceivingMessages() {
        for (Client client : listOfClients)
            if (client != null)
                client.stopReceivingMessages();
    }

    /**
     * <strong>Continue receiving messages from one client.<br></strong>
     *
     * @param index Pass index of a client in listOfClients to continue receiving messages from.
     */
    public void continueReceivingMessages(int index) {
        if (index < 0 || index >= listOfClients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client to receive from found, invalid index: " + index);

        if (listOfClients.get(index) != null)
            listOfClients.get(index).continueReceivingMessages();
    }

    /**
     *
     * <strong>Continue receiving messages from all clients.</strong><br>
     */
    public void continueReceivingMessages() {
        for (Client client : listOfClients)
            if (client != null)
                client.continueReceivingMessages();
    }

    /**
     *
     * <strong>Send messages to a client.</strong><br>
     *
     * @param index Pass index of a client in listOfClients to send a message to it.
     * @param message Pass message to send to the client.
     * @return Return, if sending was successful.
     */
    public boolean sendMessageToClient(int index, String message) {
        if (index < 0 || index >= listOfClients.size())
            throw new IndexOutOfBoundsException("Class ServerSocketEndPoint, no client to sent found, invalid index: " + index);

        return listOfClients.get(index).sendMessage(message);
    }

    /**
     *
     * <strong>
     *     Send messages to all clients. Return indices of clients for failed sending.<br>
     * </strong>
     *
     * @param message Pass message for sending it to all listed clients.
     * @return Return a list of clients to that sending failed.
     */
    public LinkedList<Integer> sendMessage(String message) {
        LinkedList<Integer> responses = new LinkedList<>();

        for (int i = 0; i < listOfClients.size(); i++)
            if (!listOfClients.get(i).sendMessage(message))
                responses.add(i);

        return responses;
    }

    /**
     *
     * Disconnect the connection from server to client, serverside disconnection.
     *
     * @param index Pass index of a client in data element listOfClients to disconnect it
     *              (serverside).
     * @throws IOException From "~Client~.disconnectClientFromServer.()"
     */
    @SuppressLint("LongLogTag")
    public void disconnectClientFromServer(int index) throws IOException {
        if (index < 0 || index >= listOfClients.size())
            throw new IndexOutOfBoundsException("ServerSocketEndPoint, no client (disconnect with index) found, invalid index: " + index);

        if (listOfClients.get(index) != null)
            listOfClients.get(index).disconnectClientFromServer();

        listOfClients.remove(index);
    }

    /**
     *
     * <strong>
     *     Disconnect all connections from server to clients, serverside disconnection.<br>
     * </strong>
     *
     * @throws IOException From "~Client~.disconnectClientFromServer.()"
     */
    public void disconnectClientsFromServer() throws IOException {
        for (Client client : listOfClients)
            if (client != null)
                client.disconnectClientFromServer();

        listOfClients.clear();
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
                    listOfClients.add(new Client(activity, context, serverEndPoint, input, output));
                    Log.e("ServerConnector", listOfClients.toString());

                    if (receiverAction != null)
                        listOfClients.get(listOfClients.size() - 1).receiveMessages(receiverAction);
                }
            } catch (SocketException e) {
                Log.e("ServerSocket was closed", "Socket was closed without sing all \"<Socket>.accepts(...)\"'s");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}