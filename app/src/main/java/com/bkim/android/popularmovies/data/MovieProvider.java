package com.bkim.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class MovieProvider extends ContentProvider {

    private final String LOG_TAG = MovieProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_DETAILS = 200;
    static final int FOO = 300;

    private static final SQLiteQueryBuilder sDetailsByMovieIdQueryBuilder;
    private static final SQLiteQueryBuilder sFooQueryBuilder;

    static {
        sDetailsByMovieIdQueryBuilder = new SQLiteQueryBuilder();

        sDetailsByMovieIdQueryBuilder.setTables(
                MovieContract.MovieDetailsEntry.TABLE_NAME
        );
    }

    static {
        sFooQueryBuilder = new SQLiteQueryBuilder();

        sFooQueryBuilder.setTables(
                MovieContract.MovieEntry.TABLE_NAME
        );
    }

    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sFooSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    private Cursor getDetailsByMovieId(Uri uri, String[] projection) {
        String movieId = MovieContract.MovieDetailsEntry.getMovieKeyFromUri(uri);

        String selection;
        String[] selectionArgs;

        selection = sMovieIdSelection;
        selectionArgs = new String[]{movieId};

        return sDetailsByMovieIdQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    private Cursor getFooByMovieId(Uri uri, String[] projection) {
        String fooId = MovieContract.MovieEntry.getFooFromUri(uri);

        String selection;
        String[] selectionArgs;

        selection = sFooSelection;
        selectionArgs = new String[]{fooId};

        return sFooQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    static UriMatcher buildUriMatcher() {
        // The code passed into the constructor represents the code to return for the root URI.
        // It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);

        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", FOO);
//        matcher.addURI(authority, MovieContract.PATH_MOVIE_DETAILS, MOVIE_DETAILS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_DETAILS:
                return MovieContract.MovieDetailsEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case FOO: {
                retCursor = getFooByMovieId(uri, projection);
                break;
            }
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case MOVIE_DETAILS: {
                retCursor = getDetailsByMovieId(uri, projection);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert()!!!!");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    returnUri = MovieContract.MovieEntry.buildMovieUri();
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            case MOVIE_DETAILS: {
                long _id = db.insert(MovieContract.MovieDetailsEntry.TABLE_NAME, null, values);
                if (_id > 0 ) {
                    returnUri = MovieContract.MovieDetailsEntry.buildMovieDetailsUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_DETAILS:
                rowsDeleted = db.delete(MovieContract.MovieDetailsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MOVIE_DETAILS:
                rowsUpdated = db.update(MovieContract.MovieDetailsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        Log.d(LOG_TAG, "bulkInsert()!!!!");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int cnt = 0;
                try {
                    for (ContentValues value : values) {
                        Log.d(LOG_TAG, "value!!!!: " + value.toString());
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        Log.d(LOG_TAG, "inserted?!!!!");
                        if (_id != -1) {
                            cnt++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return cnt;
            case MOVIE_DETAILS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieDetailsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
