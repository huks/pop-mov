package com.bkim.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by bjkim on 2016-04-28.
 */
public class BoxofficeAdapter extends CursorAdapter {

    private final String LOG_TAG = BoxofficeAdapter.class.getSimpleName();

    public BoxofficeAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        This is ported from FetchMovieTask -- but now we go straight from the cursor the the string
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
//        Log.d(LOG_TAG, "convertCursorRowToUXFormat()!!!!");
//        int idx_title = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
//        int idx_poster = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
//        int idx_synopsis = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
//        int idx_rating = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RATING);
//        int idx_date = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_DATE);

        return cursor.getString(BoxofficeFragment.COL_MOVIE_TITLE) + "/" + cursor.getString(BoxofficeFragment.COL_MOVIE_POSTER);
//        return cursor.getString(idx_title) + "/" + cursor.getString(idx_poster);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        Log.d(LOG_TAG, "newView()!!!!");
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_boxoffice, parent, false);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        Log.d(LOG_TAG, "bindView()!!!!");
        TextView tv = (TextView)view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }
}
