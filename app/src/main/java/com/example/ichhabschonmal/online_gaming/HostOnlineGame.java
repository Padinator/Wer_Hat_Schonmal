package com.example.ichhabschonmal.online_gaming;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.ichhabschonmal.R;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressLint("SetTextI18n")
public class HostOnlineGame extends AppCompatActivity {
    private Thread Thread1 = null;
    private TextView tvIP, tvPort;
    private TextView tvMessages;
    private EditText etMessage;
    private Button btnSend;

    private ServerSocket serverSocket;
    private static String SERVER_IP = "";
    private final int SERVER_PORT = 8080;

    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host_online_game);

        tvIP = findViewById(R.id.tvIP);
        tvPort = findViewById(R.id.tvPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        try {
            SERVER_IP = getLocalIpAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        Thread1 = new Thread(new Connector());
        Thread1.start();

        btnSend.setOnClickListener(v -> {
            message = etMessage.getText().toString().trim();

            if (!message.isEmpty()) {
                new Thread(new Run3(message)).start();
            }
        });
    }

    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        assert wifiManager != null;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
    }

    private PrintWriter output;
    private BufferedReader input;

    class Connector implements Runnable {

        @Override
        public void run() {
            Socket serverSocket;

            try {
                HostOnlineGame.this.serverSocket = new ServerSocket(SERVER_PORT);

                runOnUiThread(() -> {
                    tvMessages.setText("Not connected");
                    tvIP.setText("IP: " + SERVER_IP);
                    tvPort.setText("Port: " + SERVER_PORT);
                });

                try {
                    serverSocket = HostOnlineGame.this.serverSocket.accept();
                    output = new PrintWriter(serverSocket.getOutputStream());
                    input = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
                    runOnUiThread(() -> tvMessages.setText("Connected\n"));
                    new Thread(new Run2()).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private class Run2 implements Runnable {

        @Override
        public void run() {
            boolean doneReading = false;

            while (!doneReading) {
                try {
                    final String message = input.readLine();
                    Log.e("Client sent to server", "Ready");

                    if (message != null) {
                        runOnUiThread(() -> tvMessages.append("client:" + message + "\n"));
                    } else {
                        // Thread1 = new Thread(new Connector());
                        // Thread1.start();
                        doneReading = true;
                        Log.e("Client sent to server", "Not Ready");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    class Run3 implements Runnable {
        private final String message;

        Run3(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            output.println(message);
            output.flush();

            runOnUiThread(() -> {
                tvMessages.append("server sent this: " + message + "\n");
                etMessage.setText("");
            });
        }

    }
}