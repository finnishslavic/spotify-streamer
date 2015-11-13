package com.slavaware.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.slavaware.spotifystreamer.fragments.SpotifyPlayerFragment;
import com.slavaware.spotifystreamer.services.MusicPlaybackService;

public class SpotifyPlayerActivity extends AppCompatActivity {

    public static final String TAG = "SpotifyPlayerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.

            Bundle arguments = new Bundle();
            arguments.putInt(MusicPlaybackService.EXTRA_TRACK_ID,
                    getIntent().getIntExtra(MusicPlaybackService.EXTRA_TRACK_ID, 0));

            SpotifyPlayerFragment fragment = new SpotifyPlayerFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.player_container, fragment)
                    .commit();
        }
    }
}
