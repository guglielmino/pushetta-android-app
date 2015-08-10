package com.gumino.pushetta.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.gumino.pushetta.PushettaApplication;
import com.gumino.pushetta.R;
import com.gumino.pushetta.core.dto.Channel;
import com.gumino.pushetta.core.dto.ChannelSubscribeRequest;
import com.gumino.pushetta.core.dto.CheckVersion;
import com.gumino.pushetta.core.dto.PagedResult;
import com.gumino.pushetta.core.dto.PushMessage;
import com.gumino.pushetta.core.enums.ChannelSubscribeResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

/**
 * Created by fabrizio on 02/04/14.
 */
public class PushettaServiceClient implements IPushettaServiceClient {
	private static Context ctx = PushettaApplication.getInstance()
			.getApplicationContext();
	private static AsyncHttpClient client = null;

	public PushettaServiceClient() {
		client = new AsyncHttpClient();
		client.setUserAgent(String.format("Android Pushetta/%1$s (%2$s)",
				Helpers.getAppVersion(ctx), Helpers.getDeviceName()));
	}

	/**
	 * Costruisce il json parser Nota: attenzione al formato date, quello della
	 * string � corretto se il server non usa le TimeZone (e ci si aspetta tutto
	 * in UTC)
	 * 
	 * @return
	 */
	private Gson getGson() {
		// return new
		// GsonBuilder().setDateFormat(PushettaConsts.JSON_DATE_FORMAT)
		// .create();

		return new GsonBuilder().registerTypeAdapter(Date.class,
				new DateSerizlier()).create();
	}

	
	
	/**
	 * Custom date deserializer per convertire la data da UTC al localtimezone
	 * (contestualmente alla deserializzazione dal json)
	 * 
	 * @author fabrizio
	 * 
	 */
	private class DateSerizlier implements JsonDeserializer<Date> {

		@Override
		public Date deserialize(JsonElement jsonElement, Type typeOF,
				JsonDeserializationContext context) throws JsonParseException {

			Locale current = PushettaApplication.getInstance().getResources()
					.getConfiguration().locale;

			try {
				// TODO: Trovare un modo pi� elegante per convertire la data da
				// UTC a local timezone
				Calendar cal = Calendar.getInstance();
				TimeZone tz = cal.getTimeZone();
				SimpleDateFormat destFormat = new SimpleDateFormat(
						PushettaConsts.JSON_DATE_FORMAT, current);
				destFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date utcDate = destFormat.parse(jsonElement.getAsString());
				destFormat.setTimeZone(tz);
				return destFormat.parse(destFormat.format(utcDate));
			} catch (java.text.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			throw new JsonParseException("Unparseable date: "
					+ jsonElement.getAsString());
		}

	}

	@Override
	/**
	 * Verifica se la versione attuale dell'App richiede un aggiornamento
	 */
	public void checkVersion(final PushettaClientResponseHandler<CheckVersion> responseHandler){
		RequestParams params = new RequestParams();
		String url = PushettaConsts.ServiceUrls.CheckVersionUrl;

		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onStart() {
				Log.d(PushettaConsts.TAG, "checkVersion Start");
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				Gson gson = getGson();

				String strResponse = "";
				try {
					strResponse = new String(response, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				CheckVersion pmsg = gson
						.fromJson(strResponse, CheckVersion.class);

				responseHandler.onSuccess(pmsg);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				responseHandler.onError(statusCode,
						"Can't find specified Message");
			}
		});
	}
	
	@Override
	/**
	 * Nota: usa il client sincrono perch� la subscribe del device avviene in un AsyncTask
	 */
	public void subscribeDevice(String deviceId, String token, String name,
			final PushettaClientResponseHandler<Boolean> responseHandler) {
		StringEntity entity = null;

		JSONObject params = new JSONObject();
		try {

			params.put("sub_type", PushettaConsts.SUBSCRIBE_PLATFORM_NAME);
			params.put("device_id", deviceId);
			params.put("token", token);
			params.put("name", name);
			entity = new StringEntity(params.toString());

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String url = PushettaConsts.ServiceUrls.DeviceSubscribe;

		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json"));
		
		SyncHttpClient syncClient = new SyncHttpClient();
		syncClient.setUserAgent(String.format("Android Pushetta/%1$s (%2$s)",
				Helpers.getAppVersion(ctx), Helpers.getDeviceName()));

		syncClient.post(ctx, url, entity, "application/json", 
				new AsyncHttpResponseHandler() {

					@Override
					public void onStart() {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] response) {
						responseHandler.onSuccess(statusCode >= 200
								&& statusCode < 300);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] errorResponse, Throwable e) {
						responseHandler.onError(statusCode,
								"Can't register this Device, retry later");
					}
				});
	}

