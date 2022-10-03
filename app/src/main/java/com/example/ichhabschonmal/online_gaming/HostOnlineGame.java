package com.example.ichhabschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.CreatePlayers;
import com.example.ichhabschonmal.MainActivity;

import com.example.ichhabschonmal.NewGame;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.adapter.HostOnlineGameAdapter;
import com.example.ichhabschonmal.server_client_communication.ClientServerHandler;
import com.example.ichhabschonmal.server_client_communication.ServerSocketEndPoint;
import com.example.ichhabschonmal.server_client_communication.SocketCommunicator;
import com.example.ichhabschonmal.server_client_communication.SocketEndPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class HostOnlineGame extends AppCompatActivity {
    private ServerSocketEndPoint serverEndPoint;
    private SocketCommunicator.Receiver receiverAction;
    private HostOnlineGameAdapter hostOnlineGameAdapter;

    private RecyclerView recyclerView;
    private TextView tvIP, connectedClients;
    private Button continues;

    private int minStoryNumber, maxStoryNumber, maxPlayerNumber;
    private String gameName,drinkOfTheGame;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_online_game);

        // TextViews
        tvIP = findViewById(R.id.tvIP);
        connectedClients = findViewById(R.id.connectedClients);

        // Buttons
        continues = findViewById(R.id.btnSend);

        // Get from last intent
        maxPlayerNumber = getIntent().getExtras().getInt("PlayerNumber");
        maxPlayerNumber = 2;
        minStoryNumber = getIntent().getExtras().getInt("MinStoryNumber");
        maxStoryNumber = getIntent().getExtras().getInt("MaxStoryNumber");
        gameName = getIntent().getStringExtra("GameName");
        drinkOfTheGame = getIntent().getStringExtra("DrinkOfTheGame");

        // Initialize server endpoint
        try {
            serverEndPoint = new ServerSocketEndPoint(this, getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set TextViews
        tvIP.setText("Host-IP:\t\t" + serverEndPoint.getServerIP());
        connectedClients.setText("Verbunden:\t\t0 / " + (maxPlayerNumber - 1));

        // Create actions for receiving messaging
        receiverAction = new SocketCommunicator(null, null, null, null, null).new Receiver() {


            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void action() {
                if (serverEndPoint.sizeOfClients() > 0) {
                    int clientIndex = serverEndPoint.sizeOfClients() - 1;
                    String clientInfo = serverEndPoint.getAClient(clientIndex).getMessage();
                    String[] s = cutClientInfo(clientInfo);

                    serverEndPoint.getAClient(clientIndex).setIPAddress(s[0]);
                    serverEndPoint.getAClient(clientIndex).setDeviceName(s[1]);

                    runOnUiThread(() ->  {
                        connectedClients.setText("Verbunden:\t\t" + serverEndPoint.sizeOfClients() + " / " + (maxPlayerNumber - 1)); // Better: (serverEndPoint.getCountOfRequestedClients())
                        hostOnlineGameAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(hostOnlineGameAdapter);
                    });
                }
            }
        };

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //ClientAdapter
        hostOnlineGameAdapter = new HostOnlineGameAdapter(this, serverEndPoint, receiverAction, connectedClients, maxPlayerNumber);
        recyclerView.setAdapter(hostOnlineGameAdapter);

        // Create connection to Socket
        serverEndPoint.createConnection(maxPlayerNumber - 1, receiverAction);

        // Set set OnClickListener for btnContinue
        continues.setOnClickListener(this::onClick);
    }

    private String[] cutClientInfo(String clientInfo) {
        String[] s = new String[2];
        int currentIndex = 0;
        boolean gotIp = false;
        for (int i = clientInfo.length() - 1; i > 0; i--) {
            if (clientInfo.charAt(i) != ' ' && !gotIp) {
                s[0] += clientInfo.charAt(i);
            } else {
                if (!gotIp) {
                    gotIp = true;
                    currentIndex = i;
                }
            }
        }
        s[0] = s[0].substring(4);
        String temp = "";
        for (int i = s[0].length() - 1; i >= 0; i--) {
            temp += s[0].charAt(i);
        }
        s[0] = temp;
        s[1] = clientInfo.substring(0, currentIndex);
        return s;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onClick(View view) {
        if (serverEndPoint.sizeOfClients() < maxPlayerNumber - 1)
            Toast.makeText(getApplicationContext(), "Zu wenig Spieler verbunden!", Toast.LENGTH_SHORT).show();
        else if (serverEndPoint.sizeOfClients() > maxPlayerNumber - 1)
            Toast.makeText(getApplicationContext(), "Zu viele Spieler sind verbunden, starte die App neu!", Toast.LENGTH_SHORT).show();
        else {
            Intent createPlayers = new Intent(getApplicationContext(), CreatePlayers.class);
            String messageForAllClients;
            LinkedList<Integer> responses = new LinkedList<>();

            // Send message to all clients to inform them
            messageForAllClients = SocketEndPoint.CREATE_PLAYER + ";";
            messageForAllClients += minStoryNumber + ";";
            messageForAllClients = messageForAllClients + (maxStoryNumber + ";");
            messageForAllClients += gameName + ";";
            messageForAllClients += drinkOfTheGame + ";";

            for (int i = 0; i < serverEndPoint.sizeOfClients(); i++) {
                // Set player number of a client/player
                serverEndPoint.getAClient(i).setPlayerNumber(i + 1);

                // Send message for creating players to all clients
                if (!serverEndPoint.sendMessageToClient(i, messageForAllClients + (i + 1))) // Pass a player's index
                    responses.add(i);
            }

            if (!responses.isEmpty()) {
                Log.e("Sending to all clients", "Failed sending to " + responses.size() + " clients");

            /*
            // Retry sending messages 4 more times
            for (Integer index : responses) {
                boolean send = false;

                for (int i = 0; !send && i < 4; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    send = serverEndPoint.sendMessageToClient(index, messageForAllClients + (index + 1));
                }
            }
            */
            }

            // Terminate all Threads, while loading new intent
            serverEndPoint.stopReceivingMessages();

            // Disconnect server socket, no connection necessary
            try {
                serverEndPoint.disconnectServerSocket();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Set serverSocketEndPoint
            ClientServerHandler.setServerEndPoint(serverEndPoint); // Pass to all other intents

            // Pass to next intent
            createPlayers.putExtra("OnlineGame", true);
            createPlayers.putExtra("ServerSide", true);
            createPlayers.putExtra("MinStoryNumber", minStoryNumber);     // Pass storyMinNumber
            createPlayers.putExtra("MaxStoryNumber", maxStoryNumber);     // Pass storyMaxNumber
            createPlayers.putExtra("PlayerNumber", maxPlayerNumber);     // Pass number of players
            createPlayers.putExtra("GameName", gameName);     // Pass the name of the game
            createPlayers.putExtra("DrinkOfTheGame", drinkOfTheGame);
            //createPlayers.putExtra("PlayersIndex", 0);      // Host is always first player

            startActivity(createPlayers);
            finish();
        }
    }

    @Override
    public void onBackPressed() {       // Catch back button
        if (serverEndPoint != null) {
            try {
                serverEndPoint.disconnectClientsFromServer(); // Disconnect all clients from serve, serverside
                serverEndPoint.disconnectServerSocket(); // Disconnect socket of server
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (serverEndPoint != null && serverEndPoint.sizeOfClients() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Spielererstellung")
                    .setMessage("M\u00f6chtest du wirkklich zur\u00fcck gehen?")
                    .setPositiveButton("Zur\u00fcck", (dialog, which) -> {
                        Intent mainActivity = new Intent(HostOnlineGame.this, MainActivity.class);

                        startActivity(mainActivity);
                        finish();
                    })
                    .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                    });

            builder.create().show();
        } else {
            Intent mainActivity = new Intent(HostOnlineGame.this, MainActivity.class);

            startActivity(mainActivity);
            finish();
        }
    }

}

