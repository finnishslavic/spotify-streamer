package com.slavaware.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.slavaware.spotifystreamer.fragments.SpotifySearchFragment;
import com.slavaware.spotifystreamer.fragments.SpotifySearchFragment.Callback;
import com.slavaware.spotifystreamer.fragments.TopTracksFragment;
import com.slavaware.spotifystreamer.utils.Strings;

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
            if (savedInstanceState != null) {
                Bundle args = new Bundle();
                String artistId = savedInstanceState.getString(SpotifySearchFragment.ARTIST_ID_KEY);
                String artistName = savedInstanceState.getString(SpotifySearchFragment.ARTIST_NAME_KEY);

                if (!Strings.isNullOrEmpty(artistId) && !Strings.isNullOrEmpty(artistName)) {
                    args.putString(SpotifySearchFragment.ARTIST_ID_KEY, artistId);
                    args.putString(SpotifySearchFragment.ARTIST_NAME_KEY, artistName);

                    TopTracksFragment fragment = new TopTracksFragment();
                    fragment.setArguments(args);

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.top_tracks_container, fragment, TOP_TRACK_TAG)
                            .commit();
                }
            }
        } else {
            twoPane = false;
            getSupportActionBar().setElevation(0f);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
