package ru.prog_edu.movies;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import ru.prog_edu.movies.data.FavoritesContract;
import ru.prog_edu.movies.data.FavoritesDbHelper;
import ru.prog_edu.movies.utilities.NetworkState;
import ru.prog_edu.movies.utilities.NetworkUtils;

public class DetailsMovieActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>, TrailersAdapter.OnSelectedTrailerListener {

    private int id;
    private double voteAverage;
    private String title;
    private String posterPath;
    private String overview;
    private String releaseDate;

    private TextView originalTitleTextView;
    private TextView synopsisTextView;
    private TextView releaseDateTextView;
    private TextView userRatingTextView;
    private ImageView moviePosterImageView;
    private ImageButton addToFavoriteBtn;

    //fields for STAGE 2

    private static final String SEARCH_QUERY_URL_EXTRA = "query";
    private static final String SEARCH_QUERY_URL_EXTRA_REVIEW = "query_review";
    private final static String VIDEOS_PARAMETER = "videos";
    private final static String REVIEWS_PARAMETER = "reviews";
    private RecyclerView trailersRecycler;
    private RecyclerView reviewsRecycler;
    private final static int ID_LOADER = 36;
    private final static int ID_LOADER_REVIEW = 72;
    private ArrayList<Trailer> trailers;

    private final LoaderManager.LoaderCallbacks<String> reviewDataListener = new LoaderManager.LoaderCallbacks<String>() {
        @SuppressLint("StaticFieldLeak")
        @NonNull
        @Override
        public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
            return new AsyncTaskLoader<String>(getApplicationContext()) {
                String myReviewJson;

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if(args == null){
                        return;
                    }
                    if(myReviewJson!=null){
                        deliverResult(myReviewJson);
                    }else{
                        forceLoad();
                    }
                }
                @Nullable
                @Override
                public String loadInBackground() {
                    String reviewQueryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA_REVIEW);
                    if(reviewQueryUrlString == null||TextUtils.isEmpty(reviewQueryUrlString)){
                        return null;
                    }
                    try{
                        URL reviewURL = new URL(reviewQueryUrlString);
                        return NetworkUtils.getResponseFromHttpUrl(reviewURL);
                    }catch (IOException e){
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                public void deliverResult(@Nullable String data) {
                    myReviewJson = data;
                    super.deliverResult(data);
                }
            };
        }

