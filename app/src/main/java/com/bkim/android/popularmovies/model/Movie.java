package com.bkim.android.popularmovies.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.bkim.android.popularmovies.BoxofficeFragment;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie implements Parcelable {

    private int id;
    private String originalTitle; // original title
    private String posterPath; // movie poster image thumbnail
    private String overview; // a plot synopsis (called overview in the api)
    private int voteAverage; // user rating (called vote_average in the api)
    private String releaseDate; // release date

    private static final String BASE = "http://image.tmdb.org/t/p/";
    /*
     * size: "w92", "w154", "w185", "w342", "w500", "w780", or "original".
     * For most phones using “w185”is recommended.
     */
    private static final String SIZE = "w185/";

    public Movie() { }

    public Movie(int id, String originalTitle, String posterPath, String overview, int voteAverage, String releaseDate) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
    }

    private Movie(Parcel in) {
        this.id = in.readInt();
        this.originalTitle = in.readString();
        this.posterPath = in.readString();
        this.overview = in.readString();
        this.voteAverage = in.readInt();
        this.releaseDate = in.readString();
    }

    // These are the names of JSON objects that need to be extracted.
    private static final String TMDB_ID = "id";
    private static final String TMDB_ORIGINAL_TITLE = "original_title";
    private static final String TMDB_POSTER_PATH = "poster_path";
    private static final String TMDB_OVERVIEW = "overview";
    private static final String TMDB_VOTE_AVERAGE = "vote_average";
    private static final String TMDB_RELEASE_DATE = "release_date";

    public Movie(JSONObject movie) throws JSONException {
        this.id = movie.getInt(TMDB_ID);
        this.originalTitle = movie.getString(TMDB_ORIGINAL_TITLE);
        this.posterPath = movie.getString(TMDB_POSTER_PATH);
        this.overview = movie.getString(TMDB_OVERVIEW);
        this.voteAverage = movie.getInt(TMDB_VOTE_AVERAGE);
        this.releaseDate = movie.getString(TMDB_RELEASE_DATE);
    }

    public Movie(Cursor cursor) {
        this.id = cursor.getInt(BoxofficeFragment.COL_MOVIE_ID);
        this.originalTitle = cursor.getString(BoxofficeFragment.COL_ORIGINAL_TITLE);
        this.posterPath = cursor.getString(BoxofficeFragment.COL_POSTER_PATH);
        this.overview = cursor.getString(BoxofficeFragment.COL_OVERVIEW);
        this.voteAverage = cursor.getInt(BoxofficeFragment.COL_VOTE_AVERAGE);
        this.releaseDate = cursor.getString(BoxofficeFragment.COL_RELEASE_DATE);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeString(this.originalTitle);
        parcel.writeString(this.posterPath);
        parcel.writeString(this.overview);
        parcel.writeInt(this.voteAverage);
        parcel.writeString(this.releaseDate);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

    // getter
    public int getId() {
        return id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public int getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }
}