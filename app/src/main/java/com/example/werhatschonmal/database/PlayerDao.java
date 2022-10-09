package com.example.werhatschonmal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlayerDao {
    @Query("SELECT * FROM Player")
    List<Player> getAll();

    @Query("SELECT * FROM Player WHERE playerId IN (:playerIds)")
    List<Player> loadAllByPlayerIds(int[] playerIds);

    @Query("SELECT * FROM Player WHERE playerNumber LIKE :playerNumber AND name LIKE :name AND " +
            "gameId LIKE :gameId AND score LIKE :score AND countOfBeers LIKE :countOfBeers AND " +
            "countOfVodka LIKE :countOfVodka AND " + " countOfTequila LIKE :countOfTequila LIMIT 1")
    Player findByName(int playerNumber, String name, int gameId, int score, int countOfBeers, int countOfVodka, int countOfTequila);

    @Update
    void updatePlayer(Player player);

    @Insert
    void insertAll(Player... players);

    @Insert
    void insert(Player player);

    @Delete
    void delete(Player player);
}