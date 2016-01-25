package com.bkim.android.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class GridViewAdapter extends BaseAdapter {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private Context context;
    private final List<String> urls = new ArrayList<String>();

    public GridViewAdapter(Context context) {
        this.context = context;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(LOG_TAG, "getView()");

        SquaredImageView view = (SquaredImageView) convertView;
        if (view == null) {
            view = new SquaredImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        // Get the image URL for the current position.
        String url = getItem(position);
        Log.d(LOG_TAG, "url: " + url);

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
        this.urls.add(str);
        Log.d(LOG_TAG, "addFoo(" + str + ")");
    }

    public void clearFoo() {
        this.urls.clear();
    }

}