	@Override
	public void subscribeChannel(String channelName, String deviceId,
			String token,
			final PushettaClientResponseHandler<ChannelSubscribeResult> responseHandler) {
		StringEntity entity = null;

		JSONObject params = new JSONObject();
		try {

			params.put("sub_type", PushettaConsts.SUBSCRIBE_PLATFORM_NAME);
			params.put("device_id", deviceId);
			params.put("token", token);
			entity = new StringEntity(params.toString());

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String url = String
				.format(PushettaConsts.ServiceUrls.ChannelSubscriptionsUrl,
						Uri.encode(channelName));		
		
		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json"));
		client.post(ctx, url, entity, "application/json",
				new AsyncHttpResponseHandler() {

					@Override
					public void onStart() {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] response) {
						//Boolean res = statusCode >= 200 && statusCode < 300;
						ChannelSubscribeResult res = ChannelSubscribeResult.Error;
						if(statusCode == 201){
							res = ChannelSubscribeResult.Success;
						}
						else if(statusCode == 202){
							res = ChannelSubscribeResult.RequestSent;
						}
						
						responseHandler.onSuccess(res);
						
						if (res == ChannelSubscribeResult.Success || res == ChannelSubscribeResult.RequestSent) {
							Intent intent = new Intent(
									PushettaConsts.INTENT_FILTER_REFRESH_LIST);
							LocalBroadcastManager.getInstance(ctx)
									.sendBroadcast(intent);
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] errorResponse, Throwable e) {
						if (statusCode == org.apache.http.HttpStatus.SC_NOT_FOUND)
							responseHandler
									.onError(
											statusCode,
											PushettaApplication
													.getInstance()
													.getResources()
													.getString(
															R.string.error_channel_does_not_exist));
						else
							responseHandler
									.onError(
											statusCode,
											PushettaApplication
													.getInstance()
													.getResources()
													.getString(
															R.string.error_cant_subscribe_channel));
					}
				});
	}

	public void unsubscribeChannel(String channelName, String deviceId,
			final PushettaClientResponseHandler<Boolean> responseHandler) {

		String url = String.format(
				PushettaConsts.ServiceUrls.ChannelUnSubscriptionsUrl,
				Uri.encode(channelName), PushettaConsts.SUBSCRIBE_PLATFORM_NAME, deviceId);

		client.delete(ctx, url, new AsyncHttpResponseHandler() {

			@Override
			public void onStart() {

			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				responseHandler
						.onSuccess(statusCode >= 200 && statusCode < 300);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				responseHandler.onError(statusCode,
						"Can't unsubscribe the Channel, retry later");
			}
		});
	}

	/**
	 * Fake implementation to protptype the app
	 */
	public void getMyMessages(String deviceId, String token,
			PushettaClientResponseHandler<List<PushMessage>> responseHandler) {
		PushMessage not1 = new PushMessage();
		not1.setBody("A recording by Harry James with vocal by Kitty Kallen reached No. 1 on the Billboard Hot 100 chart on November 24, 1945.[1] An alternate version by Bing Crosby accompanied by The Les Paul Trio was also working its way up the charts. It replaced the James' version at No. 1 on December 8, 1945.[2] Crosby's lasted a week at No. 1, ousted by Sammy Kaye's Chickery Chick. The Harry James recording then returned to the top spot on December 22 for another week.");

		PushMessage not2 = new PushMessage();
		not2.setBody("Crosby's version features some memorable guitar by Les Paul, who recalled in an interview printed in Mojo magazine");

		PushMessage not3 = new PushMessage();
		not3.setBody("A very good message received from Pushetta");

		List<PushMessage> result = Arrays.asList(not1, not2, not3);

		responseHandler.onSuccess(result);
	}

	@Override
	public void getPushMessage(Integer msgId,
			final PushettaClientResponseHandler<PushMessage> responseHandler) {
		RequestParams params = new RequestParams();
		String url = String.format(PushettaConsts.ServiceUrls.MessagesByIdUrl,
				msgId);

		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onStart() {
				Log.d(PushettaConsts.TAG, "getPushMessage Start");
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				Gson gson = getGson();

				String strResponse = "";
				try {
					strResponse = new String(response, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				PushMessage pmsg = gson
						.fromJson(strResponse, PushMessage.class);

				responseHandler.onSuccess(pmsg);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				responseHandler.onError(statusCode,
						"Can't find specified Message");
			}
		});
	}

