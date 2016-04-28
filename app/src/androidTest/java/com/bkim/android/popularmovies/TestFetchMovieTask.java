package com.bkim.android.popularmovies;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.bkim.android.popularmovies.data.MovieContract;

/**
 * Created by bjkim on 2016-04-28.
 */
public class TestFetchMovieTask extends AndroidTestCase{
    static final int ADD_MOV_KEY = 1234;
    static final String ADD_TRAILER = "foo_trailer";
    static final String ADD_REVIEW = "foo_review";

    public void testAddMovieDetails() {
        getContext().getContentResolver().delete(MovieContract.MovieDetailsEntry.CONTENT_URI,
                MovieContract.MovieDetailsEntry.COLUMN_MOV_KEY + " = ?",
                new String[]{String.valueOf(ADD_MOV_KEY)});

        FetchMovieTask fmt = new FetchMovieTask(getContext(), null);
        long movId = fmt.addMovieDetails(ADD_MOV_KEY, ADD_TRAILER, ADD_REVIEW);

        // does addMovieDetails return a valid record ID?
        assertFalse("Error: addMovieDetails returned an invalid ID or insert",
                movId == -1);

        // test all this twice
        for ( int i = 0 ; i < 2; i++ ) {

            // doe the ID point to our mov?
            Cursor movCursor = getContext().getContentResolver().query(
                    MovieContract.MovieDetailsEntry.CONTENT_URI,
                    new String[]{
                            MovieContract.MovieDetailsEntry._ID,
                            MovieContract.MovieDetailsEntry.COLUMN_MOV_KEY,
                            MovieContract.MovieDetailsEntry.COLUMN_TRAILER,
                            MovieContract.MovieDetailsEntry.COLUMN_REVIEW
                    },
                    MovieContract.MovieDetailsEntry.COLUMN_MOV_KEY + " = ?",
                    new String[]{String.valueOf(ADD_MOV_KEY)},
                    null);

            // these match the indices of the projection
            if (movCursor.moveToFirst()) {
                assertEquals("Error: the queried value of movId does not match the returned value" +
                        "from addMovieDetails", movCursor.getLong(0), movId);
                assertEquals("Error: the queried value of mov_key is incorrect",
                        movCursor.getString(1), String.valueOf(ADD_MOV_KEY));
                assertEquals("Error: the queried value of trailer is incorrect",
                        movCursor.getString(2), ADD_TRAILER);
                assertEquals("Error: the queried value of review is incorrect",
                        movCursor.getString(3), ADD_REVIEW);
            } else {
                fail("Error: the id you used to query returned an empty cursor");
            }

            // there should be no more records
            assertFalse("Error: there should be only one record returned from a query",
                    movCursor.moveToNext());

            // add again
            long newMovId = fmt.addMovieDetails(ADD_MOV_KEY, ADD_TRAILER, ADD_REVIEW);

            assertEquals("Error: inserting again should return the same ID",
                    movId, newMovId);
        }
        // reset our state back to normal
        getContext().getContentResolver().delete(MovieContract.MovieDetailsEntry.CONTENT_URI,
                MovieContract.MovieDetailsEntry.COLUMN_MOV_KEY + " =?",
                new String[]{String.valueOf(ADD_MOV_KEY)});

        // clean up the test so that other tests can user the content provider
        getContext().getContentResolver().
                acquireContentProviderClient(MovieContract.MovieDetailsEntry.CONTENT_URI).
                getLocalContentProvider().shutdown();
    }
}
