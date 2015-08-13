package com.android.abhi.moviesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.abhi.moviesapp.R;
import com.android.abhi.moviesapp.model.Movie;
import com.squareup.picasso.Picasso;


/**
 * Created by abhi.pandey on 8/9/15.
 */
public class DetailMovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private Movie mMovie;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for movie data.
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.hasExtra("movie")) {
                mMovie = intent.getParcelableExtra("movie");

                String imageURL = mMovie.getImageURL();
                Picasso.with(getActivity().getBaseContext()).
                        load(imageURL).into((ImageView) rootView.findViewById(R.id.detail_movie_poster));

                ((TextView) rootView.findViewById(R.id.detail_movie_title))
                        .setText(mMovie.getTitle());

                String ratings = "Ratings: " +mMovie.getRatings();
                ((TextView) rootView.findViewById(R.id.detail_movie_rating))
                        .setText(ratings);

                String release = "Release: " +mMovie.getReleaseDate();
                ((TextView) rootView.findViewById(R.id.detail_movie_release))
                        .setText(release);

                TextView overViewTextView = (TextView) rootView.findViewById(R.id.detail_movie_overview);
                overViewTextView.setMovementMethod(new ScrollingMovementMethod());
                overViewTextView.setText(mMovie.getOverview());
            }

            return rootView;
        }
    }
}
