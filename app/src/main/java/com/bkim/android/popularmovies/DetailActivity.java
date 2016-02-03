package com.bkim.android.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private String mMovieStr;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.detail_fragment, container, false);

            // The detail Activity called via intent. Inspect the intent for movie data.
            Bundle bundle = getActivity().getIntent().getExtras();
            MovieData movieData = bundle.getParcelable("movieData");

            ((TextView) rootView.findViewById(R.id.detail_text)).setText(movieData.getPosterPath());

            return rootView;
        }
    }
}
