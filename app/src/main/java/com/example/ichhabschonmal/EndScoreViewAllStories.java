package com.example.ichhabschonmal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.ichhabschonmal.adapter.EndScoreAdapter;
import com.example.ichhabschonmal.adapter.EndScoreViewAllStoriesAdapter;
import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.database.Player;
import com.example.ichhabschonmal.database.Story;
import com.example.ichhabschonmal.database.StoryDao_Impl;

import java.util.List;

public class EndScoreViewAllStories extends AppCompatActivity {

    private int gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_score_view_all_stories);

        // Definitions
        RecyclerView recyclerView;
        EndScoreViewAllStoriesAdapter endScoreViewAllStoriesAdapter;
        AppDatabase db;
        List<Player> players;
        List<Story> stories;
        int[] playerIds, storyIds;
        int idOfFirstPlayer, countOfPlayers, idOfFirstStory, countOfStories;

        // Get from last intent
        gameId = getIntent().getExtras().getInt("GameId");

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        Game game = db.gameDao().loadAllByGameIds(new int[] {gameId}).get(0);

        // Used variables
        idOfFirstPlayer = game.idOfFirstPlayer;
        countOfPlayers = game.countOfPlayers;
        idOfFirstStory = game.idOfFirstStory;
        countOfStories = game.countOfStories;
        playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        storyIds = PlayGame.findSomethingOfActualGame(idOfFirstStory, countOfStories);

        players = db.playerDao().loadAllByPlayerIds(playerIds);
        stories = db.storyDao().loadAllByStoryIds(storyIds);

        // recyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // endScoreViewAllStoriesAdapter
        endScoreViewAllStoriesAdapter = new EndScoreViewAllStoriesAdapter(EndScoreViewAllStories.this, players, stories);
        recyclerView.setAdapter(endScoreViewAllStoriesAdapter);
        // EndScoreViewAllStories.this.activi


    }
}
