package com.bkim.android.popularmovies.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.bkim.android.popularmovies.data.MovieContract.MovieDetailsEntry;
import com.bkim.android.popularmovies.data.MovieContract.MovieEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );

        mContext.getContentResolver().delete(
                MovieDetailsEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Movie table during delete", 0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                MovieDetailsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from MovieDetails table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void deleteAllRecordsFromDB() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(MovieEntry.TABLE_NAME, null, null);
        db.delete(MovieDetailsEntry.TABLE_NAME, null, null);
        db.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            assertEquals("Error: MovieProvider registered with authorityL " + providerInfo.authority +
                    " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        assertEquals("Error: the MoiveEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);

        type = mContext.getContentResolver().getType(MovieDetailsEntry.CONTENT_URI);
        assertEquals("Error: the MovieDetailsEntry CONTENT_URI should return MovieDetailsEntry.CONTENT_TYPE",
                MovieDetailsEntry.CONTENT_TYPE, type);
    }

    public void testBasicMovieDetailsQuery() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createDeadpoolMovieValues();
        long movieRowId = TestUtilities.insertDeadpoolMovieValues(mContext);

        ContentValues mDetailsValues = TestUtilities.createMovieDetailsValues(movieRowId);

        long mDetailsRowId = db.insert(MovieDetailsEntry.TABLE_NAME, null, mDetailsValues);
        assertTrue("Unable to Insert MovieDetailsEntry into the Database", mDetailsRowId != -1);

        db.close();

        Cursor mDetailsCursor = mContext.getContentResolver().query(
                MovieDetailsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicMovieDetailsQuery", mDetailsCursor, mDetailsValues);
    }

    public void testBasicMovieQueries() {
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues testValues = TestUtilities.createDeadpoolMovieValues();
        long movieRowId = TestUtilities.insertDeadpoolMovieValues(mContext);

        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testBasicMovieQueries, movie query", movieCursor, testValues);

        if ( Build.VERSION.SDK_INT >= 19 ) {
            assertEquals("Error: Movie Query did not properly set NotificationUri",
                    movieCursor.getNotificationUri(), MovieEntry.CONTENT_URI);
        }
    }

    public void testUpdateMovie() {
        ContentValues values = TestUtilities.createDeadpoolMovieValues();

        Uri movieUri = mContext.getContentResolver().
                insert(MovieEntry.CONTENT_URI, values);
        long movieRowId = values.getAsLong(MovieEntry.COLUMN_MOVIE_ID);
//        long movieRowId = ContentUris.parseId(movieUri);

        assertTrue(movieRowId != -1);
        Log.d(LOG_TAG, "New row id: " + movieRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(MovieEntry._ID, movieRowId);
        updatedValues.put(MovieEntry.COLUMN_TITLE, "Star Wars");

        Cursor movieCursor = mContext.getContentResolver().query(MovieEntry.CONTENT_URI, null, null, null, null);

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        movieCursor.registerContentObserver(tco);

        int count = mContext.getContentResolver().update(
                MovieEntry.CONTENT_URI, updatedValues, MovieEntry.COLUMN_MOVIE_ID + "= ?", // _ID가 아닌 유니크 영화 ID값, COLUMN_MOVIE_ID를 사용
                new String[] { Long.toString(movieRowId)});
        Log.d(LOG_TAG, "what count?: " + count);
        assertEquals(count, 1);

        tco.waitForNotificationOrFail();

        movieCursor.unregisterContentObserver(tco);
        movieCursor.close();

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                MovieEntry._ID + " = " + movieRowId,
                null,
                null
        );

        TestUtilities.validateCursor("testUpdateMovie. Error validating movie entry update.",
                cursor, updatedValues);

        cursor.close();
    }

    public void testInsertReadProvider() {
        ContentValues testValues = TestUtilities.createDeadpoolMovieValues();

        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = testValues.getAsLong(MovieEntry.COLUMN_MOVIE_ID);
        // long movieRowId = ContentUris.parseId(movieUri);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry.",
                cursor, testValues);

        // Now ASSUME that we have a movie, add some movie details???
        ContentValues mDetailsValues = TestUtilities.createMovieDetailsValues(movieRowId);
        tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MovieDetailsEntry.CONTENT_URI, true, tco);

        Uri mDetailsInsertUri = mContext.getContentResolver()
                .insert(MovieDetailsEntry.CONTENT_URI, mDetailsValues);
        assertTrue(mDetailsInsertUri != null);

        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        Cursor mDetailsCursor = mContext.getContentResolver().query(
                MovieDetailsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieDetailsEntry insert.",
                mDetailsCursor, mDetailsValues);
    }

    public void testDeletedRecords() {
        testInsertReadProvider();

        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        TestUtilities.TestContentObserver mDetailsObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieDetailsEntry.CONTENT_URI, true, mDetailsObserver);

        deleteAllRecordsFromProvider();

        movieObserver.waitForNotificationOrFail();
        mDetailsObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(movieObserver);
        mContext.getContentResolver().unregisterContentObserver(mDetailsObserver);
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;
    static ContentValues[] createBulkInsertMovieDetailsValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues values = new ContentValues();
            values.put(MovieEntry.COLUMN_MOVIE_ID, 293660 + i);
            values.put(MovieEntry.COLUMN_TITLE, "foo_bulk_title_" + i);
            values.put(MovieEntry.COLUMN_POSTER, "foo_bulk_poster_" + i);
            values.put(MovieEntry.COLUMN_SYNOPSIS, "foo_bulk_synopsis_" + i);
            values.put(MovieEntry.COLUMN_RATING, 6.9);
            values.put(MovieEntry.COLUMN_DATE, "2016-04-28");
            returnContentValues[i] = values;
        }
        return returnContentValues;
    }

    public void testBulkInsert() {
        ContentValues[] bulkInsertContentValues = createBulkInsertMovieDetailsValues();

        TestUtilities.TestContentObserver mObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, mObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, bulkInsertContentValues);

        mObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(mObserver);

        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        cursor.moveToFirst();
        for (int i = 0 ; i < BULK_INSERT_RECORDS_TO_INSERT ; i++, cursor.moveToNext() ) {
            TestUtilities.validateCurrentRecord("testBulkInsert. Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }
}
