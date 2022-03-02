package com.example.ichhabschonmal;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

public class ViewAllStories extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rules);

        AppDatabase db = Room.databaseBuilder(this, AppDatabase.class, "database").allowMainThreadQueries().build();


        StoryDao storyDao = db.storyDao();



        Story[] stories = new Story[storyDao.getAll().size()];


        for (int i = 0; i < stories.length; i++) {
            if (stories[i] == null) {
                Toast.makeText(ViewAllStories.this, "Ist nichts da", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ViewAllStories.this, stories[i].toString(), Toast.LENGTH_LONG).show();
            }
        }










    }
}
