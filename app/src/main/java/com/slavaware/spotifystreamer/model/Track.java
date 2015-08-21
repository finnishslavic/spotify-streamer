package com.slavaware.spotifystreamer.model;

import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Track extends RealmObject {

    public static final long PREVIEW_DURATION_MS = TimeUnit.SECONDS.toMillis(30);

    @PrimaryKey
    private String id;
    private String name;
    private String albumName;
    private String photoUrl;
    private String artistName;
    private long duration;
    private String previewUrl;

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

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
         return duration;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }
}
