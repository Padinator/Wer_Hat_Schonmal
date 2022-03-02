package com.example.ichhabschonmal;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PlayGame extends AppCompatActivity {

    private Player[] players;
    private File directory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);

        Button solution, nextRound;
        TextView popupText, player, story;
        //PopupMenu popupMenu;

        // Buttons:
        solution = findViewById(R.id.solution);
        nextRound = findViewById(R.id.nextRound);

        // TextViews:
        //popupText = findViewById(R.id.popupText);
        player = findViewById(R.id.player);
        story = findViewById(R.id.story);

        //PopupMenus:
        //popupMenu = new PopupMenu(getApplicationContext(), popupText);

        // Use given values:
        players = new Player[getIntent().getExtras().getInt("countOfPlayers")];
        directory = (File) getIntent().getExtras().get("directory");

        // Collect all players in one Array in this intent
        findAllPlayers();


        // Find first player to play
        int chosenPlayer = chooseNumber(players.length, 0);        // Choose a player to guess the writer of the story

        // Write name and number of first player in the TextView player
        //player.setText("Spieler " + chosenPlayer + ": " + players[chosenPlayer].getName() + " ist an der Reihe");

        // Write a story in the TextView story
        int chosenStory = chooseNumber(players.length, chosenPlayer);
        //story.setText(players[chosenPlayer].getStory(chosenStory));


        solution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //popupMenu.getMenu();
            }
        });

        nextRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void findAllPlayers() {
        for (int i = 0; i < players.length; i++) {
            createPlayer(i);
        }
    }

    private void createPlayer(int number) {     // Find a player's number, name and stories
        /*
        File directory = new File(this.directory.toString() + "/Spieler" + number);
        File[] listOfStories = directory.listFiles();
        String name;

        // Create a player and give him his number
        players[number] = new Player(number + 1);

        // Set a player's name
        name = directory.list()[0];         // name contents the filename of the first story of the chosen player
        name.substring(0, name.indexOf('_'));       // name contains the name of the player
        players[number].setName(name);

        // Find all of a player's stories
        for (int i = 1; i <= listOfStories.length; i++) {
            try {
                FileInputStream fis = new FileInputStream(listOfStories[i]);
                byte[] storyAsBytes = new byte[fis.available()];
                String story = "";

                fis.read(storyAsBytes);         // Save data as bytes in storyAsBytes
                fis.close();

                for (int j = 0; j < storyAsBytes.length; j++) {         // Convert story from byte to string
                    story += (char) storyAsBytes[j];
                }

                players[i].addStory(story);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    private void playRound() {

    }

    // Exceptions bzw. Assertions are missingggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg
    private int chooseNumber(int number, int notNumber) {          // Algorithm to choose a number in a range
            // notNumber is a number, which should not be chosen (is necessary: chosen player
            // should not guess his own story)
        int chosenNumber = 1, factorCalc = 1;

        for (int i = 0; i < String.valueOf(number).length(); i++) {
            factorCalc *= 10;
        }

        do {
            chosenNumber = (int) (Math.random() * factorCalc);
        } while (chosenNumber > number || chosenNumber == 0 || chosenNumber == notNumber);

        return chosenNumber;
    }
}
