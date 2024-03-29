package de.android.werhatschonmal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import de.android.werhatschonmal.database.AppDatabase;
import de.android.werhatschonmal.database.Game;
import de.android.werhatschonmal.database.Player;
import de.android.werhatschonmal.database.Story;
import de.android.werhatschonmal.exceptions.GamerException;
import de.android.werhatschonmal.server_client_communication.ClientServerHandler;
import de.android.werhatschonmal.server_client_communication.SocketCommunicator;
import de.android.werhatschonmal.server_client_communication.SocketEndPoint;

public class CreatePlayers extends AppCompatActivity {

    private final TreeMap<Integer, Gamer> mapOfGamers = new TreeMap<>(); // <Index in list, Player>
    private List<String> newListOfStories = new ArrayList<>();          // Is used for deleting/replacing stories in viewYourStories
    private Semaphore semMapOfGamers = new Semaphore(1); // Semaphore for mapOfGamers

    private int minStoryNumber, maxStoryNumber, maxPlayerNumber;
    private int actualPlayersIndex = 0, idOfFirstPlayer = -1, countOfPlayers = 0, idOfFirstStory = -1, countOfStories = 0;
    private boolean alreadySavedOne = false;          // Check, if a player has saved his last story
    private boolean alreadySavedTwo = false;          // Check, if a player has saved changes of all stories

    private boolean onlineGame = false;     // Check, if actual game is an online game
    private boolean serverSide = false;     // Check, if actual device is the host/server
    private SocketCommunicator.Receiver receiverAction;

    private int respondingClients = 0; // Count of responding clients to go in PlayGame (Host)
    private Semaphore semRespondingClient = new Semaphore(1);

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

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Definitions
        Button saveAndNextStory, nextPerson, viewYourStories, btnContinue, rules;
        EditText writeStories, playerName;
        TextView playerID, charsLeft;

        // Buttons:
        saveAndNextStory = findViewById(R.id.saveAndNextStory);
        nextPerson = findViewById(R.id.nextPerson);
        viewYourStories = findViewById(R.id.viewYourStories);
        rules = findViewById(R.id.rules);
        btnContinue = findViewById(R.id.next);

        // EditTexts:
        writeStories = findViewById(R.id.writeStories);
        playerName = findViewById(R.id.playerName);

        //TextViews:
        playerID = findViewById(R.id.playerID);
        storyNumber = findViewById(R.id.storyNumber);
        charsLeft = findViewById(R.id.charsLeft);

        // Get from last intent
        //setItemsCanFocus(true);
        onlineGame = getIntent().getExtras().getBoolean("OnlineGame");

