package com.example.ichhabschonmal;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

public class EndScore extends AppCompatActivity {

    private EndScoreAdapter endScoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_score);

        // Get from last intent
        int gameId = getIntent().getExtras().getInt("GameId");

        // Create database connection
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        Game game = db.gamesDao().loadAllByGameIds(new int[] {gameId}).get(0);

        int idOfFirstPlayer = game.idOfFirstPlayer;
        int countOfPlayers = game.countOfPlayers;
        int idOfFirstStory = game.idOfFirstStory;
        int countOfStories = game.countOfStories;

        int[] playerIds = new int[countOfPlayers];

        for (int i = 0; i < countOfPlayers; i++) {
            playerIds[i] = idOfFirstPlayer + i;
        }
        //int[] playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        List<Player> players = db.userDao().loadAllByPlayerIds(playerIds);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        endScoreAdapter = new EndScoreAdapter(this, players);
        recyclerView.setAdapter(endScoreAdapter);



    }
}
