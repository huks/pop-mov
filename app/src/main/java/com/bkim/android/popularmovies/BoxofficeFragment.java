package com.bkim.android.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bkim.android.popularmovies.data.MovieContract;

public class BoxofficeFragment extends Fragment {

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
//        String fooSetting = Utility.getPreferredFoo(getActivity());

        Uri movieUri = MovieContract.MovieEntry.buildMovieUri();

        Cursor cur = getActivity().getContentResolver().query(movieUri, null, null, null, null);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot user FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the fhe first time we run.
        mBoxofficeAdapter = new BoxofficeAdapter(getActivity(), cur, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_boxoffice);
        listView.setAdapter(mBoxofficeAdapter);

        return rootView;
    }

    private void updateMovie() {
        FetchMovieTask movieTask = new FetchMovieTask(getActivity());
        movieTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovie();
    }
}
