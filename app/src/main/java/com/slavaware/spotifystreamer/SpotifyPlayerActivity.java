package com.slavaware.spotifystreamer;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.slavaware.spotifystreamer.model.Track;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.realm.Realm;

public class SpotifyPlayerActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, AudioManager.OnAudioFocusChangeListener {

    public static final String TAG = "SpotifyPlayerActivity";

    @InjectView(R.id.artist_text_view)
    TextView artistTextView;

    @InjectView(R.id.album_text_view)
    TextView albumTextView;

    @InjectView(R.id.album_cover_image_view)
    ImageView albumCoverImageView;

    @InjectView(R.id.track_title_text_view)
    TextView trackTitleTextView;

    @InjectView(R.id.playback_seek_bar)
    SeekBar seekBar;

    @InjectView(R.id.play_pause_button)
    ImageButton playPauseButton;

    @InjectView(R.id.current_playback_time_text_view)
    TextView currentPlaybackTimeTextView;

    @InjectView(R.id.track_duration_text_view)
    TextView trackDurationTextView;


    private MediaPlayer mediaPlayer;
    private Realm realm;
    private List<Track> tracks;
    private int currentTrackIndex;
    private boolean isPlayerPaused;
    private AudioManager audioManager;

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

        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        initView(tracks.get(currentTrackIndex));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mediaPlayer.release();
        mediaPlayer = null;
    }

    @OnClick(R.id.previous_track_button)
    public void onPreviousTrackButtonClicked(View v) {
        stopPlayback();
        if (currentTrackIndex <= 0) {
            currentTrackIndex = tracks.size() - 1;
        } else {
            currentTrackIndex--;
        }

        initView(tracks.get(currentTrackIndex));
    }

    @OnClick(R.id.play_pause_button)
    public void onPlayPauseButtonClicked(View v) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    @OnClick(R.id.next_track_button)
    public void onNextTrackButtonClicked(View v) {
        stopPlayback();
        if (currentTrackIndex <= 0) {
            currentTrackIndex = tracks.size() - 1;
        } else {
            currentTrackIndex--;
        }

        initView(tracks.get(currentTrackIndex));
    }

    private void initView(final Track currentTrack) {
        currentPlaybackTimeTextView.setText("0:00");
        trackDurationTextView.setText(formatTime(currentTrack.getDuration()));
        artistTextView.setText(currentTrack.getArtistName());
        albumTextView.setText(currentTrack.getAlbumName());
        trackTitleTextView.setText(currentTrack.getName());
        Picasso.with(this)
                .load(currentTrack.getPhotoUrl())
                .fit()
                .centerCrop()
                .into(albumCoverImageView);
    }

    private void startPlayback() {
        if (isPlayerPaused) {
            mediaPlayer.start();
        } else {
            final Track track = tracks.get(currentTrackIndex);
            try {
                mediaPlayer.reset();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(this, Uri.parse(track.getPreviewUrl()));
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(this);
            } catch (IOException e) {
                Log.e(TAG, "Unable to prepare media player", e);
                Toast.makeText(this, "Playback error", Toast.LENGTH_SHORT).show();
                return;
            }

            seekBar.setMax((int) track.getDuration());
        }

        playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();

            seekBar.setProgress(0);
            isPlayerPaused = false;
        }

        playPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlayerPaused = true;
        }

        playPauseButton.setImageResource(android.R.drawable.ic_media_play);
    }

    private Handler handler = new Handler();

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        // TODO: start following progress updates

        isPlayerPaused = false;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null && mediaPlayer.isPlaying()){
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBar.setProgress(currentPosition);
                    currentPlaybackTimeTextView.setText(formatTime(currentPosition));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;

            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                stopPlayback();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                pausePlayback();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }


    public static String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);

        StringBuilder timeFormat = new StringBuilder();
        if (minutes < 1) {
            timeFormat.append("0:");
        } else {
            timeFormat.append(minutes + ":");
        }

        seconds = seconds - (minutes * 60);
        if (seconds < 10) {
            timeFormat.append(0);
        }

        timeFormat.append(seconds);
        return timeFormat.toString();
    }
}
