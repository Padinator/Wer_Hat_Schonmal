package com.example.ichhabschonmal.adapter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;
import com.example.ichhabschonmal.database.Player;

import org.w3c.dom.Text;

import java.util.List;

public class EndScoreAdapter extends RecyclerView.Adapter<EndScoreAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Player> mPlayers;
    private AppDatabase db;
    private Context mContext;
    private int mGameId;
    private RelativeLayout[] allRelativeLayouts;

    public EndScoreAdapter(Context context, List<Player> players, int gameId) {
        mInflater = LayoutInflater.from(context);
        mPlayers = players;
        mContext = context;
        mGameId = gameId;
    }

    @Override
    public EndScoreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.end_score_item, parent, false);
        return new EndScoreAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EndScoreAdapter.ViewHolder holder, int pos) {

        // Create database connection
        db = Room.databaseBuilder(mContext, AppDatabase.class, "database").allowMainThreadQueries().build();
        Game actualGame = db.gameDao().loadAllByGameIds(new int[] {mGameId}).get(0);
        int lastDrinkNumber = convertDrink(actualGame.actualDrinkOfTheGame);

        holder.player.setText(mPlayers.get(pos).name + "");
        holder.points.setText(mPlayers.get(pos).score + "");

        // Save a player scores
        int[] playerScores = new int[3]; // Change manually
        playerScores[0] = mPlayers.get(pos).countOfBeers;
        playerScores[1] = mPlayers.get(pos).countOfVodka;
        playerScores[2] = mPlayers.get(pos).countOfTequila;

        if (playerScores[0] + playerScores[1] + playerScores[2] == 0) {
            setDrink(holder, 0, lastDrinkNumber, 0);
            for (int counterPos=1; counterPos<playerScores.length; counterPos++) {
                allRelativeLayouts[counterPos].setVisibility(View.INVISIBLE);
            }
        } else {
            int counterPos = 0;

            for (int i = 0; i < playerScores.length; i++) {
                if (playerScores[i] > 0) {
                    setDrink(holder, counterPos, i, playerScores[i]);
                    counterPos++;
                }
            }

            for (; counterPos < playerScores.length; counterPos++) {
                allRelativeLayouts[counterPos].setVisibility(View.GONE);
            }
        }

        // Close database connection
        db.close();
    }

    private int convertDrink(String actualDrinkOfTheGame) {
        int drinkNumber = 0;

        switch (actualDrinkOfTheGame) {
            case "Bier":
                drinkNumber = 0;
                break;
            case "Vodka Shots":
                drinkNumber = 1;
                break;
            case "Tequila":
                drinkNumber = 2;
                break;
        }

        return drinkNumber;
    }

    private void setDrink(EndScoreAdapter.ViewHolder holder, int counterPos, int drinkNumber, int playerScore) {
        holder.countOfDrinks[counterPos].setText((playerScore + ""));
        switch (drinkNumber) {
            case 0:
                holder.images[counterPos].setImageResource(R.drawable.beericon);
                break;
            case 1:
                holder.images[counterPos].setImageResource(R.drawable.vodkashot);
                break;
            case 2:
                holder.images[counterPos].setImageResource(R.drawable.tequila);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView player, points;
        ImageView[] images = new ImageView[3]; // Change manually
        ImageView[] multiplicators = new ImageView[3]; // Change manually
        TextView[] countOfDrinks = new TextView[3]; // Change manually
        RelativeLayout[] relativeLayouts = new RelativeLayout[3]; // Change manually


        ViewHolder(View itemView) {
            super(itemView);
            player = itemView.findViewById(R.id.playerName);
            points = itemView.findViewById(R.id.totalPoints);

            images[0] = itemView.findViewById(R.id.drinkIconOne);
            images[1] = itemView.findViewById(R.id.drinkIconTwo);
            images[2] = itemView.findViewById(R.id.drinkIconThree);

            multiplicators[0] = itemView.findViewById(R.id.xOne);
            multiplicators[1] = itemView.findViewById(R.id.xTwo);
            multiplicators[2] = itemView.findViewById(R.id.xThree);

            countOfDrinks[0] = itemView.findViewById(R.id.countOfDrinksOne);
            countOfDrinks[1] = itemView.findViewById(R.id.countOfDrinksTwo);
            countOfDrinks[2] = itemView.findViewById(R.id.countOfDrinksThree);

            relativeLayouts[0] = itemView.findViewById(R.id.drinkOne);
            relativeLayouts[1] = itemView.findViewById(R.id.drinkTwo);
            relativeLayouts[2] = itemView.findViewById(R.id.drinkThree);

            allRelativeLayouts = relativeLayouts;
        }
    }
}
