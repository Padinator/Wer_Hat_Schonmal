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
import com.example.ichhabschonmal.server_client_communication.SocketCommunicator;

import java.io.IOException;

@SuppressLint("SetTextI18n")
public class HostOnlineGame extends AppCompatActivity {
    private ServerSocketEndPoint serverEndPoint;
    private SocketCommunicator.Receiver receiverAction;

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
        tvIP.setText("Host-IP: " + serverEndPoint.getServerIP());
        tvPort.setText("Host-Port: " + ServerSocketEndPoint.SERVER_PORT);

        // Create actions after messaging
        receiverAction = new SocketCommunicator(null, null, null, null, null).new Receiver() {

            @Override
            public void action() {
                runOnUiThread(() -> {
                    tvMessages.append("client: " + serverEndPoint.getClientsMessage(0) + "\n");
                });
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
                serverEndPoint.stopReceivingMessages();
                Log.e("Test1", "Stopped");

                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e("Test1", "Start");
                    serverEndPoint.continueReceivingMessages();
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
                serverEndPoint.sendMessage(message);
                tvMessages.append("server sent this: " + message + "\n");
                etMessage.setText("");
            }
        });
    }
}