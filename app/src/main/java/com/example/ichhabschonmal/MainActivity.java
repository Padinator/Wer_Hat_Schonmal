package com.example.ichhabschonmal;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Definitions
        Button newGame, loadGame;

        // Buttons:
        newGame = findViewById(R.id.newGame);
        loadGame = findViewById(R.id.loadGame);

        newGame.setOnClickListener(v -> {
            Intent newGame1 = new Intent(getApplicationContext(), NewGame.class);
            startActivity(newGame1);
            finish();
        });

        loadGame.setOnClickListener(v -> {
            Intent loadGame1 = new Intent(getApplicationContext(), LoadGame.class);
            startActivity(loadGame1);
            finish();
        });
    }
}