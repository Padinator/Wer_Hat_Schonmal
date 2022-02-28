package com.example.ichhabschonmal;

public class Player {
    private String[] listOfStories = new String[0];     // Stories are saved in listOfStories, a used story
                                                        // is replaced with "done"
    public final int number;
    private String name;

    Player(int number) {       // No default-Ctor: a player always has name and a number
        this.number = number;
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
        return listOfStories.length;
    }

    public String getStory(int j) {
        return listOfStories[j];
    }

    public void addStory(String story) {        // Add a story to the story list of a player
        String[] tmp = new String[listOfStories.length+ 1];

        for (int i = 0; i < listOfStories.length; i++) {
            tmp[i] = listOfStories[i];
        }

        tmp[listOfStories.length] = story;

        listOfStories = tmp;
    }
}
