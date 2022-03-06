package com.example.ichhabschonmal;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Gamer {        // Data entity is already named Player
    private List<String> listOfStories = new ArrayList<>();     // Stories are saved temporary in listOfStories
    private final int number;
    private String name;

    public Gamer(int number) {       // No default-Ctor: a player/gamer always has a name and a number
        this.number = number;
    }

    private Gamer(int number, String name, List<String> listOfStories) {        // Copy-ctor
        this.number = number;
        this.name = name;
        this.listOfStories.addAll(listOfStories);
    }

    public int getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCountOfStories() {
        return listOfStories.size();
    }

    public String getStory(int j) {
        if (j >= 0 && j < listOfStories.size())
            return listOfStories.get(j);
        return "Keine Story gefunden, falscher Indexwert";//Exceptionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
    }

    public void addStory(String story) {        // Add a story to the story list of a player
        listOfStories.add(story);
    }

    public void deleteStory(int j) {
        listOfStories.remove(j);
    }

    public static boolean isEmpty(Gamer[] listOfPlayers) {        // Returns true, if listOfPlayers is empty
        boolean check = true;

        // Check all players
        for (int i = 0; check && i < listOfPlayers.length; i++) {
            if (listOfPlayers[i] != null)
                check = false;
        }

        return  check;
    }

    public static Gamer[] copyPlayers(Gamer[] listOfPlayers) {
        Gamer[] newListOfPlayers = new Gamer[listOfPlayers.length];

        for (int i = 0; i < listOfPlayers.length; i++) {
            newListOfPlayers[i] = new Gamer(listOfPlayers[i].number, listOfPlayers[i].name, listOfPlayers[i].listOfStories);
        }

        return newListOfPlayers;
    }
}
