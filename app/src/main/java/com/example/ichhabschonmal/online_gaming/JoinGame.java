package com.example.ichhabschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ichhabschonmal.CreatePlayers;
import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.server_client_communication.ClientSocketEndPoint;
import com.example.ichhabschonmal.server_client_communication.SocketCommunicator;
import com.example.ichhabschonmal.server_client_communication.SocketEndPoint;

import java.io.IOException;

@SuppressLint("SetTextI18n")
public class JoinGame extends AppCompatActivity {
    private EditText etIP;
    private TextView connectionStatusInfo;
    private Button btnConnect;

    private ClientSocketEndPoint clientEndPoint;
    private SocketCommunicator.Receiver receiverAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_game);
        etIP = findViewById(R.id.etIP);
        connectionStatusInfo = findViewById(R.id.connectionStatusInfo);
        btnConnect = findViewById(R.id.btnConnect);

        receiverAction = new SocketCommunicator(null, null, null, null, null).new Receiver() {

            @Override
            public void action() {

                if (clientEndPoint.getClientsMessage().equals("start")) {
                    Intent createPlayer = new Intent(getApplicationContext(), CreatePlayers.class);
                    startActivity(createPlayer);
                }

                switch(clientEndPoint.getClientsMessage()) {
                    case(SocketEndPoint.CLOSE_CONNECTION): {
                        try {
                            clientEndPoint.disconnectClient();
                            runOnUiThread(() -> connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_NOT_CONNECTED));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    } default: {
                        Log.e("Client receives", clientEndPoint.getClientsMessage());
                        break;
                    }
                }
                Log.e("Client receives2", clientEndPoint.getClientsMessage());
            }

        };

        btnConnect.setOnClickListener(v -> {
            try {
                if (clientEndPoint == null || !clientEndPoint.isConnected()) {
                    clientEndPoint = new ClientSocketEndPoint(this, getApplicationContext(), etIP.getText().toString().trim());
                    clientEndPoint.createConnection(receiverAction);

                    if (clientEndPoint.isConnected())
                        connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_CONNECTED);
                    else
                        connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_NOT_CONNECTED);

                    clientEndPoint.sendMessage(clientEndPoint.getNameOfDevice() + " " + clientEndPoint.getClientIpAddress());
                } else {
                    connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_CONNECTED);
                    Toast.makeText(getApplicationContext(), "Du bist bereits verbunden!", Toast.LENGTH_SHORT);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}