package com.example.werhatschonmal.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Game {
    @PrimaryKey(autoGenerate = true)        // Autoincrement is on
    public int gameId;

    @ColumnInfo(name = "gameName")
    public String gameName;

    @ColumnInfo(name = "onlineGame")
    public boolean onlineGame;

    @ColumnInfo(name = "idOfFirstPlayer")
    public int idOfFirstPlayer;

    @ColumnInfo(name = "countOfPlayers")
    public int countOfPlayers;

    @ColumnInfo(name = "idOfFirstStory")
    public int idOfFirstStory;

    @ColumnInfo(name = "countOfStories")
    public int countOfStories;

    @ColumnInfo(name = "roundNumber")
    public int roundNumber;

    @ColumnInfo(name = "actualDrinkOfTheGame")
    public String actualDrinkOfTheGame;
}
