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
import com.example.ichhabschonmal.database.Player;
import com.example.ichhabschonmal.database.Story;

public class CreatePlayers extends AppCompatActivity {

    private int actualPlayerNumber = 1, minStoryNumber, maxStoryNumber, maxPlayerNumber;
    private int actualGameId, idOfFirstPlayer = -1, idOfActualPlayer, idOfFirstStory = -1;
    private int countOfPlayers = 1, countOfStories = 0, countOfStoriesOfActualPlayer = 0;
    private AppDatabase db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_players);

        // Definitions
        Button saveAndNextStory, nextPerson, viewYourStories, next;
        EditText writeStories, playerName;
        TextView playerID, storyNumber;

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
        else    // Exception handling
            minStoryNumber = 3;
        if (getIntent().hasExtra("MaxStoryNumber"))
            maxStoryNumber = getIntent().getExtras().getInt("MaxStoryNumber");
        else    // Exception handling
            maxStoryNumber = 5;

        // Set number of players
        if (getIntent().hasExtra("playerNumber"))
            maxPlayerNumber = getIntent().getExtras().getInt("playerNumber");
        else    // Exception handling
            maxPlayerNumber = 5;

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // Create new game
        Game newGame = new Game();
        // newGame.gameId = newGame.gameId;      // Game id are set with autoincrement
        newGame.gameName = getIntent().getStringExtra("GameName");      // -> next-Button of the previous intent
        db.gamesDao().insert(newGame);

        // Get id of the actual game
        actualGameId = db.gamesDao().getAll().get(db.gamesDao().getAll().size() - 1).gameId;

        // Create first player and insert him
        Player player = new Player();
        player.gameId = actualGameId;
        db.userDao().insert(player);

        // Get id of first player
        idOfFirstPlayer = db.userDao().getAll().get(db.userDao().getAll().size() - 1).playerId;

        // Set used variable
        idOfActualPlayer = idOfFirstPlayer;

        /*
        // Close database connection
        db.close();
        */

        viewYourStories.setOnClickListener(v -> {
            Intent viewStories = new Intent(getApplicationContext(), ViewAllStories.class);
            startActivity(viewStories);
        });

        saveAndNextStory.setOnClickListener(v -> {

            if (countOfStoriesOfActualPlayer == maxStoryNumber) {      // Stories per player == maxStoryNumber?
                Toast.makeText(CreatePlayers.this, "Spieler hat bereits genug Stories " +
                        "aufgeschrieben!", Toast.LENGTH_LONG).show();
            } else if (writeStories.getText().toString().isEmpty()) {       // Text field for stories is empty
                Toast.makeText(CreatePlayers.this, "Kein Text zum speichern!",
                        Toast.LENGTH_LONG).show();
            } else if (writeStories.getText().toString().length() < 25) {
                Toast.makeText(CreatePlayers.this, "Story muss aus mindestens 25 zeichen " +
                        "bestehen.", Toast.LENGTH_SHORT).show();
            } else {

                // Create new Story
                Story newStory = new Story();
                newStory.content = writeStories.getText().toString();
                newStory.status = false;
                newStory.playerId = idOfActualPlayer;

                db.storyDao().insert(newStory);

                countOfStoriesOfActualPlayer++;
                countOfStories++;

                if (idOfFirstPlayer == idOfActualPlayer && countOfStories == 1)
                    idOfFirstStory = db.storyDao().getAll().get(db.storyDao().getAll().size() - 1).storyId;

                /*
                // Close database connection
                db.close();
                */

                // Set TextViews
                writeStories.setText("Schreibe in dieses Feld deine n\u00e4chste Story rein.");
                storyNumber.setText("Story " + (countOfStoriesOfActualPlayer  +1) + ":");

                Toast.makeText(CreatePlayers.this, "Story " + countOfStoriesOfActualPlayer + " gespeichert",
                        Toast.LENGTH_LONG).show();
            }
        });

        nextPerson.setOnClickListener(v -> {

            // Check if a player meet all necessary conditions
            // Exception handling
            if (countOfPlayers == maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Es k\u00f6nnen nicht weitere Spieler teilnehmen!",
                        Toast.LENGTH_LONG).show();
            else if (countOfPlayers > maxPlayerNumber)      // Exception habdling
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!",
                        Toast.LENGTH_LONG).show();
            else if (countOfStoriesOfActualPlayer < minStoryNumber)
                Toast.makeText(CreatePlayers.this, "Spieler muss mindestens " + minStoryNumber + " Stories besitzen!",
                        Toast.LENGTH_LONG).show();
            else if (countOfStoriesOfActualPlayer > maxStoryNumber)     // Exception handling
                Toast.makeText(CreatePlayers.this, "Spieler " + actualPlayerNumber + " besitzt zu viele Storys!", Toast.LENGTH_SHORT).show();
            else if (playerName.getText().toString().isEmpty()) {     // No text in playerName EditText
                Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein", Toast.LENGTH_SHORT).show();
            } else if (playerName.getText().toString().length() < 2)    // Player name has to consist of at least two chars
                Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 2 Zeichen bestehen", Toast.LENGTH_SHORT).show();
            else {

                // Update a player
                player.playerId = idOfActualPlayer;
                player.playerNumber = actualPlayerNumber;
                player.name = playerName.getText().toString();
                player.gameId = actualGameId;
                player.score = 0;
                db.userDao().updatePlayer(player);

                // Create new player and insert him
                Player newPlayer = new Player();
                newPlayer.gameId = actualGameId;
                db.userDao().insert(newPlayer);

                /*
                // Close database connection
                db.close();
                */

                Toast.makeText(CreatePlayers.this, "Spieler " + actualPlayerNumber + " erfolgreich gespeichert",
                        Toast.LENGTH_LONG).show();

                // Set used variables
                idOfActualPlayer++;
                actualPlayerNumber++;
                countOfPlayers++;
                countOfStoriesOfActualPlayer = 0;

                // Reset Text fields in new_game.xml
                playerID.setText("Du bist Spieler " + actualPlayerNumber + ":");
                playerName.setText("Dein Name");
                storyNumber.setText("Story 1:");
                writeStories.setText("Schreibe in dieses Feld deine Story rein.");
            }
        });

        viewYourStories.setOnClickListener(view -> {
            Intent yourStories = new Intent(CreatePlayers.this, ViewAllStories.class);
            yourStories.putExtra("GameId", actualGameId);
            startActivity(yourStories);
        });

        next.setOnClickListener(view -> {

            // Check, if all players meet all conditions
            // Exception handling
            if (countOfPlayers < maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu wenig eingeloggte Spieler", Toast.LENGTH_SHORT).show();
            else if (countOfPlayers > maxPlayerNumber)      // Exception handling
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler", Toast.LENGTH_SHORT).show();
            else if (countOfStoriesOfActualPlayer < minStoryNumber)
                Toast.makeText(CreatePlayers.this, "Spieler besitzt zu wenig Storys!", Toast.LENGTH_SHORT).show();
            else if (countOfStoriesOfActualPlayer > maxStoryNumber)     // Exception handling
                Toast.makeText(CreatePlayers.this, "Spieler " + actualPlayerNumber + " besitzt zu viele Storys!", Toast.LENGTH_SHORT).show();
            else if (playerName.getText().toString().isEmpty())     // Check last player's name
                Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein", Toast.LENGTH_SHORT).show();
            else if (playerName.getText().toString().length() < 2)      // Check last player's name
                Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 2 Zeichen bestehen", Toast.LENGTH_SHORT).show();
            else {

                // Update a player
                player.playerId = idOfActualPlayer;
                player.playerNumber = actualPlayerNumber;
                player.name = playerName.getText().toString();
                player.gameId = actualGameId;
                player.score = 0;
                db.userDao().updatePlayer(player);

                Toast.makeText(CreatePlayers.this, "Spieler " + actualPlayerNumber + " erfolgreich gespeichert",
                        Toast.LENGTH_LONG).show();

                // Set used variables
                countOfPlayers++;

                //Update newGame
                newGame.gameId = actualGameId;
                newGame.idOfFirstPlayer = idOfFirstPlayer;
                newGame.countOfPlayers = countOfPlayers - 1;
                newGame.idOfFirstStory = idOfFirstStory;
                newGame.countOfStories = countOfStories;
                db.gamesDao().updateGame(newGame);

                // Close database connection
                db.close();

                // Start next activity
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
                .setPositiveButton("Zur\u00fcck", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.gamesDao().delete(db.gamesDao().getAll().get(db.gamesDao().getAll().size() - 1));
                        finish();
                    }
                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                });

        builder.create().show();
    }

}
