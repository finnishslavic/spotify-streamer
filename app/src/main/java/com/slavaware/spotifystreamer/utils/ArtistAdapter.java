package com.slavaware.spotifystreamer.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.slavaware.spotifystreamer.R;
import com.slavaware.spotifystreamer.model.Artist;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ArtistAdapter extends BaseAdapter {

    private List<Artist> artists;
    private LayoutInflater layoutInflater;
    private Picasso picasso;

    public ArtistAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        picasso = Picasso.with(context);
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

    @Override
    public int getCount() {
        if (artists == null) {
            return 0;
        }

        return artists.size();
    }

    @Override
    public Object getItem(int position) {
        if (artists == null || position >= artists.size()) {
            throw new IndexOutOfBoundsException("Requested element (" + position + ") is out of bounds");
        }

        return artists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder view;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.artist_list_item, parent, false);
            view = new ViewHolder(convertView);
            convertView.setTag(view);
        } else {
            view = (ViewHolder) convertView.getTag();
        }

        Artist artist = (Artist) getItem(position);
        view.artistTextView.setText(artist.getName());

        if (!Strings.isNullOrEmpty(artist.getPhotoUrl())) {
            picasso.load(artist.getPhotoUrl())
                    .fit()
                    .centerCrop()
                    .into(view.artistImageView);
        } else {
            // TODO: put a placeholder
        }

        return convertView;
    }

    protected class ViewHolder {
        @InjectView(R.id.artist_image_view)
        ImageView artistImageView;

        @InjectView(R.id.artist_text_view)
        TextView artistTextView;

        public ViewHolder(View source) {
            ButterKnife.inject(this, source);
        }

    }
}
