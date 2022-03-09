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

    private ScoreAdapter scoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);

        // Get from last intent
        int gameId = getIntent().getExtras().getInt("GameId");

        // Definitions
        Button confirm;

        // Buttons
        confirm = findViewById(R.id.confirm);

        // Create database connection
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        Game game = db.gamesDao().loadAllByGameIds(new int[] {gameId}).get(0);

        // Find players
        int idOfFirstPlayer = game.idOfFirstPlayer;
        int countOfPlayers = game.countOfPlayers;
        int[] playerIds = new int[countOfPlayers];
        //int[] playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);

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
