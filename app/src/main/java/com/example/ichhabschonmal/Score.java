package com.example.ichhabschonmal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

public class Score extends AppCompatActivity {

    private int idOfFirstPlayer;
    private int countOfPlayers;
    private int idOfFirstStory;
    private int countOfStories;

    private ScoreAdapter scoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);


        // Get from last intent
        idOfFirstPlayer = getIntent().getExtras().getInt("IdOfFirstPlayer");
        countOfPlayers = getIntent().getExtras().getInt("CountOfPlayers");
        idOfFirstStory = getIntent().getExtras().getInt("IdOfFirstStory");
        countOfStories = getIntent().getExtras().getInt("CountOfStories");

        // Definitions
        Button confirm;

        // Buttons
        confirm = findViewById(R.id.confirm);

        // Create database connection
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        int[] playerIds = new int[countOfPlayers];

        for (int i=0; i<countOfPlayers; i++) {
            playerIds[i] = idOfFirstPlayer + i;
        }
        List<Player> players = db.userDao().loadAllByPlayerIds(playerIds);

        // RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ScoreAdapter
        scoreAdapter = new ScoreAdapter(this, players);
        recyclerView.setAdapter(scoreAdapter);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go back to last intent
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {       // Catch back button
        // Go back to last intent
        finish();
    }

}
