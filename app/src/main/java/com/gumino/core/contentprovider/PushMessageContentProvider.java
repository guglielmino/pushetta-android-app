package com.gumino.core.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.gumino.core.database.PushMessageTable;
import com.gumino.core.database.PushettaDatabaseHelper;

public class PushMessageContentProvider extends ContentProvider {
	// database
	private PushettaDatabaseHelper database;

	// used for the UriMacher
	private static final int PUSHES = 10;
	private static final int PUSH_ID = 20;

	private static final String AUTHORITY = "com.gumino.core.contentprovider";

	private static final String BASE_PATH = "pushmessages";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);

	public static Uri GetSingleContentUri(int messageId) {
		return Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + "/"
				+ messageId);
	}

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, PUSHES);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", PUSH_ID);
	}

	@Override
	public boolean onCreate() {
		database = new PushettaDatabaseHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// Using SQLiteQueryBuilder instead of query() method
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

		// check if the caller has requested a column which does not exists
		checkColumns(projection);

		// Set the table
		queryBuilder.setTables(PushMessageTable.TABLE_PUSHMESSAGE);

		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case PUSHES:
			break;
		case PUSH_ID:
			// adding the ID to the original query
			queryBuilder.appendWhere(PushMessageTable.COLUMN_ID + "="
					+ uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		SQLiteDatabase db = database.getWritableDatabase();
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		// make sure that potential listeners are getting notified
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		long id = 0;
		switch (uriType) {
		case PUSHES:
			id = sqlDB.insert(PushMessageTable.TABLE_PUSHMESSAGE, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(BASE_PATH + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case PUSHES:
			rowsDeleted = sqlDB.delete(PushMessageTable.TABLE_PUSHMESSAGE,
					selection, selectionArgs);
			break;
		case PUSH_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = sqlDB.delete(PushMessageTable.TABLE_PUSHMESSAGE,
						PushMessageTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsDeleted = sqlDB.delete(PushMessageTable.TABLE_PUSHMESSAGE,
						PushMessageTable.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		int uriType = sURIMatcher.match(uri);
		SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriType) {
		case PUSHES:
			rowsUpdated = sqlDB.update(PushMessageTable.TABLE_PUSHMESSAGE,
					values, selection, selectionArgs);
			break;
		case PUSH_ID:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsUpdated = sqlDB.update(PushMessageTable.TABLE_PUSHMESSAGE,
						values, PushMessageTable.COLUMN_ID + "=" + id, null);
			} else {
				rowsUpdated = sqlDB.update(PushMessageTable.TABLE_PUSHMESSAGE,
						values, PushMessageTable.COLUMN_ID + "=" + id + " and "
								+ selection, selectionArgs);
			}
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	private void checkColumns(String[] projection) {
		String[] available = { PushMessageTable.COLUMN_BODY,
				PushMessageTable.COLUMN_CHANNEL_NAME,
				PushMessageTable.COLUMN_CHANNEL_ICON_IMAGE,
				PushMessageTable.COLUMN_ID,
				PushMessageTable.COLUMN_DATE_CREATED,
				PushMessageTable.COLUMN_DATE_EXPIRE,
				PushMessageTable.COLUMN_SYNC_READ,
				PushMessageTable.COLUMN_PREVIEW_URL,
				PushMessageTable.COLUMN_DELETED
				};
		
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(
					Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(
					Arrays.asList(available));
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException(
						"Unknown columns in projection");
			}
		}
	}
}
