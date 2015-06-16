package com.slavaware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class Artist implements Parcelable {

    private String id;
    private String name;
    private String photoUrl;

    public Artist() {
    }

    private Artist(Parcel in) {
        id = in.readString();
        name = in.readString();
        photoUrl = in.readString();
    }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getId() {
        return id;
    }

    public static Artist fromSpotifyArtist(kaaes.spotify.webapi.android.models.Artist spotifyArtist) {
        Artist artist = new Artist();
        artist.id = spotifyArtist.id;
        artist.name = spotifyArtist.name;

        final List<Image> images = spotifyArtist.images;
        if (images != null && images.size() > 0) {
            artist.photoUrl = images.get(0).url;
        } else {
            artist.photoUrl = null;
        }

        return artist;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(photoUrl);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
}
