package com.example.ichhabschonmal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private TextView player, story, round;
    private Spinner spin;                           // spin is used to select a player
    private AppDatabase db;
    private int[] playerIds, storyIds;
    private boolean solutionPressed = false;        // Before next round begins, Button solution has to be pressed
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

        // Buttons
        solution = findViewById(R.id.solution);
        nextRound = findViewById(R.id.nextRound);

        // TextViews
        player = findViewById(R.id.player);
        story = findViewById(R.id.story);
        round = findViewById(R.id.round);

        // Get from last intent
        gameId = getIntent().getExtras().getInt("GameId");

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        actualGame = db.gameDao().loadAllByGameIds(new int[] {gameId}).get(0);////////////////////////

        // Used variables
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

        // Case if a game is loaded, only use unused stories
        storyIds = findUnusedStories();

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
            spin = findViewById(R.id.drinkVariants);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listOfPlayersForSpinner);
            spin.setAdapter(adapter);

            playGame();             // Play a game
        } else {        // The game is already over
            Intent end = new Intent(PlayGame.this, EndScore.class);
            end.putExtra("GameId", gameId);
            startActivity(end);
            finish();
        }


        solution.setOnClickListener(view -> {
            if (!solutionPressed) {     // Solution may not been pressed
                String correctInput = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName();
                int i = 0;
                String winner, loser = "";

                // Create database connection
                //db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

                // Korrekturbedarfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
                if (spin.getSelectedItem().toString().equals(correctInput)) {       // chosenPlayer has guessed correctly
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

                    winner = "Spieler " + chosenPlayer.getNumber() + ", " + chosenPlayer.getName() + " hat diese Runde gewonnen";
                    loser = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName() + " hat diese Runde verloren!";
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

                    winner = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName() + " hat diese Runde gewonnen";
                    loser = "Spieler " + chosenPlayer.getNumber() + ", " + chosenPlayer.getName() + " hat diese Runde verloren!";
                }

                // Delete story in the List "players"
                players[otherPlayer.getNumber() - 1].deleteStory(actualStoryNumber - 1);

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("ERGEBNIS")
                        .setMessage(winner)
                        .setPositiveButton("OK", (dialog, which) -> {

                        });
                builder.create().show();

                // Change value of solutionPressed
                solutionPressed = true;

                // Close database connection
                //db.close();
            } else
                Toast.makeText(PlayGame.this, "Starte zuerst die nächste Runde!", Toast.LENGTH_SHORT).show();
        });

        nextRound.setOnClickListener(view -> {
            if (solutionPressed) {          // Button solution has to be pressed
                Intent next = new Intent(PlayGame.this, Score.class);
                Intent end = new Intent(PlayGame.this, EndScore.class);

                // Create database connection
                updateAGame(++roundNumber);

                if (checkRound()) {         // Show score and then play next round
                    next.putExtra("GameId", gameId);
                    startActivity(next);
                    playRound();
                } else {            // New intent with end score
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

        for (int i = 0; i < countOfPlayers; i++) {

            // Create a new player in the list
            players[i] = new Gamer(listOfPlayers.get(i).playerNumber);
            players[i].setName(listOfPlayers.get(i).name);

            // Insert stories of a player
            for (; storyCounter < countOfStories
                    && oldPlayerId == listOfStories.get(storyCounter).playerId ; storyCounter++) {
                players[i].addStory(listOfStories.get(storyCounter).content);
            }

            // Set new id for oldStoryId
            if (storyCounter < countOfStories)
                oldPlayerId = listOfStories.get(storyCounter).playerId;
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
        spin.setAdapter(adapter);

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

        checkAll();

        // Choose randomly a player
        do {
            playerNumber = (int) (Math.random() * factor);
            if (playerNumber > 0 && playerNumber <= players.length && playerNumber != chosenPlayer.getNumber())
                Log.e("endlosschleife", "Endlosschleife2, Bedingung passt: " + "Spieler: " + playerNumber + ", " + players[playerNumber - 1].getCountOfStories());
            else if (chosenPlayer.getNumber() == playerNumber)
                Log.e("endlosschleife", "Endlosschleife2, playerNumber == chosenPlayer.getNumber(): " + "playerNumber: " + playerNumber + ", chosenPlayerNumber " + chosenPlayer.getNumber());
            else
                Log.e("endlosschleife", "Endlosschleife2, Bedingungen passen nicht, zufaellige Spielerauswahl will nicht mehr: " + playerNumber);
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

        // Set used variable
        while (listOfStories.get(actualStoryNumber).playerId != playerIds[otherPlayer.getNumber() - 1]) {       // Find the right player
            actualStoryNumber++;
            Log.e("endlosschleife", "Endlosschleife3");
        }

        // Choose randomly a story
        do {
            storyNumber = (int) (Math.random() * factor);
            Log.e("endlosschleife", "Endlosschleife4");
        } while (storyNumber <= 0 || storyNumber > countOfStories || listOfStories.get(actualStoryNumber + storyNumber - 1).status);

        // Save story in variable story
        story = players[otherPlayer.getNumber() - 1].getStory(storyNumber - 1);

        // Set used variable
        actualStoryNumber += storyNumber - 1;       // Has now the value of the actual story in the List "players[otherPlayer.getNumber() - 1]"

        if (actualStoryNumber >= 0 && actualStoryNumber < listOfStories.size()) {
            actualStoryNumberInList = actualStoryNumber;
        } else        // Exception handling
            Toast.makeText(this, "Fehler, Story konnte nicht auf benutzt gesetzt werden!", Toast.LENGTH_SHORT).show();

        this.actualStoryNumber = storyNumber;

        return story;
    }

    private void updateAGame(int roundNumber) {

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        actualGame.roundNumber = roundNumber;
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
    }

    private boolean checkRound() {      // Checks if at least one player has an unused story
        boolean nextRound = false, proofCheckPlayer = false;        // nextRound = true: another round can be played
                                        // proofCheckPlayer: proof, whether checkPlayer is different to the last
                                        // remaining player in editedPlayers (case: storyPlayer = 1)
        int storyPlayer = 0;      // storyPlayer = count of players with at least one story
        //Ist das so/auf diese Weise sinnvolllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll
        Gamer checkPlayer = null;

        for (Gamer gamer : players) {      // Check ALL players
            if (gamer.getCountOfStories() != 0) {
                nextRound = true;
                storyPlayer++;

                if (storyPlayer == 1)
                    checkPlayer = gamer;
            }
        }

        if (storyPlayer == 1) {     // Check, if the last player with a story is not as the only one contained in editedPlayers
            int i = 0;

            for (; !proofCheckPlayer && i < editedPlayers.length; i++) {       // nextRound have to be true, if this line is accessed
                if (editedPlayers[i] != null && editedPlayers[i].getNumber() != checkPlayer.getNumber())        // Searches checkPlayer in editedPlayers
                    proofCheckPlayer = true;
            }

            if (i != editedPlayers.length && !proofCheckPlayer) {        // case: editedPlayers[i].getNumber() == checkPlayer.getNumber()
                editedPlayers = Gamer.copyPlayers(players);
                editedPlayers[i] = null;        // The player with the last story/stories can not guess
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
            Log.e("aaa", listOfPlayers.get(i).playerId + ", Spieler" +
                    listOfPlayers.get(i).playerNumber + " " + listOfPlayers.get(i).name +
                    ", Score: " + listOfPlayers.get(i).score + ":" + listOfPlayers.get(i).countOfBeers +
                    ", GameId: " + listOfPlayers.get(i).gameId);
        }

        for (int i = 0; i <listOfStories.size(); i++) {
            Log.e("aaa", listOfStories.get(i).playerId + ", StoryId: " +
                    listOfStories.get(i).storyId + ", Content: " + listOfStories.get(i).content +
                    ", Story benutzt?: " + listOfStories.get(i).status + ", Rater: " +
                    listOfStories.get(i).guessingPerson + ", Story erraten?:" +
                    listOfStories.get(i).guessedStatus);
        }
    }
}
