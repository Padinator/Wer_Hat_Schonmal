package com.example.ichhabschonmal;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
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
    private List<Player> listOfPlayers;             // Contains all players of the actual game, listOfPlayers has access to the database
    private List<Story> listOfStories;              // Contains lal stories of the actual game, listOfStories has access to the database
    private TextView player, story, round;
    private Spinner spin;                           // spin is used to select a player
    private AppDatabase db;
    private boolean solutionPressed = false;        // Before next round begins, Button solution has to be pressed
    private int idOfFirstPlayer, countOfPlayers, idOfFirstStory, countOfStories, gameId, roundNumber = 1;
    private int actualStoryNumberInList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);

        // Preconditions before playing, until method "playGame()" is called
        // Definitions
        Button solution, nextRound;
        List<String> listOfPlayersForSpinner;
        ArrayAdapter<String> adapter;
        Game actualGame;
        int[] playerIds, storyIds;

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

        // Find the players' ids belonging to the actual game and their stories
        playerIds = findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        storyIds = findSomethingOfActualGame(idOfFirstStory, countOfStories);

        // Get players playing the actual game
        listOfPlayers = db.playerDao().loadAllByPlayerIds(playerIds);
        listOfStories = db.storyDao().loadAllByStoryIds(storyIds);

        // Close database connection
        db.close();

        // Save players' numbers and names for Spinner spin
        listOfPlayersForSpinner = new ArrayList<>();

        for (int i = 0; i < listOfPlayers.size(); i++) {
            listOfPlayersForSpinner.add("Spieler " + listOfPlayers.get(i).playerNumber + ", " + listOfPlayers.get(i).name);
        }

        // Save players in a new data structure
        players = saveInNewDataStructure(listOfPlayers, listOfStories);
        editedPlayers = saveInNewDataStructure(listOfPlayers, listOfStories);

        // Create drop down menu for choosing a player
        spin = findViewById(R.id.dropdown);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listOfPlayersForSpinner);
        spin.setAdapter(adapter);

        // Play a game
        playGame();

        solution.setOnClickListener(view -> {
            if (!solutionPressed) {     // Solution may not been pressed
                String correctInput = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName();
                int i = 0;
                String winner, loser = "";

                // Create database connection
                db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

                // Korrekturbedarfffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff
                if ((spin.getSelectedItem().toString().equals(correctInput))) {       // chosenPlayer has not guessed correctly
                    for (; i < listOfPlayers.size(); i++) {     // Wenn kein Spieler gefunden wird -> Exception
                        if (listOfPlayers.get(i).playerNumber == otherPlayer.getNumber()) {
                            Player player = listOfPlayers.get(i);
                            player.score++;
                            player.countOfBeers++;          // Adjust later
                            listOfPlayers.set(i, player);
                            db.playerDao().updatePlayer(player);
                            listOfStories.get(actualStoryNumberInList).guessedStatus = true;
                            db.storyDao().updateStory(listOfStories.get(actualStoryNumberInList));
                        }
                    }

                    winner = "Spieler " + chosenPlayer.getNumber() + ", " + chosenPlayer.getName() + " hat diese Runde gewonnen";
                    loser = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName() + " hat diese Runde verloren!";
                } else {        // chosenPlayer has guessed correctly
                    for (; i < listOfPlayers.size(); i++) {     // Wenn kein Spieler gefunden wird -> Exception
                        if (listOfPlayers.get(i).playerNumber == chosenPlayer.getNumber()) {
                            Player player = listOfPlayers.get(i);
                            player.score++;
                            player.countOfBeers++;          // Adjust later
                            listOfPlayers.set(i, player);
                            db.playerDao().updatePlayer(player);//////////////////////////////
                            listOfStories.get(actualStoryNumberInList).guessedStatus = false;
                            db.storyDao().updateStory(listOfStories.get(actualStoryNumberInList));
                        }
                    }

                    winner = "Spieler " + otherPlayer.getNumber() + ", " + otherPlayer.getName() + " hat diese Runde gewonnen";
                    loser = "Spieler " + chosenPlayer.getNumber() + ", " + chosenPlayer.getName() + " hat diese Runde verloren!";
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("ERGEBNIS")
                        .setMessage(winner)
                        .setPositiveButton("OK", (dialog, which) -> {

                        });
                builder.create().show();

                // Change value of solutionPressed
                solutionPressed = true;

                // Close database connection
                db.close();
            } else
                Toast.makeText(PlayGame.this, "Starte die nächste Runde!", Toast.LENGTH_SHORT).show();
        });

        nextRound.setOnClickListener(view -> {
            if (solutionPressed) {          // Button solution has to be pressed
                Intent next = new Intent(PlayGame.this, Score.class);
                Intent end = new Intent(PlayGame.this, EndScore.class);

                roundNumber++;
                if (checkRound()) {

                    // Show score and then play next round
                    next.putExtra("GameId", gameId);
                    startActivity(next);
                    playRound();
                } else {

                    // New intent with end score
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
        otherPlayer = chooseRandomPlayerToBeGuessed(chosenPlayer.getNumber());           // First choose another player
        String chosenStory = chooseRandomStory(otherPlayer, chosenPlayer.getName());         // Then choose randomly story of otherPlayer

        // Set TextViews
        round.setText("Runde Nr." + roundNumber);
        player.setText("Spieler " + chosenPlayer.getNumber() + ": " + chosenPlayer.getName() + " ist an der Reihe");
        story.setText(chosenStory);

        //log.e("aaa", "Raten:" + chosenPlayer.getName() + ", " + chosenPlayer.getNumber() + ", " + chosenPlayer.getCountOfStories());
        //log.e("aaa", "Erraten werden: " + otherPlayer.getName() + ", " + otherPlayer.getNumber() + ", " + otherPlayer.getCountOfStories());

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
        } while (playerNumber <= 0 || playerNumber > editedPlayers.length|| editedPlayers[playerNumber - 1] == null);       // If a player is null, he has already guessed

        return editedPlayers[playerNumber - 1];
    }

    private Gamer chooseRandomPlayerToBeGuessed(int notNumber) {
        int playerNumber, factor = 1;

        // factor is for calculating a number of a player in the range of the count of players
        for (int tmp = players.length; 0 < tmp; tmp /= 10) {
            factor *= 10;
        }

        // Choose randomly a player
        do {
            playerNumber = (int) (Math.random() * factor);
        } while (playerNumber == notNumber || playerNumber <= 0 || playerNumber > players.length
                || players[playerNumber - 1].getCountOfStories() == 0);     // If a player has no stories, he can not be chosen to be guessed

        return players[playerNumber - 1];
    }

    private String chooseRandomStory(Gamer otherPlayer, String guessingPerson) {       // Choose randomly a story of otherPlayer
        int storyNumber, factor = 1;        // storyNumber = storyNumber in Array listOfStories (see class Gamer), storyNumber is not an index
        int i = 0;                          // i is a counter to set stories to used
        int countOfStories = players[otherPlayer.getNumber() - 1].getCountOfStories();      // Count of stories of otherPlayer
        String story;

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // factor is for calculating a number of a story in the range of the count of stories
        for (int tmp = countOfStories; 0 < tmp; tmp /= 10) {
            factor *= 10;
        }

        // Choose randomly a story
        do {
            storyNumber = (int) (Math.random() * factor);
        } while (storyNumber <= 0 || storyNumber > countOfStories);

        // Save story in variable story
        story = players[otherPlayer.getNumber() - 1].getStory(storyNumber - 1);

        // Update story in database
        for (; !(listOfStories.get(i).content.equals(players[otherPlayer.getNumber() - 1].getStory(storyNumber - 1))); i++) {}

        if (i >= 0 && i < listOfStories.size()) {

            // Set guessed story to used = true
            listOfStories.get(i).status = true;

            // Set guessing Person in story
            listOfStories.get(i).guessingPerson = guessingPerson;

            db.storyDao().updateStory(listOfStories.get(i));//////////////////////////////////////
            actualStoryNumberInList = i;
        } else        // Exception handling
            Toast.makeText(this, "Fehler, Story konnte nicht auf benutzt gesetzt werden!", Toast.LENGTH_SHORT).show();

        // Delete story in the List "players"
        players[otherPlayer.getNumber() - 1].deleteStory(storyNumber - 1);

        // Close database connection
        db.close();

        return story;
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
                if (editedPlayers[i] != null && editedPlayers[i].getNumber() != checkPlayer.getNumber())
                    proofCheckPlayer = true;
            }

            if (!proofCheckPlayer) {        // case: editedPlayers[i].getNumber() == checkPlayer.getNumber()
                editedPlayers = Gamer.copyPlayers(players);
                editedPlayers[i - 1] = null;        // The player with the last story/stories can not guess
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
}
