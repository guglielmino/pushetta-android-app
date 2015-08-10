package com.gumino.pushetta.core;

/**
 * Created by fabrizio on 02/04/14.
 */
public interface PushettaConsts {
	public static final String GOOGLE_SENDER_ID = "365056580234";

	public static final String TAG = "PUSHETTA_DGB";

	public static final String EXTRA_MESSAGE = "message";

	public static final String PROPERTY_REG_ID = "registration_id";
	public static final String PROPERTY_APP_VERSION = "appVersion";
	public static final String PROPERTY_OTT = "onetimetoken";

	public static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	public static final String PUSHMESSAGE_TYPE_PLAIN = "plain_push";

	public static final String SUBSCRIBE_PLATFORM_NAME = "android";

	// Formato delle date storate nel db (dato che in SQL lite sono stringhe)
	public static final String DATABASE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	// Formato delle date che transitato da e verso il WebService
	public static final String SERVICE_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
	public static final String JSON_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

	public static final String INTENT_FILTER_ALERTDIALOG = "com.gumino.pushetta.alertintent";
	public static final String INTENT_FILTER_REFRESH_LIST = "com.gumino.pushetta.refresh_list";

	/**
	 * Service url constants
	 * 
	 * @author fabrizio
	 * 
	 */
	public interface ServiceUrls {
		static final String BASE_URL = "http://api.pushetta.com/api/";

		public static final String DeviceSubscribe = BASE_URL + "subscribers/";
		public static final String ChannelsSearchUrl = BASE_URL
				+ "channels/search/";
		public static final String ChannelsSuggestionsUrl = BASE_URL
				+ "channels/suggestions/%1$s/";

		public static final String ChannelSubscriptionsUrl = BASE_URL
				+ "channels/subscription/%1$s/";
		public static final String ChannelUnSubscriptionsUrl = BASE_URL
				+ "channels/subscription/%1$s/%2$s/%3$s/";

		public static final String SubscriptionsUrl = BASE_URL
				+ "subscriptions/%1$s/";

		public static final String MessagesByIdUrl = BASE_URL
				+ "messages/%1$s/";

		public static final String ReadFeedbackUrl = BASE_URL + "feedback/";

		public static final String AndroidCrashLog = BASE_URL
				+ "android/crashlog/";

		public static final String CheckVersionUrl = BASE_URL + "sys/version/";

		public static final String RequestsUrl = BASE_URL
				+ "subscriptions/requests/%1$s/";
	}
}