        if (!onlineGame) {
            actualPlayersIndex = 0;
            mapOfGamers.put(actualPlayersIndex, new Gamer(actualPlayersIndex + 1));
        } else {
            // Set used variables
            actualPlayersIndex = (ClientServerHandler.getClientEndPoint() != null) ? ClientServerHandler.getClientEndPoint().getClient().getPlayerNumber() - 1 : 0;
            //actualPlayersIndex = getIntent().getExtras().getInt("PlayersIndex");
            playerID.setText("Du bist Spieler " + (actualPlayersIndex + 1) + ":");
            mapOfGamers.put(actualPlayersIndex, new Gamer(actualPlayersIndex + 1));

            // Get from last intent
            serverSide = getIntent().getExtras().getBoolean("ServerSide");

            // Define a new action for receiving messages
            receiverAction = new SocketCommunicator(null, null, null, null, null).new Receiver() {

                @SuppressLint({"NotifyDataSetChanged", "LongLogTag"})
                @Override
                public void action() {
                    String receivedMessage = receiverAction.getMessage();

                    if (serverSide) { // Receive message from all clients
                        String[] lines = receivedMessage.split(SocketEndPoint.SEPARATOR);
                        receivedMessage = lines[0];
                        Log.e("CreatePlayers, received message outside", Arrays.toString(lines));

                        switch (receivedMessage) {
                            case (SocketEndPoint.CREATED_PLAYER): {
                                Gamer receivedPlayer;

                                // First 3 lines are separated by ';'
                                receivedPlayer = new Gamer(Integer.parseInt(lines[1])); // Set a player's number
                                receivedPlayer.setName(lines[2]); // Set a player's name

                                for (int i = 3; i < lines.length; i++)
                                    receivedPlayer.addStory(lines[i]);

                                // Last player in list is the host
                                try {
                                    semMapOfGamers.acquire();
                                    mapOfGamers.put(receivedPlayer.getNumber() - 1, receivedPlayer);
                                    Log.e("ListOfPlayers1", mapOfGamers.toString());
                                    semMapOfGamers.release();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                break;
                            }
                            case (SocketEndPoint.PLAY_GAME_HOST): {
                                int tmp = 0;

                                try {
                                    semRespondingClient.acquire();
                                    respondingClients++;
                                    tmp = respondingClients;
                                    semRespondingClient.release();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                // Stop receiving messages from each client
                                new Thread(() -> ClientServerHandler.getServerEndPoint().stopReceivingMessages(Integer.parseInt(lines[1]))).start();

                                // Start next activity (PlayGame)
                                if (tmp + 1 == mapOfGamers.size())
                                    btnContinue.callOnClick();

                                break;
                            }
                            default: {
                                Log.e("CreatePlayers, received message", Arrays.toString(lines));
                                break;
                            }
                        }
                    } else { // Receive message from host
                        if (receivedMessage.equals(SocketEndPoint.PLAY_GAME_CLIENTS)) {
                            Intent playGame = new Intent(CreatePlayers.this, PlayGame.class);
                            new Thread(() -> ClientServerHandler.getClientEndPoint().stopReceivingMessages()).start();

                            // Pass to next intent
                            playGame.putExtra("OnlineGame", true);
                            playGame.putExtra("ServerSide", serverSide);
                            playGame.putExtra("GameIsLoaded", false);

                            startActivity(playGame);
                            finish();
                        }
                    }
                }

            };

            if (serverSide)
                ClientServerHandler.getServerEndPoint().receiveMessages(receiverAction);
            else
                ClientServerHandler.getClientEndPoint().receiveMessages(receiverAction);
        }

        minStoryNumber = getIntent().getExtras().getInt("MinStoryNumber");
        maxStoryNumber = getIntent().getExtras().getInt("MaxStoryNumber");
        maxPlayerNumber = getIntent().getExtras().getInt("PlayerNumber");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        getSupportActionBar().setTitle((Html.fromHtml("<font color=\"#000000\">" + "Spielererstellung" + "</font>")));


        viewYourStories.setOnClickListener(this::openDialog);

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

        rules.setOnClickListener(view -> {
            Intent rules1 = new Intent(CreatePlayers.this, Rules.class);
            rules1.putExtra("GameIsLoaded", false);
            startActivity(rules1);
        });

        saveAndNextStory.setOnClickListener(v -> {

            // Add a players story
            if (Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getCountOfStories() == maxStoryNumber)
                Toast.makeText(CreatePlayers.this, "Du hast bereits genug Stories " +
                        "aufgeschrieben!", Toast.LENGTH_LONG).show();
            else if (writeStories.getText().toString().trim().isEmpty())
                Toast.makeText(CreatePlayers.this, "Kein Text zum speichern!",
                        Toast.LENGTH_LONG).show();
            else if (writeStories.getText().toString().length() < 8)
                Toast.makeText(CreatePlayers.this,
                        "Eine Story muss aus mindestens 8 zeichen " + "bestehen.",
                        Toast.LENGTH_SHORT).show();
            else {          // Text field is okay
                Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).addStory(writeStories.getText().toString().trim());
                writeStories.setText("");
                storyNumber.setText("Story " + (Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getCountOfStories() + 1) + ":");

                Toast.makeText(CreatePlayers.this,
                        "Story " + Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getCountOfStories() + " gespeichert",
                        Toast.LENGTH_LONG).show();

                // Set used variable
                alreadySavedOne = false;
            }
        });

        nextPerson.setOnClickListener(v -> {
            // Check inserting a new player
            if (mapOfGamers.size() == maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Es k\u00f6nnen keine weiteren Spieler teilnehmen!",
                        Toast.LENGTH_LONG).show();
            else if (Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getCountOfStories() < minStoryNumber) {
                Toast.makeText(CreatePlayers.this, "Spieler muss mindestens " + minStoryNumber + " Stories besitzen!",
                        Toast.LENGTH_LONG).show();
                if (!writeStories.getText().toString().equals(""))
                    Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert!", Toast.LENGTH_SHORT).show();
            } else if (mapOfGamers.size() > maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!", Toast.LENGTH_LONG).show();
            else if (!alreadySavedOne && !writeStories.getText().toString().equals("")) {
                Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                alreadySavedOne = true;
            } else if (playerName.getText().toString().trim().isEmpty()) {
                Toast.makeText(CreatePlayers.this, "Gib einen Spielernamen ein!", Toast.LENGTH_SHORT).show();
            } else if (playerName.getText().toString().trim().length() < 2)
                Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 2 Zeichen bestehen!", Toast.LENGTH_SHORT).show();
            else {
                Gamer player;

                // Set name of a player
                Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).setName(playerName.getText().toString().trim());

                Toast.makeText(CreatePlayers.this, "Spieler " + Objects.requireNonNull(mapOfGamers.lastEntry()).getValue().getNumber() + " erfolgreich gespeichert",
                        Toast.LENGTH_LONG).show();

                // Insert a new player
                player = new Gamer(++actualPlayersIndex + 1); // Equal to 'listOfPlayers.size()'
                mapOfGamers.put(actualPlayersIndex, player);

                // Reset Text fields in new_game.xml
                playerID.setText("Du bist Spieler " + (actualPlayersIndex + 1) + ":");
                playerName.setText("");
                storyNumber.setText("Story 1:");
                writeStories.setText("");
            }
        });

        btnContinue.setOnClickListener(view -> {

            // Check, if all players meet all conditions
            if (mapOfGamers.size() < maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu wenig eingeloggte Spieler!", Toast.LENGTH_SHORT).show();
            else if (mapOfGamers.size() > maxPlayerNumber)
                Toast.makeText(CreatePlayers.this, "Zu viele eingeloggte Spieler!", Toast.LENGTH_SHORT).show();
            else if (Objects.requireNonNull(mapOfGamers.lastEntry()).getValue().getCountOfStories() < minStoryNumber) {
                Toast.makeText(CreatePlayers.this, "Spieler " + mapOfGamers.size() + " besitzt zu wenig Storys!", Toast.LENGTH_SHORT).show();
                if (!writeStories.getText().toString().equals(""))
                    Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert!", Toast.LENGTH_SHORT).show();
            } else if (Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getCountOfStories() > maxStoryNumber)
                Toast.makeText(CreatePlayers.this, "Spieler " + mapOfGamers.size() + " besitzt zu viele Storys!", Toast.LENGTH_SHORT).show();
            else if (!alreadySavedOne && !writeStories.getText().toString().equals("")) {
                Toast.makeText(this, "Die letzte Story wurde noch nicht gespeichert, einmaliger Hinweis!", Toast.LENGTH_SHORT).show();
                alreadySavedOne = true;
            } else if (playerName.getText().toString().trim().isEmpty())     // Check last player's name
                Toast.makeText(CreatePlayers.this, "Gib einen Spielernamen ein!", Toast.LENGTH_SHORT).show();
            else if (playerName.getText().toString().trim().length() < 2)      // Check last player's name
                Toast.makeText(CreatePlayers.this, "Spielername muss aus mindestens 2 Zeichen bestehen!", Toast.LENGTH_SHORT).show();
            else if (onlineGame && !serverSide) { // Client sends player to host
                if (mapOfGamers.size() != 1)
                    Log.e("CreatePlayer as client", "More than one player in list!");
                else {
                    Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).setName(playerName.getText().toString().trim()); // Name is not set
                    ClientServerHandler.getClientEndPoint().sendMessage(SocketEndPoint.CREATED_PLAYER
                            + SocketEndPoint.SEPARATOR
                            + Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex))); // Client has only one Gamer object
                    Toast.makeText(getApplicationContext(), "Spieler "
                            + Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getNumber()
                            + " erfolgreich gespeichert", Toast.LENGTH_SHORT).show();
                }
            } else if (onlineGame && getRespondingClients() + 1 != mapOfGamers.size()) // Inform all clients to start game
                ClientServerHandler.getServerEndPoint().sendMessage(SocketEndPoint.PLAY_GAME_CLIENTS);
            else { // Local game or online gamer on serverside

                // Definitions
                int actualGameId;

                // Set name of the last player
                Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).setName(playerName.getText().toString().trim());
                Log.e("ListOfPlayers2", mapOfGamers.toString());

                runOnUiThread(() -> Toast.makeText(CreatePlayers.this, "Spieler "
                        + Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getNumber()
                        + " erfolgreich gespeichert", Toast.LENGTH_LONG).show());

                // Create database connection:
                db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

                // Create new game
                Game newGame = new Game();
                // newGame.gameId = newGame.gameId;         // Game id are set with autoincrement
                newGame.gameName = getIntent().getStringExtra("GameName");
                newGame.onlineGame = onlineGame;
                newGame.serverSide = serverSide;
                newGame.roundNumber = 1;
                newGame.actualDrinkOfTheGame = getIntent().getStringExtra("DrinkOfTheGame");
                db.gameDao().insert(newGame);

                // Set used variable
                actualGameId = db.gameDao().getAll().get(db.gameDao().getAll().size() - 1).gameId;
                Log.e("List of all gamers", mapOfGamers.toString());
                // Insert all players and their stories
                for (int i = 0; i < mapOfGamers.size(); i++) {

                    // Create a player
                    Player newPlayer = new Player();
                    //listOfNewPlayers[i].playerId = listOfNewPlayers[i].playerId;      // Player id is set with autoincrement
                    newPlayer.name = Objects.requireNonNull(mapOfGamers.get(i)).getName();
                    newPlayer.playerNumber = Objects.requireNonNull(mapOfGamers.get(i)).getNumber();
                    newPlayer.gameId = actualGameId;

                    // Insert the player
                    db.playerDao().insert(newPlayer);

                    // Set used variables for newGame
                    countOfPlayers++;
                    if (i == 0)
                        idOfFirstPlayer = db.playerDao().getAll().get(db.playerDao().getAll().size() - 1).playerId;

                    for (int j = 0; j < Objects.requireNonNull(mapOfGamers.get(i)).getCountOfStories(); j++) {

                        // Create a player's story
                        Story newStory = new Story();
                        //listOfStories[i].storyId = listOfStories[i].storyId;        // Story id is set with autoincrement
                        try {
                            newStory.content = Objects.requireNonNull(mapOfGamers.get(i)).getStory(j);
                        } catch (GamerException ex) {
                            ex.printStackTrace();
                            Log.e("SaveInDatabaseFailed", Arrays.toString(ex.getStackTrace()) + ", Message: " + ex.getMessage());
                            newStory.content = ex.toString();
                        }

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

                // Start activity in an offline game
                Intent playGame = new Intent(CreatePlayers.this, PlayGame.class);

                // Pass to next intent
                playGame.putExtra("OnlineGame", onlineGame);
                playGame.putExtra("ServerSide", serverSide);
                playGame.putExtra("GameId", actualGameId);
                playGame.putExtra("GameIsLoaded", false);

                Log.e("Hier kommt er rein", "Bin drin");

                // Start next activity
                startActivity(playGame);
                finish();
            }
        });

