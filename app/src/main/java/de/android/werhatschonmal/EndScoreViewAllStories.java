package de.android.werhatschonmal;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

import de.android.werhatschonmal.adapter.EndScoreViewAllStoriesAdapter;
import de.android.werhatschonmal.database.AppDatabase;
import de.android.werhatschonmal.database.Game;
import de.android.werhatschonmal.database.Player;
import de.android.werhatschonmal.database.Story;

public class EndScoreViewAllStories extends AppCompatActivity {

    private int gameId;

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
        Log.e("idOfFirstStory", "" + idOfFirstStory);
        Log.e("countOfStories", "" + countOfStories);
        playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        storyIds = PlayGame.findSomethingOfActualGame(idOfFirstStory, countOfStories);

        listOfPlayers = db.playerDao().loadAllByPlayerIds(playerIds);
        listOfStories = db.storyDao().loadAllByStoryIds(storyIds);

        for (Story story: listOfStories)
            Log.e("listofstories", story.content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // Request version for sorting
            listOfStories.removeIf(story -> !story.status); // Remove unused stories
        }

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
