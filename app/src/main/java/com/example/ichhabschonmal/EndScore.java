package com.example.ichhabschonmal;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.ichhabschonmal.adapter.EndScoreAdapter;
import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.database.Player;

import java.util.List;

public class EndScore extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_score);

        // Definitions
        RecyclerView recyclerView;
        EndScoreAdapter endScoreAdapter;
        AppDatabase db;
        List<Player> players;
        int[] playerIds;
        int gameId, idOfFirstPlayer, countOfPlayers, idOfFirstStory, countOfStories;

        // Get from last intent
        gameId = getIntent().getExtras().getInt("GameId");

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        Game game = db.gamesDao().loadAllByGameIds(new int[] {gameId}).get(0);

        // Used variables
        idOfFirstPlayer = game.idOfFirstPlayer;
        countOfPlayers = game.countOfPlayers;
        idOfFirstStory = game.idOfFirstStory;
        countOfStories = game.countOfStories;
        playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        players = db.userDao().loadAllByPlayerIds(playerIds);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // EndScoreAdapter
        endScoreAdapter = new EndScoreAdapter(this, players);
        recyclerView.setAdapter(endScoreAdapter);
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spiel beenden")
                .setMessage("Das Spiel wird gel\u00f6scht")
                .setPositiveButton("Verlassen und l\u00f6schen", new DialogInterface.OnClickListener() {
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
