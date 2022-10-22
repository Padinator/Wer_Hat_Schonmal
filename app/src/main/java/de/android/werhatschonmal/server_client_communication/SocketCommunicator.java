package de.android.werhatschonmal.server_client_communication;

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

/**
 *
 * Class for defining sending and receiving messages.
 */
public class SocketCommunicator {

    /**
     *
     * <strong>
     *     Data field activity for running messages on UI-Thread.<br>
     * </strong>
     */
    private final Activity activity;

    /**
     *
     * <strong>
     *     Data field context for making and showing Toasts.<br>
     * </strong>
     */
    private final Context context;

    /**
     *
     * <strong>
     *     Data field for the Socket to receive from and send messages.<br>
     * </strong>
     *
     * Problem: Field could be final, if disconnect works properly and without "endPoint = null".
     */
    private /* final */ Socket endPoint;

    /**
     *
     * <strong>
     *    Data field for the BufferedReader of the endPoint.<br>
     * </strong>
     */
    private final BufferedReader input;

    /**
     *
     * <strong>
     *     Data field for the PrintWriter of the endPoint.<br>
     * </strong>
     */
    private final PrintWriter output;

    /**
     *
     * <strong>
     *     Data field for the Thread, which receives messages.<br>
     *     No synchronization necessary:
     * </strong>
     *
     * SocketCommunicator/Client is used exactly 1 time in the host's LinkedList<br>
     * Variable "receiverThread" is only used locally and executed by Main-Thread<br>
     *      -> no sync necessary<br>
     *      -> Unequal to "receiverAction": can be modified ("setReceiverAction()")<br>
     *      and used ("action()")
     */
    private Thread receiverThread;

    /**
     *
     * <strong>
     *     Data field for the Thread, which send messages.<br>
     * </strong>
     */
    private Thread senderThread;

    /**
     *
     * <strong>
     *     Data field for saving the current receiverAction.<br>
     * </strong>
     */
    private Receiver receiverAction;

    /**
     *
     * <strong>
     *     Data field for saving the last passed receiverAction.<br>
     * </strong>
     *
     * Because of saving the last passed receiverAction, messages could be seen outside from the
     * receiverAction, which was created by user and passed to SocketCommunicator.
     */
    private Receiver oldReceiverAction;

    /**
     *
     * <strong>
     *     Semaphore for synchronizing access to a the data field receiverAction.<br>
     * </strong>
     */
    private final Semaphore semReceiverAction = new Semaphore(1); // Sync receiving action, because of setting a new one

    /**
     *
     * @param activity Pass actual activity for showing Toasts.
     * @param context Pass actual context for making Toasts.
     * @param endPoint Pass a Socket for sending and receiving messages.
     * @param input Pass the BufferedReader to the Socket.
     * @param output Pass the PrintWriter to the Socket.
     */
    public SocketCommunicator(Activity activity, Context context, Socket endPoint, BufferedReader input, PrintWriter output) {
        this.activity = activity;
        this.context = context;
        this.endPoint = endPoint;
        this.input = input;
        this.output = output;
    }

