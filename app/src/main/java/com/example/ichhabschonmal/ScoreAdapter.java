package com.example.ichhabschonmal;


import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    private Context mContext;
    private Cursor mCursor;


    public ScoreAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
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
        if (!mCursor.moveToPosition(pos)) {
            return;
        }

     /*
        String name = mCursor.getString(mCursor.getColumnIndex(DB.GroceryEntry.COLUMN_NAME));
        int amount = mCursor.getInt(mCursor.getColumnIndex(DB.GroceryEntry.COLUMN_AMOUNT));
        long id = mCursor.getLong(mCursor.getColumnIndex(DB.GroceryEntry._ID));
        gameView.name.setText(name);
        gameView.load.setText(String.valueOf(amount));
        gameView.delete.setTag(id);
      */
    }


    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }
}
