package com.gumino.core.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PushettaDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "pushetta.db";
	// TODO: Versioning per gestire le migrazioni di schema
	private static final int DATABASE_VERSION = 3;

	public PushettaDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		PushMessageTable.onCreate(db);
		ChannelTable.onCreate(db);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		PushMessageTable.onUpgrade(db, oldVersion, newVersion);
		ChannelTable.onUpgrade(db, oldVersion, newVersion);

	}

}
