package com.slavaware.spotifystreamer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.slavaware.spotifystreamer.model.Track;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.realm.Realm;

public class SpotifyPlayerActivity extends AppCompatActivity {

    @InjectView(R.id.artist_text_view)
    TextView artistTextView;

    @InjectView(R.id.album_text_view)
    TextView albumTextView;

    @InjectView(R.id.album_cover_image_view)
    ImageView albumCoverImageView;

    @InjectView(R.id.track_title_text_view)
    TextView trackTitleTextView;

    private MediaPlayer mediaPlayer;
    private Realm realm;
    private List<Track> tracks;
    private int currentTrackIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);

        ButterKnife.inject(this);

        realm = Realm.getInstance(this);
        final int trackId;
        if (savedInstanceState == null) {
            trackId = getIntent().getIntExtra(TopTracksActivity.EXTRA_TRACK_ID, 0);
        } else {
            trackId = savedInstanceState.getInt(TopTracksActivity.EXTRA_TRACK_ID);
        }

        tracks = realm.allObjects(Track.class);
        currentTrackIndex = trackId;

        initView(tracks.get(currentTrackIndex));
    }

    @OnClick(R.id.previous_track_button)
    public void onPreviousTrackButtonClicked(View v) {
        if (currentTrackIndex <= 0) {
            currentTrackIndex = tracks.size() - 1;
        } else {
            currentTrackIndex--;
        }

        initView(tracks.get(currentTrackIndex));
    }

    @OnClick(R.id.play_pause_button)
    public void onPlayPauseButtonClicked(View v) {
        Toast.makeText(this, "Play/Pause clicked", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.next_track_button)
    public void onNextTrackButtonClicked(View v) {
        Toast.makeText(this, "Next track clicked", Toast.LENGTH_SHORT).show();
    }

    private void initView(final Track currentTrack) {
        albumTextView.setText(currentTrack.getAlbumName());
        trackTitleTextView.setText(currentTrack.getName());
        Picasso.with(this)
                .load(currentTrack.getPhotoUrl())
                .centerInside()
                .fit()
                .into(albumCoverImageView);
    }

}
