package ru.prog_edu.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FavoritesDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "favoritesList.db";
    private static final int DATABASE_VERSION = 1;

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " +
                FavoritesContract.FavoritesEntry.TABLE_NAME + " (" +
                FavoritesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoritesContract.FavoritesEntry.ID_FAVORITE_MOVIE + " INTEGER NOT NULL, " +
                FavoritesContract.FavoritesEntry.MOVIE_NAME + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.DESCRIPTION_MOVIE + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.RATING_MOVIE + " REAL NOT NULL, " +
                FavoritesContract.FavoritesEntry.DATE_MOVIE_RELEASE + " TEXT NOT NULL, " +
                FavoritesContract.FavoritesEntry.POSTER_MOVIE + " TEXT NOT NULL " +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesContract.FavoritesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
