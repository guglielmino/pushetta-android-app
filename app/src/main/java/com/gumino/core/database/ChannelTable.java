package com.gumino.core.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ChannelTable {
	// Database table
	public static final String TABLE_CHANNEL = "channel";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DESCRIPTION = "description";
	public static final String COLUMN_IMAGE = "image";

	// Database creation SQL statement
	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_CHANNEL + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_NAME
			+ " text not null, " + COLUMN_DESCRIPTION + " text not null,"
			+ COLUMN_IMAGE + " text not null" + ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(ChannelTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANNEL);
		onCreate(database);
	}
}
