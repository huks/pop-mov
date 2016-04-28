package com.bkim.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bkim.android.popularmovies.data.MovieContract.MovieEntry;
import com.bkim.android.popularmovies.data.MovieContract.MovieDetailsEntry;

/**
 * Manages a local database for movies data
 */
public class MovieDbHelper extends SQLiteOpenHelper {

    // if you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabases) {
        // Create a table to hold movie data...
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
                MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MovieEntry.COLUMN_POSTER + " TEXT, " +
                MovieEntry.COLUMN_SYNOPSIS + " TEXT, " +
                MovieEntry.COLUMN_RATING + " INTEGER, " +
                MovieEntry.COLUMN_DATE + " TEXT " +
                " );";

        final String SQL_CREATE_MOVIE_DETAILS_TABLE = "CREATE TABLE " + MovieDetailsEntry.TABLE_NAME + " (" +
                MovieDetailsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieDetailsEntry.COLUMN_MOV_KEY + " INTEGER NOT NULL, " +
                MovieDetailsEntry.COLUMN_TRAILER + " TEXT NOT NULL, " +
                MovieDetailsEntry.COLUMN_REVIEW + " TEXT NOT NULL, " +

                " FOREIGN KEY (" + MovieDetailsEntry.COLUMN_MOV_KEY + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + "), " +

                " UNIQUE (" + MovieDetailsEntry.COLUMN_MOV_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabases.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabases.execSQL(SQL_CREATE_MOVIE_DETAILS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieDetailsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
