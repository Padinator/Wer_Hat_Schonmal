package com.example.ichhabschonmal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlayerDao {
    @Query("SELECT * FROM Player")
    List<Player> getAll();

    @Query("SELECT * FROM Player WHERE playerId IN (:playerIds)")
    List<Player> loadAllByPlayerIds(int[] playerIds);

    @Query("SELECT * FROM Player WHERE playerNumber LIKE :playerNumber AND name LIKE :name AND " +
            "gameId LIKE :gameId AND score LIKE :score LIMIT 1")
    Player findByName(int playerNumber, String name, int gameId, int score);

    @Insert
    void insertAll(Player... players);

    @Delete
    void delete(Player player);
}