package com.example.ichhabschonmal;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.ichhabschonmal.adapter.LoadGameAdapter;
import com.example.ichhabschonmal.database.AppDatabase;

public class LoadGame extends AppCompatActivity {
    TextView noStoriesSaved;

    @SuppressLint("SetTextI18n")
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


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        noStoriesSaved = findViewById(R.id.noStoriesSaved);
        noStoriesSaved.setText("Anzahl der gespeicherten Stories: " + loadGameAdapter.getItemCount());


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
