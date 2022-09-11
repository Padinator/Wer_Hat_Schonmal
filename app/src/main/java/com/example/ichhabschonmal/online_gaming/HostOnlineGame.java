package com.example.ichhabschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.server_client_communication.ServerSocketEndPoint;

import java.io.IOException;

@SuppressLint("SetTextI18n")
public class HostOnlineGame extends AppCompatActivity {
    private ServerSocketEndPoint serverEndPoint;

    private Thread connectionThread;
    private Thread receiverThread;
    private Thread senderThread;
    private ServerSocketEndPoint.Client.Receiver receiverAction;

    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_online_game);

        // Define and initialize TextViews, EditTexts and Buttons
        TextView tvIP = findViewById(R.id.tvIP);
        TextView tvPort = findViewById(R.id.tvPort);
        TextView tvMessages = findViewById(R.id.tvMessages);
        EditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        // Initialize server endpoint
        try {
            serverEndPoint = new ServerSocketEndPoint(this, getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set TextViews
        tvIP.setText(serverEndPoint.getSERVER_IP());
        tvPort.setText("" + ServerSocketEndPoint.SERVER_PORT);

        // Create actions after messaging
        receiverAction = serverEndPoint.new Client().new Receiver() {

            @Override
            public void action() {
                Log.e("ActionThread", "actioning: " + serverEndPoint.getMessage());
                runOnUiThread(() -> tvMessages.append("client: " + serverEndPoint.getMessage() + "\n"));
            }

        };

        // Create connection to Socket
        serverEndPoint.createConnection(1, receiverAction);

        // Receive messages
        //serverEndPoint.receiveMessages(receiverAction);

        // Disconnect from Socket
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Log.e("Test1", "Stop");
                serverEndPoint.stopReceivingMessagesFromAllClients();
                Log.e("Test1", "Stopped");

                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e("Test1", "Start");
                    serverEndPoint.continueReceivingMessagesFromAllClients();
                    Log.e("Test1", "Started");
                }).start();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


        // Set OnClickListener, send messages
        btnSend.setOnClickListener(v -> {
            message = etMessage.getText().toString().trim();

            if (!message.isEmpty()) {
                serverEndPoint.sendMessageToAllClients(message);
                tvMessages.append("server sent this: " + message + "\n");
                etMessage.setText("");
            }
        });
    }
}