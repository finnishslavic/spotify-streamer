package com.slavaware.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.slavaware.spotifystreamer.fragments.SpotifySearchFragment;
import com.slavaware.spotifystreamer.fragments.TopTracksFragment;

public class TopTracksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top_tracks_activity);

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


}
