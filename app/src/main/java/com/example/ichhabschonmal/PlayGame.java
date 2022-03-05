package com.example.ichhabschonmal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class PlayGame extends AppCompatActivity {

    //private int firstPlayerId = getIntent().getExtras().getInt("FirstPlayer");
    //private int numberOfPlayers = getIntent().getExtras().getInt("NumberOfPlayers");
    private int firstStoryId = getIntent().getExtras().getInt("FirstStory");
    private int numberOfStories = getIntent().getExtras().getInt("NumberOfStories");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);

        Button solution, nextRound;
        TextView popupText, player, story;
        EditText guessPlayer;

        // Buttons:
        solution = findViewById(R.id.solution);
        nextRound = findViewById(R.id.nextRound);

        // TextViews:
        //popupText = findViewById(R.id.popupText);
        player = findViewById(R.id.player);
        story = findViewById(R.id.story);

        // EditTexts:
        guessPlayer = findViewById(R.id.guessPlayer);

        //PopupMenus:
        //popupMenu = new PopupMenu(getApplicationContext(), popupText);

        /*
        // Find all players:
        int[] playerIds = new int[numberOfPlayers];

        int tmp = 0;
        for (int i = firstPlayerId; i < firstPlayerId + numberOfPlayers; i++) {
            playerIds[tmp] = i;
            tmp++;
        }*/

        // Database connection:
        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        //List<Game> listOfGames = db.gamesDao().getAll();
        //List<Player> listOfPlayers = db.userDao().loadAllByPlayerIds(playerIds);
        //List<Story> listOfStories = db.storyDao().getAll();

        /*// Choose player, who guess:
        Player playerWhoGuesses = chooseRandomPlayer(listOfPlayers, -1);

        // Choose player, whose story should be guess:
        Player playerToGuess = chooseRandomPlayer(listOfPlayers, playerWhoGuesses.playerId);

        // Choose one of playerToGuess's stories:
        Story chosenStory = chooseRandomStory();

        // Set TextViews:
        player.setText("Spieler " + playerWhoGuesses.playerNumber + ": " + playerWhoGuesses.name);
        story.setText(playerToGuess.);*/

        // Find all stories
        int[] storyIds = new int[numberOfStories];

        int tmp = 0;
        for (int i = firstStoryId; i < firstStoryId + numberOfStories; i++) {
            storyIds[tmp] = i;
            tmp++;
        }

        // List stories
        List<Story> listOfStories = db.storyDao().loadAllByStoryIds(storyIds);

        // List players
        List<Player> listOfAllPlayers = db.userDao().getAll();

        // Choose story to guess
        Story storyToGuess = chooseRandomStory(listOfStories, -1);

        // Choose player, who guess
        Player playerWhoGuess = searchPlayerToStoryId(chooseRandomStory(listOfStories, storyToGuess.storyId).playerId, listOfAllPlayers);

        // Set TextViews:
        player.setText("Spieler: " + playerWhoGuess.name);
        story.setText(storyToGuess.content);

        solution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (guessPlayer.getText().toString() == searchPlayerToStoryId(storyToGuess.playerId, listOfAllPlayers).name)
                    Toast.makeText(PlayGame.this, "Richtig geraten, " + searchPlayerToStoryId(storyToGuess.playerId, listOfAllPlayers).name + "!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(PlayGame.this, "Falsch geraten, du musst trinken!", Toast.LENGTH_SHORT).show();

                storyToGuess.status = true;
            }
        });

        nextRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    // Add commentsssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss
    /*private Player chooseRandomPlayer(List<Player> listOfPlayers, int notId) {
        int factorNumberOfPlayers = 1;
        int chosenPlayer = 0;//Geht so nichttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt

        for (int i = 1; i < String.valueOf(numberOfPlayers).length(); i++) {
            factorNumberOfPlayers *= 10;
        }

        do {
            chosenPlayer = (int) (Math.random() * factorNumberOfPlayers);
        } while (chosenPlayer < 0 || chosenPlayer >= listOfPlayers.size()
                || listOfPlayers.get(chosenPlayer).playerId == notId);

        return listOfPlayers.get(chosenPlayer);
    }*/

    private Story chooseRandomStory(List<Story> listOfStories, int notId) {
        int factorNumberOfStories = 1;
        int chosenStory = 0;//Geht so nichtttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt

        for (int i = 1; i < String.valueOf(numberOfStories).length(); i++) {
            factorNumberOfStories *= 10;
        }

        do {
            chosenStory = (int) (Math.random() * factorNumberOfStories);
        } while (chosenStory < 0 || chosenStory >= listOfStories.size()
                || listOfStories.get(chosenStory).playerId == notId
                || listOfStories.get(chosenStory).status);

        return listOfStories.get(chosenStory);
    }

    private Player searchPlayerToStoryId(int playerId, List<Player> listOfAllPlayers) {
        int i = 0;

        for (; playerId != listOfAllPlayers.get(i).playerId; i++) {}

        return listOfAllPlayers.get(i);
    }
}