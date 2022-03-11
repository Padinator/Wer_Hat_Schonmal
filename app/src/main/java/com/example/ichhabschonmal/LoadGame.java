package com.example.ichhabschonmal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.ichhabschonmal.adapter.LoadGameAdapter;
import com.example.ichhabschonmal.database.AppDatabase;

public class LoadGame extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.load_game);

        // Definitions
        RecyclerView recyclerView;
        LoadGameAdapter loadGameAdapter;
        AppDatabase db;

        // Create database connection
        db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // LoadGameAdapter
        loadGameAdapter = new LoadGameAdapter(this, db);
        recyclerView.setAdapter(loadGameAdapter);

        // Close database connection
        db.close();
    }
}
