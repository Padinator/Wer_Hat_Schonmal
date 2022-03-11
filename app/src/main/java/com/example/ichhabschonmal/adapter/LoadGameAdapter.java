package com.example.ichhabschonmal.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.LoadGame;
import com.example.ichhabschonmal.PlayGame;
import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.database.AppDatabase;
import com.example.ichhabschonmal.database.Game;

public class LoadGameAdapter extends RecyclerView.Adapter<LoadGameAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private AppDatabase mDatabase;
    private Context mContext;


    public LoadGameAdapter(Context context, AppDatabase database) {
        mInflater = LayoutInflater.from(context);
        mDatabase = database;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.load_game_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //String game = mData.get(position);
        holder.name.setText(mDatabase.gamesDao().getAll().get(position).gameName);
    }

    @Override
    public int getItemCount() {
        return mDatabase.gamesDao().getAll().size();
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


            load.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Test").setMessage("TETETETST " + String.valueOf(getAbsoluteAdapterPosition()))
                            .setPositiveButton("Laden", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent playGame = new Intent(view.getContext().getApplicationContext(), PlayGame.class);
                                    Game game = mDatabase.gamesDao().getAll().get(getAbsoluteAdapterPosition());

                                    playGame.putExtra("GameId", game.gameId);

                                    // Start PlayGame
                                    mContext.startActivity(playGame);
                                    ((Activity) mContext).finish();
                                }
                            })
                            .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                    builder.create().show();


                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Test").setMessage("TETETETST " + String.valueOf(getAbsoluteAdapterPosition()))
                            .setPositiveButton("L\u00f6schen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(view.getContext().getApplicationContext(), LoadGame.class);
                                    Game game = mDatabase.gamesDao().getAll().get(getAbsoluteAdapterPosition());

                                    // Delete a game
                                    mDatabase.gamesDao().delete(game);

                                    // Close database connection
                                    mDatabase.close();

                                    // Start this activity and stop it to refresh content
                                    mContext.startActivity(intent);
                                    ((Activity) mContext).finish();
                                }
                            })
                            .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });
                    builder.create().show();
                }
            });
        }
    }
}


