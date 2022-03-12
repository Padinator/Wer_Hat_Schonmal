package com.example.ichhabschonmal;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ichhabschonmal.database.AppDatabase;

public class ManageGame extends AppCompatActivity {

    private static AppDatabase mDatabase;

    ManageGame(AppDatabase database) {
        mDatabase = database;
    }


    static void deleteCreatedGame(int idFirstPlayer, int countOfPlayers, int idOfFirstStory, int countOfStories) {


    }


}
