package com.slavaware.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.slavaware.spotifystreamer.fragments.SpotifySearchFragment;
import com.slavaware.spotifystreamer.fragments.SpotifySearchFragment.Callback;
import com.slavaware.spotifystreamer.fragments.TopTracksFragment;

public class SpotifySearchActivity extends AppCompatActivity implements Callback {

    private final String LOG_TAG = SpotifySearchActivity.class.getSimpleName();

    private static final String TOP_TRACK_TAG = "TOP_TRACK_FRAGMENT";

    private boolean twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_spotify);
        if (findViewById(R.id.top_tracks_container) != null) {
            twoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top_tracks_container, new TopTracksFragment(), TOP_TRACK_TAG)
                        .commit();
            }
        } else {
            twoPane = false;
            getSupportActionBar().setElevation(0f);
        }

//        SpotifySearchFragment forecastFragment =  ((SpotifySearchFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.fragment_search));
    }

    @Override
    public void onItemSelected(String artistId, String artistName) {
        if (twoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putString(SpotifySearchFragment.ARTIST_ID_KEY, artistId);
            args.putString(SpotifySearchFragment.ARTIST_NAME_KEY, artistName);

            TopTracksFragment fragment = new TopTracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, fragment, TOP_TRACK_TAG)
                    .commit();
        } else {
            Intent showTopTracks = new Intent(this, TopTracksActivity.class);
            showTopTracks.putExtra(SpotifySearchFragment.ARTIST_ID_KEY, artistId);
            showTopTracks.putExtra(SpotifySearchFragment.ARTIST_NAME_KEY, artistName);

            startActivity(showTopTracks);
        }
    }
}
