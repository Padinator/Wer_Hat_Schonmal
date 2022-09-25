package com.example.ichhabschonmal.server_client_communication;

import android.annotation.SuppressLint;

public class ClientServerHandler {

    @SuppressLint("StaticFieldLeak")
    private static ServerSocketEndPoint serverEndPoint;
    @SuppressLint("StaticFieldLeak")
    private static ClientSocketEndPoint clientEndPoint;

    public static ClientSocketEndPoint getClientEndPoint() {
        return clientEndPoint;
    }

    public static void setClientEndPoint(ClientSocketEndPoint clientEndPoint) {
        ClientServerHandler.clientEndPoint = clientEndPoint;
    }

    public static ServerSocketEndPoint getServerEndPoint() {
        return serverEndPoint;
    }

    public static void setServerEndPoint(ServerSocketEndPoint serverEndPoint) {
        ClientServerHandler.serverEndPoint = serverEndPoint;
    }
}
