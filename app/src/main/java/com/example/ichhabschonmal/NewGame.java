package com.example.ichhabschonmal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;

import java.util.ArrayList;
import java.util.List;

public class NewGame extends AppCompatActivity {

    private Spinner spin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);

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
        spin = findViewById(R.id.dropdown);
        ArrayList<String> drinks = new ArrayList<>();
        drinks.add("Bier");
        drinks.add("Vodka Shots");
        drinks.add("Tequila");
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, drinks);
        spin.setAdapter(adapter);

        // Switches
        playMode = findViewById(R.id.playMode);

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        listOfGames = db.gameDao().getAll();    // Richtig abgeschmiert, Fotos Handy

        // Close database connection
        db.close();

        nextMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fileName, playerNumber, storyMinNumber, storyMaxNumber, drinkOfTheGame;
                fileName = gameName.getText().toString();
                playerNumber = playerCount.getText().toString();
                storyMinNumber = storyMinCount.getText().toString();
                storyMaxNumber = storyMaxCount.getText().toString();
                drinkOfTheGame = spin.getSelectedItem().toString();

                if (fileName.isEmpty())            // Check only if gameName is valid, creating starts later
                    Toast.makeText(NewGame.this, "Dateiname darf nicht leer sein!", Toast.LENGTH_SHORT).show();
                else if (exists(fileName, listOfGames))
                    Toast.makeText(NewGame.this, "Dateiname darf nicht mehrfach verwendet werden!", Toast.LENGTH_SHORT).show();
                else if (playerNumber.isEmpty())
                        Toast.makeText(NewGame.this, "Spielerzahlfeld darf nicht leer sein!", Toast.LENGTH_SHORT).show();
                else if (playerNumber.indexOf(".") != -1)
                    Toast.makeText(NewGame.this, "Spielerzahl darf keinen Punkt enthalten!", Toast.LENGTH_SHORT).show();
                else if (storyMinNumber.indexOf(".") != -1)
                    Toast.makeText(NewGame.this, "Mindest-Storyzahl darf keinen Punkt enthalten!", Toast.LENGTH_SHORT).show();
                else if (storyMaxNumber.indexOf(".") != -1)
                    Toast.makeText(NewGame.this, "Maximale Storyzahl darf keinen Punkt enthalten!", Toast.LENGTH_SHORT).show();
                else if (playerNumber.indexOf("1") != -1 && playerNumber.indexOf("2") != -1
                        && playerNumber.indexOf("3") != -1 && playerNumber.indexOf("4") != -1
                        && playerNumber.indexOf("5") != -1 && playerNumber.indexOf("6") != -1
                        && playerNumber.indexOf("7") != -1 && playerNumber.indexOf("8") != -1
                        && playerNumber.indexOf("9") != -1)
                    Toast.makeText(NewGame.this, "Mindest-Storyzahl darf nur aus Zahlen bestehen!", Toast.LENGTH_SHORT).show();
                else if (storyMinNumber.indexOf("1") != -1 && storyMinNumber.indexOf("2") != -1
                        && storyMinNumber.indexOf("3") != -1 && storyMinNumber.indexOf("4") != -1
                        && storyMinNumber.indexOf("5") != -1 && storyMinNumber.indexOf("6") != -1
                        && storyMinNumber.indexOf("7") != -1 && storyMinNumber.indexOf("8") != -1
                        && storyMinNumber.indexOf("9") != -1)
                    Toast.makeText(NewGame.this, "Maximale Storyzahl darf nur aus Zahlen bestehen!", Toast.LENGTH_SHORT).show();
                else if (storyMaxNumber.indexOf("1") != -1 && storyMaxNumber.indexOf("2") != -1
                        && storyMaxNumber.indexOf("3") != -1 && storyMaxNumber.indexOf("4") != -1
                        && storyMaxNumber.indexOf("5") != -1 && storyMaxNumber.indexOf("6") != -1
                        && storyMaxNumber.indexOf("7") != -1 && storyMaxNumber.indexOf("8") != -1
                        && storyMaxNumber.indexOf("9") != -1)
                    Toast.makeText(NewGame.this, "Spielerzahl darf nur aus Zahlen bestehen!", Toast.LENGTH_SHORT).show();
                else if (Integer.parseInt(playerNumber) < 3)
                    Toast.makeText(NewGame.this, "Spielerzahl muss gr\u00f6\u00dfer als 2 sein!", Toast.LENGTH_SHORT).show();
                else if (Integer.parseInt(storyMinNumber) <= 0)          // Cast is valid, because of if-cases before
                    Toast.makeText(NewGame.this, "Mindest-Storyzahl muss gr\u00f6\u00dfer 0 sein!", Toast.LENGTH_SHORT).show();
                else if (Integer.parseInt(storyMaxNumber) <= 0)          // Cast is valid, because of if-cases before
                    Toast.makeText(NewGame.this, "Maximum-Storyzahl muss gr\u00f6\u00dfer 0 sein!", Toast.LENGTH_SHORT).show();
                else if (Integer.parseInt(storyMinNumber) > Integer.parseInt(storyMaxNumber))          // Casts are valid, because of if-cases before
                    Toast.makeText(NewGame.this, "Minimum-Storyzahl muss kleiner oder gleich der Maximum-Storyzahl sein!", Toast.LENGTH_SHORT).show();
                else {
                     if (!playMode.isChecked()) {       // One phone for all player, only one counter
                         Intent newGameIntent = new Intent(getApplicationContext(), CreatePlayers.class);
                         newGameIntent.putExtra("MinStoryNumber", Integer.parseInt(storyMinNumber));     // Give storyMinNumber
                         newGameIntent.putExtra("MaxStoryNumber", Integer.parseInt(storyMaxNumber));     // Give storyMaxNumber
                         newGameIntent.putExtra("playerNumber", Integer.parseInt(playerNumber));     // Give number of players
                         newGameIntent.putExtra("GameName", gameName.getText().toString());     // Give the name of the game
                         newGameIntent.putExtra("DrinkOfTheGame", drinkOfTheGame);
                         startActivity(newGameIntent);
                     } /*else {
                        Intent newGameMultipleDevicesIntent = new Intent(getApplicationContext(), NewGameMultipleDevices.class);
                        startActivity(newGameMultipleDevicesIntent);
                    }*/
                }
            }
        });
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
