package com.example.ichhabschonmal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.database.Player;

import java.util.List;

public class EndScoreAdapter extends RecyclerView.Adapter<EndScoreAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Player> mPlayers;

    public EndScoreAdapter(Context context, List<Player> players) {
        mInflater = LayoutInflater.from(context);
        mPlayers = players;
    }



    @Override
    public EndScoreAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.end_score_item, parent, false);
        return new EndScoreAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(EndScoreAdapter.ViewHolder holder, int pos) {
        holder.player.setText(mPlayers.get(pos).name + "");
        holder.points.setText(mPlayers.get(pos).score + "");
    }


    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView player, points, countOfBeer;

        ViewHolder(View itemView) {
            super(itemView);
            player = itemView.findViewById(R.id.playerName);
            points = itemView.findViewById(R.id.totalPoints);
            countOfBeer = itemView.findViewById(R.id.beersDrunk);
        }

    }

}
