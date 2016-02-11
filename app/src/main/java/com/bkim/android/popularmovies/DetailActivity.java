package com.bkim.android.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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

            ImageView view = (ImageView) rootView.findViewById(R.id.poster_path);
            if (view == null) {
                view = new ImageView(getActivity());
            }
            Picasso.with(getActivity()) //
                    .load(movieData.getPosterPath()) //
                    .placeholder(R.drawable.placeholder) //
                    .error(R.drawable.error) //
                    .fit()
                    .centerCrop()
                    .tag(getActivity()) //
                    .into(view);

            ((TextView) rootView.findViewById(R.id.original_title)).setText(movieData.original_title);
            ((TextView) rootView.findViewById(R.id.overview)).setText(movieData.overview);
            ((TextView) rootView.findViewById(R.id.vote_average)).setText(String.valueOf(movieData.vote_average));
            ((TextView) rootView.findViewById(R.id.release_date)).setText(movieData.release_date);

            return rootView;
        }
    }
}
