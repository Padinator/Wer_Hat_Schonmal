package com.example.ichhabschonmal.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Update
    void updateStory(Story story);

    @Insert
    void insertAll(Story... stories);

    @Insert
    void insert(Story story);

    @Delete
    void delete(Story story);
}
