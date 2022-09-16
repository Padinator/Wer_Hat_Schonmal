package com.example.ichhabschonmal.server_client_communication;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketCommunicator {

    private final Activity activity;
    private final Context context;

    private final Socket endPoint;
    private final BufferedReader input;
    private final PrintWriter output;

    public SocketCommunicator(Activity activity, Context context, Socket endPoint, BufferedReader input, PrintWriter output) {
        this.activity = activity;
        this.context = context;
        this.endPoint = endPoint;
        this.input = input;
        this.output = output;
    }

    public boolean isClosed() {
        return endPoint.isClosed();
    }

    public void close() throws IOException {
        endPoint.close();
    }

    public boolean isConnected() {
        return endPoint.isConnected();
    }

    public abstract class Receiver implements Runnable {

        private boolean doneReading = false;
        private String message;

        public void setDoneReading(boolean doneReading) {
            this.doneReading = doneReading;
        }

        public String getMessage() {
            return message;
        }

        public abstract void action(); // Define actions of this Thread

        @Override
        public void run() {
            if (endPoint != null && !endPoint.isClosed()) {
                this.message = ""; // Reset message

                try {
                    while (!doneReading) {
                        if (input != null && input.ready()) {
                            final String message = input.readLine();
                            Log.e("Receiving ready", "Ready");

                            if (message != null) {
                                this.message = message;
                                action(); // Do defined action
                                Log.e("Receiving", "Message: " + message);
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

        private String message;

        public Sender(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            if (output != null && !endPoint.isClosed()) {
                output.println(message);
                output.flush();
            } else
                activity.runOnUiThread(() -> Toast.makeText(context, "Cannot send: No connection to end point, server end point is closed: " + endPoint.isClosed(), Toast.LENGTH_SHORT).show());
        }

    }
}
