package com.gumino.pushetta.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.gumino.core.database.PushMessageTable;
import com.gumino.core.jobs.SyncReadJob;
import com.gumino.pushetta.PushettaApplication;
import com.gumino.pushetta.R;
import com.gumino.pushetta.core.PushettaConsts;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PushCursorAdapter extends SimpleCursorAdapter {

	public PushCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, layout, c, from, to, flags);	
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);

		Cursor cursor = getCursor();
		cursor.moveToPosition(position);

		// Icona del canale
		ImageView iv = (ImageView) row.findViewById(R.id.iconChannel);
		String url = cursor.getString(cursor
				.getColumnIndex(PushMessageTable.COLUMN_CHANNEL_ICON_IMAGE));
		if (url.length() > 0) {
			ImageLoader.getInstance().displayImage(url, iv); 
		}
		
		// Eventuale preview_url
		ImageView previewImg = (ImageView) row.findViewById(R.id.imgPreviewUrl);
		url = cursor.getString(cursor
				.getColumnIndex(PushMessageTable.COLUMN_PREVIEW_URL));
		if (url != null && url.length() > 0) {
			ImageLoader.getInstance().displayImage(url, previewImg); 
		}

		// Linkfy trasforma i link in cliccabili
		TextView txtMessage = (TextView) row.findViewById(R.id.textMessageBody);
		Linkify.addLinks(txtMessage, Linkify.ALL);

		int syncreadColIndex = cursor
				.getColumnIndex(PushMessageTable.COLUMN_SYNC_READ);
		if (cursor.getInt(syncreadColIndex) == 0) {
			// SYNCO
			// MARCO l'operazione
			Log.d(PushettaConsts.TAG, "Row da syncare");
			int messageId = cursor.getInt(cursor
					.getColumnIndex(PushMessageTable.COLUMN_ID));

			String channelName = cursor.getString(cursor
					.getColumnIndex(PushMessageTable.COLUMN_CHANNEL_NAME));

			SyncReadJob syncJob = new SyncReadJob(channelName, messageId);
			PushettaApplication.getInstance().getJobManager()
					.addJobInBackground(syncJob);
		}

		return row;
	}
	

}
