package com.example.werhatschonmal;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.ichhabschonmal.R;
import com.example.werhatschonmal.adapter.EndScoreAdapter;
import com.example.werhatschonmal.database.AppDatabase;
import com.example.werhatschonmal.database.Game;
import com.example.werhatschonmal.database.Player;

import java.util.List;

public class EndScore extends AppCompatActivity {

    private int gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_score);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Definitions
        RecyclerView recyclerView;
        EndScoreAdapter endScoreAdapter;
        Button exitGame, viewAllStories;
        AppDatabase db;
        List<Player> players;
        int[] playerIds;
        int idOfFirstPlayer, countOfPlayers;

        // Buttons
        exitGame = findViewById(R.id.exitGame);
        viewAllStories = findViewById(R.id.viewAllStories);

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
        viewAllStories.setOnClickListener(view -> onViewAllStories());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spiel beenden")
                .setMessage("Das Spiel wird verlassen")
                .setPositiveButton("Verlassen", (dialog, which) -> {
                    Intent mainActivity = new Intent(EndScore.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                });

        builder.create().show();
    }

    public void onViewAllStories() {
        Intent endScoreViewAllStories = new Intent(getApplicationContext(), EndScoreViewAllStories.class);
        endScoreViewAllStories.putExtra("GameId", gameId);
        startActivity(endScoreViewAllStories);
    }
}
