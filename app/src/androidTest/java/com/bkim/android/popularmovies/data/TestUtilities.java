package com.bkim.android.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {

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
    static ContentValues createSampleMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 293660);
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Deadpool");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER, "\\/inVq3FRqcYIRl2la8iZikYYxFNR.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, "Based upon Marvel Comics’ most unconventional anti-hero, DEADPOOL tells the origin story of former Special Forces operative turned mercenary Wade Wilson, who after being subjected to a rogue experiment that leaves him with accelerated healing powers, adopts the alter ego Deadpool. Armed with his new abilities and a dark, twisted sense of humor, Deadpool hunts down the man who nearly destroyed his life.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RATING, 7.23);
        movieValues.put(MovieContract.MovieEntry.COLUMN_DATE, "2016-02-09");

        return movieValues;
    }
}
