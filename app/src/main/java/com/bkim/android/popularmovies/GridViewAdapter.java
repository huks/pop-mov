package com.bkim.android.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class GridViewAdapter extends ArrayAdapter<MovieData> {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private Context context;
    private ArrayList<MovieData> movieDataList;

    public GridViewAdapter(Context context, int i, ArrayList<MovieData> movieDataList) {
        super(context, i, movieDataList);
        this.context = context;
        this.movieDataList = movieDataList;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view = (ImageView) convertView;
        if (view == null) {
            view = new ImageView(context);
            view.setScaleType(CENTER_CROP);
        }

        MovieData movieData = movieDataList.get(position);

        if (movieData != null) {
            Picasso.with(context) //
                    .load(movieData.getPosterPath()) //
                    .placeholder(R.drawable.placeholder) //
                    .error(R.drawable.error) //
                    .fit() //
                    .tag(context) //
                    .into(view);
        }

        return view;
    }

    @Override public int getCount() {
        return movieDataList.size();
    }

    @Override public long getItemId(int position) {
        return position;
    }

}
