package com.slavaware.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.slavaware.spotifystreamer.fragments.SpotifyPlayerFragment;
import com.slavaware.spotifystreamer.fragments.SpotifySearchFragment;
import com.slavaware.spotifystreamer.fragments.TopTracksFragment;
import com.slavaware.spotifystreamer.fragments.TopTracksFragment.Callback;

public class TopTracksActivity extends AppCompatActivity implements Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putString(SpotifySearchFragment.ARTIST_ID_KEY,
                    getIntent().getStringExtra(SpotifySearchFragment.ARTIST_ID_KEY));
            arguments.putString(SpotifySearchFragment.ARTIST_NAME_KEY,
                    getIntent().getStringExtra(SpotifySearchFragment.ARTIST_NAME_KEY));

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_tracks_container, fragment)
                    .commit();
        }

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.setTitle(R.string.top_tracks_activity_title);
//        artistName = data.getString(SpotifySearchActivity.ARTIST_NAME_KEY);
//        actionBar.setSubtitle(artistName);
//        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onItemSelected(int position) {
        if (!isTablet(this)) {
            Intent playTrack = new Intent(this, SpotifyPlayerActivity.class);
            playTrack.putExtra(TopTracksFragment.EXTRA_TRACK_POSITION, position);
            startActivity(playTrack);
        } else {
            FragmentManager fm = getSupportFragmentManager();
            SpotifyPlayerFragment spotifyPlayerDialog = new SpotifyPlayerFragment();
            spotifyPlayerDialog.show(fm, "fragment_spotify_dialog");
        }
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
