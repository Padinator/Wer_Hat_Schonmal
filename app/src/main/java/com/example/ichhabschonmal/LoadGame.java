package com.example.ichhabschonmal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
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

        // Set dark mode to none
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

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


        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {       // Catch back button
        Intent mainActivity = new Intent(LoadGame.this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }
}
