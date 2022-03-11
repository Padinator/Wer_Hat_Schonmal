package com.example.ichhabschonmal;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

    @SuppressLint("SetTextI18n")
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

        viewYourStories.setOnClickListener(v -> {
            Intent viewStories = new Intent(getApplicationContext(), ViewAllStories.class);
            startActivity(viewStories);
        });


        saveAndNextStory.setOnClickListener(v -> {

            // Add a players story
            if (listOfPlayers[actualPlayer].getCountOfStories() == maxStoryNumber) {      // Stories per player == maxStoryNumber?
                Toast.makeText(CreatePlayers.this, "Spieler hat bereits genug Stories " +
                                "aufgeschrieben!", Toast.LENGTH_LONG).show();
            } else if (writeStories.getText().toString().isEmpty()) {       // Text field for stories is empty
                Toast.makeText(CreatePlayers.this, "Kein Text zum speichern!",
                        Toast.LENGTH_LONG).show();
            } else if (writeStories.getText().toString().length() < 25) {
                Toast.makeText(CreatePlayers.this, "Story muss aus mindestens 25 zeichen " +
                        "bestehen.", Toast.LENGTH_SHORT).show();
            } else {            // Text field is okay
                listOfPlayers[actualPlayer].addStory(writeStories.getText().toString());
                writeStories.setText("Schreibe in dieses Feld deine n\u00e4chste Story rein.");
                storyNumber.setText("Story " + (listOfPlayers[actualPlayer].getCountOfStories() + 1) + ":");

                Toast.makeText(CreatePlayers.this, "Story " + listOfPlayers[actualPlayer].getCountOfStories() + " gespeichert",
                        Toast.LENGTH_LONG).show();
            }
        });

        nextPerson.setOnClickListener(v -> {

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
                System.arraycopy(listOfPlayers, 0, tmpListOfPlayers, 0, listOfPlayers.length);      // Flat copy
                tmpListOfPlayers[listOfPlayers.length] = new Gamer(actualPlayer + 1);       // New player with no stories inserted
                listOfPlayers = tmpListOfPlayers;

                // Reset Text fields in new_game.xml
                playerID.setText("Du bist Spieler " + (actualPlayer + 1) + ":");
                playerName.setText("Dein Name");
                storyNumber.setText("Story 1:");
                writeStories.setText("Schreibe in dieses Feld deine Story rein.");
            }
        });

        next.setOnClickListener(view -> {

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
                int actualGameId, idOfFirstPlayer = -1, countOfPlayers = 0, idOfFirstStory = -1, countOfStories = 0;
                GameDao gamesDao;
                PlayerDao playerDao;
                StoryDao storyDao;

                // Set name of the last player
                listOfPlayers[actualPlayer].setName(playerName.getText().toString());

                Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers[actualPlayer].getNumber() + " erfolgreich gespeichert",
                        Toast.LENGTH_LONG).show();

                // Create database connection
                gamesDao = db.gamesDao();
                playerDao = db.userDao();
                storyDao = db.storyDao();

                // Insert a game
                Game newGame = new Game();
                // newGame.gameId = newGame.gameId;      // Game id are set with
                newGame.gameName = getIntent().getStringExtra("GameName");      // -> next-Button of the previous intent
                gamesDao.insert(newGame);

                // Set actual game Id
                actualGameId = gamesDao.getAll().get(gamesDao.getAll().size() - 1).gameId;

                // Insert all players
                for (int i = 0; i < listOfPlayers.length; i++) {
                    Player newPlayer = new Player();
                    // player.playerId = player.playerId;       // A player's id is set with autoincrement
                    newPlayer.playerNumber = listOfPlayers[i].getNumber();
                    newPlayer.name = listOfPlayers[i].getName();
                    newPlayer.gameId = actualGameId;
                    newPlayer.score = 0;
                    playerDao.insert(newPlayer);
                    countOfPlayers++;

                    if (i == 0)
                        idOfFirstPlayer = playerDao.getAll().get(playerDao.getAll().size() - 1).playerId;

                    // Insert a player's stories
                    for (int j = 0; j < listOfPlayers[i].getCountOfStories(); j++) {
                        Story newStory = new Story();
                        // newStory.storyId = newStory.storyId;        // Story ids are set with autoincrement
                        newStory.content = listOfPlayers[i].getStory(j);
                        newStory.playerId = playerDao.getAll().get(playerDao.getAll().size() - 1).playerId;
                        newStory.status = false;
                        storyDao.insert(newStory);
                        countOfStories++;

                        if (i == 0 && j == 0)
                            idOfFirstStory = storyDao.getAll().get(storyDao.getAll().size() - 1).storyId;
                    }
                }

                // Updated game
                newGame.gameId = actualGameId;
                newGame.idOfFirstPlayer = idOfFirstPlayer;
                newGame.countOfPlayers = countOfPlayers;
                newGame.idOfFirstStory = idOfFirstStory;
                newGame.countOfStories = countOfStories;
                gamesDao.updateGame(newGame);

                // Close database connection
                db.close();

                // Open new intent
                Intent rules = new Intent(CreatePlayers.this, Rules.class);
                rules.putExtra("GameId", actualGameId);
                startActivity(rules);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spieleinstellungen")
                .setMessage("Wenn du zur\u00fcck gehst, werden die Daten nicht gespeichert!")
                .setPositiveButton("Zur\u00fcck", (dialog, which) -> finish())
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                });

        builder.create().show();
    }

}
