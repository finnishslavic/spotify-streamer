package com.slavaware.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
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
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpotifySearchActivity extends ActionBarActivity {

    @InjectView(R.id.list_view)
    ListView listView;

    @InjectView(R.id.search_input)
    EditText searchInput;

    private SpotifyApi spotifyApi;
    private SpotifyService spotify;
    private SimpleTextWatcher searchTextWatcher;
    private ArtistAdapter artistsAdapter;

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
        super.onDestroy();
    }

    private void performSearch(String searchText) {
        if (searchText == null || searchText.length() <= 0) {
            setListItems(Collections.EMPTY_LIST);
        }

        spotify.searchArtists(searchText, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                final List<Artist> artists = artistsPager.artists.items;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (artists == null || artists.size() <= 0) {
                            Toast.makeText(SpotifySearchActivity.this,
                                    getString(R.string.no_artists_found_message), Toast.LENGTH_SHORT).show();
                            setListItems(Collections.EMPTY_LIST);
                        } else {
                            setListItems(artists);
                        }
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpotifySearchActivity.this,
                                getString(R.string.error_finding_artists), Toast.LENGTH_SHORT).show();
                        setListItems(Collections.EMPTY_LIST);
                        // TODO: add better error handling
                    }
                });
            }
        });

    }

    private void setListItems(List<Artist> artists) {
        artistsAdapter.setArtists(artists);
        artistsAdapter.notifyDataSetChanged();
    }

}
