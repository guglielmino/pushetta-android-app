package com.gumino.pushetta;

import java.util.Date;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.ImageView;
import android.widget.TextView;

import com.gumino.core.contentprovider.PushMessageContentProvider;
import com.gumino.core.database.PushMessageTable;
import com.gumino.pushetta.adapters.PushCursorAdapter;
import com.gumino.pushetta.util.UIHelpers;

public class FragmentPushes extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	Context context;

	// private Cursor cursor;
	private PushCursorAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		fillData();

		View rootView = inflater.inflate(
				com.gumino.pushetta.R.layout.fragment_pushes, container, false);
		return rootView;
	}

	private void fillData() {
		((MainActivity) getActivity()).showProgress();

		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { PushMessageTable.COLUMN_CHANNEL_NAME,
				PushMessageTable.COLUMN_CHANNEL_ICON_IMAGE,
				PushMessageTable.COLUMN_BODY,
				PushMessageTable.COLUMN_DATE_EXPIRE,
				PushMessageTable.COLUMN_PREVIEW_URL};
		// Fields on the UI to which we map
		int[] to = new int[] { 
				R.id.textChannelName, 
				R.id.iconChannel,
				R.id.textMessageBody, 
				R.id.txtTimeStamp, 
				R.id.imgPreviewUrl };

		getLoaderManager().initLoader(0, null, this);

		adapter = new PushCursorAdapter(this.getActivity(),
				com.gumino.pushetta.R.layout.listitem_pushes, null, from, to, 0);
		
		// ViewBinder per gestire in modo custom la visualizzazione della data
		// e dell'anteprima dell'url se presente
		adapter.setViewBinder(new ViewBinder() {

		    public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {

		        if (aColumnIndex == 5) {
		        		String createDate = aCursor.getString(4);
		        		Date createdOn = UIHelpers.getDateFromUIString(createDate);
		        		
		                String expireDate = aCursor.getString(aColumnIndex);
		                Date expireOn = UIHelpers.getDateFromUIString(expireDate);
		                
		                TextView textView = (TextView) aView;
		                String timeStamp = String.format("Sent %s / expire %s",
		                		DateFormat.getMediumDateFormat(getActivity()).format(createdOn),
		                		DateFormat.getMediumDateFormat(getActivity()).format(expireOn));
		                
		                textView.setText(timeStamp);
		                return true;
		         }
		        

		         return false;
		    }
		});
		

		setListAdapter(adapter);

		((MainActivity) getActivity()).hideProgress();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { PushMessageTable.COLUMN_ID,
				PushMessageTable.COLUMN_BODY,
				PushMessageTable.COLUMN_CHANNEL_NAME,
				PushMessageTable.COLUMN_CHANNEL_ICON_IMAGE,
				PushMessageTable.COLUMN_DATE_CREATED,
				PushMessageTable.COLUMN_DATE_EXPIRE,
				PushMessageTable.COLUMN_SYNC_READ,
				PushMessageTable.COLUMN_PREVIEW_URL };

		CursorLoader cursorLoader = new CursorLoader(this.getActivity(),
				PushMessageContentProvider.CONTENT_URI, projection,
				PushMessageTable.COLUMN_DATE_EXPIRE
						+ " > datetime()", // solo push non scaduti
				null, PushMessageTable.COLUMN_DATE_CREATED + " DESC");
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// data is not available anymore, delete reference
		adapter.swapCursor(null);

	}
}
