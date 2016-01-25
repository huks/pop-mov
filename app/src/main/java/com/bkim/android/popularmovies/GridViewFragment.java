package com.bkim.android.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GridViewFragment extends Fragment {
    private GridViewAdapter gvAdapter;

    public GridViewFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // The GridViewAdapter will take data from a source and
        // use it to populate the GridView it's attached to.
        gvAdapter = new GridViewAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.grid_view_activity, container, false);

        // Get a reference to the GridView, and attached this adapter to it.
        GridView gv = (GridView) rootView.findViewById(R.id.grid_view);
        gv.setAdapter(gvAdapter);

        return rootView;
    }

    private void updateMovies() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        moviesTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private class FetchMoviesTask extends AsyncTask<Void, Void, String[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results";
        final String TMDB_POSTER_PATH = "poster_path";

        private String[] getMoviesDataFromJson(String moviesJsonStr) throws JSONException {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_RESULTS);

            String[] resultStrs = new String[moviesArray.length()];

            for (int i = 0; i < moviesArray.length(); i++) {
                String posterPath;

                // Get the JSON object representing the fooMovie
                JSONObject fooMovie = moviesArray.getJSONObject(i);

                posterPath = fooMovie.getString(TMDB_POSTER_PATH);

                resultStrs[i] = posterPath;
            }
            return resultStrs;

        }

        @Override
        protected String[] doInBackground(Void... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string
            String moviesJsonStr = null;

            try {
                // Construct the URL for the TMDB query
                // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=MY_API_KEY_HERE
                final String TMDB_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
                final String SORTING_PARAM = "sort_by";
                final String APIKEY_PARMAM = "api_key";

                Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORTING_PARAM, "popularity.desc")
                        .appendQueryParameter(APIKEY_PARMAM, BuildConfig.TMDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to TMDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing TMDB.
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                gvAdapter.clearFoo();
                for (String mStr : result) {
                    gvAdapter.addFoo(mStr);
                }
                // New data is back from the server.
                gvAdapter.notifyDataSetChanged();
            }
        }
    }
}
