package de.android.werhatschonmal;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

import de.android.werhatschonmal.adapter.ScoreAdapter;
import de.android.werhatschonmal.database.AppDatabase;
import de.android.werhatschonmal.database.Game;
import de.android.werhatschonmal.database.Player;
import de.android.werhatschonmal.server_client_communication.ClientServerHandler;
import de.android.werhatschonmal.server_client_communication.SocketEndPoint;

public class Score extends AppCompatActivity {

    boolean onlineGame = false, serverSide = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

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
        game = db.gameDao().loadAllByGameIds(new int[]{gameId}).get(0);////////////////////////

        // Set used variables
        onlineGame = game.onlineGame;
        serverSide = game.serverSide;

        // Find players
        idOfFirstPlayer = game.idOfFirstPlayer;
        countOfPlayers = game.countOfPlayers;
        playerIds = PlayGame.findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        players = db.playerDao().loadAllByPlayerIds(playerIds);

        // Close database connection
        db.close();

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // ScoreAdapter
        scoreAdapter = new ScoreAdapter(this, players);
        recyclerView.setAdapter(scoreAdapter);

        confirm.setOnClickListener(view -> onBackPressed());

        // calling the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#8ab6de\">" + "Punktestand" + "</font>")));
    }

    @Override
    public void onBackPressed() { // Catch back button

        // Terminate waiting status of PlayGame
        if (!onlineGame || serverSide) // Offline game or online game (serverside)
            PlayGame.releasePlayGame();
        else // Online game (clientside)
            ClientServerHandler.getClientEndPoint().sendMessage(SocketEndPoint.VIEWED_ACTUAL_SCORE);

        // Go back to last intent
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
