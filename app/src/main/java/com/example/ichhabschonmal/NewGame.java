package com.example.ichhabschonmal;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;

import java.util.List;

public class NewGame extends AppCompatActivity {

    private AppDatabase db;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);

        // Definitions
        EditText gameName, playerCount, storyMinCount, storyMaxCount;
        Button nextMenu;
        Switch playMode;
        List<Game> listOfGames;

        // EditTexts
        gameName = findViewById(R.id.gameName);
        playerCount = findViewById(R.id.playerCount);
        storyMinCount = findViewById(R.id.storyMinCount);
        storyMaxCount = findViewById(R.id.storyMaxCount);

        // Buttons
        nextMenu = findViewById(R.id.nextMenu);

        // Switches
        playMode = findViewById(R.id.playMode);

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        listOfGames = db.gameDao().getAll();////////////////////////////

        // Close database connection
        db.close();

        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);


        nextMenu.setOnClickListener(view -> {
            String fileName, playerNumber, storyMinNumber, storyMaxNumber;
            fileName = gameName.getText().toString();
            playerNumber = playerCount.getText().toString();
            storyMinNumber = storyMinCount.getText().toString();
            storyMaxNumber = storyMaxCount.getText().toString();

            if (fileName.isEmpty())            // Check only if gameName is valid, creating starts later
                Toast.makeText(NewGame.this, "Dateiname darf nicht leer sein!", Toast.LENGTH_SHORT).show();
            else if (exists(fileName, listOfGames))
                Toast.makeText(NewGame.this, "Dateiname darf nicht mehrfach verwendet werden!", Toast.LENGTH_SHORT).show();
            else if (playerNumber.isEmpty())
                    Toast.makeText(NewGame.this, "Spielerzahlfeld darf nicht leer sein!", Toast.LENGTH_SHORT).show();
            else if (playerNumber.contains("."))
                Toast.makeText(NewGame.this, "Spielerzahl darf keinen Punkt enthalten!", Toast.LENGTH_SHORT).show();
            else if (storyMinNumber.contains("."))
                Toast.makeText(NewGame.this, "Mindest-Storyzahl darf keinen Punkt enthalten!", Toast.LENGTH_SHORT).show();
            else if (storyMaxNumber.contains("."))
                Toast.makeText(NewGame.this, "Maximale Storyzahl darf keinen Punkt enthalten!", Toast.LENGTH_SHORT).show();
            else if (playerNumber.contains("1") && playerNumber.contains("2")
                    && playerNumber.contains("3") && playerNumber.contains("4")
                    && playerNumber.contains("5") && playerNumber.contains("6")
                    && playerNumber.contains("7") && playerNumber.contains("8")
                    && playerNumber.contains("9"))
                Toast.makeText(NewGame.this, "Mindest-Storyzahl darf nur aus Zahlen bestehen!", Toast.LENGTH_SHORT).show();
            else if (storyMinNumber.contains("1") && storyMinNumber.contains("2")
                    && storyMinNumber.contains("3") && storyMinNumber.contains("4")
                    && storyMinNumber.contains("5") && storyMinNumber.contains("6")
                    && storyMinNumber.contains("7") && storyMinNumber.contains("8")
                    && storyMinNumber.contains("9"))
                Toast.makeText(NewGame.this, "Maximale Storyzahl darf nur aus Zahlen bestehen!", Toast.LENGTH_SHORT).show();
            else if (storyMaxNumber.contains("1") && storyMaxNumber.contains("2")
                    && storyMaxNumber.contains("3") && storyMaxNumber.contains("4")
                    && storyMaxNumber.contains("5") && storyMaxNumber.contains("6")
                    && storyMaxNumber.contains("7") && storyMaxNumber.contains("8")
                    && storyMaxNumber.contains("9"))
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
                     startActivity(newGameIntent);
                     finish();
                 } /*else {
                    Intent newGameMultipleDevicesIntent = new Intent(getApplicationContext(), NewGameMultipleDevices.class);
                    startActivity(newGameMultipleDevicesIntent);
                }*/
            }
        });
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

    private boolean exists(String fileName, List<Game> games) {         // Check, if file name exists
        for (int i = 0; i < games.size(); i++) {
            if (fileName.equals(games.get(i).gameName))
                return true;        // File name already exists
        }

        return false;       // File name exists not yet
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
}
