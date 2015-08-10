package com.gumino.pushetta.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gumino.pushetta.R;
import com.gumino.pushetta.core.PushettaConsts;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class UIHelpers {

	public static void showErrorMessage(Context context, String message) {
		// Il messaggio di errore genera un local broadcast event intercettato
		// dall'Application per gestire la isualizzazione di Alert su eventi
		// provenienti da non UI thread
		Intent intent = new Intent(PushettaConsts.INTENT_FILTER_ALERTDIALOG);
		intent.putExtra("message", message);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public static void showInfoMessage(Context context, String message,
			OnClickListener listener) {
		AlertDialog.Builder infoAalert = new AlertDialog.Builder(context);
		infoAalert.setTitle("Pushetta");
		infoAalert.setMessage(message);
		infoAalert.setIcon(R.drawable.ic_launcher);
		infoAalert.setPositiveButton("OK", listener);

		AlertDialog alert = infoAalert.create();
		alert.show();

	}
	
	public static Date getDateFromUIString(String dateString)
	{
		Date ret = new Date();
		DateFormat df =  new SimpleDateFormat(PushettaConsts.DATABASE_DATE_FORMAT);
		
		try {
			ret =  df.parse(dateString);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;	
	}
	
	public static String getUIStringFromDate(Date date)
	{
		DateFormat df =  new SimpleDateFormat(PushettaConsts.DATABASE_DATE_FORMAT);
		return df.format(date);
	}
}
