package com.example.ichhabschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.adapter.HostOnlineGameAdapter;
import com.example.ichhabschonmal.server_client_communication.ServerSocketEndPoint;
import com.example.ichhabschonmal.server_client_communication.SocketCommunicator;

import java.io.IOException;

@SuppressLint("SetTextI18n")
public class HostOnlineGame extends AppCompatActivity {
    private ServerSocketEndPoint serverEndPoint;
    private SocketCommunicator.Receiver receiverAction;
    private int minStoryNumber, maxStoryNumber, maxPlayerNumber, connectedPlayers = 0;

    private RecyclerView recyclerView;
    private HostOnlineGameAdapter hostOnlineGameAdapter;

    private TextView tvIP, connectedClients;
    private Button continues;

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
        minStoryNumber = getIntent().getExtras().getInt("MinStoryNumber");
        maxStoryNumber = getIntent().getExtras().getInt("MaxStoryNumber");
        maxPlayerNumber = getIntent().getExtras().getInt("PlayerNumber");

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

            @Override
            public void action() {
                if (serverEndPoint.sizeOfClients() > 0) {
                    int clientIndex = serverEndPoint.sizeOfClients() - 1;
                    String clientInfo = serverEndPoint.getClientsMessage(clientIndex);
                    String[] s = cutClientInfo(clientInfo);

                    serverEndPoint.setClientsIPAddress(clientIndex, s[0]);
                    serverEndPoint.setClientsDeviceName(clientIndex, s[1]);

                    runOnUiThread(() -> connectedClients.setText("Verbunden:\t\t" + serverEndPoint.sizeOfClients() + " / " + (maxPlayerNumber - 1)));

                    runOnUiThread(() -> {
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
    }

    private static String[] cutClientInfo(String clientInfo) {
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

