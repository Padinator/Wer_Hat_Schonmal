package com.example.ichhabschonmal.online;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.ichhabschonmal.R;


public class JoinGame extends AppCompatActivity {
    private WifiManager wifiManager;
    EditText hostToken;
    Button sendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.join_game);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        hostToken = findViewById(R.id.hostToken);
        sendToken = findViewById(R.id.sendToken);


        sendToken.setOnClickListener(view -> {

        });


    }

}