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

    public SocketCommunicator.Receiver getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return receiver.getMessage();
    }

    public void setReceiver(SocketCommunicator.Receiver receiverAction) {
        this.receiver = communicator.new Receiver() {

            @Override
            public void action() {
                receiverAction.action();
            }

        };
    }

    @Override
    public String toString() { /////////////////////////////////////
        return "Client 1: Device:" + deviceName + ", IP-Address: " + IPAddress;
    }

    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
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
            receiveMessages(receiver);
        else if (receiver == null)
            throw new NullPointerException("During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot set \"run()\"-method of Receiver-Thread, no Receiver-Action defined: " + null);
        else if (receiverThread == null)
            throw new NullPointerException("During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot start a null-referenced Thread, no Receiver-Thread defined: " + null);
        else
            throw new NullPointerException("During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot start a running Receiver-Thread!");
    }

    public void sendMessage(String message) {
        if (message != null) {
            senderThread = new Thread(communicator.new Sender(message));
            senderThread.start();
        } else
            throw new NullPointerException("During \"sendMessage(...)\": No message defined: " + null);
    }

    @SuppressLint("LongLogTag")
    public void disconnectClientFromServer() throws IOException { //////////////////////
        // Stop receiving messages
        receiver.setDoneReading(true);

        if (communicator != null && !communicator.isClosed())
            communicator.close(); // "input" and "output" will be closed too

        if (receiverThread != null && receiverThread.getState() != Thread.State.TERMINATED)
            receiverThread.interrupt();

        // Stop sending messages
        if (senderThread != null && senderThread.getState() != Thread.State.TERMINATED)
            senderThread.interrupt();
    }
}
