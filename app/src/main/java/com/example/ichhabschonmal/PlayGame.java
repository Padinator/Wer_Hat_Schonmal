package com.example.ichhabschonmal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.database.Player;
import com.example.ichhabschonmal.database.Story;

import java.util.ArrayList;
import java.util.List;

public class PlayGame extends AppCompatActivity {
    private Gamer[] players, editedPlayers;         // players contains all players of the actual game
                                                    // editedPlayers contains all players, who have not guessed yet
    private Gamer chosenPlayer, otherPlayer;        // Define the two playing players here, to proof in button solution, who is which one
    private Game actualGame;
    private List<Player> listOfPlayers;             // Contains all players of the actual game, listOfPlayers has access to the database
    private List<Story> listOfStories;              // Contains lal stories of the actual game, listOfStories has access to the database
    private TextView player, story, round, drinkOfTheGameTextView;
    private Spinner chooseAPlayer, drinkVariantsTwo;        // spin is used to select a player
    private AppDatabase db;
    private String actualDrinkOfTheGame;
    private int[] playerIds, storyIds;
    private boolean solutionPressed = false, allPlayersGuessed = false;
                    // solutionPressed: before next round begins, Button solution may not been pressed
                    // allPlayersGuessed: true means that all players guessed one time
    private int idOfFirstPlayer, countOfPlayers, idOfFirstStory, countOfStories, gameId, roundNumber;
    private int actualStoryNumberInList, actualStoryNumber;     // actualStoryNumber is a counter to set stories to used

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);

        // Preconditions before playing, until method "playGame()" is called
        // Definitions
        Button solution, nextRound;
        List<String> listOfPlayersForSpinner;
        ArrayAdapter<String> adapter;
        ArrayList<String> drinks = new ArrayList<>();
        boolean gameIsLoaded;
        int checkStoryIds;

        // Buttons
        solution = findViewById(R.id.solution);
        nextRound = findViewById(R.id.nextRound);
        drinkOfTheGameTextView = findViewById(R.id.drinkOfTheGame);

        // TextViews
        player = findViewById(R.id.player);
        story = findViewById(R.id.story);
        round = findViewById(R.id.round);

        // Get from last intent
        gameId = getIntent().getExtras().getInt("GameId");
        gameIsLoaded = getIntent().getExtras().getBoolean("GameIsLoaded");      // true: game is loaded

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        actualGame = db.gameDao().loadAllByGameIds(new int[] {gameId}).get(0);////////////////////////

        // Set used variables
        idOfFirstPlayer = actualGame.idOfFirstPlayer;
        countOfPlayers = actualGame.countOfPlayers;
        idOfFirstStory = actualGame.idOfFirstStory;
        countOfStories = actualGame.countOfStories;
        roundNumber = actualGame.roundNumber;

        // Find the players' ids belonging to the actual game and their stories
        playerIds = findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        storyIds = findSomethingOfActualGame(idOfFirstStory, countOfStories);

        // Set used variables
        checkStoryIds = storyIds.length;

        // Get players playing the actual game
        listOfPlayers = db.playerDao().loadAllByPlayerIds(playerIds);
        listOfStories = db.storyDao().loadAllByStoryIds(storyIds);

        // Close database connection
        db.close();

        // Case if a game is loaded, only use unused stories
        storyIds = findUnusedStories();

        if (gameIsLoaded)       // If game is loaded, aks to change drinks
            requestToChangeDrink();

        // Set used variable
        setDrinkOfTheGame(actualGame.actualDrinkOfTheGame);

        if (storyIds.length > 0) {      // Continue/start playing game
            listOfStories = db.storyDao().loadAllByStoryIds(storyIds);
            countOfStories = storyIds.length;
            idOfFirstStory = storyIds[0];

            // Save players' numbers and names for Spinner spin
            listOfPlayersForSpinner = new ArrayList<>();

            for (int i = 0; i < listOfPlayers.size(); i++) {
                listOfPlayersForSpinner.add("Spieler " + listOfPlayers.get(i).playerNumber + ", " + listOfPlayers.get(i).name);
            }

            // Save players in a new data structure
            players = saveInNewDataStructure(listOfPlayers, listOfStories);
            editedPlayers = saveInNewDataStructure(listOfPlayers, listOfStories);

            // Create drop down menu for choosing a player
            chooseAPlayer = findViewById(R.id.chooseAPlayer);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listOfPlayersForSpinner);
            chooseAPlayer.setAdapter(adapter);

            // Create drop down menu for choosing a drink
            drinkVariantsTwo = findViewById(R.id.drinkVariantsTwo);
            drinks.add("Bier");
            drinks.add("Vodka Shots");
            drinks.add("Tequila");
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, drinks);
            drinkVariantsTwo.setAdapter(adapter);

            if (checkRound()) {             // Play a game
                playGame();
            } else {
                Intent end = new Intent(PlayGame.this, EndScore.class);
                end.putExtra("GameId", gameId);
                startActivity(end);
                finish();
            }
        } else {        // The game is already over
            Intent end = new Intent(PlayGame.this, EndScore.class);
            end.putExtra("GameId", gameId);
            startActivity(end);
            finish();
        }


        solution.setOnClickListener(view -> {
            if (!solutionPressed) {     // Solution may not been pressed
                String correctInput = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName();
                String /*winner = "",*/ loser = "";
                int i = 0;

                if (allPlayersGuessed) {            // Change actual drink of the game, if all players guessed one time
                    changeDrink(drinkVariantsTwo.getSelectedItem().toString());
                    setDrinkOfTheGame(drinkVariantsTwo.getSelectedItem().toString());
                    Log.e("actualDrinkOfTheGame", actualGame.actualDrinkOfTheGame + ", " + actualDrinkOfTheGame);
                    allPlayersGuessed = false;
                }

                // Korrekturbedarfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
                if (chooseAPlayer.getSelectedItem().toString().equals(correctInput)) {       // chosenPlayer has guessed correctly
                    for (; i < listOfPlayers.size(); i++) {     // Wenn kein Spieler gefunden wird -> Exception
                        if (listOfPlayers.get(i).playerNumber == otherPlayer.getNumber()) {

                            // Update a player in the database
                            if (actualGame.actualDrinkOfTheGame.equals("Bier")) {
                                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers + 1, listOfPlayers.get(i).countOfVodka, listOfPlayers.get(i).countOfTequila);
                            } else if (actualGame.actualDrinkOfTheGame.equals("Vodka Shots")) {
                                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers, listOfPlayers.get(i).countOfVodka + 1, listOfPlayers.get(i).countOfTequila);
                            } else if (actualGame.actualDrinkOfTheGame.equals("Tequila")) {
                                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers, listOfPlayers.get(i).countOfVodka, listOfPlayers.get(i).countOfTequila + 1);
                            }

                            // Update a story in the database
                            updateAStory(actualStoryNumberInList, true, true, chosenPlayer.getName());
                        }
                    }

                    //winner = "Spieler " + chosenPlayer.getNumber() + ", " + chosenPlayer.getName() + " hat diese Runde gewonnen";
                    loser = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName() + " hat diese Runde verloren und muss " + actualDrinkOfTheGame + " trinken!";
                } else {        // chosenPlayer has not guessed correctly
                    for (; i < listOfPlayers.size(); i++) {     // Wenn kein Spieler gefunden wird -> Exception
                        if (listOfPlayers.get(i).playerNumber == chosenPlayer.getNumber()) {

                            // Update a player in the database
                            if (actualGame.actualDrinkOfTheGame.equals("Bier")) {
                                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers + 1, listOfPlayers.get(i).countOfVodka, listOfPlayers.get(i).countOfTequila);
                            } else if (actualGame.actualDrinkOfTheGame.equals("Vodka Shots")) {
                                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers, listOfPlayers.get(i).countOfVodka + 1, listOfPlayers.get(i).countOfTequila);
                            } else if (actualGame.actualDrinkOfTheGame.equals("Tequila")) {
                                updateAPlayer(i, listOfPlayers.get(i).score + 1, listOfPlayers.get(i).countOfBeers, listOfPlayers.get(i).countOfVodka, listOfPlayers.get(i).countOfTequila + 1);
                            }

                            // Update a story in the database
                            updateAStory(actualStoryNumberInList, true, false, chosenPlayer.getName());
                        }
                    }

                    //winner = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName() + " hat diese Runde gewonnen";
                    loser = "Spieler " + chosenPlayer.getNumber() + ", " + chosenPlayer.getName() + " hat diese Runde verloren und muss " + actualDrinkOfTheGame + " trinken!";
                }

                // Delete story in the List "players"
                players[otherPlayer.getNumber() - 1].deleteStory(actualStoryNumber - 1);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("ERGEBNIS")
                        .setMessage(loser)
                        .setPositiveButton("OK", (dialog, which) -> {

                        });
                builder.create().show();

                // Set used variable
                solutionPressed = true;

                if (!checkRound())      // Set text of button nextRound, if there is no round left
                    nextRound.setText("Spielende");
            } else
                Toast.makeText(PlayGame.this, "Starte zuerst die n\u00e4chste Runde!", Toast.LENGTH_SHORT).show();
        });

        nextRound.setOnClickListener(view -> {
            if (solutionPressed) {          // Button solution has to be pressed
                Intent next = new Intent(PlayGame.this, Score.class);
                Intent end = new Intent(PlayGame.this, EndScore.class);

                if (checkRound()) {         // Show score and then play next round

                    // Update a game
                    updateAGame(++roundNumber, actualGame.actualDrinkOfTheGame);

                    // Start Score-intent
                    next.putExtra("GameId", gameId);
                    startActivity(next);
                    playRound();
                } else {        // New intent with end score

                    // Start EndScore-intent
                    end.putExtra("GameId", gameId);
                    startActivity(end);
                    finish();       // Game is over
                }

                // Change value of solutionPressed
                solutionPressed = false;
            } else
                Toast.makeText(PlayGame.this, "Du musst zuerst auflösen!", Toast.LENGTH_SHORT).show();
        });
    }

    public static int[] findSomethingOfActualGame(int idOfFirstSomething, int countOfSomething) {     // Something can be "Player" or "Story"
        int[] idsOfSomething = new int[countOfSomething];

        for (int i = 0; i < countOfSomething; i++) {
            idsOfSomething[i] = idOfFirstSomething;
            idOfFirstSomething++;
        }
      
        return idsOfSomething;
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

    // Save players of listOfPlayers in the List players
    private Gamer[] saveInNewDataStructure(List<Player> listOfPlayers, List<Story> listOfStories) {
        Gamer[] players = new Gamer[countOfPlayers];
        int oldPlayerId = idOfFirstPlayer;
        int storyCounter = 0;

        Log.e("saveInNewDataStructure",  "CountOfPlayers: " + countOfPlayers + ", IdOfFirstPlayer: " + idOfFirstPlayer);
        for (int i = 0; i < countOfPlayers; i++) {

            if (storyCounter < listOfStories.size())
            Log.e("saveInNewDataStructure", "Spieler" + listOfPlayers.get(i).playerNumber + ", " + listOfPlayers.get(i).name + ", aktuell erste Story, dessen PlayerId: " + listOfStories.get(storyCounter).playerId);
            // Create a new player in the list
            players[i] = new Gamer(listOfPlayers.get(i).playerNumber);
            players[i].setName(listOfPlayers.get(i).name);

            // Insert stories of a player
            for (; storyCounter < countOfStories
                    && oldPlayerId == listOfStories.get(storyCounter).playerId ; storyCounter++) {
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
        if (Gamer.isEmpty(editedPlayers)) {      // Enter, if each player has guessed one time
            editedPlayers = Gamer.copyPlayers(players);

            // Request to change drink
            requestToChangeDrink();
        }

        // Choose randomly a player to guess someone's story
        chosenPlayer = chooseRandomPlayerWhoGuesses();

        // Choose a story of an other player than chosenPlayer
        otherPlayer = chooseRandomPlayerToBeGuessed();           // First choose another player
        String chosenStory = chooseRandomStory();         // Then choose randomly story of otherPlayer

        // Set TextViews
        round.setText("Runde Nr." + roundNumber);
        player.setText("Spieler " + chosenPlayer.getNumber() + ": " + chosenPlayer.getName() + " ist an der Reihe");
        story.setText(chosenStory);

        //Log.e("bbb", "Raten:" + chosenPlayer.getName() + ", " + chosenPlayer.getNumber() + ", " + chosenPlayer.getCountOfStories());        // Count of stories has not been adjusted in editedPlayers
        //Log.e("bbb", "Erraten werden: " + otherPlayer.getName() + ", " + otherPlayer.getNumber() + ", " + otherPlayer.getCountOfStories());

        // Save players' numbers and names for Spinner spin
        List<String> listOfPlayersForSpinner = new ArrayList<>();

        for (int i = 0; i < listOfPlayers.size(); i++) {
            if (listOfPlayers.get(i).playerNumber != chosenPlayer.getNumber())
                listOfPlayersForSpinner.add("Spieler " + listOfPlayers.get(i).playerNumber + ", " + listOfPlayers.get(i).name);
        }

        // Set Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listOfPlayersForSpinner);
        chooseAPlayer.setAdapter(adapter);

        // chosenPlayer may guess again, when all players have guessed
        editedPlayers[chosenPlayer.getNumber() - 1] = null;
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
        } while (playerNumber <= 0 || playerNumber > editedPlayers.length|| editedPlayers[playerNumber - 1] == null);       // If a player is null, he has already guessed

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
            if (playerNumber > 0 && playerNumber <= players.length && playerNumber != chosenPlayer.getNumber())
                Log.e("endlosschleife", "Endlosschleife2, Bedingung passt: " + "Spieler: " + playerNumber + ", " + players[playerNumber - 1].getCountOfStories());
            else if (chosenPlayer.getNumber() == playerNumber)
                Log.e("endlosschleife", "Endlosschleife2, playerNumber == chosenPlayer.getNumber(): " + "playerNumber: " + playerNumber + ", chosenPlayerNumber: " + chosenPlayer.getNumber() + ", Storyzahl: " + players[chosenPlayer.getNumber() - 1].getCountOfStories());
            //else
                //Log.e("endlosschleife", "Endlosschleife2, Bedingungen passen nicht, zufaellige Spielerauswahl will nicht mehr: " + playerNumber);
        } while (playerNumber == chosenPlayer.getNumber() || playerNumber <= 0 || playerNumber > players.length
                || players[playerNumber - 1].getCountOfStories() == 0);     // If a player has no stories, he can not be chosen to be guessed

        return players[playerNumber - 1];
    }

    private String chooseRandomStory() {        // Choose randomly a story of otherPlayer
        int storyNumber, factor = 1;            // storyNumber = storyNumber in Array listOfStories (see class Gamer), storyNumber is not an index
        int countOfStories = players[otherPlayer.getNumber() - 1].getCountOfStories();      // Count of stories of otherPlayer
        int actualStoryNumber = 0;
        String story;

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // factor is for calculating a number of a story in the range of the count of stories
        for (int tmp = countOfStories; 0 < tmp; tmp /= 10) {
            factor *= 10;
        }

        Log.e("chooseRandomStory", "Ausgewaehlte SpielerId: " + playerIds[otherPlayer.getNumber() - 1] + ", suchen dieses Spielers " + listOfStories.get(actualStoryNumber).playerId + ", actualStoryNumber: " + actualStoryNumber);

        // Set used variable
        while (listOfStories.get(actualStoryNumber).playerId != playerIds[otherPlayer.getNumber() - 1]) {       // Find the right player
            actualStoryNumber++;
            Log.e("endlosschleife", "Endlosschleife3: " + "Ausgewaehlter Spieler: " + playerIds[otherPlayer.getNumber() - 1] + ", suchen dieses Spielers " + listOfStories.get(actualStoryNumber).playerId + ", actualStoryNumber: " + actualStoryNumber);
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
        story = players[otherPlayer.getNumber() - 1].getStory(storyNumber - 1);

        // Set used variable
        actualStoryNumber += storyNumber - 1;       // Has now the value of the actual story in the List "players[otherPlayer.getNumber() - 1]"

        if (actualStoryNumber >= 0 && actualStoryNumber < listOfStories.size()) {
            actualStoryNumberInList = actualStoryNumber;
        } else        // Exception handling, value of actualStoryNumber is false
            Toast.makeText(this, "Fehler, Story konnte nicht auf benutzt gesetzt werden!", Toast.LENGTH_SHORT).show();

        this.actualStoryNumber = storyNumber;

        return story;
    }

    private void updateAGame(int roundNumber, String drink) {

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        actualGame.roundNumber = roundNumber;
        actualGame.actualDrinkOfTheGame = drink;
        db.gameDao().updateGame(actualGame);

        // Close database connection
        db.close();
    }

    // Adjust later
    private void updateAPlayer(int playerNumber, int score, int countOfBeers, int countOfVodka, int countOfTequila) {     // Add later: "int typeOfDrink

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        listOfPlayers.get(playerNumber).score = score;
        listOfPlayers.get(playerNumber).countOfBeers = countOfBeers;          // Adjust later
        listOfPlayers.get(playerNumber).countOfVodka = countOfVodka;
        listOfPlayers.get(playerNumber).countOfTequila = countOfTequila;
        db.playerDao().updatePlayer(listOfPlayers.get(playerNumber));

        // Close database connection
        db.close();
    }

    private void updateAStory(int actualStoryNumberInList, boolean status, boolean guessedStatus, String name) {

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        listOfStories.get(actualStoryNumberInList).status = status;     // Set guessed story to used = true
        listOfStories.get(actualStoryNumberInList).guessingPerson = name;       // Set guessing Person in story
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
                .setPositiveButton("Ja", (dialog, which) -> {
                    drinkVariantsTwo.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("Nein", (dialogInterface, i) -> {

                });
        builder.create().show();
    }

    private void changeDrink(String drink) {
        updateAGame(actualGame.roundNumber, drink);
        drinkVariantsTwo.setVisibility(View.INVISIBLE);
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
            default:
                actualDrinkOfTheGame = "Error Index zu hoch, dieser Drink ist nicht vorhanden!";
                break;
        }

        drinkOfTheGameTextView.setText(drink);
    }

    @SuppressLint("LongLogTag")
    private boolean checkRound() {      // Checks if at least one player has an unused story
        boolean nextRound = false, proofCheckPlayer = false;        // nextRound = true: another round can be played
                                        // proofCheckPlayer: proof, whether checkPlayer is different to the last
                                        // remaining players in editedPlayers (case: storyPlayer = 1)
        int storyPlayers = 0;      // storyPlayer = count of players with at least one story
        Gamer checkPlayer = null;

        for (Gamer gamer: editedPlayers) {
            if (gamer != null)
                Log.e("checkRound: check editedPlayers", gamer.getName());
        }

        for (Gamer gamer: players) {      // Check ALL players
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

            for (Gamer editedPlayer: editedPlayers) {
                if (editedPlayer != null)
                    checkEmptyArray = false;
            }

            if (checkEmptyArray)        // Case: "checkEmptyArray == true"
                editedPlayers = Gamer.copyPlayers(players);

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
                Log.e("checkRound", "Spieler: " + (i+1) );
            }
        }

        return nextRound;
    }

    @Override
    public void onBackPressed() {       // Catch back button
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Spiel beenden")
                .setMessage("Das Spiel wird zwischengespeichert")
                .setPositiveButton("Verlassen und speichern", (dialog, which) -> {
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

        for (int i = 0; i <listOfStories.size(); i++) {
            Log.e("checkAllStories", "PlayerId der Story: " + listOfStories.get(i).playerId + ", StoryId: " +
                    listOfStories.get(i).storyId + ", Content: " + listOfStories.get(i).content +
                    ", Story benutzt?: " + listOfStories.get(i).status + ", Rater: " +
                    listOfStories.get(i).guessingPerson + ", Story erraten?:" +
                    listOfStories.get(i).guessedStatus);
        }
    }

    private void checkPlayers(Gamer[] players) {
        for (Gamer gamer :players) {
            Log.e("checkPlayers", "Spieler" + gamer.getNumber() + ", " + gamer.getName() + ", " + gamer.getCountOfStories());
        }
    }
}
