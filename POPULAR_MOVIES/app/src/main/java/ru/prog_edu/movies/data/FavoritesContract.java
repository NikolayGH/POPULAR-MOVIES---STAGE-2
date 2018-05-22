package ru.prog_edu.movies.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoritesContract {

    public static final String CONTENT_AUTHORITY = "ru.prog_edu.movies";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "data";

    public static final class FavoritesEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVORITES);

        public static final String TABLE_NAME = "favoritesMovies";
        public static final String _ID = BaseColumns._ID;
        public static final String ID_FAVORITE_MOVIE = "favoritesMovieId";
        public static final String MOVIE_NAME = "movieName";
        public static final String DESCRIPTION_MOVIE = "descriptionMovie";
        public static final String RATING_MOVIE = "ratingMovie";
        public static final String DATE_MOVIE_RELEASE = "dateMovieRelease";
        public static final String POSTER_MOVIE = "posterMovie";
    }
}
