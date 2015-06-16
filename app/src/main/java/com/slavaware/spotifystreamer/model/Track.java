package com.slavaware.spotifystreamer.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;

public class Track implements Parcelable {

    private String id;
    private String name;
    private String albumName;
    private String photoUrl;

    public Track() {
    }

    private Track(Parcel in) {
        id = in.readString();
        name = in.readString();
        albumName = in.readString();
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

    public String getAlbumName() {
        return albumName;
    }

    public static Track fromSpotifyTrack(kaaes.spotify.webapi.android.models.Track spotifyTrack) {
        Track track = new Track();
        track.id = spotifyTrack.id;
        track.name = spotifyTrack.name;
        track.albumName = spotifyTrack.album.name;

        final List<Image> images = spotifyTrack.album.images;
        if (images != null && images.size() > 0) {
            track.photoUrl = images.get(0).url;
        } else {
            track.photoUrl = null;
        }

        return track;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(albumName);
        dest.writeString(photoUrl);
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };
}
