package com.slavaware.spotifystreamer.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.slavaware.spotifystreamer.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class TracksAdapter extends BaseAdapter {

    private List<Track> tracks;
    private LayoutInflater layoutInflater;
    private Picasso picasso;

    public TracksAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        picasso = Picasso.with(context);
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    @Override
    public int getCount() {
        if (tracks == null) {
            return 0;
        }

        return tracks.size();
    }

    @Override
    public Object getItem(int position) {
        if (tracks == null || position >= tracks.size()) {
            throw new IndexOutOfBoundsException("Requested element (" + position + ") is out of bounds");
        }

        return tracks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.song_list_item, parent, false);
            view = new ViewHolder(convertView);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        Track track = (Track) getItem(position);
        view.albumTitleTextView.setText(track.album.name);
        view.trackTitleTextView.setText(track.name);

        final List<Image> images = track.album.images;
        if (images != null && images.size() > 0) {
            Image artistImage = images.get(0);
            picasso.load(artistImage.url)
                    .fit()
                    .centerCrop()
                    .into(view.albumImageView);
        } else {
            // TODO: put a placeholder
        }

        return convertView;
    }

    protected class ViewHolder {
        @InjectView(R.id.album_image_view)
        ImageView albumImageView;

        @InjectView(R.id.album_title_text_view)
        TextView albumTitleTextView;

        @InjectView(R.id.track_title_text_view)
        TextView trackTitleTextView;

        public ViewHolder(View source) {
            ButterKnife.inject(this, source);
        }

    }
}
