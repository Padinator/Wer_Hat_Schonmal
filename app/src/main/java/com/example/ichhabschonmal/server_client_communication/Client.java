package com.example.ichhabschonmal.server_client_communication;

import android.annotation.SuppressLint;

import java.io.IOException;

public class Client {

    private Thread receiverThread;
    private Thread senderThread;

    private SocketCommunicator communicator;
    private SocketCommunicator.Receiver receiver;
    private String deviceName = "", IPAddress = "";

    public Client(SocketCommunicator endPoint, SocketCommunicator.Receiver receiverAction) {
        this.communicator = endPoint;
        setReceiver(receiverAction);
    }

    public Client(SocketCommunicator endPoint, SocketCommunicator.Receiver receiverAction, String deviceName, String IPAddress) {
        this.communicator = endPoint;
        setReceiver(receiverAction);
        this.deviceName = deviceName;
        this.IPAddress = IPAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getIPAddress() {
        return IPAddress;
    }

    public SocketCommunicator.Receiver getReceiver() {
        return receiver;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }

    public void setReceiver(SocketCommunicator.Receiver receiverAction) {
        this.receiver = communicator.new Receiver() {

            @Override
            public void action() {
                receiverAction.action();
            }

        };
    }

    public String getMessage() {
        return receiver.getMessage();
    }

    public boolean isConnected() {
        return communicator.isCOnnected();
    }

    @Override
    public String toString() {
        return "Client 1: Device: " + deviceName + ", IP-Address: " + IPAddress;
    }

    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
        try {
            setReceiver(receiverAction);
            receiver.setDoneReading(false);
            receiverThread = new Thread(receiver);
            receiverThread.start();
        } catch (NullPointerException e) {
            throw new NullPointerException("\"Class Client, during receiveMessages(...)\": No Receiver-Action defined: null");
        }
    }

    public void stopReceivingMessages() {
        if (receiver != null && receiverThread != null && receiverThread.getState() != Thread.State.TERMINATED)
            receiver.setDoneReading(true);
        else if (receiver == null)
            throw new NullPointerException("Class Client, during \"stopReceivingMessages(...)\" [for " + this + "]: Cannot terminate Receiver-Thread with \"setDoneReading()\"-method, no Receiver-Action defined: " + null);
        else if (receiverThread == null)
            throw new NullPointerException("Class Client, during \"stopReceivingMessages(...)\" [for " + this + "]: Cannot stop a null-referenced Thread, no Receiver-Thread defined: " + null);
        else
            throw new NullPointerException("Class Client, during \"stopReceivingMessages(...)\" [for " + this + "]: Cannot stop a terminated Receiver-Thread!");
    }

    public void continueReceivingMessages() {
        if (receiver != null && receiverThread != null && receiverThread.getState() == Thread.State.TERMINATED)
            receiveMessages(receiver);
        else if (receiver == null)
            throw new NullPointerException("Class Client, during \"continueReceivingMessages(...)\" [for " + this + "]: Cannot set \"run()\"-method of Receiver-Thread, no Receiver-Action defined: " + null);
        else if (receiverThread == null)
            throw new NullPointerException("Class Client, during \"continueReceivingMessages(...)\" [for " + this + "]: Cannot start a null-referenced Thread, no Receiver-Thread defined: " + null);
        else
            throw new NullPointerException("Class Client, during \"continueReceivingMessages(...)\" [for " + this + "]: Cannot start a running Receiver-Thread!");
    }

    public void sendMessage(String message) {
        if (message != null) {
            senderThread = new Thread(communicator.new Sender(message));
            senderThread.start();
        } else
            throw new NullPointerException("During \"sendMessage(...)\": No message defined: " + null);
    }

    @SuppressLint("LongLogTag")
    public void disconnectClientFromServer() throws IOException {
        // Stop receiving messages
        receiver.setDoneReading(true);

        if (communicator != null && !communicator.isClosed())
            communicator.close(); // "input" and "output" will be closed automatically too

        if (receiverThread != null && receiverThread.getState() != Thread.State.TERMINATED)
            receiverThread.interrupt();

        // Stop sending messages
        if (senderThread != null && senderThread.getState() != Thread.State.TERMINATED)
            senderThread.interrupt();
    }
}
