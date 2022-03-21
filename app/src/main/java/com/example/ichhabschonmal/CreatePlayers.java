package com.example.ichhabschonmal;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

    private Gamer[] listOfPlayers = new Gamer[]{new Gamer(1)};
    private int actualPlayer = 0, minStoryNumber, maxStoryNumber, maxPlayerNumber;
    private int idOfFirstPlayer = -1, countOfPlayers = 0, idOfFirstStory = -1, countOfStories = 0;
    private boolean alreadySad = false;         // Check, if a player has saved his last story

    private AppDatabase db;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private TextView storyNumber;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_players);

        // Definitions
        Button saveAndNextStory, nextPerson, viewYourStories, next;
        EditText writeStories, playerName;
        TextView playerID;

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

        // Exception handling
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
            maxPlayerNumber = getIntent().getExtras().getInt("playerNumber");
        else
            maxPlayerNumber = 5;

        // Create database connection:
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();


        viewYourStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(v);
            }
        });

        saveAndNextStory.setOnClickListener(v -> {

            // Add a players story
            if (listOfPlayers[actualPlayer].getCountOfStories() == maxStoryNumber)
                Toast.makeText(CreatePlayers.this, "Spieler hat bereits genug Stories " +
                        "aufgeschrieben!", Toast.LENGTH_LONG).show();
            else if (writeStories.getText().toString().isEmpty())
                Toast.makeText(CreatePlayers.this, "Kein Text zum speichern!",
                        Toast.LENGTH_LONG).show();
            else if (writeStories.getText().toString().length() < 25)
                Toast.makeText(CreatePlayers.this, "Story muss aus mindestens 25 zeichen " +
                        "bestehen.", Toast.LENGTH_SHORT).show();
            else if (writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") ||
                    writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                Toast.makeText(this, "Standardtext kann nicht als Story gespeichert werden!", Toast.LENGTH_SHORT).show();
            } else {            // Text field is okay
                listOfPlayers[actualPlayer].addStory(writeStories.getText().toString());
                writeStories.setText("Schreibe in dieses Feld deine n\u00e4chste Story rein.");
                storyNumber.setText("Story " + (listOfPlayers[actualPlayer].getCountOfStories() + 1) + ":");

                Toast.makeText(CreatePlayers.this, "Story " + listOfPlayers[actualPlayer].getCountOfStories() + " gespeichert",
                        Toast.LENGTH_LONG).show();

                // Set used variable
                alreadySad = false;
            }
        });
        nextPerson.setOnClickListener(v -> {
            // Check inserting a new player
            if (listOfPlayers.length == maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Es k\u00f6nnen nicht weitere Spieler teilnehmen!",
                        Toast.LENGTH_LONG).show();
            else if (listOfPlayers[actualPlayer].getCountOfStories() < minStoryNumber) {
                Toast.makeText(CreatePlayers.this, "Spieler muss mindestens " + minStoryNumber + " Stories besitzen!",
                        Toast.LENGTH_LONG).show();
                if (!alreadySad && !writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") &&
                        !writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                    Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                    alreadySad = true;
                }
            } else if (listOfPlayers.length > maxPlayerNumber) {
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!",
                        Toast.LENGTH_LONG).show();
                // Exception has to be added hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
            } else if (!alreadySad && !writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") &&
                    !writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                alreadySad = true;
            }
            else if (playerName.getText().toString().isEmpty()) {
                Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein", Toast.LENGTH_SHORT).show();
            } else if (playerName.getText().toString().length() < 2)
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

            // Exception handling
            // Check, if all players meet all conditions
            if (listOfPlayers.length < maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu wenig eingeloggte Spieler", Toast.LENGTH_SHORT).show();
            else if (listOfPlayers.length > maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler", Toast.LENGTH_SHORT).show();
            else if (listOfPlayers[listOfPlayers.length - 1].getCountOfStories() < minStoryNumber) {
                Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers.length + " besitzt zu wenig Storys!", Toast.LENGTH_SHORT).show();
                if (!alreadySad && !writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") &&
                        !writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                    Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                    alreadySad = true;
                }
            }else if (maxStoryNumber < listOfPlayers[listOfPlayers.length - 1].getCountOfStories())
                Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers.length + " besitzt zu viele Storys!", Toast.LENGTH_SHORT).show();
            else if (!alreadySad && !writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") &&
                    !writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                alreadySad = true;
            }
            else if (playerName.getText().toString().isEmpty())     // Check last player's name
                Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein", Toast.LENGTH_SHORT).show();
            else if (playerName.getText().toString().length() < 5)      // Check last player's name
                Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 5 Zeichen bestehen", Toast.LENGTH_SHORT).show();
            else {

                // Definitions
                Intent rules;
                int actualGameId;

                // Set name of the last player
                listOfPlayers[actualPlayer].setName(playerName.getText().toString());

                Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers[actualPlayer].getNumber() + " erfolgreich gespeichert",
                        Toast.LENGTH_LONG).show();

                // Create new game
                Game newGame = new Game();
                // newGame.gameId = newGame.gameId;         // Game id are set with autoincrement
                newGame.gameName = getIntent().getStringExtra("GameName");
                newGame.roundNumber = 1;
                newGame.actualDrinkOfTheGame = getIntent().getStringExtra("DrinkOfTheGame");
                db.gameDao().insert(newGame);

                // Set used variable
                actualGameId = db.gameDao().getAll().get(db.gameDao().getAll().size() - 1).gameId;

                // Insert all players and their stories
                for (int i = 0; i < listOfPlayers.length; i++) {

                    // Create a player
                    Player newPlayer = new Player();
                    //listOfNewPlayers[i].playerId = listOfNewPlayers[i].playerId;      // Player id is set with autoincrement
                    newPlayer.name = listOfPlayers[i].getName();
                    newPlayer.playerNumber = listOfPlayers[i].getNumber();
                    newPlayer.gameId = actualGameId;
                    newPlayer.score = 0;
                    newPlayer.countOfBeers = 0;

                    // Insert the player
                    db.playerDao().insert(newPlayer);

                    // Set used variables for newGame
                    countOfPlayers++;
                    if (i == 0)
                        idOfFirstPlayer = db.playerDao().getAll().get(db.playerDao().getAll().size() - 1).playerId;

                    for (int j = 0; j < listOfPlayers[i].getCountOfStories(); j++) {

                        // Create a player's story
                        Story newStory = new Story();
                        //listOfStories[i].storyId = listOfStories[i].storyId;        // Story id is set with autoincrement
                        newStory.content = listOfPlayers[i].getStory(j);
                        newStory.status = false;
                        newStory.guessedStatus = false;         // Set a "default value"
                        newStory.playerId = db.playerDao().getAll().get(db.playerDao().getAll().size() - 1).playerId;
                        newStory.guessingPerson = "";           // Set a "default value"

                        // Insert a story
                        db.storyDao().insert(newStory);

                        // Set used variable for newGame
                        countOfStories++;
                        if (i == 0 && j == 0)
                            idOfFirstStory = db.storyDao().getAll().get(db.storyDao().getAll().size() - 1).storyId;
                    }
                }


                // Update newGame
                newGame.gameId = actualGameId;
                newGame.idOfFirstPlayer = idOfFirstPlayer;
                newGame.countOfPlayers = countOfPlayers;
                newGame.idOfFirstStory = idOfFirstStory;
                newGame.countOfStories = countOfStories;
                db.gameDao().updateGame(newGame);

                // Close database connection
                db.close();

                // Start new activity
                rules = new Intent(CreatePlayers.this, Rules.class);
                rules.putExtra("GameId", actualGameId);
                startActivity(rules);
                finish();
            }
        });
    }

    public void openDialog(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View row = getLayoutInflater().inflate(R.layout.view_all_stories, null);
        listView = row.findViewById(R.id.myStories);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfPlayers[actualPlayer].getAllStories());
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        builder.setView(row);
        AlertDialog dialog = builder.create();
        dialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Wollen Sie diese Story editieren?")
                        .setMessage(listView.getItemAtPosition(i).toString())
                        .setPositiveButton("L\u00f6schen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Delete story
                                listOfPlayers[actualPlayer].deleteStory(i);

                                // Actualize layout
                                storyNumber.setText("Story " + (listOfPlayers[actualPlayer].getCountOfStories() + 1) + ":");
                                listView.invalidateViews();
                            }
                        })
                        .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                builder.create().show();


            }
        });
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spielererstellung")
                .setMessage("Wenn du zur\u00fcck gehst, werden die Daten nicht gespeichert!")
                .setPositiveButton("Zur\u00fcck", (dialog, which) -> {
                    Intent mainActivity = new Intent(CreatePlayers.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                });

        builder.create().show();
    }
}