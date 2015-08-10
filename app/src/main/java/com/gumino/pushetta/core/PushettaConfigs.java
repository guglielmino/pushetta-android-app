package com.gumino.pushetta.core;

import android.content.Context;
import android.content.SharedPreferences;

import com.gumino.pushetta.PushettaApplication;

public class PushettaConfigs {
	private static final String PREF_NAME = "PhuscettaPrefs";

	private static SharedPreferences getSharedInstance() {
		final SharedPreferences prefs = PushettaApplication.getInstance()
				.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return prefs;
	}

	public static void setRegId(String regId) {
		SharedPreferences prefs = getSharedInstance();
		SharedPreferences.Editor editor = prefs.edit();

		editor.putString(PushettaConsts.PROPERTY_REG_ID, regId);
		editor.commit();
	}

	public static String getRegId() {
		return getSharedInstance()
				.getString(PushettaConsts.PROPERTY_REG_ID, "");
	}

	public static void setAppVersion(int appVersion) {
		SharedPreferences prefs = getSharedInstance();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(PushettaConsts.PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public static int getAppVersion() {
		return getSharedInstance().getInt(PushettaConsts.PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
	}

	public static void setOtt(String ott) {
		SharedPreferences prefs = getSharedInstance();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PushettaConsts.PROPERTY_OTT, ott);
		editor.commit();
	}

	public static String getOtt() {
		return getSharedInstance().getString(PushettaConsts.PROPERTY_OTT, "");
	}

}
