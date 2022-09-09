package com.example.ichhabschonmal.online_gaming;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.wifi_direct.ServerSocketEndPoint;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.UnknownHostException;

@SuppressLint("SetTextI18n")
public class HostOnlineGame extends AppCompatActivity {
    private ServerSocketEndPoint serverEndPoint;
    private Thread connectionThread;
    private Thread senderThread;
    private ServerSocketEndPoint.Receiver actionThread;
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
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        // Set TextViews
        tvIP.setText(serverEndPoint.getSERVER_IP());
        tvPort.setText("" + ServerSocketEndPoint.SERVER_PORT);

        // Create actions after messaging
        actionThread = serverEndPoint.new Receiver("Message from client") {

            @Override
            public void action() {
                Log.e("ActionThread", "actioning: " + serverEndPoint.getMessage());
                runOnUiThread(() -> tvMessages.append("client: " + serverEndPoint.getMessage() + "\n"));
            }

        };

        // Create the corresponding thread
        connectionThread = new Thread(serverEndPoint.new Connector(actionThread));
        connectionThread.start();

        // Set OnClickListener, send messages
        btnSend.setOnClickListener(v -> {
            message = etMessage.getText().toString().trim();

            if (!message.isEmpty()) {
                senderThread = new Thread(serverEndPoint.new Sender(message));
                senderThread.start();
                tvMessages.append("server sent this: " + message + "\n");
                etMessage.setText("");
            }
        });
    }
}