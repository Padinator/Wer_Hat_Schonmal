package com.example.ichhabschonmal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ichhabschonmal.adapter.ViewAllStoriesAdapter;
import com.example.ichhabschonmal.database.Story;

public class ViewAllStories extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_all_stories);

        // Definitions
        ViewAllStoriesAdapter viewAllStoriesAdapter;
        Story[] stories;
        int gameId;

        // Get from last intent
        gameId = getIntent().getExtras().getInt("GameId");

        //viewAllStoriesAdapter = new ViewAllStoriesAdapter(this, stories);
    }

    @Override
    public void onBackPressed() {       // Catch back button

    }
}
