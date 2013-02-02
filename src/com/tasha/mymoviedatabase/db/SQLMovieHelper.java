package com.tasha.mymoviedatabase.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLMovieHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "MyMovieDatabase";
	private static final String TABLE_NAME = "movies";
	
	private static final String KEY_IMDB_ID = "imdb_id";
	private static final String KEY_TITLE = "title"; // Ex. Mean Girls
	private static final String KEY_YEAR = "year"; // Ex. 2007
	private static final String KEY_RATED = "rated"; // Ex. PG-13
	private static final String KEY_RATING = "rating"; // Ex. 5
	
	private Context context;
	protected SQLiteDatabase universalDB;


	public SQLMovieHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = ctx;
		universalDB = context.openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		createTable();
	}
	
	public void closeDatabase() {
		universalDB.close();
	}
	
	public void createTable() {
    	String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
				KEY_IMDB_ID + "TEXT, " + KEY_TITLE + " TEXT, " + KEY_YEAR + " CHAR(4), " +
    			KEY_RATED + " VARCHAR(5), "+ KEY_RATING + " INT(1))";
		universalDB.execSQL(CREATE_TABLE);
	}
}
