package com.bkim.android.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class GridViewAdapter extends BaseAdapter {
    static final String BASE = "http://image.tmdb.org/t/p/";
    static final String SIZE = "w185/";

    private final String LOG_TAG = this.getClass().getSimpleName();

    private Context context;
    private final List<String> urls = new ArrayList<String>();

    public GridViewAdapter(Context context) {
        this.context = context;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        String url = getItem(position);

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(url) //
                .placeholder(R.drawable.placeholder) //
                .error(R.drawable.error) //
                .fit() //
                .tag(context) //
                .into(view);

        return view;
    }

    @Override public int getCount() {
        return urls.size();
    }

    @Override public String getItem(int position) {
        return urls.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    public void addFoo(String str) {
        this.urls.add(BASE + SIZE + str);
    }

    public void clearFoo() {
        this.urls.clear();
    }

}
