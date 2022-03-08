package com.example.ichhabschonmal;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Spinner;
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

    //private Gamer[] players;
    //private File directory;
    static int roundNumber = 1;
    PopupWindow popUp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);


        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        List<Game> listOfGames = db.gamesDao().getAll();
        List<Player> listOfPlayers = db.userDao().getAll();
        List<Story> listOfStories = db.storyDao().getAll();


        Button solution, nextRound;
        TextView popupText, player, story,round,popup;
        //PopupMenu popupMenu;

        // Buttons:
        solution = findViewById(R.id.solution);
        nextRound = findViewById(R.id.nextRound);

        // TextViews:
        //popupText = findViewById(R.id.popupText);
        player = findViewById(R.id.player);
        story = findViewById(R.id.story);
        round = findViewById(R.id.round);

        // display round number of top
        // TODO: roundNumber won't increase by one, after replaced finished(); in confirm Method in Score.java
        round.setText("Runde Nr." + roundNumber);


        //PopupMenus:
        //popupMenu = new PopupMenu(getApplicationContext(), popupText);
        // inflate the layout of the popup window




        CreatePlayers cp = new CreatePlayers();

        // this array must be replaced by the player array!
        String[] test = new String[] {"Tom", "Dom", "Patrick"};



        // create drop down menu for choosing a player
        Spinner spin = findViewById(R.id.dropdown);
        ArrayAdapter<Player> adapter = new ArrayAdapter<Player>(this, android.R.layout.simple_spinner_dropdown_item, listOfPlayers);
        spin.setAdapter(adapter);

        // Collect all players in one Array in this intent
        findAllPlayers();

        // Find first player to play
        //int chosenPlayer = chooseNumber(players.length, 0);        // Choose a player to guess the writer of the story

        // Write name and number of first player in the TextView player
        //player.setText("Spieler " + chosenPlayer + ": " + players[chosenPlayer].getName() + " ist an der Reihe");

        // Write a story in the TextView story
        //int chosenStory = chooseNumber(players.length, chosenPlayer);
        //story.setText(players[chosenPlayer].getStory(chosenStory));


        solution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupWindow popup = new PopupWindow();

                ((TextView)popup.getContentView().findViewById(R.id.popup_id)).setText("hello there");

                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_window, null);

                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;

                // will dismiss, if you tap outside the popup
                boolean focusable = true;
                final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

                // shows the popup
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        });

        nextRound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // verweis auf score
                Intent next = new Intent(PlayGame.this, Score.class);
                startActivity(next);
                roundNumber++;
            }
        });
    }



    private void findAllPlayers() {
        //for (int i = 0; i < listOfPlayers.length; i++) {
        //    createPlayer(i);
        //}
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