        // Deactivate nextPerson-Button
        if (onlineGame) {
            // 1. possibility: Deactivate nextPerson and reset layout btnContinue
            /*
            nextPerson.setVisibility(View.GONE);
            */

            // 2. possibility: Deactivate btnContinue and use onclick-action of bntContinue for nextPerson
            btnContinue.setVisibility(View.GONE);
            nextPerson.setBackgroundColor(getResources().getColor(R.color.orange_one));
            nextPerson.setText(btnContinue.getText());
            nextPerson.setOnClickListener(view -> btnContinue.callOnClick());
        }
    }

    private int getRespondingClients() {
        int tmp = 0;

        try {
            semRespondingClient.acquire();
            tmp = respondingClients;
            semRespondingClient.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return tmp;
    }

    private void incrementRespondingClients() {
        try {
            semRespondingClient.acquire();
            respondingClients++;
            semRespondingClient.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ViewYourStoriesListAdapter extends ArrayAdapter<String> {
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
            @SuppressLint({"ViewHolder", "InflateParams"}) View rowView = inflater.inflate(R.layout.view_your_stories_list_item, null, true);
            EditText storyText = rowView.findViewById(R.id.storyText);
            ImageButton deleteStory = rowView.findViewById(R.id.deleteStory);

            editTexts.add(storyText);

            // Set story text in the listview-item
            storyText.setText(newListOfStories.get(position));

            storyText.setOnTouchListener((view12, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if (view12.requestFocus()) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    }

                    return true;
                }

                return false;
            });

            // Give delete button a function (delete a story)
            deleteStory.setOnClickListener(view1 -> {
                newListOfStories.remove(position);
                editTexts.remove(position);
                listView.invalidateViews();
            });

            return rowView;
        }

    }


    @SuppressLint({"InflateParams", "SetTextI18n"})
    public void openDialog(View v) {

        // Definitions
        AlertDialog.Builder builder;
        AlertDialog dialog;
        View row;

        // Initializations
        newListOfStories = Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getAllStories();
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

        saveEditedStories.setOnClickListener(view -> {
            List<String> newListOfStories = new ArrayList<>();

            // Get all stories
            for (int i = 0; i < listView.getAdapter().getCount(); i++) {
                newListOfStories.add(adapter.editTexts.get(i).getText().toString());
            }

            // Replace all stories
            Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).replaceAllStories(newListOfStories);

            // Actualize layout
            storyNumber.setText("Story " + (Objects.requireNonNull(mapOfGamers.get(actualPlayersIndex)).getCountOfStories() + 1) + ":");
            //listView.invalidateViews();

            Toast.makeText(CreatePlayers.this, "Stories wurden erfolgreich \u00fcberarbeitet", Toast.LENGTH_SHORT).show();
        });

        backButton.setOnClickListener(view -> {
            if (!alreadySavedTwo) {
                Toast.makeText(CreatePlayers.this, "Nicht gespeicherte \u00c4nderungen gehen verloren!", Toast.LENGTH_SHORT).show();
                alreadySavedTwo = true;
            } else {
                dialog.cancel();
                alreadySavedTwo = false;
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

                    // Disconnect from online services
                    try {
                        if (onlineGame && serverSide) {
                            ClientServerHandler.getServerEndPoint().disconnectClientsFromServer(); // Disconnect all clients from serve, serverside
                            ClientServerHandler.getServerEndPoint().disconnectServerSocket(); // Disconnect socket of server
                        } else if (onlineGame)
                            ClientServerHandler.getClientEndPoint().disconnectClient(); // Disconnect client from server, clientside
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // Go to next intent
                    startActivity(mainActivity);
                    finish();
                })
                .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                });

        builder.create().show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}