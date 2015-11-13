package com.slavaware.spotifystreamer.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.slavaware.spotifystreamer.model.Track;
import io.realm.Realm;
import java.io.IOException;
import java.util.List;

public class MusicPlaybackService extends Service implements MediaPlayer.OnPreparedListener,
        AudioManager.OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener {

    public static final String TAG = "MusicPlaybackService";

    public static final String ACTION_UPDATE_PLAYBACK_STATUS = "com.slavaware.spotifystreamer.action.UPDATE_PLAYBACK_STATUS";

    public static final String ACTION_PLAY = "com.slavaware.spotifystreamer.action.PLAY";
    public static final String ACTION_PAUSE = "com.slavaware.spotifystreamer.action.PAUSE";
    public static final String ACTION_STOP = "com.slavaware.spotifystreamer.action.STOP";
    public static final String ACTION_CHANGE_TRACK = "com.slavaware.spotifystreamer.action.CHANGE_TRACK";

    public static final String EXTRA_PAUSED_AT = "extra_paused_at";
    public static final String EXTRA_TRACK_ID = "extra_track_id";

    public static final String EXTRA_TRACK_LENGTH = "extra_track_length";
    public static final String EXTRA_PLAYBACK_STATUS = "extra_playback_status";
    public static final String EXTRA_CURRENT_PROGRESS = "extra_current_progress";

    public enum PlaybackStatus {
        PLAYING,
        PAUSED,
        STOPPED
    }

    private MediaPlayer mediaPlayer;
    private Realm realm;
    private List<Track> tracks;
    private int currentTrackIndex;
    private boolean isPlayerPaused;
    private int pausedAt;
    private AudioManager audioManager;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        // init members
        handler = new Handler();
        realm = Realm.getInstance(this);
        mediaPlayer = new MediaPlayer();
        audioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        // load current tracks data
        tracks = realm.allObjects(Track.class);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentTrackIndex = intent.getIntExtra(EXTRA_TRACK_ID, 0);
        pausedAt = intent.getIntExtra(EXTRA_PAUSED_AT, 0);

        final String action = intent.getAction();
        if (action.equals(ACTION_PLAY)) {
            prepareOrContinuePlayback();
        } else if (action.equals(ACTION_CHANGE_TRACK)) {
            isPlayerPaused = false;
            prepareOrContinuePlayback();
        } else if (action.equals(ACTION_PAUSE)) {
            pausePlayback();
        } else if (action.equals(ACTION_STOP)) {
            stopPlayback();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        startPlayback();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopPlayback();
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
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    private void prepareOrContinuePlayback() {
        if (isPlayerPaused) {
            startPlayback();
        } else {
            final Track track = tracks.get(currentTrackIndex);
            try {
                mediaPlayer.reset();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(this, Uri.parse(track.getPreviewUrl()));
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.setOnCompletionListener(this);
            } catch (IOException e) {
                Log.e(TAG, "Unable to prepare media player", e);
                // TODO: notify fragment of changes Toast.makeText(getActivity(), "Playback error", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void startPlayback() {
        mediaPlayer.start();
        isPlayerPaused = false;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    notifyChange(PlaybackStatus.PLAYING);
                    handler.postDelayed(this, 1000);
                }
            }
        });

        if (pausedAt > 0) {
            mediaPlayer.seekTo(pausedAt);
        }

        notifyChange(PlaybackStatus.PLAYING);
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();

            isPlayerPaused = false;
            pausedAt = 0;
        }

        notifyChange(PlaybackStatus.STOPPED);
    }

    private void pausePlayback() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            pausedAt = mediaPlayer.getCurrentPosition();
            isPlayerPaused = true;
        }

        notifyChange(PlaybackStatus.PAUSED);
    }

    private void notifyChange(PlaybackStatus status) {
        int trackLength = 0;
        int currentProgress = 0;

        if (mediaPlayer != null) {
            trackLength = mediaPlayer.getDuration();
            currentProgress = mediaPlayer.getCurrentPosition();
        }

        notifyChange(status, trackLength, currentProgress);
    }

    private void notifyChange(PlaybackStatus status, int trackLength, int currentProgress) {
        Intent broadcast = new Intent(ACTION_UPDATE_PLAYBACK_STATUS);
        broadcast.putExtra(EXTRA_PLAYBACK_STATUS, status.name());
        broadcast.putExtra(EXTRA_TRACK_LENGTH, trackLength);
        broadcast.putExtra(EXTRA_CURRENT_PROGRESS, currentProgress);
        sendBroadcast(broadcast);
    }
}
