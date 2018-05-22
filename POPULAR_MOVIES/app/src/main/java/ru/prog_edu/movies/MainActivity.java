package ru.prog_edu.movies;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import ru.prog_edu.movies.data.FavoritesContract;
import ru.prog_edu.movies.utilities.NetworkState;
import ru.prog_edu.movies.utilities.NetworkUtils;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>,
        MoviesAdapter.OnSelectedItemListener{

    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private RecyclerView movieRecycler;
    private final static String POPULAR_MOVIES_PARAMETER = "popular";
    private final static String TOP_RATED_MOVIES_PARAMETER = "top_rated";
    private final static int ID_LOADER = 18;
    private ArrayList<Movie> movies;
    private ArrayList<Movie> favoriteMovies;
    private boolean isOnline;
    private final String SELECTED_FAVORITE_POSITION = "menu position";
    private int mItemMenuSelected;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SELECTED_FAVORITE_POSITION, mItemMenuSelected);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mItemMenuSelected = savedInstanceState.getInt(SELECTED_FAVORITE_POSITION, 1);
        }

        setContentView(R.layout.activity_main);
        Context context = this;
        NetworkState networkState = new NetworkState(context);
        isOnline= networkState.isOnline();

        movieRecycler = findViewById(R.id.images_recycler_view);

        getSupportLoaderManager().initLoader(ID_LOADER, null, this);

    }

    @Override
    protected void onResume() {
        if (isOnline) {
            if(mItemMenuSelected ==1){
                getFavoritesFromDB();
                RecyclerView.Adapter imageAdapter;
                imageAdapter = new MoviesAdapter(favoriteMovies, this);
                movieRecycler.setAdapter(imageAdapter);
                imageAdapter.notifyDataSetChanged();
            }else if(mItemMenuSelected ==2){
                makeMoviesQuery(TOP_RATED_MOVIES_PARAMETER);
            }else{
                makeMoviesQuery(POPULAR_MOVIES_PARAMETER);
            }
            movieRecycler.setHasFixedSize(true);
            movieRecycler.setLayoutManager(new GridLayoutManager(this, 2));

        }else {
            Toast.makeText(this, "NO INTERNET CONNECTION!!!", Toast.LENGTH_LONG).show();
        }
        super.onResume();
    }

    private void makeMoviesQuery(String searchParameter){
        URL moviesQueryUrl = NetworkUtils.buildMovieUrl(searchParameter);
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, moviesQueryUrl.toString());
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> moviesLoader = loaderManager.getLoader(ID_LOADER);

        if (moviesLoader == null) {
            loaderManager.initLoader(ID_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(ID_LOADER, queryBundle, this);
        }
    }

    @NonNull
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {

            String myMoviesJson;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    return;
                }
                if (myMoviesJson != null) {
                    deliverResult(myMoviesJson);
                } else {
                    forceLoad();
                }
            }

            @Override
            public String loadInBackground() {
                String searchQueryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);
                if (searchQueryUrlString == null || TextUtils.isEmpty(searchQueryUrlString)) {
                    return null;
                }
                try {
                    URL movUrl = new URL(searchQueryUrlString);
                    return  NetworkUtils.getResponseFromHttpUrl(movUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                    return  null;
                }
            }

            @Override
            public void deliverResult(String githubJson) {
                myMoviesJson = githubJson;
                super.deliverResult(githubJson);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if(data != null&& !data.equals("")){
            try {
                JSONObject moviesResponse = new JSONObject(data);
                JSONArray arrayMovies = moviesResponse.getJSONArray("results");
                movies = new ArrayList<>();
                for (int i = 0; i < arrayMovies.length(); i++) {
                    movies.add(new Movie());
                    movies.get(i).setPosterPath(arrayMovies.getJSONObject(i).getString("poster_path"));
                    movies.get(i).setTitle(arrayMovies.getJSONObject(i).getString("title"));
                    movies.get(i).setReleaseDate(arrayMovies.getJSONObject(i).getString("release_date"));
                    movies.get(i).setVoteAverage(arrayMovies.getJSONObject(i).getDouble("vote_average"));
                    movies.get(i).setOverview(arrayMovies.getJSONObject(i).getString("overview"));
                    movies.get(i).setId(arrayMovies.getJSONObject(i).getInt("id"));

                }

                RecyclerView.Adapter imageAdapter;
                imageAdapter = new MoviesAdapter(movies, this);
                movieRecycler.setAdapter(imageAdapter);
                imageAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    @Override
    public void onListItemClick(int selectedMovie) {

        Log.i("Activity clicked", Integer.toString(selectedMovie));
        Movie movie;
        if(mItemMenuSelected == 1) {
            movie = favoriteMovies.get(selectedMovie);
        }else{
            movie = movies.get(selectedMovie);
        }
        Intent intent = new Intent(this, DetailsMovieActivity.class);
        intent.putExtra(Movie.class.getCanonicalName(), movie);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.movie_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.top_rated:
                mItemMenuSelected = 2;
                makeMoviesQuery(TOP_RATED_MOVIES_PARAMETER);
                movieRecycler.setHasFixedSize(true);
                movieRecycler.setLayoutManager(new GridLayoutManager(this, 2));
                return true;
            case R.id.most_popular:
                mItemMenuSelected = 3;
                makeMoviesQuery(POPULAR_MOVIES_PARAMETER);
                movieRecycler.setHasFixedSize(true);
                movieRecycler.setLayoutManager(new GridLayoutManager(this, 2));
                return true;
            case R.id.favorites:
                mItemMenuSelected = 1;
                getFavoritesFromDB();
                RecyclerView.Adapter imageAdapter;
                imageAdapter = new MoviesAdapter(favoriteMovies, this);
                movieRecycler.setAdapter(imageAdapter);
                imageAdapter.notifyDataSetChanged();
                movieRecycler.setHasFixedSize(true);
                movieRecycler.setLayoutManager(new GridLayoutManager(this, 2));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getFavoritesFromDB() {
        String[] projection = {FavoritesContract.FavoritesEntry._ID,
                FavoritesContract.FavoritesEntry.ID_FAVORITE_MOVIE,
                FavoritesContract.FavoritesEntry.MOVIE_NAME,
                FavoritesContract.FavoritesEntry.DESCRIPTION_MOVIE,
                FavoritesContract.FavoritesEntry.RATING_MOVIE,
                FavoritesContract.FavoritesEntry.DATE_MOVIE_RELEASE,
                FavoritesContract.FavoritesEntry.POSTER_MOVIE
        };

        Cursor cursor = getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI , projection, null, null, null);
        Toast.makeText(this, "Number movies in Your Favorite List " + cursor.getCount(), Toast.LENGTH_LONG).show();
        int numberOfColumns = cursor.getCount();
        favoriteMovies = new ArrayList<>();

        for (int i = 0; i < numberOfColumns; i++) {
            favoriteMovies.add(new Movie());
            cursor.moveToPosition(i);
            favoriteMovies.get(i).setPosterPath(cursor.getString(6));
            favoriteMovies.get(i).setReleaseDate(cursor.getString(5));
            favoriteMovies.get(i).setVoteAverage(cursor.getDouble(4));
            favoriteMovies.get(i).setOverview(cursor.getString(3));
            favoriteMovies.get(i).setTitle(cursor.getString(2));
            favoriteMovies.get(i).setId(cursor.getInt(1));

        }
        cursor.close();
    }
}
