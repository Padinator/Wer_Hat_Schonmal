package com.example.ichhabschonmal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.database.Player;
import com.example.ichhabschonmal.database.Story;

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
    private ArrayAdapter<String> adapter;
    private TextView storyNumber;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_players);

        setItemsCanFocus(true);////////////////////////////////////////////////////

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
                alreadySadOne = false;
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
                if (!alreadySadOne && !writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") &&
                        !writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                    Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                    alreadySadOne = true;
                }
            } else if (listOfPlayers.length > maxPlayerNumber) {
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!",
                        Toast.LENGTH_LONG).show();
                // Exception has to be added hereeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
            } else if (!alreadySadOne && !writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") &&
                    !writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                alreadySadOne = true;
            }
            else if (playerName.getText().toString().isEmpty()) {
                Toast.makeText(CreatePlayers.this, "Spielername darf nicht leer sein", Toast.LENGTH_SHORT).show();
            } else if (playerName.getText().toString().length() < 2)
                Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 2 Zeichen bestehen", Toast.LENGTH_SHORT).show();
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
                if (!alreadySadOne && !writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") &&
                        !writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                    Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                    alreadySadOne = true;
                }
            }else if (maxStoryNumber < listOfPlayers[listOfPlayers.length - 1].getCountOfStories())
                Toast.makeText(CreatePlayers.this, "Spieler " + listOfPlayers.length + " besitzt zu viele Storys!", Toast.LENGTH_SHORT).show();
            else if (!alreadySadOne && !writeStories.getText().toString().equals("Schreibe in dieses Feld deine Story rein.") &&
                    !writeStories.getText().toString().equals("Schreibe in dieses Feld deine n\u00e4chste Story rein.")) {
                Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                alreadySadOne = true;
            }
            else if (playerName.getText().toString().isEmpty())     // Check last player's name
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

    public class ViewYourStoriesListAdapter extends ArrayAdapter<String> {
        private final Activity activity;

        public ViewYourStoriesListAdapter(Activity activity) {
            super(activity, R.layout.view_your_stories_list_item, newListOfStories);
            this.activity = activity;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            // Definitions and initializations
            LayoutInflater inflater = activity.getLayoutInflater();
            @SuppressLint("ViewHolder") View rowView= inflater.inflate(R.layout.view_your_stories_list_item, null, true);
            EditText storyText = rowView.findViewById(R.id.storyText);
            ImageButton deleteStory = rowView.findViewById(R.id.deleteStory);

            // Set story text in the listview-item
            storyText.setText(newListOfStories.get(position));

            storyText.setOnTouchListener(new View.OnTouchListener() {
                @SuppressLint("ClickableViewAccessibility")
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (view.requestFocus()) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                            //showKeyboard();
                        }

                        return true;
                    }

                    return false;
                }
            });

            storyText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.requestFocus();
                            view.requestFocusFromTouch();
                        }
                    });
                }
            });

            if (position == 1)
            {
                listView.setItemsCanFocus(true);

                // Use afterDescendants, because I don't want the ListView to steal focus
                listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
                storyText.requestFocus();
            }
            else
            {
                if (!listView.isFocused())
                {
                    listView.setItemsCanFocus(false);

                    // Use beforeDescendants so that the EditText doesn't re-take focus
                    listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
                    listView.requestFocus();
                }
            }

            deleteStory.setOnClickListener(new View.OnClickListener() {     // Give delete button a function
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View view) {

                    // Delete story
                    newListOfStories.remove(position);
                    listView.invalidateViews();
                }
            });

            return rowView;
        }
    }

    public void setItemsCanFocus(boolean itemsCanFocus) {
        mItemsCanFocus = itemsCanFocus;
        if (!itemsCanFocus) {
            listView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
    }

    public void showKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard(){
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
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
        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);      // First step
        listView.setItemsCanFocus(true);                                            // Second step
        adapter = new ViewYourStoriesListAdapter(CreatePlayers.this);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        builder.setView(row);
        dialog = builder.create();

        dialog.getWindow().clearFlags(LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
        saveEditedStories = row.findViewById(R.id.saveEditedStories);
        backButton = row.findViewById(R.id.backButton);

        //showKeyboard();

        saveEditedStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> newListOfStories = new ArrayList<>();

                // Get all stories
                for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                    newListOfStories.add(String.valueOf(listView.getItemAtPosition(i)));
                }

                // Replace all stories
                listOfPlayers[actualPlayer].replaceAllStories(newListOfStories);

                // Actualize layout
                storyNumber.setText("Story " + (listOfPlayers[actualPlayer].getCountOfStories() + 1) + ":");
                listView.invalidateViews();

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

        /*
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Wollen Sie diese Story l\u00f6schen?")
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
         */
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