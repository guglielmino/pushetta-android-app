package com.gumino.core.jobs;

import android.content.ContentValues;

import com.gumino.core.contentprovider.PushMessageContentProvider;
import com.gumino.core.database.PushMessageTable;
import com.gumino.pushetta.PushettaApplication;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

public class SyncReadJob extends Job {
	// Soglia oltre la quale viene invocato il servizio di marcatura messaggi letti
	private static final int MESSAGES_THREADSHOLD = 10;
	public static final int PRIORITY = 1;
	private String channelName;
	private int messageId;


	public SyncReadJob(String channelName, int messageId) {
		// This job requires network connectivity,
		// and should be persisted in case the application exits before job is
		// completed.
		super(new Params(PRIORITY).requireNetwork().persist());
		this.messageId = messageId;
		this.channelName = channelName;
	}

	@Override
	public void onAdded() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onCancel() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRun() throws Throwable {
		// Aggiungo l'id messaggi alla lista dei letti
		PushettaApplication app = PushettaApplication.getInstance();
		
		app.readMessagesIds.push(messageId);
		int messagesToSend = app.readMessagesIds.size();
		
		// La chiamata � fire and forget visto che � ad uso statistico
		// e mi segno comunque il messaggio come synced
		ContentValues values = new ContentValues();
		values.put(PushMessageTable.COLUMN_SYNC_READ, 1); 
		
		PushettaApplication
				.getInstance()
				.getContentResolver()
				.update(PushMessageContentProvider
						.GetSingleContentUri(messageId),
						values, null, null);
				
		if(messagesToSend >= MESSAGES_THREADSHOLD){	
			app.sendReadMessagesFeedback();
		}
	}

	@Override
	protected boolean shouldReRunOnThrowable(Throwable arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
