package com.example.werhatschonmal;

import androidx.annotation.NonNull;

import com.example.werhatschonmal.exceptions.GamerException;
import com.example.werhatschonmal.server_client_communication.SocketEndPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Gamer { // Data entity is already named Player

    // Constants
    public static final List<String> drinks = Arrays.asList("Bier", "Vodka Shots", "Tequila", "Gin Shot", "Jaegermeister");
        // Change after drinks in database were changed

    // Variables
    private List<String> listOfStories = new ArrayList<>(); // Stories are saved temporary in listOfStories
    private final int number;
    private String name = "";

    public Gamer(int number) { // No default-Constructor: a player/gamer always has a name and a number
        this.number = number;
    }

    private Gamer(int number, String name, List<String> listOfStories) { // Copy-Constructor
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

    public String getStory(int j) throws GamerException {
        if (j < 0 || j >= listOfStories.size())
            throw new GamerException("No story found to read, invalid index: " + j);

        return listOfStories.get(j);
    }

    public List<String> getAllStories() {
        return new ArrayList<>(listOfStories);
    }

    public void addStory(String story) {        // Add a story to the story list of a player
        listOfStories.add(story);
    }

    public void replaceAllStories(List<String> newListOfStories) {
        this.listOfStories = newListOfStories; // Flat copy
    }

    public void deleteStory(int j) throws GamerException {
        if (j < 0 || j >= listOfStories.size())
            throw new GamerException("No story found to delete, invalid index: " + j);
        listOfStories.remove(j);
    }

    public static boolean isEmpty(Gamer[] listOfPlayers) { // Returns true, if listOfPlayers is empty
        boolean check = true;

        // Check all players
        for (Gamer listOfPlayer : listOfPlayers) {
            if (listOfPlayer != null) {
                check = false;
                break;
            }
        }

        return check;
    }

    public static Gamer copyPlayer(Gamer player) {
        return new Gamer(player.number, player.name, player.listOfStories);
    }

    public static Gamer[] copyPlayers(Gamer[] listOfPlayers) {
        Gamer[] newListOfPlayers = new Gamer[listOfPlayers.length];

        for (int i = 0; i < listOfPlayers.length; i++)
            newListOfPlayers[i] = new Gamer(listOfPlayers[i].number, listOfPlayers[i].name, listOfPlayers[i].listOfStories);

        return newListOfPlayers;
    }

    @NonNull
    @Override
    public String toString() {
        List<String> listOfStories = getAllStories();
        StringBuilder string = new StringBuilder(number + SocketEndPoint.SEPARATOR + name);

        for (String story : listOfStories)
            string.append(SocketEndPoint.SEPARATOR).append(story);

        return String.valueOf(string);
    }
}
