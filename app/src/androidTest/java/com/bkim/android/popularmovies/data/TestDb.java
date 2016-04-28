package com.bkim.android.popularmovies.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean state
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.
        This makes sure that we always have a clena test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in thd DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.MovieDetailsEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly", c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while ( c.moveToNext() );

        // if this fails, it means that your database doesn't contain the movie entry table
        assertTrue("Error: Your database was created without the movie entry table", tableNameHashSet.isEmpty());

        // now, do our table contains the correct columns? : MovieEntry table
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<>();
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RATING);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_DATE);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while ( c.moveToNext() );

        // If this fails, it mean that your database doesn't contain all of the required movie entry columns
        assertTrue("Error: The database doesn't contain call of the required movie entry columns",
                movieColumnHashSet.isEmpty());
        db.close();
    }

    public void testMovieTable() { insertMovie(); }

    public void testMovieDetailsTable() {

        long movieRowId = insertMovie();

        assertFalse("Error: Movie Not Inserted Correctly", movieRowId == -1L);

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues mDetailsValues = TestUtilities.createMovieDetailsValues(movieRowId);

        long mDetailsRowId = db.insert(MovieContract.MovieDetailsEntry.TABLE_NAME, null, mDetailsValues);
        assertTrue(mDetailsRowId != -1);

        Cursor mDetailsCursor = db.query(
                MovieContract.MovieDetailsEntry.TABLE_NAME,  // table to query
                null,  // all columns
                null,  // columns for the "where" clause
                null,  // values for the "where" clause
                null,  // columns to group by
                null,  // columns to filter by row groups
                null  // sort order
        );

        assertTrue("Error: No Records returned from movie details query", mDetailsCursor.moveToFirst());

        TestUtilities.validateCurrentRecord("testInsertReadDb movieDetailsEntry failed to validate",
                mDetailsCursor, mDetailsValues);

        assertFalse("Error: More than one record returned from movie details query",
                mDetailsCursor.moveToNext());

        mDetailsCursor.close();
        db.close();
    }

    public long insertMovie() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createDeadpoolMovieValues();

        long movieRowId;
        movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, testValues);

        assertTrue(movieRowId != -1);

        Cursor cursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: No Records returned from movie query", cursor.moveToFirst());

        TestUtilities.validateCurrentRecord("Error: Movie Query Validation Failed",
                cursor, testValues);

        assertFalse("Error: More than one record returend from movie query",
                cursor.moveToNext());

        cursor.close();
        db.close();
        return movieRowId;
    }
}
