package com.example.ichhabschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.server_client_communication.ClientSocketEndPoint;
import com.example.ichhabschonmal.server_client_communication.SocketCommunicator;

@SuppressLint("SetTextI18n")
public class JoinGame extends AppCompatActivity {
    private EditText etIP;
    private TextView tvMessages;
    private EditText etMessage;
    private Button btnSend;

    private ClientSocketEndPoint clientEndPoint;
    private SocketCommunicator.Receiver receiverAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_game);
        etIP = findViewById(R.id.etIP);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);

        receiverAction = new SocketCommunicator(null, null, null, null, null).new Receiver() {

            @Override
            public void action() {
                tvMessages.setText("server: " + clientEndPoint.getClientsMessage());
            }

        };

        btnConnect.setOnClickListener(v -> {
            clientEndPoint = new ClientSocketEndPoint(this, getApplicationContext(), etIP.getText().toString().trim());

            try {
                if (clientEndPoint.createConnection(receiverAction)) {
                    tvMessages.setText("Connected");
                    clientEndPoint.sendMessage(clientEndPoint.getNameOfDevice() + " " + clientEndPoint.getClientIpAddress());

                } else
                    tvMessages.setText("Not Connected");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();

            if (!message.isEmpty()) {
                clientEndPoint.sendMessage(message);
                tvMessages.append("server sent this: " + message);
            }

            etMessage.setText("");
        });
    }
}