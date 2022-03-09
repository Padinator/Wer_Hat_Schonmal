package com.example.ichhabschonmal;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Game.class, Player.class, Story.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract GameDao gamesDao();
    public abstract PlayerDao userDao();//Andernnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
    public abstract StoryDao storyDao();
}
