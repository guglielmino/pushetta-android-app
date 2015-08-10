package com.gumino.pushetta;

import java.util.List;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.gumino.pushetta.adapters.ChannelListAdapter;
import com.gumino.pushetta.core.DomainLayer;
import com.gumino.pushetta.core.IPushettaServiceClient;
import com.gumino.pushetta.core.PushettaClientFactory;
import com.gumino.pushetta.core.PushettaClientResponseHandler;
import com.gumino.pushetta.core.dto.Channel;

public class ChannelSearchActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channel_search);
		setTitle("Channel search");
		// Get the intent, verify the action and get the query
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			executeSearch(query);
		}
	}

	private void executeSearch(String query) {
		IPushettaServiceClient client = PushettaClientFactory.getClient();

		client.searchPublicChannel(query,
				new PushettaClientResponseHandler<List<Channel>>() {

					@Override
					public void onSuccess(List<Channel> result) {
						ChannelListAdapter adapter = new ChannelListAdapter(
								ChannelSearchActivity.this, // Context.
								result);

						// Bind to our new adapter.
						setListAdapter(adapter);

					}

					@Override
					public void onError(int httpCode, String message) {
						// TODO Auto-generated method stub

					}
				});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onClickSubscribe(View view) {
		TextView textChannelName = (TextView) ((View) view.getParent())
				.findViewById(R.id.textChannelName);

		
		final String channel_name = textChannelName.getText().toString();
		DomainLayer.getInstance().SubscribeChannel(ChannelSearchActivity.this, channel_name);
	}

}
