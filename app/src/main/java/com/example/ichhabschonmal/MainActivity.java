package com.example.ichhabschonmal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.example.ichhabschonmal.online_gaming.JoinGame;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Definitions
        Button newGame, loadGame, joinGame;

        // Buttons:
        newGame = findViewById(R.id.newGame);
        loadGame = findViewById(R.id.loadGame);
        joinGame = findViewById(R.id.joinGame);

        newGame.setOnClickListener(v -> {
            Intent newGame1 = new Intent(getApplicationContext(), NewGame.class);
            startActivity(newGame1);
            finish();
        });

        joinGame.setOnClickListener(v -> {
            Intent joinGame1 = new Intent(getApplicationContext(), JoinGame.class);
            startActivity(joinGame1);
            finish();
        });

        loadGame.setOnClickListener(v -> {
            Intent loadGame1 = new Intent(getApplicationContext(), LoadGame.class);
            startActivity(loadGame1);
            finish();
        });
    }
}