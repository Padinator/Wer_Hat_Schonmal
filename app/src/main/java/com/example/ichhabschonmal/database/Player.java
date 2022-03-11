package com.example.ichhabschonmal.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {@ForeignKey(entity = Game.class,
        parentColumns = "gameId",
        childColumns = "gameId",
        onDelete = ForeignKey.CASCADE)
})
public class Player {
    @PrimaryKey(autoGenerate = true)        // Autoincrement is on
    public int playerId;        // Every player has a unique player id

    @ColumnInfo(name = "playerNumber")
    public int playerNumber;        // Every player has a unique number in a game

    @ColumnInfo(name = "name")
    public String name;

    /*@ColumnInfo(name = "numberOfStories")
    public int numberOfStories;*/

    @ColumnInfo(name = "gameId")
    public int gameId;

    @ColumnInfo(name = "score")
    public int score;
}
