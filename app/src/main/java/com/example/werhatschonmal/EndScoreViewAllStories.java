package com.example.werhatschonmal;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.werhatschonmal.adapter.EndScoreViewAllStoriesAdapter;
import com.example.werhatschonmal.database.AppDatabase;
import com.example.werhatschonmal.database.Game;
import com.example.werhatschonmal.database.Player;
import com.example.werhatschonmal.database.Story;

import java.util.List;

public class EndScoreViewAllStories extends AppCompatActivity {

    private int gameId;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.end_score_view_all_stories);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Definitions
        RecyclerView recyclerView;
        EndScoreViewAllStoriesAdapter endScoreViewAllStoriesAdapter;
        AppDatabase db;
        List<Player> listOfPlayers;
        List<Story> listOfStories;
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

        listOfPlayers = db.playerDao().loadAllByPlayerIds(playerIds);
        listOfStories = db.storyDao().loadAllByStoryIds(storyIds);
        listOfStories.removeIf(story -> !story.status); // Remove unused stories

        // recyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // endScoreViewAllStoriesAdapter
        endScoreViewAllStoriesAdapter = new EndScoreViewAllStoriesAdapter(EndScoreViewAllStories.this, listOfPlayers, listOfStories, EndScoreViewAllStories.this);
        recyclerView.setAdapter(endScoreViewAllStoriesAdapter);

        // calling the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#000000\">" + "Alle Stories ansehen" + "</font>")));

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
    public void onBackPressed() {
        super.onBackPressed();
    }
}
