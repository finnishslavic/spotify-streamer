package com.slavaware.spotifystreamer.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.slavaware.spotifystreamer.R;
import com.slavaware.spotifystreamer.SpotifyPlayerActivity;
import com.slavaware.spotifystreamer.TopTracksActivity;
import com.slavaware.spotifystreamer.model.ModelConverter;
import com.slavaware.spotifystreamer.model.Track;
import com.slavaware.spotifystreamer.utils.TracksAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;
import io.realm.RealmResults;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class TopTracksFragment extends Fragment {

    public static final String SEARCH_RESULTS = "search_results";
    public static final String EXTRA_TRACK_POSITION = "track_id";

    public static final String TAG = TopTracksActivity.class.getSimpleName();

    @InjectView(R.id.track_list)
    ListView listView;

    private SpotifyApi spotifyApi;
    private SpotifyService spotify;
    private TracksAdapter tracksAdapter;
    private TopTracksAsyncTask topTracksAsyncTask;

    private List<Track> searchResults;
    private String artistId;
    private String artistName;
    private Realm realm;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.top_tracks_fragment, container, false);
        ButterKnife.inject(this, rootView);

        // Init other components
        realm = Realm.getInstance(getActivity());
        spotifyApi = new SpotifyApi();
        spotify = spotifyApi.getService();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent playTrack = new Intent(getActivity(), SpotifyPlayerActivity.class);
                playTrack.putExtra(EXTRA_TRACK_POSITION, position);
                startActivity(playTrack);
            }
        });

        tracksAdapter = new TracksAdapter(getActivity());
        tracksAdapter.setTracks(Collections.EMPTY_LIST);
        listView.setAdapter(tracksAdapter);

        if (savedInstanceState != null) {
            initView(savedInstanceState);
        } else {
            initView(getActivity().getIntent().getExtras());
        }

        return rootView;
    }


    @Override
    public void onDestroyView() {
        stopBackgroundSearch();

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SpotifySearchFragment.ARTIST_ID_KEY, artistId);
        outState.putString(SpotifySearchFragment.ARTIST_NAME_KEY, artistName);

        realm.beginTransaction();
        if (searchResults != null && searchResults.size() > 0) {
            realm.copyToRealmOrUpdate(searchResults);
        }

        realm.commitTransaction();
    }

    private void initView(Bundle data) {
        // Get params
        artistId = data.getString(SpotifySearchFragment.ARTIST_ID_KEY);
        if (artistId == null || artistId.isEmpty()) {
            throw new IllegalArgumentException("Artist must be provided");
        }

        RealmResults<Track> trackRealmResults = realm.allObjects(Track.class);
        if (trackRealmResults != null && trackRealmResults.size() > 0) {
            setListItems(trackRealmResults.subList(0, trackRealmResults.size() - 1));
        } else {
            performTopTracksSearch(artistId);
        }
    }

    private void performTopTracksSearch(String searchText) {
        if (searchText == null || searchText.length() <= 0) {
            setListItems(new ArrayList<Track>(0));
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
        searchResults = tracks;
        tracksAdapter.setTracks(tracks);
        tracksAdapter.notifyDataSetChanged();
    }

    public class TopTracksAsyncTask extends AsyncTask<String, Void, ArrayList<Track>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Realm is so fast that it has to work on main thread, really
            // Cleanup old results
            realm.beginTransaction();
            realm.clear(Track.class);
            realm.commitTransaction();
        }

        @Override
        protected ArrayList<Track> doInBackground(String... params) {
            if (params == null || params.length <= 0) {
                throw new IllegalArgumentException("Search parameters must be provided");
            }

            String searchArtistText = params[0];
            ArrayList<Track> result;
            try {
                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("country", "US");
                Tracks tracks = spotify.getArtistTopTrack(searchArtistText, queryParams);
                result = new ArrayList<>(tracks.tracks.size());
                for (kaaes.spotify.webapi.android.models.Track track:tracks.tracks) {
                    result.add(ModelConverter.fromSpotifyTrack(track));
                }
            } catch (RetrofitError e) {
                Log.e(TAG, "Error loading top tracks", e);
                result = null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Track> tracks) {
            if (tracks == null) {
                // most likely was interrupted or malformed request
                return;
            } else if (tracks.size() <= 0) {
                Toast.makeText(getActivity(),
                        getString(R.string.no_tracks_found_message), Toast.LENGTH_SHORT).show();
            }

            setListItems(tracks);

            // Save track to internal db for further playback
            // save search result to db
            realm.beginTransaction();
            realm.copyToRealm(tracks);
            realm.commitTransaction();
        }
    }

}
