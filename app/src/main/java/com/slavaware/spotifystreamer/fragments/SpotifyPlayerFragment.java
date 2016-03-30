package com.slavaware.spotifystreamer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.slavaware.spotifystreamer.R;
import com.slavaware.spotifystreamer.model.Track;
import com.slavaware.spotifystreamer.services.MusicPlaybackService;
import com.squareup.picasso.Picasso;
import io.realm.Realm;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SpotifyPlayerFragment extends DialogFragment {

    public static final String TAG = "SpotifyPlayerFragment";

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

    private Realm realm;
    private List<Track> tracks;
    private int currentTrackIndex;
    private PlaybackUpdatesReceiver updatesReceiver = new PlaybackUpdatesReceiver();
    private ShareActionProvider shareActionProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_player, container, false);

        ButterKnife.inject(this, rootView);

        // load current tracks data
        realm = Realm.getInstance(getActivity());
        tracks = realm.allObjects(Track.class);

        if (savedInstanceState == null) {
            currentTrackIndex = getArguments().getInt(MusicPlaybackService.EXTRA_TRACK_ID, 0);
        } else {
            currentTrackIndex = savedInstanceState.getInt(MusicPlaybackService.EXTRA_TRACK_ID);
        }

        initView(tracks.get(currentTrackIndex));
        startPlaybackService(currentTrackIndex);

        getActivity().registerReceiver(updatesReceiver,
                new IntentFilter(MusicPlaybackService.ACTION_UPDATE_PLAYBACK_STATUS));

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(updatesReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.playback_menu, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Intent myShareIntent = new Intent(Intent.ACTION_SEND);
            myShareIntent.setType("text/plain");
            myShareIntent.putExtra(Intent.EXTRA_TEXT,
                    tracks.get(currentTrackIndex).getPreviewUrl());
            setShareIntent(myShareIntent);
            startActivity(Intent.createChooser(myShareIntent, "Send to"));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.previous_track_button)
    public void onPreviousTrackButtonClicked(View v) {
        if (currentTrackIndex <= 0) {
            currentTrackIndex = tracks.size() - 1;
        } else {
            currentTrackIndex--;
        }

        seekBar.setProgress(0);
        initView(tracks.get(currentTrackIndex));
        performServerCommand(MusicPlaybackService.ACTION_CHANGE_TRACK, currentTrackIndex);
    }

    @OnClick(R.id.play_pause_button)
    public void onPlayPauseButtonClicked(View v) {
        pausePlaybackService();
    }

    @OnClick(R.id.next_track_button)
    public void onNextTrackButtonClicked(View v) {
        if (currentTrackIndex <= 0) {
            currentTrackIndex = tracks.size() - 1;
        } else {
            currentTrackIndex--;
        }

        seekBar.setProgress(0);
        initView(tracks.get(currentTrackIndex));
        performServerCommand(MusicPlaybackService.ACTION_CHANGE_TRACK, currentTrackIndex);
    }

    public static String formatTime(long millis) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);

        StringBuilder timeFormat = new StringBuilder();
        if (minutes < 1) {
            timeFormat.append("0:");
        } else {
            timeFormat.append(minutes);
            timeFormat.append(":");
        }

        seconds = seconds - (minutes * 60);
        if (seconds < 10) {
            timeFormat.append(0);
        }

        timeFormat.append(seconds);
        return timeFormat.toString();
    }

    private void initView(final Track currentTrack) {
        currentPlaybackTimeTextView.setText(formatTime(0));
        trackDurationTextView.setText(formatTime(currentTrack.getDuration()));
        artistTextView.setText(currentTrack.getArtistName());
        albumTextView.setText(currentTrack.getAlbumName());
        trackTitleTextView.setText(currentTrack.getName());
        seekBar.setEnabled(false);

        Picasso.with(getActivity())
                .load(currentTrack.getPhotoUrl())
                .fit()
                .centerCrop()
                .into(albumCoverImageView);
    }

    private void startPlaybackService(int trackIndex) {
        performServerCommand(MusicPlaybackService.ACTION_PLAY, trackIndex);
    }

    private void pausePlaybackService() {
        performServerCommand(MusicPlaybackService.ACTION_PAUSE, currentTrackIndex);
    }

    private void performServerCommand(final String action, final int trackIndex) {
        Intent musicPlayback = new Intent(getActivity(), MusicPlaybackService.class);
        musicPlayback.setAction(action);
        musicPlayback.putExtra(MusicPlaybackService.EXTRA_TRACK_ID, trackIndex);
        musicPlayback.putExtra(MusicPlaybackService.EXTRA_PAUSED_AT, seekBar.getProgress());
        getActivity().startService(musicPlayback);
    }

    private void updatePlaybackStatus(MusicPlaybackService.PlaybackStatus status, int trackDuration, int currentPosition) {
        if (status == MusicPlaybackService.PlaybackStatus.PLAYING) {
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause);

            seekBar.setMax(trackDuration);
            seekBar.setEnabled(true);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        currentPlaybackTimeTextView.setText(formatTime(seekBar.getProgress()));
                        performServerCommand(MusicPlaybackService.ACTION_PLAY, currentTrackIndex);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            seekBar.setProgress(currentPosition);
            currentPlaybackTimeTextView.setText(formatTime(currentPosition));
        } else if (status == MusicPlaybackService.PlaybackStatus.PAUSED) {
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else if (status == MusicPlaybackService.PlaybackStatus.STOPPED) {
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
            seekBar.setEnabled(false);
        }

        trackDurationTextView.setText(formatTime(trackDuration));
    }

    private class PlaybackUpdatesReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final Bundle extras = intent.getExtras();

            MusicPlaybackService.PlaybackStatus status =
                    MusicPlaybackService.PlaybackStatus.valueOf(extras.getString(MusicPlaybackService.EXTRA_PLAYBACK_STATUS));
            int trackDuration = extras.getInt(MusicPlaybackService.EXTRA_TRACK_LENGTH);
            int currentPosition = extras.getInt(MusicPlaybackService.EXTRA_CURRENT_PROGRESS);

            updatePlaybackStatus(status, trackDuration, currentPosition);
        }
    }
}
