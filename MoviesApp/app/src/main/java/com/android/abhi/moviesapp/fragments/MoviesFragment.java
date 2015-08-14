package com.android.abhi.moviesapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.android.abhi.moviesapp.R;
import com.android.abhi.moviesapp.activities.MovieDetailActivity;
import com.android.abhi.moviesapp.adapters.MoviesAdapter;
import com.android.abhi.moviesapp.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhi.pandey on 8/9/15.
 */
public class MoviesFragment extends Fragment {

    private MoviesAdapter mMoviesAdapter;
    private ArrayList<Movie> mMoviesList = new ArrayList<>();
    private Menu optionsMenu;
    private String mSortBy;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("movies")) {
                mMoviesList = savedInstanceState.getParcelableArrayList("movies");
            }
            if (savedInstanceState.containsKey("sortBy")) {
                mSortBy = savedInstanceState.getString("sortBy");
            }
        } else {
            mSortBy = getString(R.string.pref_sort_popularity);
            updateMovies(mSortBy);
        }
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_moviefragment, menu);
        this.optionsMenu = menu;

        inflater.inflate(R.menu.refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_popularity: {
                mSortBy = getString(R.string.pref_sort_popularity);
                updateMovies(mSortBy);
                return true;
            }
            case R.id.action_toprated: {
                mSortBy = getString(R.string.pref_sort_toprated);
                updateMovies(mSortBy);
                return true;
            }
            case R.id.airport_menuRefresh: {
                setRefreshActionButtonState(true);
                updateMovies(mSortBy);
                setRefreshActionButtonState(false);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ArrayAdapter will take data from a source and
        // use it to populate the GridView it's attached to.
        mMoviesAdapter =
                new MoviesAdapter(
                        getActivity(), // The current context (this activity)
                        mMoviesList);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the GridView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.gridView_movies);
        gridView.setAdapter(mMoviesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mMoviesAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra("movie", movie);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("movies", mMoviesList);
        savedInstanceState.putString("sortBy", mSortBy);
        super.onSaveInstanceState(savedInstanceState);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    private void createToastWithMessage(String toastMessage) {
        Context context = getActivity().getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, toastMessage, duration);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.airport_menuRefresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    private void updateMovies(String sortBy) {
        //check network connection here
        if (isNetworkConnected()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            String sortOrder = prefs.getString(getString(R.string.pref_sort_key),
                    getString(R.string.sort_desc));
            String order = sortBy.concat(".").concat(sortOrder);
            FetchMovieTask moviesTask = new FetchMovieTask();
            moviesTask.execute(order);
        } else {
            createToastWithMessage("No internet connectivity");
        }
    }


    public class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        private final String API_KEY = getString(R.string.pref_apikey);

        //TODO: Need to put minimum votes as settings item
        private String minimumVotes = "1000";

        @Override
        protected List<Movie> doInBackground(String... params) {

            // If there's no params, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieJsonStr = null;

            try {
                // Construct the URL for the MovieDB API query
                final String MOVIEDB_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String KEY = "api_key";
                final String SORT_PARAM = "sort_by";
                final String VOTE_COUNT_G_PARAM = "vote_count.gte";

                Uri builtUri = Uri.parse(MOVIEDB_BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0])
                        .appendQueryParameter(VOTE_COUNT_G_PARAM, minimumVotes)
                        .appendQueryParameter(KEY, API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());


                // Create the request to MovieDB API, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                Log.d(LOG_TAG, "HTTP code - " + urlConnection.getResponseMessage());

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the moviedata.
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movieList) {
            if (movieList != null) {
                mMoviesList.addAll(movieList);
                mMoviesAdapter.replace(movieList);
            }
        }

        private List<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String MOVIE_LIST = "results";
            final String MOVIE_ID = "id";
            final String MOVIE_TITLE = "title";
            final String MOVIE_OVERVIEW = "overview";
            final String MOVIE_RELEASE_DATE = "release_date";
            final String MOVIE_POSTER_PATH = "poster_path";
            final String MOVIE_RATINGS = "vote_average";

            final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
            final String POSTER_SIZE = "w185";

            JSONObject moviesJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = moviesJson.getJSONArray(MOVIE_LIST);

            List<Movie> moviesList = new ArrayList<>();

            for (int i = 0; i < movieArray.length(); i++) {
                String id;
                String title;
                String overview;
                String releaseDate;
                String ratings;
                String posterPath;

                JSONObject movie = movieArray.getJSONObject(i);

                id = movie.getString(MOVIE_ID);
                title = movie.getString(MOVIE_TITLE);
                overview = movie.getString(MOVIE_OVERVIEW);
                releaseDate = movie.getString(MOVIE_RELEASE_DATE);
                ratings = movie.getString(MOVIE_RATINGS);
                posterPath = POSTER_BASE_URL + POSTER_SIZE + movie.getString(MOVIE_POSTER_PATH);
                Movie m = new Movie(id, title, overview, releaseDate, ratings, posterPath);
                moviesList.add(m);
            }
            return moviesList;
        }
    }
}