    /**
     *
     * @param receiverAction Pass and set new action after receiving messages.
     */
    protected void setReceiverAction(SocketCommunicator.Receiver receiverAction) {
        try {
            semReceiverAction.acquire();
            oldReceiverAction = receiverAction;
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

    /**
     *
     * @return Return, if the actual action for receiving messages is unequal to null
     * (if null: true)
     */
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

    /**
     *
     * @return Return, if the actual SocketCommunicator is connected (if connected: true)
     */
    public boolean isConnected() {
        if (endPoint != null)
            return endPoint.isConnected();

        return false;
    }

    /**
     * <strong>
     *     Receiving messages with doing new action after receiving a message.<br>
     *     Delete old one and set new one synchronized.
     * </strong>
     * @param receiverAction Pass a new receiverAction.
     */
    public void receiveMessages(SocketCommunicator.Receiver receiverAction) {
        try {
            setReceiverAction(receiverAction);

            // Start new receiver-Thread, if no one exists or last one is terminated
            if (receiverThread == null || receiverThread.getState() == Thread.State.TERMINATED) {
                Log.e("Test1", "Test1");
                receiverThread = new Thread(this.receiverAction);
                receiverThread.start();
            } // else: last still running receiver-Thread receives with new receiverAction
        } catch (NullPointerException e) {
            throw new NullPointerException("\"During receiveMessages(...)\": No Receiver-Action defined: null");
        }
    }

    /**
     *
     * <strong>
     *     Stops receiving messages. Thread is going to be terminated normally (by itself).<br>
     * </strong>
     */
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

    /**
     *
     * <strong>
     *      Continue receiving messages with last receiver action.<br>
     * </strong>
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

    /**
     *
     * @param message Pass a message to send.
     * @return Return, if the message was sent successfully (if success: true)
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

    /**
     *
     * @return Return the last message of the current receiverAction.
     */
    public String getMessage() {
        return receiverAction.getMessage();
    }

    /**
     *
     * <strong>
     *     No safe method!!!<br>
     * </strong>
     *
     * @return Return the next read line (synchronized access, but internally the receiver message
     * reads too from the BufferedReader of the Socket)
     * @throws IOException If something went wrong with reading from the BufferedWriter of a Socket.
     */
    public String readLine() throws IOException {
        String line = "";

        try {
            semReceiverAction.acquire();
            line = receiverAction.readLine();
            semReceiverAction.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return line;
    }

    /**
     *
     * <strong>
     *     Try to disconnect a client from server.<br>
     * </strong>
     *
     * @throws IOException If disconnecting a client from server failed.
     */
    @SuppressLint("LongLogTag")
    public void disconnectClientFromServer() throws IOException {
        // Stop receiving messages
        receiverAction.setDoneReading(true);

        // Close Socket for communication
        if (!endPoint.isClosed()) {
            endPoint.close(); // "input" and "output" will be closed automatically too
            endPoint = null; // Disconnect does not really work
        }
    }

    /**
     *
     * Class for receiving messages.
     */
    public abstract class Receiver implements Runnable {

        /**
         * <strong>
         *     Semaphore for synchronizing data field doneReading.<br>
         * </strong>
         */
        private final Semaphore semDoneReading = new Semaphore(1); // Semaphore for start/stop receiving

        /**
         *
         * <strong>
         *     Semaphore for synchronizing data field message.<br>
         * </strong>
         */
        private final Semaphore semMessage = new Semaphore(1); // Semaphore for get/set messages

        /**
         * <strong>
         *    Data field for state of reading ("done": true or "still reading": false)<br>
         * </strong>
         */
        private boolean doneReading = false;

        /**
         *
         * <strong>
         *    Data field of the actual message.<br>
         * </strong>
         */
        private String message = "";

        /**
         *
         * @return Return data field doneReading (synchronized access)
         */
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

        /**
         *
         * @return Return latest received message (synchronized access)
         */
        public String getMessage() {
            String tmp = "";

            try {
                semMessage.acquire();
                tmp = message;
                semMessage.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return tmp;
        }

        /**
         *
         * @param doneReading Pass and set a new value for data field doneReading
         *                    (synchronized access)
         */
        public void setDoneReading(boolean doneReading) {
            try {
                semDoneReading.acquire();
                this.doneReading = doneReading;
                semDoneReading.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         *
         * @param message Pass and set last received message (synchronized access)
         */
        public void setMessage(String message) {
            try {
                semMessage.acquire();
                this.message = message;
                semMessage.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        /**
         *
         * <strong>
         *     Here is the possibility to read the next line manually.<br>
         *     Method is not synchronized yet!!!
         * </strong>
         *
         * @return Return the next line of the BufferedReader of a Socket.
         * @throws IOException If reading from BufferedReader of a Socket failed.
         */
        public String readLine() throws IOException {
            return input.readLine();
        }

        /**
         * <strong>
         *    Redefine method to define an action when receiving messages.<br>
         * </strong>
         */
        public abstract void action();

        /**
         *
         * <strong>
         *     Default part during handling of receiving messages.<br>
         *     It will be called automatically by starting the corresponding Thread.<br>
         * </strong>
         */
        @Override
        public void run() {
            if (endPoint != null && !endPoint.isClosed()) {
                message = ""; // Reset message

                try {
                    while (!getDoneReading()) {
                        if (input != null && input.ready()) {
                            final String receivedMsg = input.readLine();
                            Log.e("Receiving ready", "Ready");

                            if (receivedMsg != null) {

                                setMessage(receivedMsg);
                                oldReceiverAction.setMessage(receivedMsg);

                                // Sync action method for receiving
                                try {
                                    Log.e("Received", receivedMsg);
                                    semReceiverAction.acquire();
                                    action(); // Do defined action
                                    semReceiverAction.release();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    Log.e("Receiving stopped", "stopped reading");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else
                activity.runOnUiThread(() -> Toast.makeText(context, "Cannot receive: No connection to end point", Toast.LENGTH_SHORT).show());
        }

    }

    /**
     *
     * Class for sending messages.
     */
    public class Sender implements Runnable {

        /**
         *
         * Data field for a message to send.
         */
        private final String message;

        /**
         *
         * Data field to set, if a message was sent successfully (success: true)
         */
        private boolean sent = false; // true: message was sent successfully

        /**
         *
         * <strong>
         *     Create a new Sender object to send messages.<br>
         * </strong>
         *
         * @param message Pass the message to send
         */
        public Sender(String message) {
            this.message = message;
        }

        /**
         *
         * @return Return the date field sent.
         */
        public boolean getSent() {
            return sent;
        }

        /**
         *
         * <strong>
         *     Default part during sending a message.<br>
         *     It will be called automatically by starting the corresponding Thread.<br>
         * </strong>
         */
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
