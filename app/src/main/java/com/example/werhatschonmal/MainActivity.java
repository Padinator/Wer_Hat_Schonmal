package com.example.werhatschonmal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.werhatschonmal.online_gaming.JoinGame;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // calling the action bar
        /*ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));*/


    }
}