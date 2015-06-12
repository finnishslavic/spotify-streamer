package com.slavaware.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.slavaware.spotifystreamer.utils.TracksAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class TopTracksActivity extends ActionBarActivity {

    public static final String TAG = TopTracksActivity.class.getSimpleName();

    @InjectView(R.id.track_list)
    ListView listView;

    private SpotifyApi spotifyApi;
    private SpotifyService spotify;
    private TracksAdapter tracksAdapter;
    private TopTracksAsyncTask topTracksAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.top_tracks_activity);

        ButterKnife.inject(this);

        // Get params
        final String artistId = getIntent().getStringExtra(SpotifySearchActivity.ARTIST_ID_KEY);
        if (artistId == null || artistId.isEmpty()) {
            throw new IllegalArgumentException("Artist must be provided");
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: show player
                Toast.makeText(TopTracksActivity.this, R.string.not_implemented_yet_message,
                        Toast.LENGTH_SHORT).show();
            }
        });

        tracksAdapter = new TracksAdapter(this);
        tracksAdapter.setTracks(Collections.EMPTY_LIST);
        listView.setAdapter(tracksAdapter);

        // Init other components
        spotifyApi = new SpotifyApi();
        spotify = spotifyApi.getService();

        performTopTracksSearch(artistId);
    }

    @Override
    protected void onDestroy() {
        stopBackgroundSearch();
        super.onDestroy();
    }

    private void performTopTracksSearch(String searchText) {
        if (searchText == null || searchText.length() <= 0) {
            setListItems(Collections.EMPTY_LIST);
        }

        stopBackgroundSearch();
        topTracksAsyncTask = new TopTracksAsyncTask();
        topTracksAsyncTask.execute(searchText);
    }

    private void stopBackgroundSearch() {
        if (topTracksAsyncTask != null && !topTracksAsyncTask.isCancelled()) {
            topTracksAsyncTask.cancel(true);
            topTracksAsyncTask = null;
        }
    }

    private void setListItems(List<Track> tracks) {
        tracksAdapter.setTracks(tracks);
        tracksAdapter.notifyDataSetChanged();
    }

    public class TopTracksAsyncTask extends AsyncTask<String, Void, List<Track>> {

        @Override
        protected List<Track> doInBackground(String... params) {
            if (params == null || params.length <= 0) {
                throw new IllegalArgumentException("Search parameters must be provided");
            }

            String searchArtistText = params[0];
            List<Track> result;
            try {
                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("country", "US");
                Tracks tracks = spotify.getArtistTopTrack(searchArtistText, queryParams);
                result = tracks.tracks;
            } catch (RetrofitError e) {
                Log.e(TAG, "Error loading top tracks", e);
                result = Collections.EMPTY_LIST;
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<Track> tracks) {
            if (tracks.size() <= 0) {
                Toast.makeText(TopTracksActivity.this,
                        getString(R.string.no_tracks_found_message), Toast.LENGTH_SHORT).show();
                setListItems(tracks);
            } else {
                setListItems(tracks);
            }
        }
    }

}
