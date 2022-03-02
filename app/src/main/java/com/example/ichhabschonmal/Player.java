package com.example.ichhabschonmal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Player {
    @PrimaryKey(autoGenerate = true)        // Autoincrement is on
    public int playerId;        // Every player has a unique player id

    @ColumnInfo(name = "playerNumber")
    public int playerNumber;        // Every player has a unique number in a game

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "gameId")
    public int gameId;

    @ColumnInfo(name = "score")
    public int score;
}
