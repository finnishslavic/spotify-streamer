package com.slavaware.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.slavaware.spotifystreamer.fragments.SpotifySearchFragment;
import com.slavaware.spotifystreamer.fragments.TopTracksFragment;
import com.slavaware.spotifystreamer.services.MusicPlaybackService;
import com.slavaware.spotifystreamer.utils.TrackSelectedCallback;

public class TopTracksActivity extends AppCompatActivity implements TrackSelectedCallback {

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
        Intent playTrack = new Intent(this, SpotifyPlayerActivity.class);
        playTrack.putExtra(MusicPlaybackService.EXTRA_TRACK_ID, position);
        startActivity(playTrack);
    }
}
