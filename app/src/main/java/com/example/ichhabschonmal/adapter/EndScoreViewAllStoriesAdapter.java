package com.example.ichhabschonmal.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
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
import java.util.ArrayList;
import java.util.List;

public class EndScoreViewAllStoriesAdapter extends  RecyclerView.Adapter<EndScoreViewAllStoriesAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Player> mPlayers;
    private List<Story> mStories;
    private Activity mActivity;
    // private EndScoreViewAllStoriesAdapterListAdapter adapter;

    private int storyCounter = 0;

    public EndScoreViewAllStoriesAdapter(Context context, List<Player> players, List<Story> stories, Activity activity ) {
        mInflater = LayoutInflater.from(context);
        mPlayers = players;
        mStories = stories;
        mActivity = activity;
    }

    @Override
    public EndScoreViewAllStoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.end_score_view_all_stories_item, parent, false);
        return new EndScoreViewAllStoriesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EndScoreViewAllStoriesAdapter.ViewHolder holder, int pos) {
        holder.playerName.setText("Spieler: " + mPlayers.get(pos).name + "");
        List<Story> actualStories = getActualStories(mPlayers.get(pos).playerId);
        for (Story story: actualStories) {
            Log.e("Actual Stories",  pos + ", " +story.storyId + ", " + story.playerId + ", " + story.content + ", " + actualStories.size());
        }
        String storyText = "";
        for (int i=0; i<actualStories.size(); i++) {
            if (i < actualStories.size() - 1) {
                if (actualStories.get(i).guessedStatus) {
                    storyText += actualStories.get(i).content + "\n" + "Wurde richtig geraten von: " + actualStories.get(i).guessingPerson + "\n\n";
                } else {
                    storyText += actualStories.get(i).content + "\n" + "Wurde falsch geraten von: " + actualStories.get(i).guessingPerson + "\n\n";
                }
            } else {
                if (actualStories.get(i).guessedStatus) {
                    storyText += actualStories.get(i).content + "\n" + "Wurde richtig geraten von: " + actualStories.get(i).guessingPerson;
                } else {
                    storyText += actualStories.get(i).content + "\n" + "Wurde falsch geraten von: " + actualStories.get(i).guessingPerson;
                }
            }
        }
        holder.stories.setText(storyText);
        // adapter = new EndScoreViewAllStoriesAdapterListAdapter(mActivity, actualStories);
        // holder.endScoreListView.setAdapter(adapter);
    }

    private List<Story> getActualStories(int playerId) {
        List<Story> actualStories = new ArrayList<>();

        for (; storyCounter < mStories.size() && mStories.get(storyCounter).playerId == playerId; storyCounter++) {
            actualStories.add(mStories.get(storyCounter));
        }

        return actualStories;
    }

    /*
    public class EndScoreViewAllStoriesAdapterListAdapter extends ArrayAdapter<Story> {
        private final Activity activity;
        private final List<Story> actualStories;
        private int counter = 0;

        public EndScoreViewAllStoriesAdapterListAdapter(Activity activity, List<Story> actualStories) {
            super(activity, R.layout.view_your_stories_list_item, actualStories); // giving actualStories possible because of "extends ArrayAdapter<Story>"
            this.activity = activity;
            this.actualStories = actualStories;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            // Definitions and initializations
            LayoutInflater inflater = activity.getLayoutInflater();
            @SuppressLint("ViewHolder") View rowView= inflater.inflate(R.layout.end_score_view_all_stories_list_item, null, true);
            Log.e("ViewAllStories", position + ", " + actualStories.get(position).storyId + ", " + actualStories.get(position).playerId + ", " + actualStories.get(position).content + ", " + actualStories.size());
            // list item setzen
            TextView storyNumber, storyContent, guessedStory, guessingPerson;
            storyNumber = rowView.findViewById(R.id.storyNumber);
            storyContent = rowView.findViewById(R.id.storyContent);
            // guessedStory = rowView.findViewById(R.id.guessedStory);
            guessingPerson = rowView.findViewById(R.id.guessingPerson);

            // storyNumber.setText("Story " + (position+1) + ":");
            storyNumber.setText("Story " + (++counter) + ":");
            storyContent.setText(actualStories.get(position).content);
            // guessedStory.setText("Wurde geraten von:"); // already defined in xml, text is always the same
            guessingPerson.setText(actualStories.get(position).guessingPerson);

            if (mStories.get(position).guessedStatus) {
                guessingPerson.setTextColor(ContextCompat.getColor(mInflater.getContext(), R.color.lightGreen));
            } else {
                guessingPerson.setTextColor(ContextCompat.getColor(mInflater.getContext(), android.R.color.holo_red_light));
            }
            return rowView;
        }
    }
     */

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView playerName, stories; // playerStory, guessedPerson;
        // ListView endScoreListView;



        ViewHolder(View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.endScorePlayerName);
            stories = itemView.findViewById(R.id.endScoreStories);
            // endScoreListView = itemView.findViewById(R.id.endScoreListView);
            // playerStory = itemView.findViewById(R.id.endScoreStory);
            // guessedPerson = itemView.findViewById(R.id.endScoreGuessed);
        }

    }
}
