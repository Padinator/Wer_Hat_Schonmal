package com.example.ichhabschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.CreatePlayers;
import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.adapter.HostOnlineGameAdapter;
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
                    String clientInfo = serverEndPoint.getClientsMessage(clientIndex);
                    String[] s = cutClientInfo(clientInfo);

                    serverEndPoint.setClientsIPAddress(clientIndex, s[0]);
                    serverEndPoint.setClientsDeviceName(clientIndex, s[1]);

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
        serverEndPoint.createConnection(1, receiverAction);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            serverEndPoint.createConnection(1, receiverAction);
        }).start();

        // Set set OnClickListener for btnContinue
        continues.setOnClickListener(this::onClick);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onClick(View view) {
        Intent createPlayers = new Intent(getApplicationContext(), CreatePlayers.class);
        String messageForAllClients = "";
        LinkedList<Integer> responses = new LinkedList<>();

        // Send message to all clients to inform them
        messageForAllClients += SocketEndPoint.CREATE_PLAYER + "\n";
        messageForAllClients += minStoryNumber + "\n";
        messageForAllClients = messageForAllClients + (maxStoryNumber + "\n");
        messageForAllClients += gameName + "\n";
        messageForAllClients += drinkOfTheGame +"\n";

        for (int i = 0; i < serverEndPoint.sizeOfClients(); i++) {
            serverEndPoint.setClientsPlayerNumber(i, i + 1);

            if (!serverEndPoint.sendMessageToClient(i, messageForAllClients + (i + 1)))
                responses.add(i);
        }

        if (!responses.isEmpty()) {
            Log.e("Sending to all clients", "Failed sending to " + responses.size() + " clients");

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
        }

        // Terminate all Threads, while loading new intent
        serverEndPoint.stopReceivingMessages();

        // Pass to next intent
        createPlayers.putExtra("OnlineGame", true);
        createPlayers.putExtra("EndPoint", serverEndPoint);
        createPlayers.putExtra("MinStoryNumber", minStoryNumber);     // Pass storyMinNumber
        createPlayers.putExtra("MaxStoryNumber", maxStoryNumber);     // Pass storyMaxNumber
        createPlayers.putExtra("PlayerNumber", maxPlayerNumber);     // Pass number of players
        createPlayers.putExtra("GameName", gameName);     // Pass the name of the game
        createPlayers.putExtra("DrinkOfTheGame", drinkOfTheGame);
        createPlayers.putExtra("PlayersIndex", 0);      // Host is always first player
        startActivity(createPlayers);
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
}

