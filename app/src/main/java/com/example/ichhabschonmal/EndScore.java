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

    private int idOfFirstPlayer;
    private int countOfPlayers;
    private int idOfFirstStory;
    private int countOfStories;

    private EndScoreAdapter endScoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_score);

        // Create database connection
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        idOfFirstPlayer = getIntent().getExtras().getInt("IdOfFirstPlayer");
        countOfPlayers = getIntent().getExtras().getInt("CountOfPlayers");
        idOfFirstStory = getIntent().getExtras().getInt("IdOfFirstStory");
        countOfStories = getIntent().getExtras().getInt("CountOfStories");

        int[] playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        List<Player> players = db.userDao().loadAllByPlayerIds(playerIds);



        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        endScoreAdapter = new EndScoreAdapter(this, players);
        recyclerView.setAdapter(endScoreAdapter);



    }
}
