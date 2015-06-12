package com.slavaware.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.slavaware.spotifystreamer.utils.ArtistAdapter;
import com.slavaware.spotifystreamer.utils.SimpleTextWatcher;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class SpotifySearchActivity extends AppCompatActivity {

    public static final String TAG = SpotifySearchActivity.class.getSimpleName();

    public static final String SEARCH_TEXT_PARAM = "search_text";
    public static final String ARTIST_ID_KEY = "artist_id_key";
    public static final String ARTIST_NAME_KEY = "artist_name_key";

    @InjectView(R.id.list_view)
    ListView listView;

    @InjectView(R.id.search_input)
    EditText searchInput;

    private SpotifyApi spotifyApi;
    private SpotifyService spotify;
    private SimpleTextWatcher searchTextWatcher;
    private ArtistAdapter artistsAdapter;
    private SearchArtistsTask searchArtistsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);

        ButterKnife.inject(this);

        // Init views
        searchTextWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                super.afterTextChanged(s);

                performSearch(s.toString());
            }
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = (Artist) parent.getAdapter().getItem(position);
                Intent showTopTracks = new Intent(SpotifySearchActivity.this, TopTracksActivity.class);
                showTopTracks.putExtra(ARTIST_ID_KEY, artist.id);
                showTopTracks.putExtra(ARTIST_NAME_KEY, artist.name);

                startActivity(showTopTracks);
            }
        });

        searchInput.addTextChangedListener(searchTextWatcher);
        artistsAdapter = new ArtistAdapter(this);
        artistsAdapter.setArtists(Collections.EMPTY_LIST);
        listView.setAdapter(artistsAdapter);

        // Init other components
        spotifyApi = new SpotifyApi();
        spotify = spotifyApi.getService();
    }

    @Override
    protected void onDestroy() {
        searchInput.removeTextChangedListener(searchTextWatcher);
        stopBackgroundSearch();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_TEXT_PARAM, searchInput.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String savedText = savedInstanceState.getString(SEARCH_TEXT_PARAM);
        searchInput.setText(savedText);
        searchInput.setSelection(savedText.length());
        performSearch(savedText);
    }

    private void performSearch(String searchText) {
        if (searchText == null || searchText.length() <= 0) {
            setListItems(Collections.EMPTY_LIST);
        }

        stopBackgroundSearch();
        searchArtistsTask = new SearchArtistsTask();
        searchArtistsTask.execute(searchText);
    }

    private void stopBackgroundSearch() {
        if (searchArtistsTask != null && !searchArtistsTask.isCancelled()) {
            searchArtistsTask.cancel(true);
            searchArtistsTask = null;
        }
    }

    private void setListItems(List<Artist> artists) {
        artistsAdapter.setArtists(artists);
        artistsAdapter.notifyDataSetChanged();
    }

    public class SearchArtistsTask extends AsyncTask<String, Void, List<Artist>> {

        @Override
        protected List<Artist> doInBackground(String... params) {
            if (params == null || params.length <= 0) {
                throw new IllegalArgumentException("Search parameters must be provided");
            }

            String searchArtistText = params[0];
            List<Artist> result;
            try {
                ArtistsPager pager = spotify.searchArtists(searchArtistText);
                result = pager.artists.items;
            } catch (RetrofitError e) {
                Log.e(TAG, "Error loading artists", e);
                result = null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<Artist> artists) {
            if (artists == null) {
                // just ignore results as they are either coming from interruption or malformed request
                return;
            } else if (artists.size() <= 0) {
                Toast.makeText(SpotifySearchActivity.this,
                        getString(R.string.no_artists_found_message), Toast.LENGTH_SHORT).show();
            }

            setListItems(artists);
        }
    }

}
