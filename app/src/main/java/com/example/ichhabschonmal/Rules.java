package com.example.ichhabschonmal;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.ichhabschonmal.database.AppDatabase;

import java.util.ArrayList;

public class Rules extends AppCompatActivity {

    ListView listView;
    int gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules);

        // Definitions
        Button start;
        ArrayAdapter adapter;
        AppDatabase db;

        // Buttons
        start = findViewById(R.id.start);

        // ListView
        listView = (ListView) findViewById(R.id.listView);

        // ArrayList for all Game Rules
        ArrayList<String> rules = new ArrayList<>();
        rules.add("1. Eine gute Story beginnt mit \"Ich hab schon mal\".");
        rules.add("2. Aufgeschriebene Stories m\u00fcssen tats\u00e4chlich passiert sein.");
        rules.add("3. Spieler sowie Stories werden zuf\u00e4llig ausgew\u00e4hlt.");
        rules.add("4. Jeder Spieler kommt irgendwann dran.");
        rules.add("5. Gib keine direkten Tips, wenn du weißt, von wem die Story ist.");
        rules.add("6. Wer falsch r\u00e4t, muss nat\u00fcrlich trinken.");
        rules.add("7. Wird richtig geraten, dann muss der Storybesitzer einen trinken.");
        rules.add("8. Eine Aulf\u00f6sung gibts am Ende.");

        // Adapter
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rules);
        listView.setAdapter(adapter);

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

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
                        // Delete game and its players and their stories
                        //ManageGame.deleteGame(gameId);
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
