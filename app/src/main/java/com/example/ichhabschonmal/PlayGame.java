package com.example.ichhabschonmal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.List;

public class PlayGame extends AppCompatActivity {

    private int idOfFirstPlayer;
    private int countOfPlayers;
    private int idOfFirstStory;
    private int countOfStories;

    private Gamer[] players, editedPlayers;         // players contains all players of the actual game
                                                    // editedPlayers contains all players, who have not guessed yet
    private List<Player> listOfPlayers;         // Contains all players of the actual game, listOfPlayers has access to the database
    private List<Story> listOfStories;          // Contains lal stories of the actual game, listOfStories has access to the database

    private int roundNumber = 1;
    private TextView player, story, round;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);

        // Definitions
        Button solution, nextRound;

        // Buttons
        solution = findViewById(R.id.solution);
        nextRound = findViewById(R.id.nextRound);

        // TextViews
        player = findViewById(R.id.player);
        story = findViewById(R.id.story);
        round = findViewById(R.id.round);

        idOfFirstPlayer = getIntent().getExtras().getInt("IdOfFirstPlayer");
        countOfPlayers = getIntent().getExtras().getInt("CountOfPlayers");
        idOfFirstStory = getIntent().getExtras().getInt("IdOfFirstStory");
        countOfStories = getIntent().getExtras().getInt("CountOfStories");

        // Create database connection
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // Find the players' ids belonging to the actual game and their stories
        int[] playerIds = findSomethingOfActualGame(idOfFirstPlayer, countOfPlayers);
        int[] storyIds = findSomethingOfActualGame(idOfFirstStory, countOfStories);

        // Get players playing the actual game
        listOfPlayers = db.userDao().loadAllByPlayerIds(playerIds);
        listOfStories = db.storyDao().loadAllByStoryIds(storyIds);

        Toast.makeText(getApplicationContext(), idOfFirstPlayer + " : " + countOfPlayers + ", " + idOfFirstStory + " : " + countOfStories, Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), listOfPlayers.size() + " : " + listOfStories.size(), Toast.LENGTH_SHORT).show();
        Toast.makeText(getApplicationContext(), listOfPlayers.get(0).playerId + " : " + listOfPlayers.get(1).playerId + " : " + listOfPlayers.get(2).playerId, Toast.LENGTH_SHORT).show();


        // Save players in a new data structure
        players = saveInNewDataStructure(listOfPlayers, listOfStories);
        editedPlayers = saveInNewDataStructure(listOfPlayers, listOfStories);

        // Create drop down menu for choosing a player
        Spinner spin = findViewById(R.id.dropdown);
        ArrayAdapter<Player> adapter = new ArrayAdapter<Player>(this, android.R.layout.simple_spinner_dropdown_item, listOfPlayers);
        spin.setAdapter(adapter);

        playGame();
        /**/

        solution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        nextRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent next = new Intent(PlayGame.this, Score.class);
                startActivity(next);

                roundNumber++;
                if (checkRound())
                    playRound();
                else
                    Toast.makeText(getApplicationContext(), "Alle Stories sind aufgebraucht, das Spiel ist zu Ende!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int[] findSomethingOfActualGame(int idOfFirstSomething, int countOfSomething) {     // Something can be "Player" or "Story"
        int[] idsOfSomething = new int[countOfSomething];

        for (int i = 0; i < countOfSomething; i++) {
            idsOfSomething[i] = idOfFirstSomething + 1;
            idOfFirstSomething++;
            //Toast.makeText(getApplicationContext(), idOfFirstSomething + "", Toast.LENGTH_SHORT).show();
        }

        return idsOfSomething;
    }

    // Save players of listOfPlayers in the List players
    private Gamer[] saveInNewDataStructure(List<Player> listOfPlayers, List<Story> listOfStories) {
        Gamer[] players = new Gamer[countOfPlayers];
        int oldStoryId = idOfFirstStory;
        int storyCounter = 0;

        for (int i = 0; i < countOfPlayers; i++) {

            // Create a new player
            Gamer player = new Gamer(listOfPlayers.get(i).playerNumber);
            Toast.makeText(getApplicationContext(), listOfPlayers.get(i).playerNumber + "", Toast.LENGTH_SHORT).show();
            player.setName(listOfPlayers.get(i).name);

            // Insert stories of a player
            for (; storyCounter < countOfStories
                    && oldStoryId == listOfStories.get(storyCounter).storyId ; storyCounter++) {
                player.addStory(listOfStories.get(storyCounter).content);
            }

            // Set new id for oldStoryId
            if (storyCounter < countOfStories)
                oldStoryId = listOfStories.get(storyCounter).storyId;

            // Add player to the list
            players[i] = player;
        }

        return players;
    }

    private void playGame() {
        playRound();
    }

    private void playRound() {

        if (Gamer.isEmpty(editedPlayers)) {      // Each player has guessed one time
            editedPlayers = Gamer.copyPlayers(players);
        }

        // Choose randomly a player to guess someone's story
        Gamer chosenPlayer = chooseRandomPlayerWhoGuesses();

        // Choose a story of an other player than chosenPlayer
        Gamer otherPlayer = chooseRandomPlayerToBeGuessed(chosenPlayer.getNumber());           // First choose another player
        Toast.makeText(getApplicationContext(), otherPlayer.getNumber() + "", Toast.LENGTH_SHORT).show();

        /*String chosenStory = chooseRandomStory(otherPlayer);         // Then choose randomly story of otherPlayer

        // Set TextViews:
        round.setText("Runde Nr." + roundNumber);
        player.setText("Spieler " + chosenPlayer.getNumber() + ": " + chosenPlayer.getName() + "ist an der Reihe");
        story.setText(chosenStory);

        // chosenPlayer may guess again, when all players have guessed
        editedPlayers[chosenPlayer.getNumber() - 1] = null;*/

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
        } while (playerNumber <= 0 || playerNumber > editedPlayers.length|| editedPlayers[playerNumber - 1] == null);

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

    private String chooseRandomStory(Gamer otherPlayer) {
        int storyNumber, factor = 1;        // storyNumber = storyNumber in Array listOfStories, see class Gamer
        int i = 0;                          // i is a counter to set stories to used
        int countOfStories = players[otherPlayer.getNumber() - 1].getCountOfStories();      // Count of stories of otherPlayer

        // factor is for calculating a number of a story in the range of the count of stories
        for (int tmp = countOfStories; 0 < tmp; tmp /= 10) {
            factor *= 10;
        }

        // Choose randomly a story
        do {
            storyNumber = (int) (Math.random() * factor);
        } while (storyNumber <= 0 || storyNumber > countOfStories);

        // Set guessed story to used = true
        for (; !(listOfStories.get(i).content.equals(players[otherPlayer.getNumber() - 1].getStory(storyNumber - 1))); i++) {}
        listOfStories.get(i).status = true;

        // Delete story in the List "players"
        players[otherPlayer.getNumber() - 1].deleteStory(storyNumber - 1);

        return players[otherPlayer.getNumber() - 1].getStory(storyNumber);
    }

    private boolean checkRound() {      // Checks if at least one player has an unused story
        boolean nextRound = false, proofCheckPlayer = false;        // nextRound = true: another round can be played
                                        // proofCheckPlayer: proof, whether checkPlayer is different to the last
                                        // remaining player in editedPlayers (case: storyPlayer = 1)
        int storyPlayer = 0;      // storyPlayer = count of players with at least one story
        //Ist das so/auf diese Weise sinnvollllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll:
        Gamer checkPlayer = null;

        for (int i = 0; i < players.length; i++) {      // Check ALL players
            if (players[i].getCountOfStories() != 0) {
                nextRound = true;
                storyPlayer++;

                if (storyPlayer == 1)
                    checkPlayer = players[i];
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
}
