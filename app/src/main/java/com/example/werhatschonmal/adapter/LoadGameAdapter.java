package com.example.werhatschonmal.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.werhatschonmal.EndScore;
import com.example.werhatschonmal.R;
import com.example.werhatschonmal.Rules;
import com.example.werhatschonmal.database.AppDatabase;
import com.example.werhatschonmal.database.Game;

public class LoadGameAdapter extends RecyclerView.Adapter<LoadGameAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private final AppDatabase mDatabase;
    private final Context mContext;
    private int countOfGames;


    public LoadGameAdapter(Context context, AppDatabase database) {
        mInflater = LayoutInflater.from(context);
        mDatabase = database; // Database is never closed in LoadGameAdapter
        mContext = context;
        countOfGames = mDatabase.gameDao().getAll().size() - 1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.load_game_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(mDatabase.gameDao().getAll().get(countOfGames - position).gameName);
    }

    @Override
    public int getItemCount() {
        return mDatabase.gameDao().getAll().size();//////////////////
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        Button load, viewGame;
        ImageButton delete;
        TextView name;

        @SuppressLint("NotifyDataSetChanged")
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            load = itemView.findViewById(R.id.load);
            viewGame = itemView.findViewById(R.id.view_game);
            delete = itemView.findViewById(R.id.delete);

            load.setOnClickListener(view -> {
                Game game = mDatabase.gameDao().getAll().get(countOfGames - getAbsoluteAdapterPosition());
                // "getAbsoluteAdapterPosition()" works only here

                if (game.onlineGame && !game.serverSide) { // Edit view for online gaming showing (clientside)
                    load.setText("Ansehen");
                    viewGame.setVisibility(View.INVISIBLE);
                }

                if (game.onlineGame && !game.serverSide) // Online game, clientside
                    viewGame.callOnClick();
                else { // Offline game or online game (serverside)
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Spiel laden")
                            .setMessage("Soll das Spiel " + game.gameName + " geladen werden?")
                            .setPositiveButton("Laden", (dialog, which) -> {
                                Intent rules = new Intent(view.getContext().getApplicationContext(), Rules.class);

                                rules.putExtra("GameId", game.gameId);
                                rules.putExtra("GameIsLoaded", true);

                                // Start PlayGame activity
                                mContext.startActivity(rules);
                                ((Activity) mContext).finish();
                            })
                            .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                            });
                    builder.create().show();
                }
            });

            delete.setOnClickListener(view -> {
                Game game = mDatabase.gameDao().getAll().get(countOfGames - getAbsoluteAdapterPosition());
                // "getAbsoluteAdapterPosition()" works only here

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Spiel l\u00f6schen")
                        .setMessage("Soll das Spiel " + game.gameName + " gel\u00f6scht werden?")
                        .setPositiveButton("L\u00f6schen", (dialog, which) -> {

                            // Delete a game
                            mDatabase.gameDao().delete(game);

                            // Set used variables
                            countOfGames--;

                            // Notify adapter
                            notifyDataSetChanged();
                        })
                        .setNegativeButton("Abbrechen", (dialogInterface, i) -> {});
                builder.create().show();
            });

            viewGame.setOnClickListener(view -> {
                Game game = mDatabase.gameDao().getAll().get(countOfGames - getAbsoluteAdapterPosition());
                // "getAbsoluteAdapterPosition()" works only here

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Spiel laden")
                        .setMessage("Soll das Spiel " + game.gameName + " angesehen werden?")
                        .setPositiveButton("Ansehen", (dialog, which) -> {
                            Intent rules = new Intent(view.getContext().getApplicationContext(), EndScore.class);

                            rules.putExtra("GameId", game.gameId);

                            // Start PlayGame activity
                            mContext.startActivity(rules);
                            ((Activity) mContext).finish();
                        })
                        .setNegativeButton("Abbrechen", (dialogInterface, i) -> {});
                builder.create().show();
            });
        }
    }
}


