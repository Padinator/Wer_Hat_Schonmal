package de.android.werhatschonmal.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = {@ForeignKey(entity = Player.class,
        parentColumns = "playerId",
        childColumns = "playerId",
        onDelete = ForeignKey.CASCADE)
})
public class Story {
    @PrimaryKey(autoGenerate = true) // Autoincrement is on
    public int storyId;

    @ColumnInfo(name = "content")
    public String content;

    @ColumnInfo(name = "status")
    public boolean status = false; // false: unused; true: used

    @ColumnInfo(name = "guessedStatus")
    public boolean guessedStatus = false; // false: guessed wrong; true: guessed correctly

    @ColumnInfo(name = "playerId")
    public int playerId; // PlayerId of player, who owns the story

    @ColumnInfo(name = "guessingPerson")
    public String guessingPerson; // Player number and name of a player, who guessed this story
}
