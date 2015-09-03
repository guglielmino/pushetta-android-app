package com.gumino.core.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class PushMessageTable {
	// Database table
	public static final String TABLE_PUSHMESSAGE = "pushmessage";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_BODY = "body";
	public static final String COLUMN_CHANNEL_NAME = "channel_name";
	public static final String COLUMN_CHANNEL_ICON_IMAGE = "channel_icon_image";
	public static final String COLUMN_DATE_CREATED = "date_created";
	public static final String COLUMN_DATE_EXPIRE = "expire";
	public static final String COLUMN_SYNC_READ = "is_sync_read";
	public static final String COLUMN_PREVIEW_URL = "preview_url";
	public static final String COLUMN_DELETED = "deleted";

	// Database creation SQL statement
	private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_PUSHMESSAGE + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_BODY
			+ " text not null, " + COLUMN_CHANNEL_NAME + " text not null,"
			+ COLUMN_CHANNEL_ICON_IMAGE + " text not null,"
			+ COLUMN_DATE_CREATED + " datetime null," + COLUMN_DATE_EXPIRE
			+ " datetime null," + COLUMN_SYNC_READ
			+ " integer not null default 0," + COLUMN_PREVIEW_URL + " text null,"
			+ COLUMN_DELETED + " integer not null default 0"
			+ ");";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(PushMessageTable.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		
		if(oldVersion == 1 && newVersion == 2){
			database.execSQL("ALTER TABLE  " + TABLE_PUSHMESSAGE + " ADD COLUMN " + COLUMN_PREVIEW_URL + " text null");
		}
		else if(oldVersion == 2 && newVersion == 3) {
			database.execSQL("ALTER TABLE  " + TABLE_PUSHMESSAGE + " ADD COLUMN " + COLUMN_DELETED + " integer default 0");
		}
		else{
			database.execSQL("DROP TABLE IF EXISTS " + TABLE_PUSHMESSAGE);
		}
		// onCreate gestisce IF EXIST e quindi può essere sempre chiamato
		onCreate(database);
	}
}

	