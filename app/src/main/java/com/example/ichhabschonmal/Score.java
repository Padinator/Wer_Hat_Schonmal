package com.example.ichhabschonmal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.ichhabschonmal.adapter.ScoreAdapter;
import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.database.Player;

import java.util.List;

public class Score extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);

        // Definitions
        RecyclerView recyclerView;
        ScoreAdapter scoreAdapter;
        Button confirm;
        AppDatabase db;
        List<Player> players;
        Game game;
        int[] playerIds;
        int gameId, idOfFirstPlayer, countOfPlayers;

        // Buttons
        confirm = findViewById(R.id.confirm);

        // Get from last intent
        gameId = getIntent().getExtras().getInt("GameId");

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        game = db.gamesDao().loadAllByGameIds(new int[] {gameId}).get(0);

        // Find players
        idOfFirstPlayer = game.idOfFirstPlayer;
        countOfPlayers = game.countOfPlayers;
        playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);

        players = db.userDao().loadAllByPlayerIds(playerIds);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
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
