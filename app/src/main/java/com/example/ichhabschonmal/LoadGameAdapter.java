package com.example.ichhabschonmal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.util.List;

public class LoadGameAdapter extends RecyclerView.Adapter<LoadGameAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private AppDatabase mDatabase;
    private Context mContext;


    LoadGameAdapter(Context context, AppDatabase database) {
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
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Game game = mDatabase.gamesDao().getAll().get(getAbsoluteAdapterPosition());

                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Game game = mDatabase.gamesDao().getAll().get(getAbsoluteAdapterPosition());
                                    Log.e("POS", String.valueOf(getAbsoluteAdapterPosition()));
                                    mDatabase.gamesDao().delete(game);

                                    int gameId = game.gameId;

                                    Intent intent = new Intent(view.getContext().getApplicationContext(), LoadGame.class);

                                    intent.putExtra("GameName", gameId);

                                    view.getContext().startActivity(intent);
                                    ((Activity) mContext).finish();
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
