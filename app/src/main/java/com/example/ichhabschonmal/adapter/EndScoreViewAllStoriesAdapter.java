package com.example.ichhabschonmal.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ichhabschonmal.EndScoreViewAllStories;
import com.example.ichhabschonmal.R;
import com.example.ichhabschonmal.database.Player;
import com.example.ichhabschonmal.database.Story;

import java.lang.reflect.Array;
import java.util.List;

public class EndScoreViewAllStoriesAdapter extends  RecyclerView.Adapter<EndScoreViewAllStoriesAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Player> mPlayers;
    private List<Story> mStories;
    private ArrayAdapter<String> adapter;

    public EndScoreViewAllStoriesAdapter(Context context, List<Player> players, List<Story> stories) {
        mInflater = LayoutInflater.from(context);
        mPlayers = players;
        mStories = stories;

    }

    @Override
    public EndScoreViewAllStoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.end_score_view_all_stories_item, parent, false);
        return new EndScoreViewAllStoriesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EndScoreViewAllStoriesAdapter.ViewHolder holder, int pos) {
        holder.playerName.setText("Spieler: " + mPlayers.get(pos).name + "");
        adapter = new EndScoreViewAllStoriesAdapterListAdapter(EndScoreViewAllStories.this);
        holder.endScoreListView.setAdapter(adapter);
        // holder.playerStory.setText(mStories.get(pos).content + "");
        // holder.guessedPerson.setText("Wurde geraten von: " + mStories.get(pos).guessingPerson + "");
        if (mStories.get(pos).guessedStatus) {
           // holder.guessedPerson.setTextColor(ContextCompat.getColor(mInflater.getContext(), R.color.lightGreen));
        } else {
           // holder.guessedPerson.setTextColor(ContextCompat.getColor(mInflater.getContext(), android.R.color.holo_red_light));
        }
    }

    public class EndScoreViewAllStoriesAdapterListAdapter extends ArrayAdapter<String> {
        private final Activity activity;

        public EndScoreViewAllStoriesAdapterListAdapter(Activity activity) {
            super(activity, R.layout.view_your_stories_list_item);
            this.activity = activity;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            // Definitions and initializations
            LayoutInflater inflater = activity.getLayoutInflater();
            @SuppressLint("ViewHolder") View rowView= inflater.inflate(R.layout.view_your_stories_list_item, null, true);

            return rowView;
        }
    }

    /*
     ArrayAdapter adapter = new ArrayAdapter(mInflater.getContext(), android.R.layout.simple_list_item_2, android.R.id.text1, mPlayers) {
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView text1 = (TextView) view.findViewById(android.R.id.text1);
            TextView text2 = (TextView) view.findViewById(android.R.id.text2);

            text1.setText(mStories.get(position).content);
            text2.setText(mStories.get(position).guessingPerson);
            return view;
        }
    };
     */


    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView playerName; // playerStory, guessedPerson;
        ListView endScoreListView;


        ViewHolder(View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.endScorePlayerName);
            endScoreListView = itemView.findViewById(R.id.endScoreListView);
            // playerStory = itemView.findViewById(R.id.endScoreStory);
            // guessedPerson = itemView.findViewById(R.id.endScoreGuessed);
        }

    }
}
