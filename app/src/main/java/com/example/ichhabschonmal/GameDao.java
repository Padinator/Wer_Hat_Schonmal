package com.example.ichhabschonmal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GameDao {
    @Query("SELECT * FROM Game")
    List<Game> getAll();

    @Query("SELECT * FROM Game WHERE gameId IN (:gameIds)")
    List<Game> loadAllByGameIds(int[] gameIds);

   /* @Query("SELECT * FROM Game WHERE gameName LIKE :gameName AND idOfFirstPlayer LIKE :idOfFirstPlayer AND " +
            "countOfPlayers LIKE :countOfPlayers AND idOfFirstStory LIKE :idOfFirstStory AND " +
            "countOfStories LIKE :countOfStories LIMIT 1")
    Game findByName(String gameName, int idOfFirstPlayer, int countOfPlayers, int idOfFirstStory,
                    int countOfStories);*/

    @Insert
    void insertAll(Game... games);

    @Delete
    void delete(Game game);
}
