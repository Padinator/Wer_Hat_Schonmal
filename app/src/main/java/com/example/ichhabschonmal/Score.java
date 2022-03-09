package com.example.ichhabschonmal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

public class Score extends AppCompatActivity {

    private int idOfFirstPlayer;
    private int countOfPlayers;
    private int idOfFirstStory;
    private int countOfStories;

    private ScoreAdapter scoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score);

        idOfFirstPlayer = getIntent().getExtras().getInt("IdOfFirstPlayer");
        countOfPlayers = getIntent().getExtras().getInt("CountOfPlayers");
        idOfFirstStory = getIntent().getExtras().getInt("IdOfFirstStory");
        countOfStories = getIntent().getExtras().getInt("CountOfStories");

        Button confirm;

        confirm = findViewById(R.id.confirm);

        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        int[] playerIds = new int[countOfPlayers];
        for (int i=0; i<countOfPlayers; i++) {
            playerIds[i] = idOfFirstPlayer + i;
        }
        List<Player> players = db.userDao().loadAllByPlayerIds(playerIds);


        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        scoreAdapter = new ScoreAdapter(this, players);
        recyclerView.setAdapter(scoreAdapter);

        Toast.makeText(this, players.size() + "", Toast.LENGTH_SHORT).show();



        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go back to last intent
                finish();
            }
        });



    }
}
