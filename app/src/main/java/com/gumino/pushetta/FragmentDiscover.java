package com.gumino.pushetta;

import java.util.List;

import android.app.Activity;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gumino.pushetta.adapters.ChannelSuggestionsListAdapter;
import com.gumino.pushetta.core.Helpers;
import com.gumino.pushetta.core.IPushettaServiceClient;
import com.gumino.pushetta.core.PushettaClientFactory;
import com.gumino.pushetta.core.PushettaClientResponseHandler;
import com.gumino.pushetta.core.PushettaConsts;
import com.gumino.pushetta.core.dto.Channel;
import com.gumino.pushetta.util.UIHelpers;

public class FragmentDiscover extends ListFragment {

	private BroadcastReceiver resultReceiver;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (resultReceiver == null) {
			resultReceiver = createBroadcastReceiver();
			Context context = PushettaApplication.getInstance()
					.getApplicationContext();
			LocalBroadcastManager.getInstance(context)
					.registerReceiver(
							resultReceiver,
							new IntentFilter(
									PushettaConsts.INTENT_FILTER_REFRESH_LIST));
		}

		getDiscorverList();

		View rootView = inflater.inflate(
				com.gumino.pushetta.R.layout.fragment_discover, container,
				false);
		return rootView;
	}

	public void getDiscorverList() {

		Activity main = getActivity();
		if (main != null && main instanceof MainActivity) {
			((MainActivity) main).showProgress();
		}
		
		IPushettaServiceClient client = PushettaClientFactory.getClient();
		client.getSuggestions(Helpers.getUniquePsuedoID(),
				new PushettaClientResponseHandler<List<Channel>>() {

					@Override
					public void onSuccess(List<Channel> result) {
						Activity main = getActivity();
						if (main != null) {
							ChannelSuggestionsListAdapter adapter = new ChannelSuggestionsListAdapter(
									main, result);
							setListAdapter(adapter);

							if (main instanceof MainActivity) {
								((MainActivity) main).hideProgress();
							}
						}
					}

					@Override
					public void onError(int httpCode, String message) {
						if (getActivity() != null
								&& !getActivity().isFinishing()) {
							Context ctx = PushettaApplication.getInstance()
									.getApplicationContext();
							UIHelpers
									.showErrorMessage(
											ctx,
											ctx.getResources()
													.getString(
															R.string.error_getting_subscriptions));
							((MainActivity) getActivity()).hideProgress();
						}
					}
				});
	}

	/**
	 * Receiver per l'intent broadcast (locale) usato per il refresh della
	 * discover list (a seguito di una subscribe)
	 */

	private BroadcastReceiver createBroadcastReceiver() {
		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				getDiscorverList();
			}

		};
	}

}
