package com.bkim.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.bkim.android.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bjkim on 2016-04-27.
 */
public class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

//    private ArrayAdapter<String> mBoxofficeAdapter;
    private final Context mContext;

    public FetchMovieTask(Context context) {
        mContext = context;
//        mBoxofficeAdapter = boxofficeAdapter;
    }

    private boolean DEBUG = true;

//    long addMovieDetails(int movKey, String trailer, String review) {
//        long movId;
//
//        Cursor movCursor = mContext.getContentResolver().query(
//                MovieContract.MovieDetailsEntry.CONTENT_URI,
//                new String[]{MovieContract.MovieDetailsEntry._ID},
//                MovieContract.MovieDetailsEntry.COLUMN_MOV_KEY + " = ?",
//                new String[]{String.valueOf(movKey)},
//                null);
//
//        if (movCursor.moveToFirst()) {
//            int movIndex = movCursor.getColumnIndex(MovieContract.MovieDetailsEntry._ID);
//            movId = movCursor.getLong(movIndex);
//        } else {
//            ContentValues movValues = new ContentValues();
//
//            movValues.put(MovieContract.MovieDetailsEntry.COLUMN_MOV_KEY, movKey);
//            movValues.put(MovieContract.MovieDetailsEntry.COLUMN_TRAILER, trailer);
//            movValues.put(MovieContract.MovieDetailsEntry.COLUMN_REVIEW, review);
//
//            Uri insertedUri = mContext.getContentResolver().insert(
//                    MovieContract.MovieDetailsEntry.CONTENT_URI,
//                    movValues
//            );
//
//            movId = ContentUris.parseId(insertedUri);
//        }
//        movCursor.close();
//
//        return movId;
//    }

//    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
//        // return strings to keep UI functional for now
//        String[] resultStrs = new String[cvv.size()];
//        for ( int i = 0; i < cvv.size(); i++ ) {
//            ContentValues movieValues = cvv.elementAt(i);
//            resultStrs[i] = movieValues.getAsString(MovieEntry.COLUMN_TITLE);
//        }
//        return resultStrs;
//    }

    private List<Movie> getMovieDataFromJson(String jsonStr) throws JSONException {

        Log.d(LOG_TAG, "getMovieDataFromJson()!!!!");

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results"; // "Boxoffice" list
        final String TMDB_ID = "id";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";

        try {
            JSONObject movieJson = new JSONObject(jsonStr);
            JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

            List<Movie> movieList = new ArrayList<>();

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject fooMovie = movieArray.getJSONObject(i);
                Movie movie = new Movie(fooMovie);
                movieList.add(movie);
            }
            return movieList;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected List<Movie> doInBackground(String... params) {

        Log.d(LOG_TAG, "doInBackground("+params+")!!!!");

        // If there's no SORTING_PARAM(like "popularity.desc"), return null. Verify size of params.
        if (params.length == 0) {
            return null;
        }
        String sortQuery = params[0];

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string
        String jsonStr = null;

        try {
            // Construct the URL for the TMDB query
            // http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&api_key=MY_API_KEY_HERE
            final String TMDB_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORTING_PARAM = "sort_by";
            final String VOTE_COUNT_PARAM = "vote_count.gte";
            final int MIN_VOTE_COUNT = 50;
            final String API_KEY_PARMAM = "api_key";

            Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                    .appendQueryParameter(SORTING_PARAM, sortQuery)
                    .appendQueryParameter(API_KEY_PARMAM, BuildConfig.TMDB_API_KEY)
                    .build();

            // For better sorting highest-rated movies
            if (sortQuery.equals("vote_average.desc")) {
                Log.d(LOG_TAG, "vote_average.desc!!!!");
                builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORTING_PARAM, sortQuery)
                        .appendQueryParameter(VOTE_COUNT_PARAM, String.valueOf(MIN_VOTE_COUNT))
                        .appendQueryParameter(API_KEY_PARMAM, BuildConfig.TMDB_API_KEY)
                        .build();
            } else {
                // do nothing
            }

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
            jsonStr = buffer.toString();
            Log.d(LOG_TAG, "jsonStr!!!!: " + jsonStr);
            getMovieDataFromJson(jsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
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
        return null;
    }
}
