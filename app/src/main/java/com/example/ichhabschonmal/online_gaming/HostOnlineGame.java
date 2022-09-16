package com.example.ichhabschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.adapter.ClientAdapter;
import com.example.ichhabschonmal.server_client_communication.ServerSocketEndPoint;
import com.example.ichhabschonmal.server_client_communication.SocketCommunicator;

import java.io.IOException;

@SuppressLint("SetTextI18n")
public class HostOnlineGame extends AppCompatActivity {
    private ServerSocketEndPoint serverEndPoint;
    private SocketCommunicator.Receiver receiverAction;


    private String message;
    ImageButton kickClient;
    RecyclerView recyclerView;
    ClientAdapter clientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_online_game);

        // Define and initialize TextViews, EditTexts and Buttons
        TextView tvIP = findViewById(R.id.tvIP);
        TextView tvPort = findViewById(R.id.tvPort);
        EditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);


        // Initialize server endpoint
        try {
            serverEndPoint = new ServerSocketEndPoint(this, getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        //ClientAdapter
        clientAdapter = new ClientAdapter(this, serverEndPoint);
        recyclerView.setAdapter(clientAdapter);


        // Set TextViews
        tvIP.setText("Host-IP: " + serverEndPoint.getServerIP());
        tvPort.setText("Host-Port: " + ServerSocketEndPoint.SERVER_PORT);


        // Create actions after messaging
        receiverAction = new SocketCommunicator(null, null, null, null, null).new Receiver() {

            @Override
            public void action() {
                if (serverEndPoint.sizeOfClients() > 0) {
                    int clientIndex = serverEndPoint.sizeOfClients() - 1;
                    String clientInfo = serverEndPoint.getClientsMessage(clientIndex);
                    String[] s = cutClientInfo(clientInfo);

                    serverEndPoint.setClientsIPAddress(clientIndex, s[0]);
                    serverEndPoint.setClientsDeviceName(clientIndex, s[1]);

                    Log.e("Server receives", serverEndPoint.clients.toString());
                    runOnUiThread(() -> {
                        clientAdapter.notifyDataSetChanged();
                        recyclerView.setAdapter(clientAdapter);
                    });
                }
            }
        };

        // Create connection to Socket
        serverEndPoint.createConnection(2, receiverAction);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serverEndPoint.sendMessage("123");

            }
        });


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

