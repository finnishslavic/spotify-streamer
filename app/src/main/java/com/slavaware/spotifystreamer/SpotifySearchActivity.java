package com.slavaware.spotifystreamer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.slavaware.spotifystreamer.model.Artist;
import com.slavaware.spotifystreamer.model.ModelConverter;
import com.slavaware.spotifystreamer.utils.ArtistAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import io.realm.Realm;
import io.realm.RealmResults;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class SpotifySearchActivity extends AppCompatActivity {

    public static final String TAG = SpotifySearchActivity.class.getSimpleName();

    public static final String SEARCH_TEXT_PARAM = "search_text";
    public static final String SEARCH_RESULTS = "search_results";
    public static final String ARTIST_ID_KEY = "artist_id_key";
    public static final String ARTIST_NAME_KEY = "artist_name_key";

    @InjectView(R.id.list_view)
    ListView listView;

    @InjectView(R.id.search_input)
    SearchView searchInput;

    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;

    private SpotifyApi spotifyApi;
    private SpotifyService spotify;
    private ArtistAdapter artistsAdapter;
    private SearchArtistsTask searchArtistsTask;
    private List<Artist> searchResults;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify);

        ButterKnife.inject(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Artist artist = (Artist) parent.getAdapter().getItem(position);
                Intent showTopTracks = new Intent(SpotifySearchActivity.this, TopTracksActivity.class);
                showTopTracks.putExtra(ARTIST_ID_KEY, artist.getId());
                showTopTracks.putExtra(ARTIST_NAME_KEY, artist.getName());

                startActivity(showTopTracks);
            }
        });

        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && newText.length() >= 3) {
                    performSearch(newText);
                } else {
                    setListItems(new ArrayList<Artist>(0));
                }

                return true;
            }
        });

        artistsAdapter = new ArtistAdapter(this);
        artistsAdapter.setArtists(new ArrayList<Artist>(0));
        listView.setAdapter(artistsAdapter);

        // Init other components
        spotifyApi = new SpotifyApi();
        spotify = spotifyApi.getService();
        realm = realm.getInstance(this);
    }

    @Override
    protected void onDestroy() {
        stopBackgroundSearch();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SEARCH_TEXT_PARAM, searchInput.getQuery().toString());

        realm.beginTransaction();
        if (searchResults != null && searchResults.size() > 0) {
            realm.copyToRealmOrUpdate(searchResults);
        }
        realm.commitTransaction();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String savedText = savedInstanceState.getString(SEARCH_TEXT_PARAM);
        searchInput.setQuery(savedText, false);

        // Realm db is sooo fast!
        RealmResults<Artist> results = realm.allObjects(Artist.class);
        if (results.size() > 0) {
            setListItems(results.subList(0, results.size() - 1));
        }
    }

    private void performSearch(String searchText) {
        if (searchText == null || searchText.length() <= 0) {
            setListItems(new ArrayList<Artist>(0));
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
        searchResults = artists;
        artistsAdapter.setArtists(artists);
        artistsAdapter.notifyDataSetChanged();
    }

    public class SearchArtistsTask extends AsyncTask<String, Void, ArrayList<Artist>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<Artist> doInBackground(String... params) {
            if (params == null || params.length <= 0) {
                throw new IllegalArgumentException("Search parameters must be provided");
            }

            String searchArtistText = params[0];
            ArrayList<Artist> result;
            try {
                ArtistsPager pager = spotify.searchArtists(searchArtistText);
                result = new ArrayList<>(pager.artists.items.size());
                for (kaaes.spotify.webapi.android.models.Artist artist:pager.artists.items) {
                    result.add(ModelConverter.fromSpotifyArtist(artist));
                }

            } catch (RetrofitError e) {
                Log.e(TAG, "Error loading artists", e);
                result = null;
            }

            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Artist> artists) {
            if (artists == null) {
                // just ignore results as they are either coming from interruption or malformed request
                return;
            } else if (artists.size() <= 0) {
                Toast.makeText(SpotifySearchActivity.this,
                        getString(R.string.no_artists_found_message), Toast.LENGTH_SHORT).show();
            }

            setListItems(artists);
            progressBar.setVisibility(View.GONE);
        }
    }

}
