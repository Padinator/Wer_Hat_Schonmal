package com.example.ichhabschonmal;

import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.ArrayList;
import java.util.List;

public class LoadGame extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_game);


        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        List<Game> games = db.gamesDao().getAll();


        Toast.makeText(LoadGame.this,  "MIMIMI", Toast.LENGTH_LONG).show();



        Toast.makeText(getApplicationContext(), games.get(0).gameName + "MIMIMI", Toast.LENGTH_LONG).show();
        Toast.makeText(getApplicationContext(), games.get(1).gameName, Toast.LENGTH_LONG).show();









    }

}
