package com.example.ichhabschonmal.server_client_communication;

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
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class ServerSocketEndPoint {
    private final Activity activity;
    private final Context context;

    private ServerSocket serverSocket;
    private Socket serverEndPoint;
    private Socket clientEndPoint;
    private final String SERVER_IP;
    public static final int SERVER_PORT = 8080;

    private LinkedList<Client> clients = new LinkedList<>();
    private String message;

    private Thread connectionThread;
    private ServerSocketEndPoint.Client.Receiver receiverAction;
    private int countOfClients;

    /*
     *
     * Creates an end point for a Server-Socket
     *
     */
    public ServerSocketEndPoint(Activity activity, Context context) throws IOException {
        this.activity = activity;
        this.context = context;
        SERVER_IP = getLocalIpAddress();
        serverSocket = new ServerSocket(SERVER_PORT);
        serverSocket.setReuseAddress(true);
    }

    public String getSERVER_IP() {
        return SERVER_IP;
    }

    public String getMessage() {
        return message;
    }

    /*
     *
     * Returns the local IP-address of the actual device as String.
     *
     */
    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();

        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(ipInt).array()).getHostAddress();
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
    public void createConnection(int countOfClients, ServerSocketEndPoint.Client.Receiver receiverAction) {
        this.receiverAction = receiverAction;
        this.countOfClients = countOfClients;
        connectionThread = new Thread(new Connector());
        connectionThread.start(); // Thread is running until connection is canceled
    }

    /*
    *
    * Start Receiving messages from all clients
    *
     */
    public void receiveMessagesForAllClients(ServerSocketEndPoint.Client.Receiver receiverAction) {
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
    public void stopReceivingMessagesFromAllClients() {
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
    public void continueReceivingMessagesFromAllClients() {
        for (Client client: clients)
            client.continueReceivingMessages();
    }

    /*
     *
     * Send messages to all clients
     *
     */
    public void sendMessageToAllClients(String message) {
        for (Client client : clients) {
            client.sendMessage(message);
        }
    }

    public void disconnectAllClientsFromServer() throws IOException {
        for (Client client : clients) {
            client.disconnectClientFromServer();
            Log.e("Disconnected", client.toString());
        }
    }

    public void disconnectServer() throws IOException {
        // Cancel connection
        if (connectionThread != null && connectionThread.getState() != Thread.State.TERMINATED) {
            connectionThread.interrupt();
        }

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    private class Connector implements Runnable {

        @Override
        public void run() {
            try {
                for (int i = 0; i < countOfClients; i++) {
                    BufferedReader input;
                    PrintWriter output;

                    serverEndPoint = serverSocket.accept();
                    input = new BufferedReader(new InputStreamReader(serverEndPoint.getInputStream()));
                    output = new PrintWriter(serverEndPoint.getOutputStream());
                    clients.add(new Client(serverEndPoint, input, output, receiverAction));

                    if (clients.getLast().getReceiver() != null)
                        clients.getLast().receiveMessages(receiverAction);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public class Client {
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;
        private Receiver receiver;

        private Thread receiverThread;
        private Thread senderThread;

        private String deviceName = "", IPAddress = "";

        public Client() {
            input = null;
            output = null;
            receiverAction = null;
        }

        public Client(Socket serverEndPoint, BufferedReader input, PrintWriter output, Receiver receiverAction) {
            socket = serverEndPoint;
            this.input = input;
            this.output = output;
            setReceiver(receiverAction);
        }

        public Receiver getReceiver() {
            return receiver;
        }

        public void setReceiver(Receiver receiverAction) {
            this.receiver = new Receiver() {
                @Override
                public void action() {
                    receiverAction.action();
                }
            };
        }

        @Override
        public String toString() {
            return "Client " + clients.indexOf(this) + ": Device:" + deviceName + ", IP-Address: " + IPAddress;
        }

        public void receiveMessages(ServerSocketEndPoint.Client.Receiver receiverAction) {
            try {
                setReceiver(receiverAction);
                receiver.setDoneReading(false);
                receiverThread = new Thread(receiver);
                receiverThread.start();
            } catch (NullPointerException e) {
                throw new NullPointerException("\"During receiveMessages(...)\": No Receiver-Action defined: " + receiverAction);
            }
        }

        public void stopReceivingMessages() {
            if (receiver != null && receiverThread != null && receiverThread.getState() != Thread.State.TERMINATED) // interrupted???
                receiver.setDoneReading(true);
            else if (receiver == null)
                throw new NullPointerException("During \"stopReceivingMessages(...)\" [for " + this + "]: Cannot terminate Receiver-Thread with \"setDoneReading()\"-method, no Receiver-Action defined: " + null);
            else if (receiverThread == null)
                throw new NullPointerException("During \"stopReceivingMessages(...)\" [for " + this + "]: Cannot stop a null-referenced Thread, no Receiver-Thread defined: " + null);
            else
                throw new NullPointerException("During \"stopReceivingMessages(...)\" [for " + this + "]: Cannot stop a terminated Receiver-Thread!");
        }

        public void continueReceivingMessages() {
            if (receiver != null && receiverThread != null && receiverThread.getState() == Thread.State.TERMINATED) // interrupted???
                receiveMessages(receiverAction);
            else if (receiver == null)
                throw new NullPointerException("During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot set \"run()\"-method of Receiver-Thread, no Receiver-Action defined: " + null);
            else if (receiverThread == null)
                throw new NullPointerException("During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot start a null-referenced Thread, no Receiver-Thread defined: " + null);
            else
                throw new NullPointerException("During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot start a running Receiver-Thread!");
        }

        public void sendMessage(String message) {
            if (message != null) {
                senderThread = new Thread(new Sender(message));
                senderThread.start();
            } else
                throw new NullPointerException("During \"sendMessage(...)\": No message defined: " + null);
        }

        @SuppressLint("LongLogTag")
        public void disconnectClientFromServer() throws IOException {
            // Stop receiving messages
            receiver.setDoneReading(true);

            if (socket != null && !socket.isClosed())
                socket.close(); // "input" and "output" will be closed too

            if (receiverThread != null && receiverThread.getState() != Thread.State.TERMINATED)
                receiverThread.interrupt();

            // Stop sending messages
            if (senderThread != null && senderThread.getState() != Thread.State.TERMINATED)
                senderThread.interrupt();

            if (!clients.remove(this))
                throw new NoSuchElementException("Could not delete " + this + " from list of clients, because not found.");
        }

        public abstract class Receiver implements Runnable {

            private boolean doneReading = false;

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
                                    Log.e("Server is sending", "Message: " + message);
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
}