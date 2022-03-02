package com.example.ichhabschonmal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreatePlayers extends AppCompatActivity {

    private Gamer[] listOfPlayers = new Gamer[] {new Gamer(1)};       // List of all players
    private int actualPlayer = 0;
    private int minStoryNumber;
    private int maxStoryNumber;
    private int playerNumber;       // Number of players

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_players);

        Button saveAndNextStory, nextPerson, viewYourStories, next;
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

        // Database connection:
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        //muss uebergeben werdennnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn

        saveAndNextStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Add a players story
                if (listOfPlayers[actualPlayer].getCountOfStories() == maxStoryNumber) {      // Stories per player == maxStoryNumber?
                    Toast.makeText(CreatePlayers.this, "Spieler hat bereits genug Stories aufgeschrieben!",
                            Toast.LENGTH_LONG).show();
                } else if (writeStories.getText().toString().isEmpty()) {       // Text field for stories is empty
                    Toast.makeText(CreatePlayers.this, "Kein Text zum speichern!",
                            Toast.LENGTH_LONG).show();
                } else {            // Text field is okay
                    listOfPlayers[actualPlayer].addStory(writeStories.getText().toString());
                    writeStories.setText("Schreibe in dieses Feld deine n\u00e4chste Story rein.");
                    storyNumber.setText("Story " + (listOfPlayers[actualPlayer].getCountOfStories() + 1) + ":");

                    Toast.makeText(CreatePlayers.this, "Story " + listOfPlayers[actualPlayer].getCountOfStories() + " gespeichert",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        nextPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check inserting a new player
                if (listOfPlayers.length == playerNumber)
                    Toast.makeText(CreatePlayers.this, "Es k\u00f6nnen nicht weitere Spieler teilnehmen!",
                            Toast.LENGTH_LONG).show();
                else if (listOfPlayers[actualPlayer].getCountOfStories() < minStoryNumber)
                    Toast.makeText(CreatePlayers.this, "Spieler muss mindestens " + minStoryNumber + " Stories besitzen!",
                            Toast.LENGTH_LONG).show();
                else if (listOfPlayers.length > playerNumber){
                    Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!",
                            Toast.LENGTH_LONG).show();
                    // Exception has to be added hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
                } else {

                    // Set name of the last player
                    listOfPlayers[actualPlayer].setName(playerName.getText().toString());

                    // Insert a new player
                    actualPlayer++;
                    Gamer[] tmpListOfPlayers = new Gamer[listOfPlayers.length + 1];

                    for (int i = 0; i < listOfPlayers.length; i++) {//nur flache Kopiennnnnnnnnnnnnnnnnnnnnnnn
                        tmpListOfPlayers[i] = listOfPlayers[i];
                    }
                    tmpListOfPlayers[listOfPlayers.length] = new Gamer(actualPlayer + 1);       // New player with no stories inserted
                    listOfPlayers = tmpListOfPlayers;

                    Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers[actualPlayer - 1].getNumber() + " erfolgreich gespeichert",
                            Toast.LENGTH_LONG).show();

                    // Reset Text fields in new_game.xml
                    playerID.setText("Du bist Spieler " + (actualPlayer + 1) + ":");
                    playerName.setText("Dein Name");
                    storyNumber.setText("Story 1:");
                    writeStories.setText("Schreibe in dieses Feld deine Story rein.");
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean correctInput = true;//spaeter durch Exceptions bzw. Assertions austauschennnnnnnnnnnnn

                // Check, if all players meet all conditions
                //Auch durch Exceptions bzw. Assertions austauschennnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
                if (listOfPlayers.length < playerNumber)
                    Toast.makeText(CreatePlayers.this, "Zu wenig eingeloggte Spieler", Toast.LENGTH_SHORT).show();
                else if (listOfPlayers.length > playerNumber)
                    Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler", Toast.LENGTH_SHORT).show();
                else if (listOfPlayers[listOfPlayers.length - 1].getCountOfStories() < minStoryNumber)
                    Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers.length + " besitzt zu wenig Storys!", Toast.LENGTH_SHORT).show();
                else if (maxStoryNumber < listOfPlayers[listOfPlayers.length - 1].getCountOfStories())
                    Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers.length + " besitzt zu viele Storys!", Toast.LENGTH_SHORT).show();
                else {
                    // Database connection
                    GameDao gamesDao = db.gamesDao();
                    PlayerDao playerDao = db.userDao();
                    StoryDao storyDao = db.storyDao();

                    // Insert name of the game  -> next-Button of the previous intent
                    Game newGame = new Game();
                    //newGame.gameId = newGame.gameId;          // Game id is set with autoincrement
                    newGame.gameName = getIntent().getStringExtra("GameName");
                    gamesDao.insertAll(newGame);

                    // Create an Array to insert in playerDao
                    Player[] listOfNewPlayers = new Player[listOfPlayers.length];

                    // Insert all players     -> nextPlayer-Button
                    for (int i = 0; i < listOfPlayers.length; i++) {
                        //listOfNewPlayers[i].playerId = listOfNewPlayers[i].playerId;      // Player id is set with autoincrement

                        listOfNewPlayers[i] = new Player();
                        listOfNewPlayers[i].name = listOfPlayers[i].getName();
                        listOfNewPlayers[i].gameId = newGame.gameId;
                        listOfNewPlayers[i].score = 0;

                        // Create an Array to insert in storyDao
                        Story[] listOfStories = new Story[listOfPlayers[i].getCountOfStories()];

                        // Insert a player's stories
                        for (int j = 0; j < listOfPlayers[i].getCountOfStories(); j++) {
                            listOfStories[j] = new Story();
                            //listOfStories[i].storyId = listOfStories[i].storyId;        // Story id is set with autoincrement
                            listOfStories[j].content = listOfPlayers[i].getStory(j);
                            listOfStories[j].status = false;
                            listOfStories[j].playerId = listOfNewPlayers[i].playerId;
                        }

                        storyDao.insertAll(listOfStories);
                    }

                    playerDao.insertAll(listOfNewPlayers);

                    //Ist das so/auf diese Weise sinnvollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll
                    if (correctInput) {         // If database is correctly created
                        Intent rules = new Intent(CreatePlayers.this, Rules.class);
                        startActivity(rules);
                    }
                }
            }
        });

        viewYourStories.setOnClickListener(new View.OnClickListener() {//nachbearbeitennnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
            @Override
            public void onClick(View view) {
                // Use RecyclerView
            }
        });
    }
}
