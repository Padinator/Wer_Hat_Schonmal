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
import java.util.Arrays;

@SuppressLint("SetTextI18n")
public class JoinGame extends AppCompatActivity {
    private EditText etIP;
    private TextView connectionStatusInfo;
    private Button btnConnect;

    private ClientSocketEndPoint clientEndPoint;
    private SocketCommunicator.Receiver receiverAction;

    @SuppressLint("LongLogTag")
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

                String clientsMessage = clientEndPoint.getClientsMessage();
                String[] lines = clientsMessage.split("\n");
                clientsMessage = lines[0];

                // Work off a client's message
                switch (clientsMessage) {
                    case (SocketEndPoint.CLOSE_CONNECTION): {
                        try {
                            clientEndPoint.disconnectClient();
                            runOnUiThread(() -> connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_NOT_CONNECTED));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                    case (SocketEndPoint.CREATE_PLAYER): {
                        Intent createPlayers = new Intent(getApplicationContext(), CreatePlayers.class);

                        // Terminate receiving Thread, while loading new intent
                        clientEndPoint.stopReceivingMessages();

                        // Pass to next intent
                        createPlayers.putExtra("EndPoint", clientEndPoint);
                        createPlayers.putExtra("OnlineGame", true);
                        createPlayers.putExtra("MinStoryNumber", lines[1]);     // Pass storyMinNumber
                        createPlayers.putExtra("MaxStoryNumber", lines[2]);     // Pass storyMaxNumber
                        createPlayers.putExtra("PlayerNumber", 1);     // Pass number of players
                        createPlayers.putExtra("GameName", lines[3]);     // Pass the name of the game
                        createPlayers.putExtra("DrinkOfTheGame", lines[4]);
                        createPlayers.putExtra("PlayersIndex", lines[5]);  // Pass the player's number
                        startActivity(createPlayers);
                        break;
                    }
                    default: {
                        Log.e("Client receives outside", Arrays.toString(lines));
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Client receives outside: " + Arrays.toString(lines), Toast.LENGTH_LONG).show());
                        clientEndPoint.sendMessage("clients sends 123");
                        break;
                    }
                }
            }

        };

        btnConnect.setOnClickListener(v -> {
            try {
                if (clientEndPoint == null || !clientEndPoint.isConnected()) {
                    boolean connected;

                    clientEndPoint = new ClientSocketEndPoint(this, getApplicationContext(), etIP.getText().toString().trim());
                    connected = clientEndPoint.createConnection(receiverAction);

                    Log.e("Client-Connection", connected + ", " + clientEndPoint.isConnected());

                    if (connected && clientEndPoint.isConnected()) {
                        Log.e("Test1", "1");
                        connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_CONNECTED);
                        clientEndPoint.sendMessage(clientEndPoint.getNameOfDevice() + " " + clientEndPoint.getClientIpAddress());
                    } else if (!clientEndPoint.isConnected()) {
                        Log.e("Test1", "2");
                        connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_NOT_CONNECTED);
                    } else
                        Log.e("Connect-Button of client", "No connection created, but one is already active");
                } else {
                    connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_CONNECTED);
                    Toast.makeText(getApplicationContext(), "Du bist bereits verbunden!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}