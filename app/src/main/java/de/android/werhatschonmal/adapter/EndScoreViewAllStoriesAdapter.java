package de.android.werhatschonmal.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.android.werhatschonmal.R;
import de.android.werhatschonmal.database.Player;
import de.android.werhatschonmal.database.Story;

public class EndScoreViewAllStoriesAdapter extends RecyclerView.Adapter<EndScoreViewAllStoriesAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Player> mPlayers;
    private List<Story> mStories;
    private Activity mActivity;
    private EndScoreViewAllStoriesAdapterListAdapter adapter;

    private int storyCounter = 0;

    public EndScoreViewAllStoriesAdapter(Context context, List<Player> players, List<Story> stories, Activity activity) {
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

        adapter = new EndScoreViewAllStoriesAdapterListAdapter(mActivity, actualStories);

        holder.endScoreListView.getLayoutParams().height = actualStories.size() * 250 + 50;
        holder.endScoreListView.setLayoutParams(holder.endScoreListView.getLayoutParams());
        holder.endScoreListView.setAdapter(adapter);
    }

    /**
     *
     * @param playerId Input player-ID of a player to get his stories.
     * @return Return all stories of a player.
     */
    private List<Story> getActualStories(int playerId) {
        List<Story> actualStories = new ArrayList<>();

        for (; storyCounter < mStories.size() && mStories.get(storyCounter).playerId == playerId; storyCounter++)
            actualStories.add(mStories.get(storyCounter));

        return actualStories;
    }


    public class EndScoreViewAllStoriesAdapterListAdapter extends ArrayAdapter<Story> {
        private final Activity activity;
        private final List<Story> actualStories;

        public EndScoreViewAllStoriesAdapterListAdapter(Activity activity, List<Story> actualStories) {
            super(activity, R.layout.view_your_stories_list_item, actualStories); // Giving actualStories is possible because of "extends ArrayAdapter<Story>"
            this.activity = activity;
            this.actualStories = actualStories;
        }

        @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
        @Override
        public View getView(int position, View view, ViewGroup parent) {

            // Definitions and initializations
            LayoutInflater inflater = activity.getLayoutInflater();
            @SuppressLint({"ViewHolder", "InflateParams"}) View rowView = inflater.inflate(R.layout.end_score_view_all_stories_list_item, null, true);
            Log.e("ViewAllStories", position + ", " + actualStories.get(position).storyId + ", " + actualStories.get(position).playerId + ", " + actualStories.get(position).content + ", " + actualStories.size() + actualStories.get(position).guessedStatus);

            // Definitions
            TextView storyNumber, storyContent, guessedStory, guessingPerson;

            // Initializations
            storyNumber = rowView.findViewById(R.id.storyNumber);
            storyContent = rowView.findViewById(R.id.storyContent);
            // guessedStory = rowView.findViewById(R.id.guessedStory);
            guessingPerson = rowView.findViewById(R.id.guessingPerson);

            // Set texts
            // storyNumber.setText("Story " + (position+1) + ":");
            storyNumber.setText("Story " + (position + 1) + ":");
            storyContent.setText(actualStories.get(position).content);
            // guessedStory.setText("Wurde geraten von:"); // already defined in xml, text is always the same
            guessingPerson.setText(actualStories.get(position).guessingPerson);

            if (actualStories.get(position).guessedStatus) {
                guessingPerson.setTextColor(ContextCompat.getColor(mInflater.getContext(), R.color.lightGreen));
            } else {
                guessingPerson.setTextColor(ContextCompat.getColor(mInflater.getContext(), android.R.color.holo_red_light));
            }

            return rowView;
        }
    }


    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView playerName;
        ListView endScoreListView;

        ViewHolder(View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.endScorePlayerName);
            endScoreListView = itemView.findViewById(R.id.endScoreListView);
        }
    }
}
