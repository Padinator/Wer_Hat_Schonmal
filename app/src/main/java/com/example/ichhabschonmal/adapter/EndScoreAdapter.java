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

import java.util.List;

public class EndScoreAdapter extends RecyclerView.Adapter<EndScoreAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Player> mPlayers;
    private AppDatabase db;
    private Context mContext;
    private int mGameId;

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
        db = Room.databaseBuilder(mContext, AppDatabase.class, "database").allowMainThreadQueries().build();
        Game actualGame = db.gameDao().loadAllByGameIds(new int[] {mGameId}).get(0);
        holder.player.setText(mPlayers.get(pos).name + "");
        holder.points.setText(mPlayers.get(pos).score + "");
        holder.countOfDrinks.setText((mPlayers.get(pos).score + ""));
        if (actualGame.actualDrinkOfTheGame.equals("Bier")) {
            holder.beer.setImageResource(R.drawable.beericon);
        } else if (actualGame.actualDrinkOfTheGame.equals("Vodka Shots")) {
            holder.beer.setImageResource(R.drawable.vodkashot);
        } else if (actualGame.actualDrinkOfTheGame.equals("Tequila")) {
            holder.beer.setImageResource(R.drawable.tequila);
        }
        db.close();
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
         TextView player, points, countOfDrinks;
         ImageView beer, multiplicator;

        ViewHolder(View itemView) {
            super(itemView);
            player = itemView.findViewById(R.id.playerName);
            points = itemView.findViewById(R.id.totalPoints);
            countOfDrinks = itemView.findViewById(R.id.countOfDrinks);
            multiplicator = itemView.findViewById(R.id.x);
            beer = itemView.findViewById(R.id.drinkIcon);
        }
    }
}
