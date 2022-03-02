package com.example.ichhabschonmal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Game {
    @PrimaryKey(autoGenerate = true)        // Autoincrement is on
    public int gameId;

    @ColumnInfo(name = "gameName")
    public String gameName;
}
