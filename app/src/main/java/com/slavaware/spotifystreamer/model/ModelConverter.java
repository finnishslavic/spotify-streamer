package com.slavaware.spotifystreamer.model;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;

public class ModelConverter {

    public static Artist fromSpotifyArtist(kaaes.spotify.webapi.android.models.Artist spotifyArtist) {
        Artist artist = new Artist();
        artist.setId(spotifyArtist.id);
        artist.setName(spotifyArtist.name);

        final List<Image> images = spotifyArtist.images;
        if (images != null && images.size() > 0) {
            artist.setPhotoUrl(images.get(0).url);
        }

        return artist;
    }

    public static Track fromSpotifyTrack(kaaes.spotify.webapi.android.models.Track spotifyTrack) {
        Track track = new Track();
        track.setId(spotifyTrack.id);
        track.setName(spotifyTrack.name);
        track.setAlbumName(spotifyTrack.album.name);
        track.setPreviewUrl(spotifyTrack.preview_url);
        track.setDuration(spotifyTrack.duration_ms);

        final List<ArtistSimple> artists = spotifyTrack.artists;
        if (artists != null && artists.size() > 0) {
            ArtistSimple artist = artists.get(0);
            track.setArtistName(artist.name);
        }

        final List<Image> images = spotifyTrack.album.images;
        if (images != null && images.size() > 0) {
            track.setPhotoUrl(images.get(0).url);
        }

        return track;
    }

}
