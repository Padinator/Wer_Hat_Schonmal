package com.example.werhatschonmal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.room.Room;

import com.example.werhatschonmal.database.AppDatabase;
import com.example.werhatschonmal.database.Game;
import com.example.werhatschonmal.database.Player;
import com.example.werhatschonmal.database.Story;
import com.example.werhatschonmal.exceptions.FalseValuesException;
import com.example.werhatschonmal.exceptions.GamerException;
import com.example.werhatschonmal.server_client_communication.ClientServerHandler;
import com.example.werhatschonmal.server_client_communication.SocketCommunicator;
import com.example.werhatschonmal.server_client_communication.SocketEndPoint;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class PlayGame extends AppCompatActivity {

    // Game settings and status
    private Gamer[] players, editedPlayers;         // players contains all players of the actual game
    // editedPlayers contains all players, who have not guessed yet
    private Gamer guessingPlayer, guessedPlayer;        // Define the two playing players here, to proof in button solution, who is which one
    private Game actualGame;
    private List<Player> listOfPlayers;             // Contains all players of the actual game, listOfPlayers has access to the database
    private final List<String> listOfPlayersForSpinner = new ArrayList<>();
    private List<Story> listOfStories;              // Contains lal stories of the actual game, listOfStories has access to the database
    private TextView player, story, round, drinkOfTheGameTextView;
    private Button solution, save, back;
    private static Semaphore semPlayGame;

    // Variables for database and client-server-communication
    private AppDatabase db;
    private String actualDrinkOfTheGame, newDrinkOfTheGame;
    private int[] playerIds, storyIds;
    private boolean onlineGame = false, serverSide = false, solutionPressed = false,
            allPlayersGuessed = false, preconditionsSet = false, anotherRound = false,
            selectedPlayerSelected = false;
    // solutionPressed: before next round begins, Button solution may not been pressed
    // allPlayersGuessed: true means that all players guessed one time
    private int idOfFirstPlayer, countOfPlayers, idOfFirstStory, countOfStories, gameId, roundNumber;
    private int actualStoryNumberInList, actualStoryNumber;     // actualStoryNumber is a counter to set stories to used

    // Extra Views (alert dialog)
    private AlertDialog dialog;
    private AlertDialog.Builder dialogBuilder;
    private ListView listDrink;

    // TextInputLayout for choosing a player
    private TextInputLayout dropDownMenu;
    private AutoCompleteTextView autoCompleteText;
    private ArrayAdapter<String> adapter;
    private String selectedPlayer;
    private int selectedPlayerPosition;

    @SuppressLint({"SetTextI18n", "LongLogTag"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Preconditions before playing, until method "playGame()" is called
        // Definitions
        SocketCommunicator.Receiver receiverAction;
        boolean gameIsLoaded;

        // Buttons
        solution = findViewById(R.id.solution);
        drinkOfTheGameTextView = findViewById(R.id.drinkOfTheGame);

        // TextViews
        player = findViewById(R.id.player);
        story = findViewById(R.id.story);
        round = findViewById(R.id.round);

        // New drop down menu for selecting a player
        dropDownMenu = findViewById(R.id.menu);
        autoCompleteText = findViewById(R.id.auto_complete);

        if (!onlineGame) { // Set solution-possibility to visible
            dropDownMenu.setVisibility(View.VISIBLE);
            solution.setVisibility(View.VISIBLE);
        }

        // Get from last intent
        onlineGame = getIntent().getExtras().getBoolean("OnlineGame");
        serverSide = getIntent().getExtras().getBoolean("ServerSide");
        gameIsLoaded = getIntent().getExtras().getBoolean("GameIsLoaded"); // true: game is loaded

        if (onlineGame && !serverSide) { // Define a new action for receiving messages as a client
            receiverAction = new SocketCommunicator(null, null, null, null, null).new Receiver() {

                @SuppressLint({"NotifyDataSetChanged", "LongLogTag"})
                @Override
                public void action() {
                    String clientsMessage = getMessage();
                    String[] lines = clientsMessage.split(SocketEndPoint.SEPARATOR);
                    clientsMessage = lines[0];
                    Log.e("Clients messagePlayGame", Arrays.toString(lines));

                    switch (clientsMessage) {
                        case (SocketEndPoint.SET_PRE_CONDITIONS_OF_PLAYING_GAME): { // Set spinner (after game has started)

                            // Create database
                            db = Room.databaseBuilder(PlayGame.this, AppDatabase.class, "database").allowMainThreadQueries().build();

                            // Set used variable
                            listOfPlayersForSpinner.addAll(Arrays.asList(lines).subList(2, lines.length));

                            // Save actual game in game
                            actualGame = new Game();
                            actualGame.onlineGame = true;
                            actualGame.serverSide = false;
                            actualGame.countOfPlayers = listOfPlayersForSpinner.size();
                            // game.gameId = game.gameId; // Set with auto increment
                            actualGame.gameName = lines[1];
                            actualGame.countOfStories = 0;
                            // Set later: gameId of host
                            db.gameDao().insert(actualGame);

                            // Set used variable
                            gameId = db.gameDao().getAll().get(db.gameDao().getAll().size() - 1).gameId;

                            for (int i = 0; i < listOfPlayersForSpinner.size(); i++) {
                                String playerInfoLine = listOfPlayersForSpinner.get(i).replaceFirst("Spieler ", "");
                                Player player = new Player();
                                String[] playerInfo;

                                playerInfoLine = playerInfoLine.replaceFirst(",", SocketEndPoint.SEPARATOR);
                                playerInfo = playerInfoLine.split(SocketEndPoint.SEPARATOR);

                                player.playerNumber = Integer.parseInt(playerInfo[0]);
                                player.name = playerInfo[1];
                                player.gameId = gameId;

                                // Insert player in database
                                db.playerDao().insert(player);

                                if (i == 0)
                                    idOfFirstPlayer = db.playerDao().getAll().get(db.playerDao().getAll().size() - 1).playerId;
                            }

                            // Update actualGame
                            actualGame.idOfFirstPlayer = idOfFirstPlayer;
                            actualGame.gameId = gameId;
                            db.gameDao().updateGame(actualGame);

                            // Get players playing the actual game (list contains player-IDs)
                            playerIds = findSomethingOfActualGame(idOfFirstPlayer, actualGame.countOfPlayers);
                            listOfPlayers = db.playerDao().loadAllByPlayerIds(playerIds);

                            // Close database connection
                            db.close();

                            // Set used variable
                            idOfFirstStory = -1;

                            break;
                        }
                        case (SocketEndPoint.GAME_STATUS_CHANGED): { // Set changed game status (general)
                            Story nextStory = new Story();

                            // Set TextViews
                            runOnUiThread(() -> { // Set UI
                                round.setText(lines[2]);
                                drinkOfTheGameTextView.setText(lines[4]);
                                story.setText(lines[5]);
                                player.setText(String.valueOf(lines[7]));
                            });

                            // Actualize database
                            updateAGame(Integer.parseInt(lines[1]), lines[3]);

                            // Create database connection
                            db = Room.databaseBuilder(PlayGame.this, AppDatabase.class, "database").allowMainThreadQueries().build();

                            // Add story to database
                            nextStory.content = lines[5];
                            // nextStory.storyId = nextStory.storyId // Set with auto increment
                            nextStory.guessingPerson = listOfPlayers.get(Integer.parseInt(lines[6])).name; // Set guessing person
                            nextStory.playerId = listOfPlayers.get(Integer.parseInt(lines[8])).playerId; // Set guessed person

                            // Insert next story in database
                            db.storyDao().insert(nextStory);
                            nextStory = db.storyDao().getAll().get(db.storyDao().getAll().size() - 1);
                            actualGame.countOfStories++;

                            // Set idOfFirstStory
                            if (idOfFirstStory == -1) {
                                listOfStories = new LinkedList<>();
                                idOfFirstStory = nextStory.storyId;
                                actualGame.idOfFirstStory = idOfFirstStory;
                            }

                            // Update actual game in database
                            db.gameDao().updateGame(actualGame);

                            // Insert next story in list
                            listOfStories.add(nextStory);

                            // Close database connection
                            db.close();

                            // Set used variables
                            guessingPlayer = new Gamer(listOfPlayers.get(Integer.parseInt(lines[6])).playerNumber);

                            for (Player player : listOfPlayers)
                                if (player.playerId == nextStory.playerId) { // Set guessed player
                                    guessedPlayer = new Gamer(player.playerNumber);
                                    break;
                                }

                            // Set solution-possibility to invisible
                            runOnUiThread(() -> {
                                dropDownMenu.setVisibility(View.INVISIBLE);
                                solution.setVisibility(View.INVISIBLE);
                            });

                            break;
                        }
                        case (SocketEndPoint.YOUR_TURN): { // Receive that you are the guessing player
                            List<String> actualListOfPlayersForSpinner = new ArrayList<>(listOfPlayersForSpinner);
                            ArrayAdapter<String> adapter;

                            // Set adapter for a client
                            actualListOfPlayersForSpinner.remove(guessingPlayer.getNumber() - 1);
                            adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, actualListOfPlayersForSpinner);
                            Log.e("actualListOfPlayersForSpinner", actualListOfPlayersForSpinner.toString());

                            runOnUiThread(() -> { // Set UI
                                // Set adapter of drop down menu
                                autoCompleteText.setAdapter(adapter);
                                autoCompleteText.setOnItemClickListener((adapterView, view, i, l) -> {
                                    selectedPlayer = adapterView.getItemAtPosition(i).toString();
                                    selectedPlayerPosition = i;
                                    selectedPlayerSelected = true;
                                });
                                autoCompleteText.setVisibility(View.VISIBLE);

                                // Set solution possibility to visible
                                dropDownMenu.setVisibility(View.VISIBLE);
                                solution.setVisibility(View.VISIBLE);
                            });

                            break;
                        }
                        case (SocketEndPoint.RESULT_OF_GUESSING): { // Show results of guessing
                            runOnUiThread(() -> { // Set UI
                                AlertDialog.Builder builder = new AlertDialog.Builder(PlayGame.this);
                                builder.setTitle("ERGEBNIS")
                                        .setMessage(lines[1])
                                        .setPositiveButton("OK", (dialog, which) -> {

                                        });
                                builder.create().show();
                            });

                            Log.e("Received lines[2]", lines[2]);
                            Log.e("Results", guessedPlayer.getName() + ", " + guessedPlayer.getNumber());
                            Log.e("Updatedd story", listOfStories.get(listOfStories.size() - 1).content);
                            Log.e("Updatedd story", listOfStories.get(listOfStories.size() - 1).guessedStatus + "");

                            for (Player player : listOfPlayers)
                                Log.e("All players", player.name + ", " + player.playerNumber);

                            // Actualize database
                            if (Integer.parseInt(lines[2]) == guessedPlayer.getNumber()) { // It was guessed correctly
                                Log.e("Results", "1");

                                // Actualize guessedPlayer
                                updateAPlayer(guessedPlayer.getNumber() - 1);

                                // Actualize nextStory
                                updateAStory(listOfStories.size() - 1, true, true, "");
                            } else { // It was guessed not correctly
                                Log.e("Results", "2");

                                // Actualize guessedPlayer
                                updateAPlayer(guessingPlayer.getNumber() - 1);

                                // Actualize nextStory
                                updateAStory(listOfStories.size() - 1, true, false, "");
                            }

                            // Set used variables
                            anotherRound = Boolean.parseBoolean(lines[3]);
                            solutionPressed = true;
                            selectedPlayerSelected = true;

                            runOnUiThread(() -> {

                                // Set solution-Button
                                if (anotherRound) // Another round is left
                                    solution.setText("Weiter");
                                else // No round is left
                                    solution.setText("Spielende");

                                solution.setVisibility(View.VISIBLE);
                            });

                            break;
                        }
                        default: {
                            Log.e("Client receives outside, PlayGame", clientsMessage);
                            break;
                        }
                    }

                }

            };

            // Receive messages with a new pattern
            ClientServerHandler.getClientEndPoint().receiveMessages(receiverAction);

            // Each client should send here a message
            ClientServerHandler.getClientEndPoint().sendMessage(SocketEndPoint.PLAY_GAME_HOST
                    + SocketEndPoint.SEPARATOR + (ClientServerHandler.getClientEndPoint().getClient().getPlayerNumber() - 2));
        } else { // Equal to 'if (!onlineGame || serverSide)' -> local-/online-serverside-game
            // Get from last intent
            gameId = getIntent().getExtras().getInt("GameId");

            // Create database
            db = Room.databaseBuilder(PlayGame.this, AppDatabase.class, "database").allowMainThreadQueries().build();

            // Set used variables
            actualGame = db.gameDao().loadAllByGameIds(new int[]{gameId}).get(0);////////////////////////
            idOfFirstPlayer = actualGame.idOfFirstPlayer;
            countOfPlayers = actualGame.countOfPlayers;
            idOfFirstStory = actualGame.idOfFirstStory;
            countOfStories = actualGame.countOfStories;
            roundNumber = actualGame.roundNumber;

            // Find the players' ids belonging to the actual game and their stories
            playerIds = findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
            storyIds = findSomethingOfActualGame(idOfFirstStory, countOfStories);

            // Get players playing the actual game
            listOfPlayers = db.playerDao().loadAllByPlayerIds(playerIds);
            listOfStories = db.storyDao().loadAllByStoryIds(storyIds);

            // Close database connection
            db.close();

            // Set used variables and views
            if (!onlineGame) { // Local game
                dropDownMenu.setVisibility(View.VISIBLE);
                solution.setVisibility(View.VISIBLE);
                semPlayGame = new Semaphore(0);
            } else // Online game
                semPlayGame = new Semaphore(countOfPlayers - 2);

            // Case if a game is loaded, only use unused stories
            storyIds = findUnusedStories();

            if (storyIds.length > 0) {      // Continue/start playing game
                ArrayAdapter<String> adapterItems;

                if (gameIsLoaded)       // If game is loaded, aks to change drinks
                    requestToChangeDrink();

                // Set used variable
                setDrinkOfTheGame(actualGame.actualDrinkOfTheGame);
                newDrinkOfTheGame = actualGame.actualDrinkOfTheGame;

                listOfStories = db.storyDao().loadAllByStoryIds(storyIds);
                countOfStories = storyIds.length;
                idOfFirstStory = storyIds[0];

                // Save players in a new data structure
                players = saveInNewDataStructure(listOfPlayers, listOfStories);
                editedPlayers = saveInNewDataStructure(listOfPlayers, listOfStories);

                // New drop down menu for selecting a player
                adapterItems = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfPlayersForSpinner);
                autoCompleteText.setAdapter(adapterItems);
                autoCompleteText.setOnItemClickListener((adapterView, view, i, l) -> {
                    selectedPlayer = adapterView.getItemAtPosition(i).toString();
                    selectedPlayerPosition = i;
                });

                if (checkRound()) { // Play a game
                    playGame();
                } else { // The game is already over
                    Intent end = new Intent(PlayGame.this, EndScore.class);
                    end.putExtra("GameId", gameId);
                    startActivity(end);
                    finish();
                }
            } else { // The game is already over
                Intent end = new Intent(PlayGame.this, EndScore.class);
                end.putExtra("GameId", gameId);
                startActivity(end);
                finish();
            }
        }

        solution.setOnClickListener(view -> {

            // The host (or in local game the one player/device) presses the solution-Button
            if (!selectedPlayerSelected)
                Toast.makeText(PlayGame.this, "W\u00e4hle zuerst einen Spieler aus!", Toast.LENGTH_SHORT).show();
            else if (!solutionPressed) { // solution-Button may not been pressed
                if (onlineGame && !serverSide) { // A client pressed solution-Button
                    //String guessedPlayerNumber = SocketEndPoint.PLAYER_CHOSEN + SocketEndPoint.SEPARATOR + autoCompleteText.getListSelection();/////////////
                    String guessedPlayerNumber = SocketEndPoint.PLAYER_CHOSEN + SocketEndPoint.SEPARATOR + selectedPlayer;
                    Log.e("Client sends guessedPlayerNumber", guessedPlayerNumber);
                    ClientServerHandler.getClientEndPoint().sendMessage(guessedPlayerNumber);
                } else { // Host (or player/device in a local game) pressed solution-Button
                    String correctInput = "Spieler " + guessedPlayer.getNumber() + ", " + guessedPlayer.getName(), loser;
                    boolean anotherRound;
                    int i = 0;

                    if (allPlayersGuessed) { // Change actual drink of the game, if all players guessed one time
                        changeDrink(newDrinkOfTheGame);
                        setDrinkOfTheGame(newDrinkOfTheGame);
                        Log.e("actualDrinkOfTheGame", actualGame.actualDrinkOfTheGame + ", " + actualDrinkOfTheGame);
                        allPlayersGuessed = false;
                    }

                    if (selectedPlayer.equals(correctInput)) {       // Guessing player has guessed correctly

                        // Update a player in the database
                        updateAPlayer(guessedPlayer.getNumber() - 1);

                        // Update a story in the database
                        updateAStory(actualStoryNumberInList, true, true, guessingPlayer.getName());

                        loser = "Spieler " + guessedPlayer.getNumber() + ", " + guessedPlayer.getName() + " muss " + actualDrinkOfTheGame + " trinken!";
                    } else {        // chosenPlayer has not guessed correctly

                        // Update a player in the database
                        updateAPlayer(guessingPlayer.getNumber() - 1);

                        // Update a story in the database
                        updateAStory(actualStoryNumberInList, true, false, guessingPlayer.getName());

                        loser = "Spieler " + guessingPlayer.getNumber() + ", " + guessingPlayer.getName() + " muss " + actualDrinkOfTheGame + " trinken!";
                    }

                    // Update a game
                    updateAGame(++roundNumber, actualGame.actualDrinkOfTheGame);

                    // Delete story in the List "players"
                    try {
                        players[guessedPlayer.getNumber() - 1].deleteStory(actualStoryNumber - 1);
                    } catch (GamerException ex) {
                        ex.printStackTrace();
                        Log.e("DeleteStoryFailed", Arrays.toString(ex.getStackTrace()) + ", Message: " + ex.getMessage());
                        // There is no story to delete
                    }

                    // Set used variables
                    anotherRound = checkRound();

                    // Inform all clients about result
                    if (serverSide) {
                        String chosenPlayersNumber; // = chooseAPlayer.getSelectedItem().toString();
                        String[] result; // Contains split String: "Spieler <number>, <name>"

                        chosenPlayersNumber = selectedPlayer.replaceFirst("Spieler ", "");
                        result = chosenPlayersNumber.split(",");
                        ClientServerHandler.getServerEndPoint().sendMessage(SocketEndPoint.RESULT_OF_GUESSING
                                + SocketEndPoint.SEPARATOR + loser + SocketEndPoint.SEPARATOR
                                + result[0] + SocketEndPoint.SEPARATOR // Send looser
                                + anotherRound);
                    }

                    // Show result
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("ERGEBNIS")
                            .setMessage(loser)
                            .setPositiveButton("OK", (dialog, which) -> {

                            });
                    builder.create().show();

                    // Set TextView
                    if (anotherRound) // Another round is left
                        solution.setText("Weiter");
                    else // No round is left
                        solution.setText("Spielende");
                }

                // Set used variable
                solutionPressed = true;
            } else { // solution-Button was already pressed
                //Toast.makeText(PlayGame.this, "Starte zuerst die n\u00e4chste Runde!", Toast.LENGTH_SHORT).show();
                Intent next = new Intent(PlayGame.this, Score.class);
                Intent end = new Intent(PlayGame.this, EndScore.class);

                if ((!onlineGame || serverSide) && checkRound() || anotherRound) { // Show score and then play next round
                    next.putExtra("GameId", gameId);
                    startActivity(next);

                    new Thread(() -> {
                        if (!onlineGame || serverSide) { // Start next round

                            // Wait for closing next intent
                            try {
                                if (!onlineGame) // Local game
                                    waitForSth(1);
                                else // Online game, serverside
                                    waitForSth(countOfPlayers);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            // Play another round
                            playRound();
                        }

                        // Set used variable
                        solutionPressed = false;
                        selectedPlayerSelected = false;

                        runOnUiThread(() -> {
                            if (!onlineGame) // Set TextView
                                solution.setText("Aufl\u00f6sen");
                            else { // "Deactivate" temporary solution-possibility for all players
                                dropDownMenu.setVisibility(View.INVISIBLE);
                                solution.setVisibility(View.INVISIBLE);
                            }
                            // resets the autoCompleteText for every round
                            autoCompleteText.setText(null);
                            //autoCompleteText.setText("");     // or you can use this
                            autoCompleteText.setFocusable(false);
                        });
                    }).start();

                } else { // New intent with end score, game is over
                    runOnUiThread(() -> { // Run last Intent on UI-Thread

                        // Actualize game in database to "game is over"
                        actualGame.gameIsOver = true;
                        db = Room.databaseBuilder(PlayGame.this, AppDatabase.class, "database").allowMainThreadQueries().build();
                        db.gameDao().updateGame(actualGame);
                        db.close();

                        end.putExtra("GameId", gameId);
                        startActivity(end);
                        finish();
                    });
                }
            }
        });

        // calling the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setTitle((Html.fromHtml("<big><font color=\"#000000\">" + "Wer hat schonmal..." + "</font></big>")));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("LongLogTag")
    public static int[] findSomethingOfActualGame(int idOfFirstSomething, int countOfSomething) {     // Something can be a "Player" or a "Story"
        try {
            if (idOfFirstSomething > 0 && countOfSomething > 0) {
                int[] idsOfSomething = new int[countOfSomething];

                for (int i = 0; i < countOfSomething; i++) {
                    idsOfSomething[i] = idOfFirstSomething;
                    idOfFirstSomething++;
                }

                return idsOfSomething;
            } else if (idOfFirstSomething <= 0 && countOfSomething <= 0)
                throw new FalseValuesException("Id and count of something are less or equal than zero");
            else if (idOfFirstSomething <= 0)
                throw new FalseValuesException("Id of something is less or equal than zero");
            else // if (countOfSomething <= 0)
                throw new FalseValuesException("Count of something is less or equal than zero");
        } catch (FalseValuesException ex) {
            ex.printStackTrace();
            Log.e("FindSomethingOfActualGame", Arrays.toString(ex.getStackTrace()) + ", Message: " + ex.getMessage());
            return new int[]{1, 2, 3, 4, 5};
        }
    }

    private int[] findUnusedStories() {
        List<Integer> listOfNewStoryIds = new ArrayList<>();
        int[] newStoryIds;

        for (int i = 0; i < storyIds.length; i++) {     // Find count of unused stories
            if (!listOfStories.get(i).status)
                listOfNewStoryIds.add(storyIds[i]);
        }

        // Insert story ids in an array
        newStoryIds = new int[listOfNewStoryIds.size()];

        for (int i = 0; i < listOfNewStoryIds.size(); i++) {       // Copy unused story ids
            newStoryIds[i] = listOfNewStoryIds.get(i);
        }

        return newStoryIds;
    }

    // Save players of listOfPlayers in an array of players
    private Gamer[] saveInNewDataStructure(List<Player> listOfPlayers, List<Story> listOfStories) {
        Gamer[] players = new Gamer[countOfPlayers];
        int oldPlayerId = idOfFirstPlayer;
        int storyCounter = 0;

        Log.e("saveInNewDataStructure", "CountOfPlayers: " + countOfPlayers + ", IdOfFirstPlayer: " + idOfFirstPlayer);
        for (int i = 0; i < countOfPlayers; i++) {

            if (storyCounter < listOfStories.size())
                Log.e("saveInNewDataStructure", "Spieler" + listOfPlayers.get(i).playerNumber + ", " + listOfPlayers.get(i).name + ", aktuell erste Story, dessen PlayerId: " + listOfStories.get(storyCounter).playerId);
            // Create a new player in the list
            players[i] = new Gamer(listOfPlayers.get(i).playerNumber);
            players[i].setName(listOfPlayers.get(i).name);

            // Insert stories of a player
            for (; storyCounter < countOfStories
                    && oldPlayerId == listOfStories.get(storyCounter).playerId; storyCounter++) {
                players[i].addStory(listOfStories.get(storyCounter).content);
                Log.e("saveInNewDataStructure", "StoryCounter: " + storyCounter + ", PlayerId: " + listOfStories.get(storyCounter).playerId + ", OldPlayerId: " + oldPlayerId + ", StoryId: " + listOfStories.get(storyCounter).storyId);
            }

            // Set new id for oldStoryId
            if (storyCounter < countOfStories)
                oldPlayerId++;
        }

        return players;
    }

    private void playGame() {
        playRound();
    }

    @SuppressLint("SetTextI18n")
    private void playRound() {
        runOnUiThread(() -> { //  Main-Thread = UI-Thread
            ArrayAdapter<String> adapter;
            List<String> actualListOfPlayersForSpinner;
            StringBuilder messagePrecondition = new StringBuilder(SocketEndPoint.SET_PRE_CONDITIONS_OF_PLAYING_GAME
                    + SocketEndPoint.SEPARATOR + actualGame.gameName);
            String messageForAllClients = SocketEndPoint.GAME_STATUS_CHANGED + SocketEndPoint.SEPARATOR;

            if (allPlayersGuessed || Gamer.isEmpty(editedPlayers)) { // Enter, if each player has guessed one time
                editedPlayers = Gamer.copyPlayers(players);
                requestToChangeDrink(); // Request to change drink
            }

            guessingPlayer = chooseRandomPlayerWhoGuesses(); // Choose randomly a player to guess someone's story
            guessedPlayer = chooseRandomPlayerToBeGuessed(); // Choose another player to be guessed
            String chosenStory = chooseRandomStory(); // Then choose randomly story of this player

            // Set TextViews
            round.setText("Runde Nr." + roundNumber);
            story.setText(chosenStory);

            if (onlineGame)
                player.setText("Spieler " + guessingPlayer.getNumber() + ": " + guessingPlayer.getName() + " ist an der Reihe");
            else
                player.setText("Gib das Gerät Spieler " + guessingPlayer.getNumber() + ", " + guessingPlayer.getName());

            //Log.e("bbb", "Raten:" + chosenPlayer.getName() + ", " + chosenPlayer.getNumber() + ", " + chosenPlayer.getCountOfStories());        // Count of stories has not been adjusted in editedPlayers
            //Log.e("bbb", "Erraten werden: " + otherPlayer.getName() + ", " + otherPlayer.getNumber() + ", " + otherPlayer.getCountOfStories());

            for (Player player : listOfPlayers)
                Log.e("All players", player.name + ", " + player.playerNumber);


            // Save players' numbers and names for Spinner spin, only one time
            if (!preconditionsSet) {
                for (int i = 0; i < listOfPlayers.size(); i++) {
                    String element = "Spieler " + listOfPlayers.get(i).playerNumber + ", " + listOfPlayers.get(i).name;
                    listOfPlayersForSpinner.add(element);
                    messagePrecondition.append(SocketEndPoint.SEPARATOR).append(element);
                }

                // Set used variable
                preconditionsSet = true;

                // Inform clients
                if (serverSide)
                    ClientServerHandler.getServerEndPoint().sendMessage(String.valueOf(messagePrecondition));
            }

            // Set adapter of spinner
            actualListOfPlayersForSpinner = new ArrayList<>(listOfPlayersForSpinner); // Reset list of players for spinner for actual round
            actualListOfPlayersForSpinner.remove(guessingPlayer.getNumber() - 1); // Remove guessing player
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, actualListOfPlayersForSpinner);
            autoCompleteText.setAdapter(adapter);
            autoCompleteText.setOnItemClickListener((adapterView, view, i, l) -> {
                selectedPlayer = adapterView.getItemAtPosition(i).toString();
                selectedPlayerPosition = i;
                selectedPlayerSelected = true;
            });

            // chosenPlayer may guess again, when all players have guessed
            editedPlayers[guessingPlayer.getNumber() - 1] = null;

            // Set adapter
            if (onlineGame && serverSide) { // Online game: inform all clients about new situation
                messageForAllClients += roundNumber + SocketEndPoint.SEPARATOR;
                messageForAllClients += round.getText() + SocketEndPoint.SEPARATOR;
                messageForAllClients += actualGame.actualDrinkOfTheGame + SocketEndPoint.SEPARATOR;
                messageForAllClients += drinkOfTheGameTextView.getText() + SocketEndPoint.SEPARATOR;
                messageForAllClients += chosenStory + SocketEndPoint.SEPARATOR;
                messageForAllClients += guessingPlayer.getNumber() - 1 + SocketEndPoint.SEPARATOR;
                messageForAllClients += player.getText() + SocketEndPoint.SEPARATOR;
                messageForAllClients += guessedPlayer.getNumber() - 1;
                ClientServerHandler.getServerEndPoint().sendMessage(messageForAllClients);

                // Set solution-possibility
                if (guessingPlayer.getNumber() == 1) { // Host may guess
                    Log.e("guessingPlayer ist", guessingPlayer.getNumber() + ", ist 1");
                    // Set solution-possibility to visible
                    dropDownMenu.setVisibility(View.VISIBLE);
                    solution.setVisibility(View.VISIBLE);
                    dropDownMenu.notifyAll();
                } else { // Another player may guess
                    final int guessingPlayersNumber = guessingPlayer.getNumber();
                    Log.e("guessingPlayer ist", guessingPlayer.getNumber() + "");

                    // Set solution-possibility of host to invisible
                    dropDownMenu.setVisibility(View.INVISIBLE);
                    solution.setVisibility(View.INVISIBLE);
                    dropDownMenu.notifyAll();

                    // Inform guessing player to guess
                    ClientServerHandler.getServerEndPoint().sendMessageToClient(guessingPlayersNumber - 2, SocketEndPoint.YOUR_TURN);

                    // Receive results from guessing player
                    ClientServerHandler.getServerEndPoint().receiveMessages(guessingPlayersNumber - 2,
                            new SocketCommunicator(null, null, null, null, null).new Receiver() {

                                @SuppressLint("LongLogTag")
                                @Override
                                public void action() {
                                    String receivedMessage = getMessage();
                                    String[] lines = receivedMessage.split(SocketEndPoint.SEPARATOR);
                                    receivedMessage = lines[0];

                                    switch (receivedMessage) {
                                        case (SocketEndPoint.PLAYER_CHOSEN): {
                                            runOnUiThread(() -> {
                                                //chooseAPlayer.setSelection(Integer.parseInt(lines[1]));/////////////////
                                                //autoCompleteText.setText(autoCompleteText.getAdapter().getItem(Integer.parseInt(lines[1])).toString(), false);
                                                //autoCompleteText.setListSelection(Integer.parseInt(lines[1]));
                                                selectedPlayer = lines[1];
                                                selectedPlayerSelected = true;
                                                solution.callOnClick();
                                                solution.setVisibility(View.VISIBLE);
                                            });

                                            break;
                                        }
                                        case (SocketEndPoint.VIEWED_ACTUAL_SCORE): { // Terminate waiting status "peu a peu"
                                            releasePlayGame();

                                            // Stop receiving from this client
                                            new Thread(() -> ClientServerHandler.getServerEndPoint().stopReceivingMessages(guessingPlayersNumber - 2)).start();

                                            break;
                                        }
                                        default: {
                                            Log.e("Server receives outside, PlayGame", Arrays.toString(lines));
                                            break;
                                        }
                                    }
                                }
                            }
                    );
                }
            }
        });
    }

    private Gamer chooseRandomPlayerWhoGuesses() {
        int playerNumber, factor = 1;

        // factor is for calculating a number of a player in the range of the count of players
        for (int tmp = editedPlayers.length; 0 < tmp; tmp /= 10) {
            factor *= 10;
        }

        // Choose randomly a player
        do {
            playerNumber = (int) (Math.random() * factor);
            Log.e("endlosschleife", "Endlosschleife1");
        } while (playerNumber <= 0 || playerNumber > editedPlayers.length || editedPlayers[playerNumber - 1] == null);       // If a player is null, he has already guessed

        return editedPlayers[playerNumber - 1];
    }

    private Gamer chooseRandomPlayerToBeGuessed() {
        int playerNumber, factor = 1;

        // factor is for calculating a number of a player in the range of the count of players
        for (int tmp = players.length; 0 < tmp; tmp /= 10) {
            factor *= 10;
        }

        // Choose randomly a player
        do {
            playerNumber = (int) (Math.random() * factor);
            checkAll();
            if (playerNumber > 0 && playerNumber <= players.length && playerNumber != guessingPlayer.getNumber())
                Log.e("endlosschleife", "Endlosschleife2, Bedingung passt: " + "Spieler: " + playerNumber + ", " + players[playerNumber - 1].getCountOfStories());
            else if (guessingPlayer.getNumber() == playerNumber)
                Log.e("endlosschleife", "Endlosschleife2, playerNumber == chosenPlayer.getNumber(): " + "playerNumber: " + playerNumber + ", guessingPlayerNumber: " + guessingPlayer.getNumber() + ", Storyzahl: " + players[guessingPlayer.getNumber() - 1].getCountOfStories());
            //else
            //Log.e("endlosschleife", "Endlosschleife2, Bedingungen passen nicht, zufaellige Spielerauswahl will nicht mehr: " + playerNumber);
        } while (playerNumber == guessingPlayer.getNumber() || playerNumber <= 0 || playerNumber > players.length
                || players[playerNumber - 1].getCountOfStories() == 0);     // If a player has no stories, he can not be chosen to be guessed

        return players[playerNumber - 1];
    }

    private String chooseRandomStory() {        // Choose randomly a story of otherPlayer
        int storyNumber, factor = 1;            // storyNumber = storyNumber in Array listOfStories (see class Gamer), storyNumber is not an index
        int countOfStories = players[guessedPlayer.getNumber() - 1].getCountOfStories();      // Count of stories of otherPlayer
        int actualStoryNumber = 0;
        String story;

        // factor is for calculating a number of a story in the range of the count of stories
        for (int tmp = countOfStories; 0 < tmp; tmp /= 10) {
            factor *= 10;
        }

        Log.e("chooseRandomStory", "Ausgewaehlte SpielerId: " + playerIds[guessedPlayer.getNumber() - 1] + ", suchen dieses Spielers " + listOfStories.get(actualStoryNumber).playerId + ", actualStoryNumber: " + actualStoryNumber);

        // Set used variable
        while (listOfStories.get(actualStoryNumber).playerId != playerIds[guessedPlayer.getNumber() - 1]) {       // Find the right player
            actualStoryNumber++;
            Log.e("endlosschleife", "Endlosschleife3: " + "Ausgewaehlter Spieler: " + playerIds[guessedPlayer.getNumber() - 1] + ", suchen dieses Spielers " + listOfStories.get(actualStoryNumber).playerId + ", actualStoryNumber: " + actualStoryNumber);
        }

        // Choose randomly a story
        do {
            storyNumber = (int) (Math.random() * factor);
            if (storyNumber > 0 && storyNumber <= countOfStories)
                Log.e("endlosschleife", "Endlosschleife4: " + actualStoryNumber + ", " + storyNumber + ", " + listOfStories.get(actualStoryNumber + storyNumber - 1).status);
            else
                Log.e("endlosschleife", "Endlosschleife4: " + actualStoryNumber + ", " + storyNumber);
        } while (storyNumber <= 0 || storyNumber > countOfStories || listOfStories.get(actualStoryNumber + storyNumber - 1).status);

        // Save story in variable story
        try {
            story = players[guessedPlayer.getNumber() - 1].getStory(storyNumber - 1);
        } catch (GamerException ex) {
            ex.printStackTrace();
            Log.e("ChooseRandomStory", Arrays.toString(ex.getStackTrace()) + ", Message: " + ex.getMessage());
            story = ex.toString();
        }

        // Set used variable
        actualStoryNumber += storyNumber - 1;       // Has now the value of the actual story in the List "players[otherPlayer.getNumber() - 1]"

        if (actualStoryNumber >= 0 && actualStoryNumber < listOfStories.size()) {
            actualStoryNumberInList = actualStoryNumber;
        } else
            Toast.makeText(this, "Fehler, Story konnte nicht auf benutzt gesetzt werden!", Toast.LENGTH_SHORT).show();

        this.actualStoryNumber = storyNumber;

        return story;
    }

    private void updateAGame(int roundNumber, String drink) {

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // Set new values
        actualGame.roundNumber = roundNumber;
        actualGame.actualDrinkOfTheGame = drink;
        db.gameDao().updateGame(actualGame);

        // Close database connection
        db.close();
    }

    private void updateAPlayer(int i) { // Update a player in the database
        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // Set new values
        switch (actualGame.actualDrinkOfTheGame) {
            case "Bier":
                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers + 1, listOfPlayers.get(i).countOfVodka, listOfPlayers.get(i).countOfTequila, listOfPlayers.get(i).countOfGin, listOfPlayers.get(i).countOfLiqueur);
                break;
            case "Vodka Shots":
                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers, listOfPlayers.get(i).countOfVodka + 1, listOfPlayers.get(i).countOfTequila, listOfPlayers.get(i).countOfGin, listOfPlayers.get(i).countOfLiqueur);
                break;
            case "Tequila":
                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers, listOfPlayers.get(i).countOfVodka, listOfPlayers.get(i).countOfTequila + 1, listOfPlayers.get(i).countOfGin, listOfPlayers.get(i).countOfLiqueur);
                break;
            case "Gin Shot":
                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers, listOfPlayers.get(i).countOfVodka, listOfPlayers.get(i).countOfTequila, listOfPlayers.get(i).countOfGin + 1, listOfPlayers.get(i).countOfLiqueur);
                break;
            case "Jaegermeister":
                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers, listOfPlayers.get(i).countOfVodka, listOfPlayers.get(i).countOfTequila, listOfPlayers.get(i).countOfGin, listOfPlayers.get(i).countOfLiqueur + 1);
                break;
        }

        // Close database connection
        db.close();
    }

    // Adjust later
    private void updateAPlayer(int playerNumber, int score, int countOfBeers, int countOfVodka, int countOfTequila, int countOfGin, int countOfLiqueur) {     // Add later: "int typeOfDrink

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // Set new values
        listOfPlayers.get(playerNumber).score = score;
        listOfPlayers.get(playerNumber).countOfBeers = countOfBeers; // Adjust later
        listOfPlayers.get(playerNumber).countOfVodka = countOfVodka;
        listOfPlayers.get(playerNumber).countOfTequila = countOfTequila;
        listOfPlayers.get(playerNumber).countOfGin = countOfGin;
        listOfPlayers.get(playerNumber).countOfLiqueur = countOfLiqueur;
        db.playerDao().updatePlayer(listOfPlayers.get(playerNumber));

        // Close database connection
        db.close();
    }

    private void updateAStory(int actualStoryNumberInList, boolean status, boolean guessedStatus, String name) {

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // Set new values
        if (!name.isEmpty()) // Set guessing Person in story
            listOfStories.get(actualStoryNumberInList).guessingPerson = name;

        listOfStories.get(actualStoryNumberInList).status = status; // Set guessed story to used = true
        listOfStories.get(actualStoryNumberInList).guessedStatus = guessedStatus;
        db.storyDao().updateStory(listOfStories.get(actualStoryNumberInList));

        // Close database connection
        db.close();

        // Delete story from list listOfStories
        listOfStories.remove(actualStoryNumberInList);
    }

    private void requestToChangeDrink() {
        allPlayersGuessed = true;       // All players guessed one time

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Getr\u00e4nk wechseln")
                .setMessage("Soll der Drink des Spiels gewechselt werden?\nBei Aufl\u00f6sung der n\u00e4chsten Story wird der Drink gewechselt.")
                .setPositiveButton("Ja", (dialog, which) -> showDrinkSelection(Gamer.drinks))
                .setNegativeButton("Nein", (dialogInterface, i) -> {
                });
        builder.create().show();
    }

    private void changeDrink(String drink) {
        updateAGame(actualGame.roundNumber, drink);
    }

    private void setDrinkOfTheGame(String drink) {
        switch (drink) {
            case "Bier":
                actualDrinkOfTheGame = "ein Bier";
                break;
            case "Vodka Shots":
                actualDrinkOfTheGame = "einen Vodka Shot";
                break;
            case "Tequila":
                actualDrinkOfTheGame = "einen Tequila";
                break;
            case "Gin Shot":
                actualDrinkOfTheGame = "einen Gin Shot";
                break;
            case "Jaegermeister":
                actualDrinkOfTheGame = "einen Jaegermeister Shot";
                break;
            default:
                actualDrinkOfTheGame = "Error: Index zu hoch, dieser Drink ist nicht vorhanden!";
                break;
        }

        drink = "Das Getränk des Spiels ist: " + drink;
        drinkOfTheGameTextView.setText(drink);
    }

    public void showDrinkSelection(List<String> drinks) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View popUpView = getLayoutInflater().inflate(R.layout.popup, null);
        listDrink = (ListView) popUpView.findViewById(R.id.listDrink);
        save = (Button) popUpView.findViewById(R.id.save);
        back = (Button) popUpView.findViewById(R.id.back);

        // set list
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, drinks);
        listDrink.setAdapter(adapter);

        listDrink.setOnItemClickListener((adapterView, view, i, l) -> {

            save.setOnClickListener(v -> {
                newDrinkOfTheGame = drinks.get(i);
                dialog.dismiss();
            });

            back.setOnClickListener(v -> dialog.dismiss());
        });

        dialogBuilder.setView(popUpView);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    @SuppressLint("LongLogTag")
    private boolean checkRound() {      // Checks if at least one player has an unused story
        boolean nextRound = false, proofCheckPlayer = false;        // nextRound = true: another round can be played
        // proofCheckPlayer: proof, whether checkPlayer is different to the last
        // remaining players in editedPlayers (case: storyPlayer = 1)
        int storyPlayers = 0;      // storyPlayer = count of players with at least one story
        Gamer checkPlayer = null;

        for (Gamer gamer : editedPlayers) {
            if (gamer != null)
                Log.e("checkRound: check editedPlayers", gamer.getName());
        }

        for (Gamer gamer : players) {      // Check ALL players
            if (gamer.getCountOfStories() != 0) {
                nextRound = true;
                storyPlayers++;

                if (storyPlayers == 1)
                    checkPlayer = gamer;
            }
        }

        if (checkPlayer != null)
            Log.e("checkRound", "storyPlayer: " + storyPlayers + ", Spieler" + checkPlayer.getNumber() + ", " + checkPlayer.getName() + ", CountOfStories: " + checkPlayer.getCountOfStories());
        else
            Log.e("checkRound", "storyPlayer: " + storyPlayers);

        if (storyPlayers == 1) {     // Check, if the last player with a story is not as the only one contained in editedPlayers
            boolean checkEmptyArray = true;         // true: all fields of editedPlayers are null
            int i = 0;

            Log.e("checkRound", "EditedPlayers: " + editedPlayers.length);

            for (Gamer editedPlayer : editedPlayers) {
                if (editedPlayer != null) {
                    checkEmptyArray = false;
                    break;
                }
            }

            if (checkEmptyArray) {          // Case: "checkEmptyArray == true"
                editedPlayers = Gamer.copyPlayers(players);
                allPlayersGuessed = true;
            }

            while (!proofCheckPlayer && i < editedPlayers.length) {       // nextRound have to be true, if this line is accessed
                //Log.e("checkRound", editedPlayers[i] + ", " + editedPlayers[i].getNumber() + ", " + checkPlayer.getNumber());
                if (editedPlayers[i] != null && editedPlayers[i].getNumber() == checkPlayer.getNumber())        // Searches checkPlayer in editedPlayers
                    proofCheckPlayer = true;
                else
                    i++;
                //Log.e("checkRound", "PlayerNumber: " + editedPlayers[i].getNumber() + ", i: " + i + ", proofCheckPlayer: " + proofCheckPlayer);
            }

            //Log.e("checkRound", editedPlayers[0].getNumber() + ", " + editedPlayers[1].getNumber() + ", " + editedPlayers[2].getNumber() + ", " + editedPlayers[i].getNumber());

            //Log.e("checkRound", "proofCheckPlayer: " + proofCheckPlayer);

            if (proofCheckPlayer/* && i != editedPlayers.length*/) {        // case: editedPlayers[i].getNumber() == checkPlayer.getNumber()
                editedPlayers = Gamer.copyPlayers(players);
                editedPlayers[i] = null;        // The player with the last story/stories can not guess
                Log.e("checkRound", "Spieler: " + (i + 1));
            }
        }

        return nextRound;
    }

    /**
     * Use a semaphore of class PlayGame to wait for sth. Another class can terminate this
     * waiting-status with using the method "releasePlayGame()" (releasing the semaphore).
     */
    private void waitForSth(int i) throws InterruptedException {
        semPlayGame.acquire(i);
    }

    /**
     * Terminates waiting status of class PlayGame (release a semaphore).
     */
    public static void releasePlayGame() {
        if (semPlayGame.availablePermits() >= 0)
            semPlayGame.release();
        else
            throw new UnsupportedOperationException("Cannot perform releasing of semaphore, permits: " + semPlayGame.availablePermits());
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spiel beenden")
                .setMessage("Das Spiel wird zwischengespeichert")
                .setPositiveButton("Verlassen & speichern", (dialog, which) -> {
                    // No safe, which player is currently guessing and which player has already guessed
                    Intent mainActivity = new Intent(PlayGame.this, MainActivity.class);
                    startActivity(mainActivity);
                    finish();
                })
                .setNegativeButton("Weiterspielen", (dialogInterface, i) -> {

                });

        builder.create().show();
    }


    private void checkAll() {
        for (int i = 0; i < listOfPlayers.size(); i++) {
            Log.e("checkAllPlayers", "PlayerId: " + listOfPlayers.get(i).playerId + ", Spieler" +
                    listOfPlayers.get(i).playerNumber + " " + listOfPlayers.get(i).name +
                    ", Score: " + listOfPlayers.get(i).score + ":" + listOfPlayers.get(i).countOfBeers +
                    ", GameId: " + listOfPlayers.get(i).gameId);
        }

        for (int i = 0; i < listOfStories.size(); i++) {
            Log.e("checkAllStories", "PlayerId der Story: " + listOfStories.get(i).playerId + ", StoryId: " +
                    listOfStories.get(i).storyId + ", Content: " + listOfStories.get(i).content +
                    ", Story benutzt?: " + listOfStories.get(i).status + ", Rater: " +
                    listOfStories.get(i).guessingPerson + ", Story erraten?:" +
                    listOfStories.get(i).guessedStatus);
        }
    }

    private void checkPlayers(Gamer[] players) {
        for (Gamer gamer : players) {
            Log.e("checkPlayers", "Spieler" + gamer.getNumber() + ", " + gamer.getName() + ", " + gamer.getCountOfStories());
        }
    }
}
