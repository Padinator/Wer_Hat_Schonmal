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

public class LoadGameAdapter extends RecyclerView.Adapter<LoadGameAdapter.LoadGameViewHolder> {

    private Context mContext;
    private Cursor mCursor;


    public LoadGameAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
    }

    public class LoadGameViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public Button load;
        public Button delete;

        public LoadGameViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            load = view.findViewById(R.id.load);
            delete = view.findViewById(R.id.delete);
        }
    }

    @Override
    public LoadGameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.load_game_item, parent, false);
        return new LoadGameViewHolder(view);
    }


    @Override
    public void onBindViewHolder(LoadGameViewHolder holder, int pos) {
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
