package com.example.ichhabschonmal;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoadGame extends AppCompatActivity {

    private LoadGameAdapter loadGameAdapter;
    private Button load;
    private Button delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_game);


        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        List<Game> games = db.gamesDao().getAll();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        List<String> listGames = new ArrayList<>();
        for (int i = 0; i < games.size(); i++) {
            listGames.add(games.get(i).gameName);
            Toast.makeText(this, games.get(i).gameId + " " + games.get(i).gameName, Toast.LENGTH_SHORT).show();
        }


        loadGameAdapter = new LoadGameAdapter(this, listGames, db);
        loadGameAdapter.setClickListener(this::onItemClick);
        recyclerView.setAdapter(loadGameAdapter);

    }

    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + loadGameAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();

    }

}
