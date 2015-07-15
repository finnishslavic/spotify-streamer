package com.slavaware.spotifystreamer.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Track extends RealmObject {

    @PrimaryKey
    private String id;
    private String name;
    private String albumName;
    private String photoUrl;

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
}
