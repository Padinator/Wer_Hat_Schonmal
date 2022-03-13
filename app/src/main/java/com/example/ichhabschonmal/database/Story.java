package com.example.ichhabschonmal.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Story {
    @PrimaryKey(autoGenerate = true)        // Autoincrement is on
    public int storyId;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "status")
    public boolean status;          // true: used, false: unused

    @ColumnInfo(name = "playerId")
    public int playerId;
}
