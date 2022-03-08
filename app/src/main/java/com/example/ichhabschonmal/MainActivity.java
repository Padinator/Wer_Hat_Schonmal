package com.example.ichhabschonmal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button newGame, loadGame;

        // Buttons:
        newGame = findViewById(R.id.newGame);
        loadGame = findViewById(R.id.loadGame);

        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newGame = new Intent(getApplicationContext(), NewGame.class);
                startActivity(newGame);
            }
        });

        loadGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loadGame = new Intent(getApplicationContext(), LoadGame.class);
                startActivity(loadGame);
            }
        });
    }
}