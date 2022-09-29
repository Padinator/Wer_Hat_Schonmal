package com.example.ichhabschonmal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface GameDao {
    @Query("SELECT * FROM Game")
    List<Game> getAll();

    @Query("SELECT * FROM Game WHERE gameId IN (:gameIds)")
    List<Game> loadAllByGameIds(int[] gameIds);

    @Query("SELECT * FROM Game WHERE gameName LIKE :gameName AND onlineGame LIKE :onlineGame AND " +
            " idOfFirstPlayer LIKE :idOfFirstPlayer AND countOfPlayers LIKE :countOfPlayers AND " +
            "idOfFirstStory LIKE :idOfFirstStory AND countOfStories LIKE :countOfStories AND " +
            " actualDrinkOfTheGame LIKE :actualDrinkOfTheGame AND roundNumber LIKE :roundNumber LIMIT 1")
    Game findByName(String gameName, boolean onlineGame, int idOfFirstPlayer, int countOfPlayers, int idOfFirstStory,
                    int countOfStories, int roundNumber, String actualDrinkOfTheGame);

    @Update
    void updateGame(Game game);

    @Insert
    void insertAll(Game... games);

    @Insert
    void insert(Game game);

    @Delete
    void delete(Game game);
}
