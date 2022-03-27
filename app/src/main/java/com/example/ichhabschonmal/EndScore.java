package com.example.ichhabschonmal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
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

    private ImageView img;
    private int gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_score);


        // Definitions
        RecyclerView recyclerView;
        EndScoreAdapter endScoreAdapter;
        Button exitGame;
        AppDatabase db;
        List<Player> players;
        int[] playerIds;
        int idOfFirstPlayer, countOfPlayers;

        // Buttons
        exitGame = findViewById(R.id.exitGame);

        // Get from last intent
        gameId = getIntent().getExtras().getInt("GameId");

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        Game game = db.gameDao().loadAllByGameIds(new int[] {gameId}).get(0);

        // Used variables
        idOfFirstPlayer = game.idOfFirstPlayer;
        countOfPlayers = game.countOfPlayers;
        playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        players = db.playerDao().loadAllByPlayerIds(playerIds);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // EndScoreAdapter
        endScoreAdapter = new EndScoreAdapter(this, players, gameId);
        recyclerView.setAdapter(endScoreAdapter);

        exitGame.setOnClickListener(view -> onBackPressed());

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spiel beenden")
                .setMessage("Das Spiel wird gel\u00f6scht")
                .setPositiveButton("Verlassen und l\u00f6schen", (dialog, which) -> {
                    Intent mainActivity = new Intent(EndScore.this, MainActivity.class);
                    AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

                    // Delete game and its players and their stories
                    db.gameDao().delete(db.gameDao().loadAllByGameIds(new int[] {gameId}).get(0));

                    // Close database connection
                    db.close();

                    startActivity(mainActivity);
                    finish();
                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                });

        builder.create().show();
    }
}
