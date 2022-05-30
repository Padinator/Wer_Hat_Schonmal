package com.example.ichhabschonmal.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.LoadGame;
import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.Rules;
import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;

public class LoadGameAdapter extends RecyclerView.Adapter<LoadGameAdapter.ViewHolder> {

    private final LayoutInflater mInflater;
    private final AppDatabase mDatabase;
    private final Context mContext;
    private final int countOfGames;


    public LoadGameAdapter(Context context, AppDatabase database) {
        mInflater = LayoutInflater.from(context);
        mDatabase = database;
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
        Button load, delete;
        TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            load = itemView.findViewById(R.id.load);
            delete = itemView.findViewById(R.id.delete);

            load.setOnClickListener(view -> {
                Game game = mDatabase.gameDao().getAll().get(countOfGames - getAbsoluteAdapterPosition());

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Spiel laden")
                        .setMessage("Soll das Spiel " + game.gameName + " geladen werden?")
                        .setPositiveButton("Laden", (dialog, which) -> {
                            Intent rules = new Intent(view.getContext().getApplicationContext(), Rules.class);

                            rules.putExtra("GameId", game.gameId);
                            rules.putExtra("GameIsLoaded", true);

                            // Close database connection
                            mDatabase.close();

                            // Start PlayGame activity
                            mContext.startActivity(rules);
                            ((Activity) mContext).finish();
                        })
                        .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                        });
                builder.create().show();


            });

            delete.setOnClickListener(view -> {
                Game game = mDatabase.gameDao().getAll().get(getAbsoluteAdapterPosition());

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Spiel l\u00f6schen")
                        .setMessage("Soll das Spiel " + game.gameName + " gel\u00f6scht werden?")
                        .setPositiveButton("L\u00f6schen", (dialog, which) -> {
                            Intent intent = new Intent(view.getContext().getApplicationContext(), LoadGame.class);

                            // Delete a game
                            mDatabase.gameDao().delete(game);

                            // Close database connection
                            mDatabase.close();

                            // Start this activity and stop it to refresh content
                            mContext.startActivity(intent);
                            ((Activity) mContext).finish();
                        })
                        .setNegativeButton("Abbrechen", (dialogInterface, i) -> {

                        });
                builder.create().show();
            });
        }
    }
}


