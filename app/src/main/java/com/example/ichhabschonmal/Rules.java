package com.example.ichhabschonmal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ComponentActivity;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rules extends AppCompatActivity {

    int idOfFirstPlayer;
    int countOfPlayers;
    int idOfFirstStory;
    int countOfStories;

    int gameId;

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules);

        // Definitions
        Button start;

        // Buttons
        start = findViewById(R.id.start);

        // ListView
        listView = (ListView) findViewById(R.id.listView);

        // ArrayList for all Game Rules
        ArrayList<String> rules = new ArrayList<>();
        rules.add("1: bla bla bla");
        rules.add("2: blub blub blub");
        rules.add("3: bli bli bli");
        rules.add("4:");
        rules.add("5:");
        rules.add("6:");
        rules.add("7:");
        rules.add("8:");
        rules.add("9:");
        rules.add("10:");
        rules.add("11:");
        rules.add("12:");

        // Adapter
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rules);
        listView.setAdapter(adapter);

        // Create database connection
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // Get from last intent
        gameId = getIntent().getExtras().getInt("GameId");

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startGame = new Intent(Rules.this, PlayGame.class);

                startGame.putExtra("GameId", gameId);

                startActivity(startGame);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spiel verlassen")
                .setMessage("Wenn du zur\u00fcck gehst, werden die Daten gel\u00f6scht!")
                .setPositiveButton("Zur\u00fcck", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //ManageGame.deleteGame(gameId);
                        //Wird das Spiel/game auch geloeschttttttttttttttttttttttttttttttttttttttttttttttttttt
                        finish();
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        builder.create().show();
    }
}
