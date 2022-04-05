package com.example.ichhabschonmal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.database.Player;
import com.example.ichhabschonmal.database.Story;
import com.example.ichhabschonmal.exceptions.GamerException;

import java.util.ArrayList;
import java.util.List;

public class CreatePlayers extends AppCompatActivity {

    private Gamer[] listOfPlayers = new Gamer[]{new Gamer(1)};
    private int actualPlayer = 0, minStoryNumber, maxStoryNumber, maxPlayerNumber;
    private int idOfFirstPlayer = -1, countOfPlayers = 0, idOfFirstStory = -1, countOfStories = 0;
    private boolean alreadySadOne = false;          // Check, if a player has saved his last story
    private boolean alreadySadTwo = false;          // Check, if a player has saved changes of all stories

    private List<String> newListOfStories = new ArrayList<>();          // Is used for deleting/replacing stories in viewYourStories
    private boolean mItemsCanFocus;

    private AppDatabase db;
    private ListView listView;
    private Button backButton, saveEditedStories;
    private ViewYourStoriesListAdapter adapter;
    private TextView storyNumber;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_players);

        // Definitions
        Button saveAndNextStory, nextPerson, viewYourStories, next;
        EditText writeStories, playerName;
        TextView playerID, charsLeft;

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
        charsLeft = findViewById(R.id.charsLeft);

        // Set used variables
        minStoryNumber = getIntent().getExtras().getInt("MinStoryNumber");
        maxStoryNumber = getIntent().getExtras().getInt("MaxStoryNumber");
        maxPlayerNumber = getIntent().getExtras().getInt("playerNumber");
        //setItemsCanFocus(true);

        // Calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // Showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        viewYourStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialog(v);
            }
        });

        writeStories.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() <= 250) {
                    charsLeft.setText(editable.toString().length() + "/250");
                }

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
            else {          // Text field is okay
                listOfPlayers[actualPlayer].addStory(writeStories.getText().toString());
                writeStories.setText("");
                storyNumber.setText("Story " + (listOfPlayers[actualPlayer].getCountOfStories() + 1) + ":");

                Toast.makeText(CreatePlayers.this, "Story " + listOfPlayers[actualPlayer].getCountOfStories() + " gespeichert",
                        Toast.LENGTH_LONG).show();

                // Set used variable
                alreadySadOne = false;
            }
        });
        nextPerson.setOnClickListener(v -> {
            // Check inserting a new player
            if (listOfPlayers.length == maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Es k\u00f6nnen keine weiteren Spieler teilnehmen!",
                        Toast.LENGTH_LONG).show();
            else if (listOfPlayers[actualPlayer].getCountOfStories() < minStoryNumber) {
                Toast.makeText(CreatePlayers.this, "Spieler muss mindestens " + minStoryNumber + " Stories besitzen!",
                        Toast.LENGTH_LONG).show();
                if (!writeStories.getText().toString().equals(""))
                    Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert!", Toast.LENGTH_SHORT).show();
            } else if (listOfPlayers.length > maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!", Toast.LENGTH_LONG).show();
            else if (!alreadySadOne && !writeStories.getText().toString().equals("")) {
                Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                alreadySadOne = true;
            }
            else if (playerName.getText().toString().isEmpty()) {
                Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein!", Toast.LENGTH_SHORT).show();
            } else if (playerName.getText().toString().length() < 2)
                Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 2 Zeichen bestehen!", Toast.LENGTH_SHORT).show();
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
                playerName.setText("");
                storyNumber.setText("Story 1:");
                writeStories.setText("");
            }
        });

        next.setOnClickListener(view -> {

            // Check, if all players meet all conditions
            if (listOfPlayers.length < maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu wenig eingeloggte Spieler", Toast.LENGTH_SHORT).show();
            else if (listOfPlayers.length > maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler", Toast.LENGTH_SHORT).show();
            else if (listOfPlayers[listOfPlayers.length - 1].getCountOfStories() < minStoryNumber) {
                Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers.length + " besitzt zu wenig Storys!", Toast.LENGTH_SHORT).show();
                if (!writeStories.getText().toString().equals(""))
                    Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert!", Toast.LENGTH_SHORT).show();
            } else if (maxStoryNumber < listOfPlayers[listOfPlayers.length - 1].getCountOfStories())
                Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers.length + " besitzt zu viele Storys!", Toast.LENGTH_SHORT).show();
            else if (!alreadySadOne && !writeStories.getText().toString().equals("")) {
                Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                alreadySadOne = true;
            } else if (playerName.getText().toString().isEmpty())     // Check last player's name
                Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein", Toast.LENGTH_SHORT).show();
            else if (playerName.getText().toString().length() < 2)      // Check last player's name
                Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 2 Zeichen bestehen", Toast.LENGTH_SHORT).show();
            else {

                // Definitions
                Intent rules;
                int actualGameId;

                // Set name of the last player
                listOfPlayers[actualPlayer].setName(playerName.getText().toString());

                Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers[actualPlayer].getNumber() + " erfolgreich gespeichert",
                        Toast.LENGTH_LONG).show();

                // Create database connection:
                db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

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
                    newPlayer.countOfVodka = 0;
                    newPlayer.countOfTequila = 0;

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
                        try {
                            newStory.content = listOfPlayers[i].getStory(j);
                        } catch (GamerException ge) {
                            ge.printStackTrace();
                            newStory.content = ge.toString();
                        }
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
                rules.putExtra("GameIsLoaded", false);
                startActivity(rules);
                finish();
            }
        });
    }

    public class ViewYourStoriesListAdapter extends ArrayAdapter<String> {
        private final Activity activity;
        private final List<EditText> editTexts = new ArrayList<>();

        public ViewYourStoriesListAdapter(Activity activity) {
            super(activity, R.layout.view_your_stories_list_item, newListOfStories);
            this.activity = activity;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            // Definitions and initializations
            LayoutInflater inflater = activity.getLayoutInflater();
            @SuppressLint({"ViewHolder", "InflateParams"}) View rowView= inflater.inflate(R.layout.view_your_stories_list_item, null, true);
            EditText storyText = rowView.findViewById(R.id.storyText);
            ImageButton deleteStory = rowView.findViewById(R.id.deleteStory);

            editTexts.add(storyText);

            // Set story text in the listview-item
            storyText.setText(newListOfStories.get(position));

            storyText.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (view.requestFocus()) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                        }

                        return true;
                    }

                    return false;
                }
            });

            deleteStory.setOnClickListener(new View.OnClickListener() {     // Give delete button a function
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View view) {

                    // Delete story
                    newListOfStories.remove(position);
                    editTexts.remove(position);
                    listView.invalidateViews();
                }
            });

            return rowView;
        }
    }


    public void openDialog(View v) {

        // Definitions
        AlertDialog.Builder builder;
        AlertDialog dialog;
        View row;

        // Initializations
        newListOfStories = listOfPlayers[actualPlayer].getAllStories();
        builder = new AlertDialog.Builder(this);
        row = getLayoutInflater().inflate(R.layout.view_your_stories, null);
        listView = row.findViewById(R.id.myStories);
        adapter = new ViewYourStoriesListAdapter(CreatePlayers.this);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        builder.setView(row);
        dialog = builder.create();
        dialog.show();      // Statement have to be exactly here at thus line

        // First show dialog and then set the rest, then keyboard will be displayed
        dialog.getWindow().clearFlags(LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_ALT_FOCUSABLE_IM);    // First step
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);     // Second step

        saveEditedStories = row.findViewById(R.id.saveEditedStories);
        backButton = row.findViewById(R.id.backButton);

        saveEditedStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> newListOfStories = new ArrayList<>();

                // Get all stories
                for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                    newListOfStories.add(adapter.editTexts.get(i).getText().toString());
                }

                // Replace all stories
                listOfPlayers[actualPlayer].replaceAllStories(newListOfStories);

                // Actualize layout
                storyNumber.setText("Story " + (listOfPlayers[actualPlayer].getCountOfStories() + 1) + ":");
                //listView.invalidateViews();

                Toast.makeText(CreatePlayers.this, "Stories wurden erfolgreich \u00fcberarbeitet", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!alreadySadTwo) {
                    Toast.makeText(CreatePlayers.this, "Nicht gespeicherte \u00c4nderungen gehen verloren!", Toast.LENGTH_SHORT).show();
                    alreadySadTwo = true;
                } else {
                    dialog.cancel();
                    alreadySadTwo = false;
                }
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