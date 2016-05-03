package com.bkim.android.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bkim.android.popularmovies.data.MovieContract;

public class BoxofficeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = BoxofficeFragment.class.getSimpleName();

    private static final int BOXOFFICE_LOADER = 0;
    private BoxofficeAdapter mBoxofficeAdapter;

    public BoxofficeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to hand mene events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.boxofficefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMovie();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView()!!!!");

        // The CursorAdapter will take data from our cursor and populate the ListView.
        mBoxofficeAdapter = new BoxofficeAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_boxoffice);
        listView.setAdapter(mBoxofficeAdapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(BOXOFFICE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateMovie() {
        Log.d(LOG_TAG, "updateMovie()!!!!");
        FetchMovieTask movieTask = new FetchMovieTask(getActivity());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sorting = prefs.getString(
                getString(R.string.pref_sorting_key), getString(R.string.pref_sorting_popularity));
        movieTask.execute(sorting);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        Uri movieUri = MovieContract.MovieEntry.buildMovieUri();

        return new CursorLoader(getActivity(),
                movieUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(LOG_TAG, "onLoadFinished()!!!!");
        mBoxofficeAdapter.swapCursor(cursor);
        Log.d(LOG_TAG, "swapCursor()!!!!");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.d(LOG_TAG, "onLoaderReset()!!!!");
        mBoxofficeAdapter.swapCursor(null);
    }
}
