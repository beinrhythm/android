package com.android.abhi.moviesapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.android.abhi.moviesapp.R;
import com.android.abhi.moviesapp.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by abhi.pandey on 8/9/15.
 */
public class MoviesAdapter extends BaseAdapter {

    private Context mContext;
    private List<Movie> mMovies;

    public MoviesAdapter(Context context, List<Movie> movies) {
        mContext = context;
        mMovies = movies;
    }

    @Override
    public int getCount() {
        return mMovies.size();
    }

    @Override
    public Movie getItem(int position) {
        return mMovies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.grid_item_movie, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.movieIcon = (ImageView) convertView.findViewById(R.id.movieIcon);

            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        Movie movie = mMovies.get(position);
        String imageURL = movie.getImageURL();
        Picasso.with(mContext).load(imageURL).into(viewHolder.movieIcon);

        return convertView;
    }

    public void replace(List<Movie> movieList) {
        this.mMovies.clear();
        this.mMovies.addAll(movieList);
        notifyDataSetChanged();
    }

    /**
     * The view holder design pattern prevents using findViewById()
     * repeatedly in the getView() method of the adapter.
     */
    private static class ViewHolder {
        ImageView movieIcon;
    }
}
