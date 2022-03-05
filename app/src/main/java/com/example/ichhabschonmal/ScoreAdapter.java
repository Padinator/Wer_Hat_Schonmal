package com.example.ichhabschonmal;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private Context mContext;
    private List<Player> mPlayers;

    public ScoreAdapter(Context context, List<Player> players) {
        mContext = context;
        mPlayers = players;
    }

    public class ScoreViewHolder extends RecyclerView.ViewHolder {
        public TextView player;
        public TextView points;

        public ScoreViewHolder(View view) {
            super(view);
            player = view.findViewById(R.id.player);
            points = view.findViewById(R.id.points);
        }
    }

    @Override
    public ScoreViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.score_item, parent, false);
        return new ScoreViewHolder(view);
    }


    @Override
    public void onBindViewHolder(ScoreViewHolder holder, int pos) {
        holder.player.setText(mPlayers.get(pos).name + "");
        holder.points.setText(mPlayers.get(pos).score + "");
    }


    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView player, points;

        ViewHolder(View itemView) {
            super(itemView);
            player = itemView.findViewById(R.id.player);
            points = itemView.findViewById(R.id.points);
        }

    }
}
