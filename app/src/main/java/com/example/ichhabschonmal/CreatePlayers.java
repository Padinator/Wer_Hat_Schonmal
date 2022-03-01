package com.example.ichhabschonmal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreatePlayers extends AppCompatActivity {

    private Player[] players = new Player[] {new Player(1)};       // List of all players
    private int actualPlayer = 0;       // Player zero is the first player
    private int minStoryNumber;
    private int maxStoryNumber;
    private int playerNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_players);

        Button saveAndNextStory, nextPerson, rules, start, viewYourStories, next;
        EditText writeStories, playerName;
        TextView playerID, storyNumber;

        // Buttons:
        saveAndNextStory = findViewById(R.id.saveAndNextStory);
        nextPerson = findViewById(R.id.nextPerson);
        next = findViewById(R.id.next);
        viewYourStories = findViewById(R.id.viewYourStories);//nachbearbeitennnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn

        // EditTexts:
        writeStories = findViewById(R.id.writeStories);
        playerName = findViewById(R.id.playerName);

        //TextViews:
        playerID = findViewById(R.id.playerID);
        storyNumber = findViewById(R.id.storyNumber);

        // Set minimum and maximum of story per player
        if (getIntent().hasExtra("MinStoryNumber"))
            minStoryNumber = getIntent().getExtras().getInt("MinStoryNumber");
        else
            minStoryNumber = 3;
        if (getIntent().hasExtra("MaxStoryNumber"))
            maxStoryNumber = getIntent().getExtras().getInt("MaxStoryNumber");
        else
            maxStoryNumber = 5;

        // Set number of players
        if (getIntent().hasExtra("playerNumber"))
             playerNumber = getIntent().getExtras().getInt("playerNumber");
        else
            playerNumber = 5;

        saveAndNextStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Add a players story
                if (players[actualPlayer].getCountOfStories() == maxStoryNumber) {      // Stories per player == maxStoryNumber?
                    Toast.makeText(CreatePlayers.this, "Spieler hat bereits genug Stories aufgeschrieben!",
                            Toast.LENGTH_LONG).show();
                } else if (writeStories.getText().toString().isEmpty()) {       // Text field for stories is empty
                    Toast.makeText(CreatePlayers.this, "Kein Text zum speichern!",
                            Toast.LENGTH_LONG).show();
                } else {            // Text field is okay
                    players[actualPlayer].addStory(writeStories.getText().toString());
                    writeStories.setText("Schreibe in dieses Feld deine n\u00e4chste Story rein.");
                    storyNumber.setText("Story " + (players[actualPlayer].getCountOfStories() + 1) + ":");

                    Toast.makeText(CreatePlayers.this, "Story " + players[actualPlayer].getCountOfStories() + " gespeichert",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        nextPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check inserting a new player
                if (players.length == playerNumber)
                    Toast.makeText(CreatePlayers.this, "Es k\u00f6nnen nicht weitere Spieler teilnehmen!",
                            Toast.LENGTH_LONG).show();
                else if (players[actualPlayer].getCountOfStories() < minStoryNumber)
                    Toast.makeText(CreatePlayers.this, "Spieler muss mindestens " + minStoryNumber + " Stories besitzen!",
                            Toast.LENGTH_LONG).show();
                else if (players.length > playerNumber){
                    Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!",
                            Toast.LENGTH_LONG).show();
                    // Exception has to be added hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
                } else {

                    // Set name of the last player
                    players[actualPlayer].setName(playerName.getText().toString());

                    // Insert a new player
                    actualPlayer++;
                    Player[] tmpPlayerNumbers = new Player[players.length + 1];

                    for (int i = 0; i < players.length; i++) {      //nur flache Kopiennnnnnnnnnnnnnnnnnnnnnnn
                        tmpPlayerNumbers[i] = players[i];
                    }
                    tmpPlayerNumbers[players.length] = new Player(actualPlayer + 1);         // New player with no stories inserted
                    players = tmpPlayerNumbers;

                    Toast.makeText(CreatePlayers.this, "Spieler " + players[actualPlayer - 1].getNumber() + " erfolgreich gespeichert",
                            Toast.LENGTH_LONG).show();

                    // Reset Text fields in new_game.xml
                    playerID.setText("Du bist Spieler " + (actualPlayer + 1) + ":");
                    playerName.setText("Dein Name");
                    storyNumber.setText("Story 1:");
                    writeStories.setText("Schreibe in dieses Feld deine Story rein.");
                }
            }
        });

        /*
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correctInput = true;//spaeter durch Exceptions bzw. Assertions austauschennnnnnnnnnnnn

                // Check, if all players meet all conditions
                //Auch durch Exceptions bzw. Assertions austauschennnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
                if (players.length < playerNumber)
                    Toast.makeText(CreatePlayers.this, "Zu wenig eingeloggte Spieler", Toast.LENGTH_SHORT).show();
                else if (players.length > playerNumber)
                    Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler", Toast.LENGTH_SHORT).show();
                else if (players[players.length - 1].getCountOfStories() < minStoryNumber)
                    Toast.makeText(CreatePlayers.this, "Spieler " + players.length + " besitzt zu wenig Storys!", Toast.LENGTH_SHORT).show();
                else if (maxStoryNumber < players[players.length - 1].getCountOfStories())
                    Toast.makeText(CreatePlayers.this, "Spieler " + players.length + " besitzt zu viele Storys!", Toast.LENGTH_SHORT).show();
                else {

                    // directory = game name directory
                    File directory = (File) getIntent().getExtras().get("directory");
                    Toast.makeText(CreatePlayers.this, directory.toString(), Toast.LENGTH_SHORT).show();

                    if (directory.mkdirs()) {

                        // Create game with all players' stories
                        for (int i = 0; i < players.length; i++) {
                            File playersDir = new File(directory.toString() + "/" + "Spieler" + players[i].getNumber());
                                // playersDir = "game name directory"/"player's name"


                            if (playersDir.mkdir()) {
                                for (int j = 0; j < players[i].getCountOfStories(); j++) {
                                    try {       // Create file and write data
                                        File playersFileDir = new File(playersDir  players[i].getName() + "_" + "story" + j);

                                        //playersFileDir.createNewFile();         // playersFileDir = location to save files in playersDir

                                        FileOutputStream fos = new FileOutputStream(playersFileDir);
                                        fos.write(players[i].getStory(j).getBytes());
                                        fos.close();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                // Go to class PlayLocalGame
                                Intent startLocalGameIntent = new Intent(getApplicationContext(), PlayGame.class);

                                // Give players and their stories, do not change content of files
                                startLocalGameIntent.putExtra("countOfPlayers", players.length);
                                    // Give count of players
                                startLocalGameIntent.putExtra("directory", directory);
                                    // Give directory

                                startActivity(startLocalGameIntent);

                            } else {
                                Toast.makeText(CreatePlayers.this, "Spielerverzeichnis " +
                                        "konnte nicht erstellt werden", Toast.LENGTH_SHORT).show();
                                correctInput = false;
                            }
                        }

                    } else {
                        Toast.makeText(CreatePlayers.this, "Spielverzeichnis konnte nicht " +
                                "erstellt werden", Toast.LENGTH_SHORT).show();
                        correctInput = false;
                    }
                }
            }
        });
        */

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent rules = new Intent(CreatePlayers.this, Rules.class);
                startActivity(rules);
            }
        });

        viewYourStories.setOnClickListener(new View.OnClickListener() {//nachbearbeitennnnnnnnnnnnnnnnnnnnnnnnnnnnn
            @Override
            public void onClick(View view) {
                // Use RecyclerView
            }
        });
    }
}
