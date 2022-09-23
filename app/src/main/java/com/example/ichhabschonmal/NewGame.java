package com.example.ichhabschonmal;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.online_gaming.HostOnlineGame;

import java.util.ArrayList;
import java.util.List;

public class NewGame extends AppCompatActivity {

    private Spinner drinkVariantsOne;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Definitions
        EditText gameName, playerCount, storyMinCount, storyMaxCount;
        Button nextMenu;
        Switch playMode;
        ArrayAdapter adapter;
        AppDatabase db;
        List<Game> listOfGames;

        // EditTexts
        gameName = findViewById(R.id.gameName);
        playerCount = findViewById(R.id.playerCount);
        storyMinCount = findViewById(R.id.storyMinCount);
        storyMaxCount = findViewById(R.id.storyMaxCount);

        // Buttons
        nextMenu = findViewById(R.id.nextMenu);

        // Create drop down menu for choosing a drink
        drinkVariantsOne = findViewById(R.id.drinkVariantsOne);
        ArrayList<String> drinks = new ArrayList<>();
        drinks.add("Bier");
        drinks.add("Vodka Shots");
        drinks.add("Tequila");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, drinks);
        drinkVariantsOne.setAdapter(adapter);

        // Switches
        playMode = findViewById(R.id.playMode);

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        listOfGames = db.gameDao().getAll();    // Richtig abgeschmiert, Fotos Handy

        // Close database connection
        db.close();

        nextMenu.setOnClickListener(view -> {
            String fileName, playerNumber, storyMinNumber, storyMaxNumber, drinkOfTheGame;
            fileName = gameName.getText().toString();
            playerNumber = playerCount.getText().toString();
            storyMinNumber = storyMinCount.getText().toString();
            storyMaxNumber = storyMaxCount.getText().toString();
            drinkOfTheGame = drinkVariantsOne.getSelectedItem().toString();

            if (fileName.isEmpty())            // Check only if gameName is valid, creating starts later
                Toast.makeText(NewGame.this, "Dateiname darf nicht leer sein!", Toast.LENGTH_LONG).show();
            else if (exists(fileName, listOfGames))
                Toast.makeText(NewGame.this, "Dateiname darf nicht mehrfach verwendet werden!", Toast.LENGTH_LONG).show();
            else if (playerNumber.isEmpty())
                Toast.makeText(NewGame.this, "Spielerzahlfeld darf nicht leer sein!", Toast.LENGTH_LONG).show();
            else if (playerNumber.contains("."))
                Toast.makeText(NewGame.this, "Spielerzahl darf keinen Punkt enthalten!", Toast.LENGTH_LONG).show();
            else if (storyMinNumber.isEmpty())
                Toast.makeText(this, "Mindest-Storyzahl darf nicht leer sein!", Toast.LENGTH_SHORT).show();
            else if (storyMaxNumber.isEmpty())
                Toast.makeText(this, "Maxmiale Storyzahl darf nicht leer sein!", Toast.LENGTH_SHORT).show();
            else if (storyMinNumber.contains("."))
                Toast.makeText(NewGame.this, "Mindest-Storyzahl darf keinen Punkt enthalten!", Toast.LENGTH_LONG).show();
            else if (storyMaxNumber.contains("."))
                Toast.makeText(NewGame.this, "Maximale Storyzahl darf keinen Punkt enthalten!", Toast.LENGTH_LONG).show();
            else if (playerNumber.contains("1") && playerNumber.contains("2")
                    && playerNumber.contains("3") && playerNumber.contains("4")
                    && playerNumber.contains("5") && playerNumber.contains("6")
                    && playerNumber.contains("7") && playerNumber.contains("8")
                    && playerNumber.contains("9"))
                Toast.makeText(NewGame.this, "Mindest-Storyzahl darf nur aus Zahlen bestehen!", Toast.LENGTH_LONG).show();
            else if (storyMinNumber.contains("1") && storyMinNumber.contains("2")
                    && storyMinNumber.contains("3") && storyMinNumber.contains("4")
                    && storyMinNumber.contains("5") && storyMinNumber.contains("6")
                    && storyMinNumber.contains("7") && storyMinNumber.contains("8")
                    && storyMinNumber.contains("9"))
                Toast.makeText(NewGame.this, "Maximale Storyzahl darf nur aus Zahlen bestehen!", Toast.LENGTH_LONG).show();
            else if (storyMaxNumber.contains("1") && storyMaxNumber.contains("2")
                    && storyMaxNumber.contains("3") && storyMaxNumber.contains("4")
                    && storyMaxNumber.contains("5") && storyMaxNumber.contains("6")
                    && storyMaxNumber.contains("7") && storyMaxNumber.contains("8")
                    && storyMaxNumber.contains("9"))
                Toast.makeText(NewGame.this, "Spielerzahl darf nur aus Zahlen bestehen!", Toast.LENGTH_LONG).show();
            else if (Integer.parseInt(playerNumber) < 3)
                Toast.makeText(NewGame.this, "Spielerzahl muss gr\u00f6\u00dfer als 2 sein!", Toast.LENGTH_LONG).show();
            else if (Integer.parseInt(storyMinNumber) <= 0)          // Cast is valid, because of if-cases before
                Toast.makeText(NewGame.this, "Mindest-Storyzahl muss gr\u00f6\u00dfer 0 sein!", Toast.LENGTH_LONG).show();
            else if (Integer.parseInt(storyMaxNumber) <= 0)          // Cast is valid, because of if-cases before
                Toast.makeText(NewGame.this, "Maximum-Storyzahl muss gr\u00f6\u00dfer 0 sein!", Toast.LENGTH_LONG).show();
            else if (Integer.parseInt(storyMinNumber) > Integer.parseInt(storyMaxNumber))          // Casts are valid, because of if-cases before
                Toast.makeText(NewGame.this, "Minimum-Storyzahl muss kleiner oder gleich der Maximum-Storyzahl sein!", Toast.LENGTH_LONG).show();
            else {
                Intent intent;

                if (!playMode.isChecked())        // One phone for all player, only one counter
                    intent = new Intent(getApplicationContext(), CreatePlayers.class);
                else
                    intent = new Intent(getApplicationContext(), HostOnlineGame.class);

                // Pass to next intent
                intent.putExtra("OnlineGame", playMode.isChecked());
                intent.putExtra("MinStoryNumber", Integer.parseInt(storyMinNumber));     // Give storyMinNumber
                intent.putExtra("MaxStoryNumber", Integer.parseInt(storyMaxNumber));     // Give storyMaxNumber
                intent.putExtra("PlayerNumber", Integer.parseInt(playerNumber));     // Give number of players
                intent.putExtra("GameName", gameName.getText().toString());     // Give the name of the game
                intent.putExtra("DrinkOfTheGame", drinkOfTheGame);

                startActivity(intent);
            }
        });
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        //actionBar.setDisplayHomeAsUpEnabled(true);

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

    private boolean exists(String fileName, List<Game> games) {         // Check, if file name exists
        for (int i = 0; i < games.size(); i++) {
            if (fileName.equals(games.get(i).gameName))
                return true;        // File name already exists
        }

        return false;       // File name exists not yet
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spieleinstellungen")
                .setMessage("Wenn du zur\u00fcck gehst, werden die Daten nicht gespeichert!")
                .setPositiveButton("Zur\u00fcck", (dialog, id) -> {
                    Intent mainActivity = new Intent(NewGame.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                });
        builder.create().show();
    }
}
