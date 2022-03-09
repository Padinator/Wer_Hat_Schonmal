package com.example.ichhabschonmal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

public class LoadGame extends AppCompatActivity {

    private LoadGameAdapter loadGameAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_game);

        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadGameAdapter = new LoadGameAdapter(this, db);
        recyclerView.setAdapter(loadGameAdapter);
    }
}