	@Override
	public PushMessage getPushMessageSync(Integer msgId) {
		PushMessage ret = null;

		String url = String.format(PushettaConsts.ServiceUrls.MessagesByIdUrl,
				msgId);
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpGet httpget = new HttpGet(url);

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status
			Log.i("Praeda", response.getStatusLine().toString());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				Gson gson = getGson();
				ret = gson.fromJson(result, PushMessage.class);

				DateFormat df = new SimpleDateFormat(
						PushettaConsts.SERVICE_DATE_FORMAT);
				Log.d(PushettaConsts.TAG, df.format(ret.getDate_created()));
				instream.close();
			}

		} catch (Exception e) {

			Log.e(PushettaConsts.TAG, e.getMessage());
		}

		return ret;
	}

	@Override
	public void getSubscribedChannels(String deviceId,
			final PushettaClientResponseHandler<List<Channel>> responseHandler) {
		RequestParams params = new RequestParams();

		client.get(String.format(PushettaConsts.ServiceUrls.SubscriptionsUrl,
				deviceId), params, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {

			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				Gson gson = getGson();
				List<Channel> result = new ArrayList<Channel>();

				String strResponse = "";
				try {
					strResponse = new String(response, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (strResponse != null && !strResponse.isEmpty()) {
					Type respType = new TypeToken<List<Channel>>() {
					}.getType();
					result = gson.fromJson(strResponse, respType);
				}

				responseHandler.onSuccess(result);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				responseHandler.onError(
						statusCode,
						PushettaApplication.getInstance().getString(
								R.string.error_cant_find_any_channel));
			}
		});
	}

	@Override
	public void searchPublicChannel(String query,
			final PushettaClientResponseHandler<List<Channel>> responseHandler) {
		RequestParams params = new RequestParams();

		params.add("q", query);

		client.get(PushettaConsts.ServiceUrls.ChannelsSearchUrl, params,
				new AsyncHttpResponseHandler() {

					@Override
					public void onStart() {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] response) {
						PagedResult<Channel> result = new PagedResult<Channel>();
						result.setResults(new Channel[0]);

						Gson gson = getGson();
						String strResponse = "";
						try {
							strResponse = new String(response, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (strResponse != null && !strResponse.isEmpty()) {
							Type respType = new TypeToken<PagedResult<Channel>>() {
							}.getType();
							result = gson.fromJson(strResponse, respType);
						}
						responseHandler.onSuccess(new ArrayList<Channel>(Arrays
								.asList(result.getResults())));
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] errorResponse, Throwable e) {
						responseHandler.onError(
								statusCode,
								PushettaApplication.getInstance().getString(
										R.string.error_cant_find_any_channel));
					}
				});
	}

	@Override
	public void messageReadFeedback( String deviceId, Integer[] msgIds) {
		StringEntity entity = null;

		JSONObject params = new JSONObject();
		try {

			params.put("device_id", deviceId);
			JSONArray jsonMsgIds = new JSONArray();
			for(Integer msgId:msgIds){
				jsonMsgIds.put(msgId);
			}
			params.put("messages_id", jsonMsgIds);
			entity = new StringEntity(params.toString());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		String url = PushettaConsts.ServiceUrls.ReadFeedbackUrl;

		entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json"));
		client.post(ctx, url, entity, "application/json",
				new AsyncHttpResponseHandler() {

					@Override
					public void onStart() {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] response) {
						Log.d(PushettaConsts.TAG, "Feedback service OK");
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] errorResponse, Throwable e) {
						Log.d(PushettaConsts.TAG, "Feedback service Error");
					}
				});

	}

	@Override
	public void getSuggestions(String deviceId,
			final PushettaClientResponseHandler<List<Channel>> responseHandler) {
		RequestParams params = new RequestParams();

		String url = String.format(PushettaConsts.ServiceUrls.ChannelsSuggestionsUrl, deviceId);
		client.get(url, params,
				new AsyncHttpResponseHandler() {

					@Override
					public void onStart() {

					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] response) {
						Gson gson = getGson();
						List<Channel> result = new ArrayList<Channel>();

						String strResponse = "";
						try {
							strResponse = new String(response, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (strResponse != null && !strResponse.isEmpty()) {
							Type respType = new TypeToken<List<Channel>>() {
							}.getType();
							result = gson.fromJson(strResponse, respType);
						}

						responseHandler.onSuccess(result);
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] errorResponse, Throwable e) {
						responseHandler.onError(
								statusCode,
								PushettaApplication.getInstance().getString(
										R.string.error_cant_find_any_channel));
					}
				});
	}
	
	@Override
	public void getChannelsRequests(String deviceId,
			final PushettaClientResponseHandler<List<ChannelSubscribeRequest>> responseHandler){
		RequestParams params = new RequestParams();

		client.get(String.format(PushettaConsts.ServiceUrls.RequestsUrl,
				deviceId), params, new AsyncHttpResponseHandler() {
			@Override
			public void onStart() {

			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					byte[] response) {
				Gson gson = getGson();
			
				
				List<ChannelSubscribeRequest> result = new ArrayList<ChannelSubscribeRequest>();

				String strResponse = "";
				try {
					strResponse = new String(response, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (strResponse != null && !strResponse.isEmpty()) {
					Type respType = new TypeToken<List<ChannelSubscribeRequest>>() {
					}.getType();
					result = gson.fromJson(strResponse, respType);
				}

				responseHandler.onSuccess(result);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] errorResponse, Throwable e) {
				responseHandler.onError(
						statusCode,
						PushettaApplication.getInstance().getString(
								R.string.error_cant_find_any_channel));
			}
		});
	}

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
