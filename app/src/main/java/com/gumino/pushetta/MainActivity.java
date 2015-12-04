package com.gumino.pushetta;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.Fragment;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.content.LocalBroadcastManager;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


import com.daimajia.swipe.SwipeLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.gumino.pushetta.adapters.DrawerMenuAdapter;
import com.gumino.pushetta.core.DomainLayer;
import com.gumino.pushetta.core.Helpers;
import com.gumino.pushetta.core.IPushettaServiceClient;
import com.gumino.pushetta.core.PushettaClientFactory;
import com.gumino.pushetta.core.PushettaClientResponseHandler;
import com.gumino.pushetta.core.PushettaConfigs;
import com.gumino.pushetta.core.PushettaConsts;
import com.gumino.pushetta.core.dto.CheckVersion;
import com.gumino.pushetta.core.enums.ChannelSubscribeResult;
import com.gumino.pushetta.util.UIHelpers;

public class MainActivity extends Activity  implements DrawerMenuAdapter.OnItemClickListener{

	private String regid;
	private Context context;
	private GoogleCloudMessaging gcm;
	private BroadcastReceiver resultReceiver;


	// Gestione del Drawer
    private DrawerLayout mDrawerLayout;
	private RecyclerView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerMenuItems;

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	List<Fragment> fragments = Arrays.asList(new FragmentDiscover(),
			new FragmentPushes(), new FragmentSubscriptions());

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
     	setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mDrawerMenuItems = getResources().getStringArray(R.array.drawer_menu_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (RecyclerView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
		mDrawerList.setHasFixedSize(true);
		mDrawerList.setLayoutManager(new LinearLayoutManager(this));

        mDrawerList.setAdapter(new DrawerMenuAdapter(mDrawerMenuItems, this));

		// enable ActionBar app icon to behave as action to toggle nav drawer

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

		mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(1);
        }


        context = getApplicationContext();

		// Gestione apertura da URL con lettura dei parametri
		Uri data = getIntent().getData();
		if (data != null) {
			String channel_name = data.getQueryParameter("channel");
			if (channel_name != null && !channel_name.isEmpty()) {
				subscribeChannel(channel_name);
			}

		}

		// Local broadcast receiver per la gestione degli AlertDialog da
		// messaggi di background
		resultReceiver = createBroadcastReceiver();
		LocalBroadcastManager.getInstance(this).registerReceiver(
				resultReceiver,
				new IntentFilter(PushettaConsts.INTENT_FILTER_ALERTDIALOG));

		// Check device for Play Services APK.
		if (checkPlayServices()) {
			// regid = getRegistrationId(context);

			// if (regid.isEmpty()) {
			registerInBackground();
			// }
		} else {
			UIHelpers.showErrorMessage(this,
					getString(R.string.error_need_pushes));

		}

		// Check della versione per eventuale update
		IPushettaServiceClient client = PushettaClientFactory.getClient();
		client.checkVersion(new PushettaClientResponseHandler<CheckVersion>() {

                                @Override
                                public void onSuccess(CheckVersion result) {
                                    if (result.getNeed_update()) {
                                        UIHelpers.showInfoMessage(MainActivity.this,
                                                result.getMessage(), new OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        finish();
                                                    }
                                                });
                                    } else if (!result.getMessage().isEmpty()) {
                                        UIHelpers.showErrorMessage(MainActivity.this,
                                                result.getMessage());
                                    }

                                }

                                @Override
                                public void onError(int httpCode, String message) {
                                    // TODO Auto-generated method stub

                                }

                            }

        );
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	@Override
	public void onClick(View view, int position) {
        selectItem(position);
	}


	/* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = this.fragments.get(position);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();

        // update selected item title, then close the drawer
        setTitle(mDrawerMenuItems[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);

		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.action_search)
				.getActionView();

		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(new ComponentName(this,
						ChannelSearchActivity.class)));
		searchView.setIconifiedByDefault(false);
		searchView.setSubmitButtonEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}



	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// Flush degli eventuali messaggi pending da marcare come letti server
		// side
		PushettaApplication.getInstance().sendReadMessagesFeedback();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current tab position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkPlayServices();
	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {

		case R.id.action_add_custom:
			AskCustomChannelSubscribe();
			return  true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void AskCustomChannelSubscribe() {
		final EditText txtChannelName = new EditText(this);

		// Set the default text to a link of the Queen
		txtChannelName.setHint(getResources().getString(
				R.string.channel_input_hint));

		new AlertDialog.Builder(this)
				.setTitle(
						getResources().getString(R.string.channel_input_title))

				.setMessage(
						getResources()
								.getString(R.string.channel_input_message))

				.setView(txtChannelName)
				.setPositiveButton(R.string.channel_input_confirm,

				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						final String channelName = txtChannelName.getText()
								.toString();
						IPushettaServiceClient client = PushettaClientFactory
								.getClient();
						client.subscribeChannel(
								channelName,
								Helpers.getUniquePsuedoID(),
								PushettaConfigs.getRegId(),
								new PushettaClientResponseHandler<ChannelSubscribeResult>() {

									@Override
									public void onSuccess(
											ChannelSubscribeResult result) {
										String message = "";
										switch (result) {
										case Success:
											message = String
													.format(getResources()
															.getString(
																	R.string.gen_channel_subscribed),
															channelName);
											break;
										case RequestSent:
											message = String
													.format(getResources()
															.getString(
																	R.string.gen_channel_request_sent),
															channelName);
										default:
											message = getResources()
													.getString(
															R.string.error_cant_subscribe_channel);
											break;
										}
										UIHelpers.showInfoMessage(
												MainActivity.this, message,
												new OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														;

													}
												});
									}

									@Override
									public void onError(int httpCode,
											String message) {
										UIHelpers.showErrorMessage(
												MainActivity.this, message);
									}
								});

					}

				})

				.setNegativeButton(R.string.channel_input_cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int whichButton) {

							}

						})

				.show();
	}

	private void runSearchActivity() {
		Intent intent = new Intent(this, ChannelSearchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(final Context context) {

		String registrationId = PushettaConfigs.getRegId();
		if (registrationId.isEmpty()) {
			Log.i(PushettaConsts.TAG, "Registration not found.");
			return "";
		} else {
			Log.i(PushettaConsts.TAG, "Registration found " + registrationId);
		}

		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = PushettaConfigs.getAppVersion();
		int currentVersion = Helpers.getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.i(PushettaConsts.TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
		new AsyncTaskExtension().execute(null, null, null);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use
	 * GCM/HTTP or CCS to send messages to your app. Not needed for this demo
	 * since the device sends upstream messages to a server that echoes back the
	 * message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {

		IPushettaServiceClient client = PushettaClientFactory.getClient();
		client.subscribeDevice(Helpers.getUniquePsuedoID(),
				PushettaConfigs.getRegId(), Helpers.getDeviceName(),
				new PushettaClientResponseHandler<Boolean>() {

					@Override
					public void onSuccess(Boolean result) {
						// Risultato silente

					}

					@Override
					public void onError(int httpCode, String message) {
						UIHelpers.showErrorMessage(context, message);

					}
				});

	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
		int appVersion = Helpers.getAppVersion(context);

		PushettaConfigs.setRegId(regId);
		PushettaConfigs.setAppVersion(appVersion);
	}

	/**
	 * Check if Google Cloud Messaging is avaiable for the device
	 * 
	 * @return
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PushettaConsts.PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(PushettaConsts.TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Inner class for async registering of GCM token
	 * 
	 * @author fabrizio
	 * 
	 */
	private final class AsyncTaskExtension extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... params) {
			try {
				if (gcm == null) {
					gcm = GoogleCloudMessaging.getInstance(context);
				}

				regid = gcm.register(PushettaConsts.GOOGLE_SENDER_ID);

				// Persist the regID - no need to register again.
				storeRegistrationId(context, regid);

				sendRegistrationIdToBackend();
			} catch (IOException ex) {
			}

			return null;
		}

	}

	/**
	 * Unsubscribe button in subscriptions fragment
	 * 
	 * @param view
	 */
	public void onClickUnSubscribe(View view) {
		Log.d(PushettaConsts.TAG, "onClickUnSubscribe");

		TextView textChannelName = (TextView) ((View) view.getParent())
				.findViewById(R.id.textChannelName);

		IPushettaServiceClient client = PushettaClientFactory.getClient();
		client.unsubscribeChannel(textChannelName.getText().toString(),
				Helpers.getUniquePsuedoID(),
				new PushettaClientResponseHandler<Boolean>() {

					@Override
					public void onSuccess(Boolean result) {
						// Refresh list of subscriptions
						if (!fragments.isEmpty()) {
							for (Fragment fragment : fragments) {
								if (fragment instanceof FragmentSubscriptions) {
									((FragmentSubscriptions) fragment)
											.getSubscriptions();
									break;

								}
							}
						}
					}

					@Override
					public void onError(int httpCode, String message) {
						UIHelpers.showErrorMessage(
								getApplicationContext(),
								getResources().getString(
										R.string.error_getting_subscriptions));
					}
				});
	}

	public void showProgress() {

		setProgressBarIndeterminateVisibility(true);
	}

	public void hideProgress() {
		setProgressBarIndeterminateVisibility(false);
	}

	/**
	 * Receiver per l'intent broadcast (locale) usato per i messaggi provenienti
	 * da thread non UI
	 */

	private BroadcastReceiver createBroadcastReceiver() {
		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String message = intent.getStringExtra("message");

				AlertDialog.Builder errorAlert = new AlertDialog.Builder(
						MainActivity.this);
				errorAlert.setTitle("Pushetta");
				errorAlert.setMessage(message);

				AlertDialog alert = errorAlert.create();
				alert.show();
			}

		};
	}

	/**
	 * Gestione del subscribe al canale dall view delle suggestion
	 * 
	 * @param view
	 */
	public void onClickSubscribe(View view) {

		TextView textChannelName = (TextView) ((View) view.getParent())
				.findViewById(R.id.textChannelName);

		subscribeChannel(textChannelName.getText().toString());
	}

	private void subscribeChannel(final String channel_name) {	
		DomainLayer.getInstance().SubscribeChannel(MainActivity.this, channel_name);
	}

}
