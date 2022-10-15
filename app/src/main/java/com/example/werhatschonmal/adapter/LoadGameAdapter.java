package com.example.werhatschonmal.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
    private int countOfGames, actualGameNumber;

    public LoadGameAdapter(Context context, AppDatabase database) {
        mInflater = LayoutInflater.from(context);
        mDatabase = database; // Database is never closed in LoadGameAdapter
        mContext = context;
        countOfGames = mDatabase.gameDao().getAll().size() - 1;
        actualGameNumber = countOfGames;
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

        @SuppressLint({"NotifyDataSetChanged", "LongLogTag", "SetTextI18n"})
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            load = itemView.findViewById(R.id.load);
            viewGame = itemView.findViewById(R.id.view_game);
            delete = itemView.findViewById(R.id.delete);
            Log.e("GetAbsolutAdapterPosition", getAbsoluteAdapterPosition() + "");

            // Set buttons, if a game is already over
            Game actualGame = mDatabase.gameDao().getAll().get(actualGameNumber);

            if (actualGame.gameIsOver || actualGame.onlineGame && !actualGame.serverSide) {
                // Game is already over or edit view for online gaming showing (clientside)
                load.setText("Ansehen");
                viewGame.setVisibility(View.INVISIBLE);
                viewGame.callOnClick();
            }

            // Set used variable
            actualGameNumber--; // Count number of actual games, latest game is on top

            load.setOnClickListener(view -> {
                Game game = mDatabase.gameDao().getAll().get(countOfGames - getAbsoluteAdapterPosition());
                // "getAbsoluteAdapterPosition()" works only here
                Log.e("Game is over", game.gameIsOver + "");
                if (game.gameIsOver || game.onlineGame && !game.serverSide) // Game is already over or edit view for online gaming showing (clientside)
                    viewGame.callOnClick();
                else { // Game is not over, offline game or online game (serverside)
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
                        .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                        });
                builder.create().show();
            });

            viewGame.setOnClickListener(view -> {
                Game game = mDatabase.gameDao().getAll().get(countOfGames - getAbsoluteAdapterPosition());
                // "getAbsoluteAdapterPosition()" works only here

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Spiel ist bereits vorbei")
                        .setMessage("Soll das Spiel " + game.gameName + " angesehen werden?")
                        .setPositiveButton("Ansehen", (dialog, which) -> {
                            Intent rules = new Intent(view.getContext().getApplicationContext(), EndScore.class);

                            rules.putExtra("GameId", game.gameId);

                            // Start PlayGame activity
                            mContext.startActivity(rules);
                            ((Activity) mContext).finish();
                        })
                        .setNegativeButton("Abbrechen", (dialogInterface, i) -> {
                        });
                builder.create().show();
            });
        }
    }
}


