package com.example.ichhabschonmal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class ManageGames extends AppCompatActivity {

    private static AppDatabase mDatabase;

    ManageGames(AppDatabase database) {
        mDatabase = database;
    }


    static void deleteCreatedGame(int idFirstPlayer, int countOfPlayers, int idOfFirstStory, int countOfStories) {


    }


}
