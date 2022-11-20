package de.android.werhatschonmal.server_client_communication;

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

/**
 *
 * <string>
 *     Class connecting, receiving messages and sending messages as client to a serevr.<br>
 * </string>
 */
public class ClientSocketEndPoint extends SocketEndPoint {

    /**
     *
     * Data element "Client client" contains the clients information and SocketCommunicator-skills.
     */
    private Client client;

    /**
     *
     * Semaphore for returning, if connection was created or not (not, if connection is available).
     */
    private final Semaphore semConnection = new Semaphore(0); // Connect client with host

    // Constants
    /**
     *
     * Constant, if client is connected.
     */
    public static final String STATUS_CONNECTED = "Warten auf Host";

    /**
     *
     * Constant, if client is not connected.
     */
    public static final String STATUS_NOT_CONNECTED = "Nicht verbunden";

    /**
     *
     * @param activity Pass actual activity for running messages on UI-Thread.
     * @param context Pass actual context for making and showing Toasts.
     * @param serverIP Pass the IP-address of the server (to connect as client)
     */
    public ClientSocketEndPoint(Activity activity, Context context, String serverIP) {
        super(activity, context, serverIP);

        this.activity = activity;
        this.context = context;
        this.serverIP = serverIP;
    }

    /**
     *
     * @return Return the actual data element client.
     */
    public Client getClient() {
        return client;
    }

    /**
     *
     * @param client Set the actual data element client.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     *
     * @return Returns the client's connection status.
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

    /**
     *
     * <strong>Creates connection between host and client.</strong><br>
     *
     * @return Returns, if connection could be created (not, if connection is available)<br>
     *         -> automatically synchronization with return value.
     * @throws InterruptedException If semaphore for waiting for creating connection cannot failed
     *                              during "~Semaphore~.acquire()".
     */
    public boolean createConnection() throws InterruptedException {
        return createConnection(null);
    }

    /**
     *
     * <strong>
     *     Creates connection between host and client and starts receiving messages.<br>
     * </strong>
     *
     * @param receiverAction Pass a receiverAction for defining an action, when receiving a message.
     * @return Returns, if connection could be created (not, if connection is available)<br>
     *         -> automatically synchronization with return value.
     * @throws InterruptedException If semaphore for waiting for creating connection cannot failed
     *                              during "~Semaphore~.acquire()".
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

    /**
     *
     * <strong>Start Receiving messages from server.</strong><br>
     *
     * @param receiverAction Pass receiver action to receive messages from server
     */
    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
        if (client != null)
            client.receiveMessages(receiverAction);
        else
            throw new NullPointerException("\"Class ClientSocketEndPoint, during receiveMessages(...)\": No client defined: null");
    }

    /**
     *
     * <strong>Stops receiving messages from server.</strong><br>
     */
    public void stopReceivingMessages() {
        client.stopReceivingMessages();

        if (receiverAction != null)
            receiverAction.setDoneReading(true);
        else
            throw new NullPointerException("Class ClientSocketEndPoint, during \"stopReceivingMessages(...)\": No Receiver-Action defined: null");
    }

    /**
     *
     * <strong>Continue receiving messages from server.</strong><br>
     */
    public void continueReceivingMessages() {
        if (client != null)
            client.continueReceivingMessages();
        else
            throw new NullPointerException("Class ClientSocketEndPoint, during \"continueReceivingMessages(...)\": No client defined: null");
    }

    /**
     *
     * <strong>Send messages to the server.</strong><br>
     *
     * @param message Pass a message to send to the server.
     * @return Return, whether message was send successfully.
     */
    public boolean sendMessage(String message) {
        if (client != null)
            return client.sendMessage(message);
        else
            throw new NullPointerException("Class ClientSocketEndPoint, during \"sendMessage(...)\": No client defined: null");
    }

    /**
     *
     * <strong>Disconnect clients end point from server</strong><br>
     *
     * @throws IOException From "~Client~.disconnectClientFromServer()"
     * @throws NullPointerException If no client is defined to disconnect it (clientside).
     */
    public void disconnectClient() throws IOException, NullPointerException {
        if (client != null)
            client.disconnectClientFromServer();
        else
            throw new NullPointerException("Class ClientSocketEndPoint, during \"disconnectClient(...)\": No client defined: null");
    }

    /**
     *
     * @author Patrick
     *
     * <strong>Class for connectiing to server as a client.</strong>
     * Class is derived from Runnable, use in "~Thread~.start(~ClientConnector~)".
     */
    private class ClientConnector implements Runnable {

        /**
         *
         * Redefine run method
         */
        @Override
        public void run() {
            try {
                BufferedReader input;
                PrintWriter output;
                Socket clientEndPoint;

                Log.e("Client sees Server", "Server-IP: " + serverIP + ", Server-Port: " + SERVER_PORT + "");

                clientEndPoint = new Socket(serverIP, SERVER_PORT);
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