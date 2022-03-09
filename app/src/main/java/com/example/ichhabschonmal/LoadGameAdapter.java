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

import java.util.ArrayList;
import java.util.List;

public class LoadGameAdapter extends RecyclerView.Adapter<LoadGameAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener clicker;
    private AppDatabase mDatabase;
    private Context context;


    LoadGameAdapter(Context context, List<String> data, AppDatabase database) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mDatabase = database;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.load_game_item, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String game = mData.get(position);
        holder.name.setText(game);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements com.example.ichhabschonmal.ViewHolder, View.OnLongClickListener {
        Button load, delete;
        TextView name;


        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            load = itemView.findViewById(R.id.load);
            delete = itemView.findViewById(R.id.delete);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int pos = getAbsoluteAdapterPosition();


                }
            });

            load.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                }
            });

            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Test").setMessage("TETETETST "+ String.valueOf(getAbsoluteAdapterPosition()))
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
                                    ((Activity)context).finish();
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

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View view) {
            return false;
        }
    }

    String getItem(int id) {
        return mData.get(id);
    }

    void setClickListener(ItemClickListener itemClickListener) {
        this.clicker = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
