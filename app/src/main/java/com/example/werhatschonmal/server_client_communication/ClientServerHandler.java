package com.example.werhatschonmal.server_client_communication;

import android.annotation.SuppressLint;

/**
 *
 * @author Patrick<br>
 *
 * Class for server and client communication. Package can be extracted and used separatly.
 * You can get and set the server and/or client end points with getter and setter.
 */
public class ClientServerHandler {

    /**
     *
     * <strong>Data field for the end point (serverside).</strong><br>
     * Default value: null
     */
    @SuppressLint("StaticFieldLeak")
    private static ServerSocketEndPoint serverEndPoint;

    /**
     *
     * <strong>Data field for the end point (clientside).</strong><br>
     * Default value: null
     */
    @SuppressLint("StaticFieldLeak")
    private static ClientSocketEndPoint clientEndPoint;

    /**
     *
     * @return Return the private client end point variable;
     */
    public static ClientSocketEndPoint getClientEndPoint() {
        return clientEndPoint;
    }

    /**
     *
     * @param clientEndPoint Set the client end point.
     */
    public static void setClientEndPoint(ClientSocketEndPoint clientEndPoint) {
        ClientServerHandler.clientEndPoint = clientEndPoint;
    }

    /**
     *
     * @return Return the private server end point variable.
     */
    public static ServerSocketEndPoint getServerEndPoint() {
        return serverEndPoint;
    }

    /**
     *
     * @param serverEndPoint Set the server end point.
     */
    public static void setServerEndPoint(ServerSocketEndPoint serverEndPoint) {
        ClientServerHandler.serverEndPoint = serverEndPoint;
    }
}
