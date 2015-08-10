package com.gumino.pushetta.core;

import java.util.UUID;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;

public final class Helpers {

	/**
	 * Return pseudo unique ID
	 * 
	 * @return ID
	 */
	public static String getUniquePsuedoID() {
		// If all else fails, if the user does have lower than API 9 (lower
		// than Gingerbread), has reset their phone or 'Secure.ANDROID_ID'
		// returns 'null', then simply the ID returned will be solely based
		// off their Android device information. This is where the collisions
		// can happen.
		// Thanks http://www.pocketmagic.net/?p=1662!
		// Try not to use DISPLAY, HOST or ID - these items could change.
		// If there are collisions, there will be overlapping data
		String m_szDevIDShort = "35" + (Build.BOARD.length() % 10)
				+ (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10)
				+ (Build.DEVICE.length() % 10)
				+ (Build.MANUFACTURER.length() % 10)
				+ (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);

		// Thanks to @Roman SL!
		// http://stackoverflow.com/a/4789483/950427
		// Only devices with API >= 9 have android.os.Build.SERIAL
		// http://developer.android.com/reference/android/os/Build.html#SERIAL
		// If a user upgrades software or roots their phone, there will be a
		// duplicate entry
		String serial = null;
		try {
			serial = android.os.Build.class.getField("SERIAL").get(null)
					.toString();

			// Go ahead and return the serial for api => 9
			return new UUID(m_szDevIDShort.hashCode(), serial.hashCode())
					.toString();
		} catch (Exception e) {
			// String needs to be initialized
			serial = "serial"; // some value
		}

		// Thanks @Joe!
		// http://stackoverflow.com/a/2853253/950427
		// Finally, combine the values we have found by using the UUID class to
		// create a unique identifier
		return new UUID(m_szDevIDShort.hashCode(), serial.hashCode())
				.toString();
	}

	public static String getDeviceName() {
		return android.os.Build.MANUFACTURER + android.os.Build.PRODUCT;
	}

	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}
}