        @Override
        public void onLoadFinished(@NonNull Loader<String> loader, String data) {
            if(data!=null&&!data.equals("")){
                try{
                    JSONObject reviewsResponse = new JSONObject(data);
                    JSONArray arrayReviews = reviewsResponse.getJSONArray("results");
                    ArrayList<Review> reviews = new ArrayList<>();
                    for (int i = 0; i < arrayReviews.length(); i++) {
                        reviews.add(new Review());
                        reviews.get(i).setAuthor(arrayReviews.getJSONObject(i).getString("author"));
                        reviews.get(i).setContent(arrayReviews.getJSONObject(i).getString("content"));
                    }
                    RecyclerView.Adapter reviewAdapter;
                    reviewAdapter = new ReviewAdapter(reviews);
                    reviewsRecycler.setAdapter(reviewAdapter);
                    reviewAdapter.notifyDataSetChanged();
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }
        @Override
        public void onLoaderReset(@NonNull Loader<String> loader) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_movie_activity);
        NetworkState networkState = new NetworkState(this);
        boolean isOnline = networkState.isOnline();
        getSupportLoaderManager().initLoader(ID_LOADER, null, this);
        final String mainImageUrl = "http://image.tmdb.org/t/p/w185/";

        Movie movie = getIntent().getParcelableExtra(
                Movie.class.getCanonicalName());

        initViews();

        String imageURL = mainImageUrl+movie.getPosterPath();

        Picasso.get()
                .load(imageURL)
                .into(moviePosterImageView);

        id = movie.getId();
        title = movie.getTitle();
        overview = movie.getOverview();
        voteAverage = movie.getVoteAverage();
        releaseDate = movie.getReleaseDate();
        posterPath = movie.getPosterPath();

        originalTitleTextView.setText(movie.getTitle());
        synopsisTextView.setText(movie.getOverview());
        releaseDateTextView.setText(movie.getReleaseDate());
        userRatingTextView.setText(String.valueOf(movie.getVoteAverage()));
        int movieId = movie.getId();


        if(isOnline){
            makeTrailersQuery(movieId, VIDEOS_PARAMETER);
            makeReviewsQuery(movieId, REVIEWS_PARAMETER);

            trailersRecycler.setHasFixedSize(true);
            reviewsRecycler.setHasFixedSize(true);
            trailersRecycler.setLayoutManager(new LinearLayoutManager(this));
            reviewsRecycler.setLayoutManager(new LinearLayoutManager(this));
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(trailersRecycler.getContext(),
                    DividerItemDecoration.VERTICAL);
            trailersRecycler.addItemDecoration(dividerItemDecoration);
            DividerItemDecoration reviewsDividerItemDecoration = new DividerItemDecoration(reviewsRecycler.getContext(),
                    DividerItemDecoration.VERTICAL);
            reviewsRecycler.addItemDecoration(reviewsDividerItemDecoration);
        }else{
            Toast.makeText(this, "NO INTERNET CONNECTION!!!", Toast.LENGTH_LONG).show();
        }

        addToFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToFavoriteList();
            }
        });
        FavoritesDbHelper favoritesDbHelper = new FavoritesDbHelper(this);
        favoritesDbHelper.getWritableDatabase();
    }

    private void addToFavoriteList(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(FavoritesContract.FavoritesEntry.ID_FAVORITE_MOVIE, id);
        contentValues.put(FavoritesContract.FavoritesEntry.MOVIE_NAME, title);
        contentValues.put(FavoritesContract.FavoritesEntry.DESCRIPTION_MOVIE, overview);
        contentValues.put(FavoritesContract.FavoritesEntry.RATING_MOVIE, voteAverage);
        contentValues.put(FavoritesContract.FavoritesEntry.DATE_MOVIE_RELEASE, releaseDate);
        contentValues.put(FavoritesContract.FavoritesEntry.POSTER_MOVIE, posterPath);
        Uri newUri = getContentResolver().insert(FavoritesContract.FavoritesEntry.CONTENT_URI, contentValues);

        if (newUri == null) {
            Toast.makeText(this, "Movie was not saved as favorite :(",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Movie was saved as favorite !!!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void makeTrailersQuery(int id, String searchParameter) {
        URL trailersQueryUrl = NetworkUtils.buildTrailersUrl(id, searchParameter);
        Log.e("---trailersQueryUrl--- ", String.valueOf(trailersQueryUrl));
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, trailersQueryUrl.toString());
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> trailerLoader = loaderManager.getLoader(ID_LOADER);
        if (trailerLoader == null) {
            loaderManager.initLoader(ID_LOADER, queryBundle, this);
        } else {
            loaderManager.restartLoader(ID_LOADER, queryBundle, this);
        }
    }

    private void makeReviewsQuery(int id, String searchParameter){
        URL reviewsUrl = NetworkUtils.buildTrailersUrl(id, searchParameter);
        Log.e("---reviewsUrl--- ", String.valueOf(reviewsUrl));
        Bundle queryBundle = new Bundle();
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA_REVIEW, reviewsUrl.toString());
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> reviewLoader = loaderManager.getLoader(ID_LOADER_REVIEW);
        if (reviewLoader == null) {
            loaderManager.initLoader(ID_LOADER_REVIEW, queryBundle, reviewDataListener);
        } else {
            loaderManager.restartLoader(ID_LOADER_REVIEW, queryBundle, reviewDataListener);
        }
    }

    private void initViews() {
        originalTitleTextView = findViewById(R.id.original_title);
        synopsisTextView = findViewById(R.id.synopsis);
        releaseDateTextView = findViewById(R.id.release_date);
        userRatingTextView = findViewById(R.id.user_rating);
        moviePosterImageView = findViewById(R.id.movie_poster);
        addToFavoriteBtn = findViewById(R.id.add_to_favorite_button);
        trailersRecycler = findViewById(R.id.trailers_recycler_view);
        reviewsRecycler = findViewById(R.id.reviews_recycler_view);
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            String myTrailersJson;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if(args == null){
                    return;
                }
                if (myTrailersJson != null) {
                    deliverResult(myTrailersJson);
                } else {
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public String loadInBackground() {
                String trailerQueryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);
                if(trailerQueryUrlString == null|| TextUtils.isEmpty(trailerQueryUrlString)){
                    return null;
                }
                try {
                    URL trailerURL = new URL(trailerQueryUrlString);
                    return NetworkUtils.getResponseFromHttpUrl(trailerURL);
                }  catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable String data) {
                myTrailersJson = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        if(data != null && !data.equals("")){
            try {
                JSONObject trailersResponse = new JSONObject(data);
                JSONArray arrayTrailers = trailersResponse.getJSONArray("results");
                trailers = new ArrayList<>();
                for (int i = 0; i < arrayTrailers.length(); i++) {
                    trailers.add(new Trailer());
                    trailers.get(i).setKey(arrayTrailers.getJSONObject(i).getString("key"));
                    trailers.get(i).setName(arrayTrailers.getJSONObject(i).getString("name"));
                    trailers.get(i).setSite(arrayTrailers.getJSONObject(i).getString("site"));
                    trailers.get(i).setSize(arrayTrailers.getJSONObject(i).getInt("size"));
                    trailers.get(i).setType(arrayTrailers.getJSONObject(i).getString("type"));
                }

                RecyclerView.Adapter trailersAdapter;
                trailersAdapter = new TrailersAdapter(trailers, this);
                trailersRecycler.setAdapter(trailersAdapter);
                trailersAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {

    }

    @Override
    public void onListItemClick(int selectedTrailer) {
        String selectedTrailerKey = trailers.get(selectedTrailer).getKey();
        watchYoutubeVideo(this, selectedTrailerKey);

    }
    private static void watchYoutubeVideo(Context context, String selectedTrailerKey){
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + selectedTrailerKey));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + selectedTrailerKey));
        try {
            context.startActivity(appIntent);
        } catch (ActivityNotFoundException ex) {
            context.startActivity(webIntent);
        }
    }
}
