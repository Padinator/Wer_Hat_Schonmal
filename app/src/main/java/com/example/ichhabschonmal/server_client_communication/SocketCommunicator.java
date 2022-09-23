package com.example.ichhabschonmal.server_client_communication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class SocketCommunicator {

    private final Activity activity;
    private final Context context;

    private final Socket endPoint;
    private final BufferedReader input;
    private final PrintWriter output;
    private String message = "";

    private Thread receiverThread;
    // No sync necessary: SocketCommunicator/Client is used exactly 1 time in the host's LinkedList
    // Variable "receiverThread" is only used locally and executed by Main-Thread -> no sync necessary
    //      -> Unequal to "receiverAction": can be modified ("setReceiverAction()") and used ("action()")
    private Thread senderThread;
    private Receiver receiverAction;
    private final Semaphore semReceiverAction = new Semaphore(1); // Sync receiving action, because of setting a new one

    public SocketCommunicator(Activity activity, Context context, Socket endPoint, BufferedReader input, PrintWriter output) {
        this.activity = activity;
        this.context = context;
        this.endPoint = endPoint;
        this.input = input;
        this.output = output;
    }

    protected void setReceiverAction(SocketCommunicator.Receiver receiverAction) {
        try {
            semReceiverAction.acquire();
            this.receiverAction = new Receiver() {

                @Override
                public void action() {
                    receiverAction.action();
                }

            };
            semReceiverAction.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean receiverActionUnequalNull() {
        boolean tmp = false;

        try {
            semReceiverAction.acquire();
            tmp = receiverAction != null;
            semReceiverAction.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return tmp;
    }

    public boolean isClosed() {
        return endPoint.isClosed();
    }

    public void close() throws IOException {
        if (!isClosed())
            endPoint.close();
    }

    public boolean isConnected() {
        return endPoint.isConnected();
    }

    /*
     *
     * Receiving messages with new receiver action.
     *
     */
    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
        try {
            setReceiverAction(receiverAction);

            // Start new receiver-Thread, if no one exists or last one is terminated
            if (receiverThread == null || receiverThread.getState() == Thread.State.TERMINATED) {
                Log.e("Test1", "Test1");
                receiverThread = new Thread(this.receiverAction);
                receiverThread.start();
            }
        } catch (NullPointerException e) {
            throw new NullPointerException("\"During receiveMessages(...)\": No Receiver-Action defined: null");
        }
    }

    @SuppressLint("LongLogTag")
    public void stopReceivingMessages() {
        boolean unequalNull = receiverActionUnequalNull();

        if (unequalNull && receiverThread != null && receiverThread.getState() != Thread.State.TERMINATED)
            receiverAction.setDoneReading(true);
        else if (!unequalNull)
            throw new NullPointerException("During \"stopReceivingMessages(...)\" [for " + this + "]: Cannot terminate Receiver-Thread with \"setDoneReading()\"-method, no Receiver-Action defined: " + null);
        else if (receiverThread == null)
            throw new NullPointerException("During \"stopReceivingMessages(...)\" [for " + this + "]: Cannot stop a null-referenced Thread, no Receiver-Thread defined: " + null);
        else
            Log.e("Thread is already terminated", "During \"stopReceivingMessages(...)\" [for " + this + "]: Cannot stop a terminated Receiver-Thread!");
    }

    /*
     *
     * Receiving messages with last receiver action.
     *
     */
    @SuppressLint("LongLogTag")
    public void continueReceivingMessages() {
        boolean unequalNull = receiverActionUnequalNull();

        if (unequalNull && receiverThread != null && receiverThread.getState() == Thread.State.TERMINATED) {
            receiverAction.setDoneReading(false);
            receiveMessages(receiverAction);
        } else if (!unequalNull)
            throw new NullPointerException("During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot set \"run()\"-method of Receiver-Thread, no Receiver-Action defined: " + null);
        else if (receiverThread == null)
            throw new NullPointerException("During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot start a null-referenced Thread, no Receiver-Thread defined: " + null);
        else
            Log.e("Thread is already running", "During \"continueReceivingMessages(...)\" [for " + this + "]: Cannot start a running Receiver-Thread!");
    }

    /*
     *
     * Returns whether the message was sent successfully or not.
     *
     */
    public boolean sendMessage(String message) {
        if (message != null) {
            Sender sender = new Sender(message);
            senderThread = new Thread(sender);
            senderThread.start();

            try {
                senderThread.join();
            } catch (InterruptedException e) {
                return false;
            }

            return sender.getSent();
        }

        throw new NullPointerException("During \"sendMessage(...)\": Cannot send message, no message defined: " + null);
    }

    public String getMessage() {
        return message;
    }

    @SuppressLint("LongLogTag")
    public void disconnectClientFromServer() throws IOException {
        // Stop receiving messages
        receiverAction.setDoneReading(true);

        /*
        if (receiverThread != null && receiverThread.getState() != Thread.State.TERMINATED)
            receiverThread.interrupt();

        // Stop sending messages
        if (senderThread != null && senderThread.getState() != Thread.State.TERMINATED)
            senderThread.interrupt();
        */

        // Close Socket for communication
        if (!isClosed())
            close(); // "input" and "output" will be closed automatically too
    }

    public abstract class Receiver implements Runnable {

        private final Semaphore semDoneReading = new Semaphore(1); // Semaphore for start/stop receiving
        private boolean doneReading = false;

        public boolean getDoneReading() {
            boolean tmp = false;

            try {
                semDoneReading.acquire();
                tmp = doneReading;
                semDoneReading.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return tmp;
        }

        public void setDoneReading(boolean doneReading) {
            try {
                semDoneReading.acquire();
                this.doneReading = doneReading;
                semDoneReading.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public abstract void action(); // Define actions of this Thread

        @Override
        public void run() {
            if (endPoint != null && !endPoint.isClosed()) {
                message = ""; // Reset message

                try {
                    while (!getDoneReading()) {
                        if (input != null && input.ready()) {
                            final String receivedMsg = input.readLine();
                            Log.e("Receiving ready", "Ready");

                            if (message != null) {
                                message = receivedMsg;

                                // Sync action method for receiving
                                try {
                                    Log.e("Received", message);
                                    semReceiverAction.acquire();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                action(); // Do defined action
                                semReceiverAction.release();
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

    public class Sender implements Runnable {

        private final String message;
        private boolean sent = false; // true: message was sent successfully

        public Sender(String message) {
            this.message = message;
        }

        public boolean getSent() {
            return sent;
        }

        @Override
        public void run() {
            if (output != null && !endPoint.isClosed()) {
                output.println(message);
                output.flush();
                sent = true;
                Log.e("sent", "true, " + message);
            } else {
                sent = false;
                Log.e("sent", "false, " + message);
            }
        }

    }
}
