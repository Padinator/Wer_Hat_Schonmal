package com.example.ichhabschonmal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ComponentActivity;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rules extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // make Content visible
        setContentView(R.layout.rules);

        Button start;

        start = findViewById(R.id.start);

        listView = (ListView)findViewById(R.id.listView);

        // ArrayList for all Game Rules
        ArrayList <String> rules = new ArrayList<>();
        rules.add("1: bla bla bla");
        rules.add("2: blub blub blub");
        rules.add("3: bli bli bli");
        rules.add("4:");
        rules.add("5:");
        rules.add("6:");
        rules.add("7:");
        rules.add("8:");
        rules.add("9:");
        rules.add("10:");
        rules.add("11:");
        rules.add("12:");

        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,rules);
        listView.setAdapter(adapter);


        // if press on button "weiter" then the game will start
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startGame = new Intent(Rules.this, PlayGame.class);
                int idOfFirstPlayer = getIntent().getExtras().getInt("IdOfFirstPlayer");
                int countOfPlayers = getIntent().getExtras().getInt("CountOfPlayers");
                int idOfFirstStory = getIntent().getExtras().getInt("IdOfFirstStory");
                int countOfStories = getIntent().getExtras().getInt("CountOfStories");

                startGame.putExtra("IdOfFirstPlayer", idOfFirstPlayer);
                startGame.putExtra("CountOfPlayers", countOfPlayers);
                startGame.putExtra("IdOfFirstStory", idOfFirstStory);
                startGame.putExtra("CountOfStories", countOfStories);

                startActivity(startGame);
                finish();
            }
        });





    }


}
