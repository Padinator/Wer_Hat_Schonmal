package com.example.ichhabschonmal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;

public class NewGame extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_game);

        EditText gameName, playerCount, storyMinCount, storyMaxCount;
        Button nextMenu;
        Switch playMode;

        gameName = findViewById(R.id.gameName);
        playerCount = findViewById(R.id.playerCount);
        storyMinCount = findViewById(R.id.storyMinCount);
        storyMaxCount = findViewById(R.id.storyMaxCount);

        nextMenu = findViewById(R.id.nextMenu);

        playMode = findViewById(R.id.playMode);

        nextMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File directory = new File(getApplicationContext().getFilesDir().toString() + "/" + gameName.getText().toString());
                String playerNumber = playerCount.getText().toString();
                String storyMinNumber = storyMinCount.getText().toString();
                String storyMaxNumber = storyMaxCount.getText().toString();

                if (gameName.getText().toString().isEmpty())            // Check only if gameName is valid, creating starts later
                    Toast.makeText(NewGame.this, "Dateiname darf nicht leer sein!", Toast.LENGTH_SHORT).show();
                else if (directory.exists())
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
                     if (!playMode.isChecked()) {       // One phone for all player, only one counter:
                         Intent newGameIntent = new Intent(getApplicationContext(), CreatePlayers.class);
                         newGameIntent.putExtra("MinStoryNumber", Integer.parseInt(storyMinNumber));     // Give storyMinNumber
                         newGameIntent.putExtra("MaxStoryNumber", Integer.parseInt(storyMaxNumber));     // Give storyMaxNumber
                         newGameIntent.putExtra("playerNumber", Integer.parseInt(playerNumber));     // Give number of players
                         //newGame.putExtra("directory", directory);       // Give directory
                         newGameIntent.putExtra("GameName", gameName.getText().toString());     // Give the name of the game
                         startActivity(newGameIntent);
                     } /*else {
                        Intent newGameMultipleDevicesIntent = new Intent(getApplicationContext(), NewGameMultipleDevices.class);
                        startActivity(newGameMultipleDevicesIntent);
                    }*/
                }
            }
        });
    }
}
