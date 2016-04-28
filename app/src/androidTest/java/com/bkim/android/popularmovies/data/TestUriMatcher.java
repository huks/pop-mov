package com.bkim.android.popularmovies.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

public class TestUriMatcher extends AndroidTestCase {

    // content://com.bkim.android.popularmovies.app/movie
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    // content://com.bkim.android.popularmovies.app/movie_details
    private static final Uri TEST_MOVIE_DETAILS_DIR = MovieContract.MovieDetailsEntry.CONTENT_URI;

    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);
        assertEquals("Error: MOVIE DETAILS URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DETAILS_DIR), MovieProvider.MOVIE_DETAILS);
    }
}
