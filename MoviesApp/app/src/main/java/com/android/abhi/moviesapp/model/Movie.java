package com.android.abhi.moviesapp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by abhi.pandey on 8/9/15.
 */
public class Movie implements Parcelable {

    private String id;
    private String title;
    private String overview;
    private String releaseDate;
    private String ratings;
    private String imageURL;

    public Movie(String id, String title, String overview, String releaseDate, String ratings, String imageURL) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.ratings = ratings;
        this.imageURL = imageURL;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getRatings() {
        return ratings;
    }

    public String getImageURL() {
        return imageURL;
    }

    private Movie(Parcel in) {
        id = in.readString();
        title = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        ratings = in.readString();
        imageURL = in.readString();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeString(ratings);
        dest.writeString(imageURL);
    }
    public static final Creator<Movie> CREATOR = new Creator<Movie>(){

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[0];
        }
    };
}
