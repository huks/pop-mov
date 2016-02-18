package com.bkim.android.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returend. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /* Default movie values for the database tests. */
    static ContentValues createMovieValues(long movieId) {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieId);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "title");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER, "poster");
        movieValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, "synopsis");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, "rating");
        movieValues.put(MovieContract.MovieEntry.COLUMN_DATE, "date");

        return movieValues;
    }
}
