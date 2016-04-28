package com.bkim.android.popularmovies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.bkim.android.popularmovies.data.MovieContract;
import com.bkim.android.popularmovies.data.MovieContract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by bjkim on 2016-04-27.
 */
public class FetchMovieTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private ArrayAdapter<String> mBoxOfficeAdapter;
    private final Context mContext;

    public FetchMovieTask(Context context, ArrayAdapter<String> boxOfficeAdapter) {
        mContext = context;
        mBoxOfficeAdapter = boxOfficeAdapter;
    }

    private boolean DEBUG = true;

    long addMovieDetails(int movKey, String trailer, String review) {
        long movId;

        Cursor movCursor = mContext.getContentResolver().query(
                MovieContract.MovieDetailsEntry.CONTENT_URI,
                new String[]{MovieContract.MovieDetailsEntry._ID},
                MovieContract.MovieDetailsEntry.COLUMN_MOV_KEY + " = ?",
                new String[]{String.valueOf(movKey)},
                null);

        if (movCursor.moveToFirst()) {
            int movIndex = movCursor.getColumnIndex(MovieContract.MovieDetailsEntry._ID);
            movId = movCursor.getLong(movIndex);
        } else {
            ContentValues movValues = new ContentValues();

            movValues.put(MovieContract.MovieDetailsEntry.COLUMN_MOV_KEY, movKey);
            movValues.put(MovieContract.MovieDetailsEntry.COLUMN_TRAILER, trailer);
            movValues.put(MovieContract.MovieDetailsEntry.COLUMN_REVIEW, review);

            Uri insertedUri = mContext.getContentResolver().insert(
                    MovieContract.MovieDetailsEntry.CONTENT_URI,
                    movValues
            );

            movId = ContentUris.parseId(insertedUri);
        }
        movCursor.close();

        return movId;
    }

    String[] convertContentValuesToUXFormat(Vector<ContentValues> cvv) {
        // return strings to keep UI functional for now
        String[] resultStrs = new String[cvv.size()];
        for ( int i = 0; i < cvv.size(); i++ ) {
            ContentValues movieValues = cvv.elementAt(i);
            resultStrs[i] = movieValues.getAsString(MovieEntry.COLUMN_TITLE);
        }
        return resultStrs;
    }

    private String[] getMovieDataFromJson(String boxOfficeJsonStr, String sortSetting) throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results"; // "BoxOffice" list
        final String TMDB_ID = "id";
        final String TMDB_ORIGINAL_TITLE = "original_title";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_OVERVIEW = "overview";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_RELEASE_DATE = "release_date";

        try {
            JSONObject boxOffice = new JSONObject(boxOfficeJsonStr);
            JSONArray movieArray = boxOffice.getJSONArray(TMDB_RESULTS);

            // Insert the new movie information into the...
            Vector<ContentValues> cvVector = new Vector<ContentValues>(movieArray.length());

            for (int i = 0; i < movieArray.length(); i++) {
                String _id;
                String originalTitle;
                String posterPath;
                String overview;
                int voteAverage;
                int voteCount; // for better highest rated sorting
                String releaseDate;

                // Get the JSON object representing the...
                JSONObject fooMovie = movieArray.getJSONObject(i);

                _id = fooMovie.getString(TMDB_ID);
                originalTitle = fooMovie.getString(TMDB_ORIGINAL_TITLE);
                posterPath = fooMovie.getString(TMDB_POSTER_PATH);
                overview = fooMovie.getString(TMDB_OVERVIEW);
                voteAverage = fooMovie.getInt(TMDB_VOTE_AVERAGE);
                releaseDate = fooMovie.getString(TMDB_RELEASE_DATE);

                ContentValues movieValues = new ContentValues();

                movieValues.put(MovieEntry.COLUMN_MOVIE_ID, _id);
                movieValues.put(MovieEntry.COLUMN_TITLE, originalTitle);
                movieValues.put(MovieEntry.COLUMN_POSTER, posterPath);
                movieValues.put(MovieEntry.COLUMN_SYNOPSIS, overview);
                movieValues.put(MovieEntry.COLUMN_RATING, voteAverage);
                movieValues.put(MovieEntry.COLUMN_DATE, releaseDate);

                cvVector.add(movieValues);
            }

            // add to database
            if (cvVector.size() > 0) {
                ContentValues[] cvArray = new ContentValues[cvVector.size()];
                cvVector.toArray(cvArray);
                mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, cvArray);
            }

            // Sort order?
//            String sortOrder = MovieEntry.COLUMN_RATING + " DESC";
            Uri movieUri = MovieEntry.buildMovieUri();

            // Display what you stored in the bulkInsert
            Cursor cursor = mContext.getContentResolver().query(movieUri, null, null, null, null);

            cvVector = new Vector<ContentValues>(cursor.getCount());
            if (cursor.moveToFirst()) {
                do {
                    ContentValues cv = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(cursor, cv);
                    cvVector.add(cv);
                } while (cursor.moveToNext());
            }

            Log.d(LOG_TAG, "FetchMovieTask Complete. " + cvVector.size() + " Inserted");

            String[] resultStrs = convertContentValuesToUXFormat(cvVector);
            return resultStrs;

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected String[] doInBackground(String... params) {

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
        String boxOfficeJsonStr = null;

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
            boxOfficeJsonStr = buffer.toString();

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
            return getMovieDataFromJson(boxOfficeJsonStr, sortQuery);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing TMDB.
        return null;
    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null && mBoxOfficeAdapter != null) {
            mBoxOfficeAdapter.clear();
            for (String fooMovie : result) {
                mBoxOfficeAdapter.add(fooMovie);
            }
        }
    }
}
