package de.android.werhatschonmal;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.IOException;
import java.util.ArrayList;

import de.android.werhatschonmal.server_client_communication.ClientServerHandler;

public class Rules extends AppCompatActivity {

    private int gameId;
    private boolean gameIsLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Definitions
        ListView listView;
        Button start;
        ArrayAdapter<String> adapter;

        // Buttons
        start = findViewById(R.id.start);

        // ListView
        listView = findViewById(R.id.listView);

        // ArrayList for all Game Rules
        ArrayList<String> rules = new ArrayList<>();
        rules.add("1. Eine gute Story beginnt mit \"Ich hab schon mal\".");
        rules.add("2. Aufgeschriebene Stories m\u00fcssen tats\u00e4chlich passiert sein.");
        rules.add("3. Spieler sowie Stories werden zuf\u00e4llig ausgew\u00e4hlt.");
        rules.add("4. Jeder Spieler kommt irgendwann dran.");
        rules.add("5. Gib keine direkten Tipps, wenn du weißt, von wem die Story ist.");
        rules.add("6. Wer falsch r\u00e4t, muss nat\u00fcrlich trinken.");
        rules.add("7. Wird richtig geraten, dann muss der Storybesitzer einen trinken.");
        rules.add("8. Eine Aulf\u00f6sung gibts am Encom.example.");

        // Adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, rules);
        listView.setAdapter(adapter);

        // Get from last intent
        gameIsLoaded = getIntent().getExtras().getBoolean("GameIsLoaded");

        // Set visibility of starting a game
        if (gameIsLoaded) {
            start.setVisibility(View.VISIBLE);

            // Get from last intent
            gameId = getIntent().getExtras().getInt("GameId");
        }

        start.setOnClickListener(v -> {
            Intent startGame = new Intent(Rules.this, PlayGame.class);

            startGame.putExtra("GameId", gameId);
            startGame.putExtra("GameIsLoaded", gameIsLoaded);
            startActivity(startGame);
            finish();
        });

        // calling the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        getSupportActionBar().setTitle((Html.fromHtml("<big><font color=\"#000000\">" + "Wer hat schonmal..." + "</font></big>")));

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
        if (gameIsLoaded) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Spiel verlassen")
                    .setMessage("Wenn du zur\u00fcck gehst, wird das Spiel gespeichert und beendet!")
                    .setPositiveButton("Zur\u00fcck", (dialog, which) -> {
                        Intent mainActivity = new Intent(Rules.this, MainActivity.class);

                        // Disconnect from online services
                        try {
                            if (ClientServerHandler.getServerEndPoint() != null) {
                                ClientServerHandler.getServerEndPoint().disconnectClientsFromServer(); // Disconnect all clients from serve, serverside
                                ClientServerHandler.getServerEndPoint().disconnectServerSocket(); // Disconnect socket of server
                            } else if (ClientServerHandler.getClientEndPoint() != null)
                                ClientServerHandler.getClientEndPoint().disconnectClient(); // Disconnect client from server, clientside
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Go to next intent
                        startActivity(mainActivity);
                        finish();
                    })
                    .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                    });

            builder.create().show();
        } else
            finish();
    }
}
