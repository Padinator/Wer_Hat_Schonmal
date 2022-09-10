package com.example.ichhabschonmal.online_gaming;

import android.annotation.SuppressLint;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ichhabschonmal.HelpFunctions;
import com.example.ichhabschonmal.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressLint("SetTextI18n")
public class JoinGame extends AppCompatActivity {
    private EditText etIP, etPort;
    private TextView tvMessages;
    private EditText etMessage;
    private Button btnSend;

    private Thread Thread1 = null;
    private String SERVER_IP;
    private int SERVER_PORT;

    private BufferedReader input;
    private PrintWriter output;

    HelpFunctions helpFunctions;
    String device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_game);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        Button btnConnect = findViewById(R.id.btnConnect);

        helpFunctions = new HelpFunctions();
        device = helpFunctions.getNameOfDevice();

        btnConnect.setOnClickListener(v -> {
            tvMessages.setText("");
            SERVER_IP = etIP.getText().toString().trim();
            SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
            Thread1 = new Thread(new Connector());
            Thread1.start();
        });

        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();

            if (!message.isEmpty()) {
                new Thread(new Run3(message)).start();
            }
        });
    }

    class Connector implements Runnable {
        public void run() {
            Socket socket;

            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnUiThread(() -> tvMessages.setText("Connected\n"));

                new Thread(new Run2()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class Run2 implements Runnable {

        @Override
        public void run() {
            boolean doneReading = false;

            while (!doneReading) {
                try {
                    final String message = input.readLine();
                    Log.e("Server sent to client", "Ready");

                    if (message != null) {
                        runOnUiThread(() -> tvMessages.append("server:" + message + device +"\n"));
                    } else {
                        // Thread1 = new Thread(new Connector());
                        // Thread1.start();
                        doneReading = true;
                        Log.e("Server sent to client", "Not Ready");
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

                tvMessages.append(device+ ": " + message + device+ "\n");
                etMessage.setText("");
            });
        }
    }


    private String getLocalIpAddress() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();

        return InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                .putInt(ipInt).array()).getHostAddress();
    }
}