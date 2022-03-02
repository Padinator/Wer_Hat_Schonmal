package com.example.ichhabschonmal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface StoryDao {
    @Query("SELECT * FROM Story")
    List<Story> getAll();

    @Query("SELECT * FROM Story WHERE storyId IN (:storyIds)")
    List<Story> loadAllByStoryIds(int[] storyIds);

    @Query("SELECT * FROM Story WHERE content LIKE :content AND status LIKE :status AND " +
            " playerId LIKE :playerId LIMIT 1")
    Story findByName(String content, boolean status, int playerId);

    @Insert
    void insertAll(Story... stories);

    @Delete
    void delete(Story story);
}
