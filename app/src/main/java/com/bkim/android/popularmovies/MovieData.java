package com.bkim.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieData implements Parcelable {
    String original_title; // original title
    String poster_path; // movie poster image thumbnail
    String overview; // a plot synopsis (called overview in the api)
    int vote_average; // user rating (called vote_average in the api)
    String release_date; // release date

    private static final String BASE = "http://image.tmdb.org/t/p/";
    private static final String SIZE = "w185/"; // size: "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones using “w185”is recommended.

    public MovieData() { }

    public MovieData(String originalTitle, String posterPath, String overview, int voteAverage, String releaseDate) {
        this.original_title = originalTitle;
        this.poster_path = posterPath;
        this.overview = overview;
        this.vote_average = voteAverage;
        this.release_date = releaseDate;
    }

    private MovieData(Parcel in) {
        original_title = in.readString();
        poster_path = in.readString();
        overview = in.readString();
        vote_average = in.readInt();
        release_date = "(" + in.readString() + ")";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return original_title + "--" + poster_path + "--" + overview + "--" + vote_average + "--" + release_date;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.original_title);
        parcel.writeString(this.poster_path);
        parcel.writeString(this.overview);
        parcel.writeInt(this.vote_average);
        parcel.writeString(this.release_date);
    }

    public static final Parcelable.Creator<MovieData> CREATOR = new Parcelable.Creator<MovieData>() {
        @Override
        public MovieData createFromParcel(Parcel parcel) {
            return new MovieData(parcel);
        }

        @Override
        public MovieData[] newArray(int i) {
            return new MovieData[i];
        }
    };

    public String getPosterPath() {
        return BASE + SIZE + poster_path;
    }

    public void setPosterPath(String posterPath) {
        this.poster_path = posterPath;
    }

}