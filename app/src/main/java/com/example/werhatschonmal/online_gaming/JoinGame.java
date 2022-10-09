package com.example.werhatschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.werhatschonmal.CreatePlayers;
import com.example.werhatschonmal.MainActivity;
import com.example.werhatschonmal.R;
import com.example.werhatschonmal.server_client_communication.ClientServerHandler;
import com.example.werhatschonmal.server_client_communication.ClientSocketEndPoint;
import com.example.werhatschonmal.server_client_communication.SocketCommunicator;
import com.example.werhatschonmal.server_client_communication.SocketEndPoint;

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
                String clientsMessage = clientEndPoint.getClient().getMessage();
                String[] lines = clientsMessage.split(";");
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
                        new Thread(() -> clientEndPoint.stopReceivingMessages()).start();
                            // Start a new thread, because method and receiverAction uses the same semaphore

                        // Set serverSocketEndPoint
                        ClientServerHandler.setClientEndPoint(clientEndPoint); // Pass to all other intents

                        // Pass to next intent
                        createPlayers.putExtra("OnlineGame", true);
                        createPlayers.putExtra("ServerSide", false);
                        createPlayers.putExtra("MinStoryNumber", Integer.parseInt(lines[1]));     // Pass storyMinNumber
                        createPlayers.putExtra("MaxStoryNumber", Integer.parseInt(lines[2]));     // Pass storyMaxNumber
                        createPlayers.putExtra("PlayerNumber", 1);     // Pass number of players
                        createPlayers.putExtra("GameName", lines[3]);     // Pass the name of the game
                        createPlayers.putExtra("DrinkOfTheGame", lines[4]);
                        ClientServerHandler.getClientEndPoint().getClient().setPlayerNumber(Integer.parseInt(lines[5]) + 1);

                        startActivity(createPlayers);
                        finish();
                        break;
                    }
                    default: {
                        Log.e("Client receives outside, JoinGame", Arrays.toString(lines));
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
                        connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_CONNECTED);
                        clientEndPoint.sendMessage(clientEndPoint.getNameOfDevice() + " " + clientEndPoint.getClient().getIPAddress());
                    } else if (!clientEndPoint.isConnected())
                        connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_NOT_CONNECTED);
                    else
                        Log.e("Connect-Button of client", "No connection created, but one is already active");
                } else {
                    connectionStatusInfo.setText("Status:\t\t" + ClientSocketEndPoint.STATUS_CONNECTED);
                    Toast.makeText(getApplicationContext(), "Du bist bereits verbunden!", Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        // calling the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {       // Catch back button
        if (clientEndPoint != null) {
            try {
                clientEndPoint.disconnectClient(); // Disconnect client, clientside
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (clientEndPoint != null && clientEndPoint.isConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Spielererstellung")
                    .setMessage("M\u00f6chtest du wirkklich zur\u00fcck gehen?")
                    .setPositiveButton("Zur\u00fcck", (dialog, which) -> {
                        Intent mainActivity = new Intent(JoinGame.this, MainActivity.class);

                        startActivity(mainActivity);
                        finish();
                    })
                    .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                    });

            builder.create().show();
        } else {
            Intent mainActivity = new Intent(JoinGame.this, MainActivity.class);

            startActivity(mainActivity);
            finish();
        }
    }

}