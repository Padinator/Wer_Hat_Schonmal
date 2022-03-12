package com.example.ichhabschonmal;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.database.GameDao;
import com.example.ichhabschonmal.database.Player;
import com.example.ichhabschonmal.database.PlayerDao;
import com.example.ichhabschonmal.database.Story;
import com.example.ichhabschonmal.database.StoryDao;

public class CreatePlayers extends AppCompatActivity {

    private Gamer[] listOfPlayers = new Gamer[]{new Gamer(1)};       // List of all players
    private int actualPlayer = 0;
    private int minStoryNumber;
    private int maxStoryNumber;
    private int playerNumber;       // Number of players
    private int idOfFirstPlayer = -1, countOfPlayers = 0, idOfFirstStory = -1, countOfStories = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_players);

        // Definitions
        Button saveAndNextStory, nextPerson, viewYourStories, next;
        EditText writeStories, playerName;
        TextView playerID, storyNumber;
        AppDatabase db;

        // Buttons:
        saveAndNextStory = findViewById(R.id.saveAndNextStory);
        nextPerson = findViewById(R.id.nextPerson);
        next = findViewById(R.id.next);
        viewYourStories = findViewById(R.id.viewYourStories);

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
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        viewYourStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewStories = new Intent(getApplicationContext(), ViewAllStories.class);
                startActivity(viewStories);
            }
        });


        saveAndNextStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Add a players story
                if (listOfPlayers[actualPlayer].getCountOfStories() == maxStoryNumber) {      // Stories per player == maxStoryNumber?
                    Toast.makeText(CreatePlayers.this, "Spieler hat bereits genug Stories " +
                            "aufgeschrieben!", Toast.LENGTH_LONG).show();
                } else if (writeStories.getText().toString().isEmpty()) {       // Text field for stories is empty
                    Toast.makeText(CreatePlayers.this, "Kein Text zum speichern!",
                            Toast.LENGTH_LONG).show();
                } else if (writeStories.getText().toString().length() < 15) {
                    Toast.makeText(CreatePlayers.this, "Story muss aus mindestens 15 zeichen " +
                            "bestehen.", Toast.LENGTH_SHORT).show();
                } else {            // Text field is okay
                    listOfPlayers[actualPlayer].addStory(writeStories.getText().toString());
                    writeStories.setText("Ich hab schonmal ");
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
                else if (listOfPlayers.length > playerNumber) {
                    Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!",
                            Toast.LENGTH_LONG).show();
                    // Exception has to be added hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
                } else if (playerName.getText().toString().isEmpty()) {
                    Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein", Toast.LENGTH_SHORT).show();
                } else if (playerName.getText().toString().length() < 5)
                    Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 5 Zeichen bestehen", Toast.LENGTH_SHORT).show();
                else {

                    // Set name of a player
                    listOfPlayers[actualPlayer].setName(playerName.getText().toString());

                    Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers[actualPlayer].getNumber() + " erfolgreich gespeichert",
                            Toast.LENGTH_LONG).show();

                    // Insert a new player
                    actualPlayer++;
                    Gamer[] tmpListOfPlayers = new Gamer[listOfPlayers.length + 1];

                    for (int i = 0; i < listOfPlayers.length; i++) {//nur flache Kopiennnnnnnnnnnnnnnnnnnnnnnn
                        tmpListOfPlayers[i] = listOfPlayers[i];
                    }
                    tmpListOfPlayers[listOfPlayers.length] = new Gamer(actualPlayer + 1);       // New player with no stories inserted
                    listOfPlayers = tmpListOfPlayers;

                    // Reset Text fields in new_game.xml
                    playerID.setText("Du bist Spieler " + (actualPlayer + 1) + ":");
                    playerName.setText("Dein Name");
                    storyNumber.setText("Story 1:");
                    writeStories.setText("Ich hab schonmal ");
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
                else if (playerName.getText().toString().isEmpty())     // Check last player's name
                    Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein", Toast.LENGTH_SHORT).show();
                else if (playerName.getText().toString().length() < 5)      // Check last player's name
                    Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 5 Zeichen bestehen", Toast.LENGTH_SHORT).show();
                else {

                    // Definitions
                    int actualGameId = 0, actualPlayerId;
                    GameDao gamesDao;
                    PlayerDao playerDao;
                    StoryDao storyDao;
                    Player[] listOfNewPlayers;

                    // Set name of the last player
                    listOfPlayers[actualPlayer].setName(playerName.getText().toString());

                    Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers[actualPlayer].getNumber() + " erfolgreich gespeichert",
                            Toast.LENGTH_LONG).show();

                    // Create database connection
                    gamesDao = db.gamesDao();
                    playerDao = db.userDao();
                    storyDao = db.storyDao();

                    // Get from database
                    if (gamesDao.getAll().size() == 0 && playerDao.getAll().size() == 0 && storyDao.getAll().size() == 0) {
                        idOfFirstPlayer = 1;
                        idOfFirstStory = 1;
                        actualGameId = 1;
                    } else if (gamesDao.getAll().size() > 0 && playerDao.getAll().size() > 0 && storyDao.getAll().size() > 0) {
                        idOfFirstPlayer = playerDao.getAll().get(playerDao.getAll().size() - 1).playerId + 1;       // Id of last player +1 = new playerId
                        idOfFirstStory = storyDao.getAll().get(storyDao.getAll().size() - 1).storyId + 1;      // Id of last story +1 = new storyId
                        actualGameId = gamesDao.getAll().get(gamesDao.getAll().size() - 1).gameId + 1;      // Id of last game +1 = new gameId
                    } else {        // Exception handlingggggggggggggggggggggggggggggggggggggggggggggggggggggg
                        Toast.makeText(CreatePlayers.this, "Fehler beim festlegen der Ids", Toast.LENGTH_SHORT).show();
                    }

                    actualPlayerId = idOfFirstPlayer;

                    // Create an Array to insert in playerDao
                    listOfNewPlayers = new Player[listOfPlayers.length];

                    // Insert all players     -> nextPlayer-Button
                    for (int i = 0; i < listOfPlayers.length; i++) {
                        listOfNewPlayers[i] = new Player();
                        //listOfNewPlayers[i].playerId = listOfNewPlayers[i].playerId;      // Player id is set with autoincrement
                        listOfNewPlayers[i].name = listOfPlayers[i].getName();
                        listOfNewPlayers[i].playerNumber = listOfPlayers[i].getNumber();
                        listOfNewPlayers[i].gameId = actualGameId;
                        listOfNewPlayers[i].score = 0;
                        countOfPlayers++;

                        // Create an Array to insert in storyDao
                        Story[] listOfStories = new Story[listOfPlayers[i].getCountOfStories()];

                        // Insert a player's stories
                        for (int j = 0; j < listOfPlayers[i].getCountOfStories(); j++) {
                            listOfStories[j] = new Story();
                            //listOfStories[i].storyId = listOfStories[i].storyId;        // Story id is set with autoincrement
                            listOfStories[j].content = listOfPlayers[i].getStory(j);
                            listOfStories[j].status = false;
                            listOfStories[j].playerId = actualPlayerId;
                            countOfStories++;
                        }

                        actualPlayerId++;
                        storyDao.insertAll(listOfStories);
                    }

                    playerDao.insertAll(listOfNewPlayers);

                    // Insert a game
                    Game newGame = new Game();
                    //newGame.gameId = newGame.gameId;          // Game id is set with autoincrement
                    newGame.gameName = getIntent().getStringExtra("GameName");      // -> next-Button of the previous intent
                    newGame.idOfFirstPlayer = idOfFirstPlayer;
                    newGame.countOfPlayers = countOfPlayers;
                    newGame.idOfFirstStory = idOfFirstStory;
                    newGame.countOfStories = countOfStories;
                    gamesDao.insertAll(newGame);

                    // Exception handlingggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg
                    if (actualGameId != gamesDao.getAll().size()) {
                        Toast.makeText(CreatePlayers.this, "Fehler ids stimmen nicht ueberin:" + actualGameId + " : " + newGame.gameId, Toast.LENGTH_SHORT).show();
                    }

                    //Ist das so/auf diese Weise sinnvolllllllllllllllllllllllllllllllllllllllllllllllllllllll
                    if (correctInput) {         // If database is correctly created
                        Intent rules = new Intent(CreatePlayers.this, Rules.class);

                        rules.putExtra("GameId", actualGameId);

                        startActivity(rules);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spieleinstellungen")
                .setMessage("Wenn du zur\u00fcck gehst, werden die Daten nicht gespeichert!")
                .setPositiveButton("Zur\u00fcck", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        builder.create().show();
    }

}