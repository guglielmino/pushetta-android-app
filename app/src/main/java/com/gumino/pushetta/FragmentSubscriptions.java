package com.gumino.pushetta;

import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.applidium.headerlistview.HeaderListView;
import com.gumino.pushetta.adapters.RequestsAdapter;
import com.gumino.pushetta.core.Helpers;
import com.gumino.pushetta.core.IPushettaServiceClient;
import com.gumino.pushetta.core.PushettaClientFactory;
import com.gumino.pushetta.core.PushettaClientResponseHandler;
import com.gumino.pushetta.core.PushettaConsts;
import com.gumino.pushetta.core.dto.ChannelSubscribeRequest;
import com.gumino.pushetta.util.UIHelpers;

public class FragmentSubscriptions extends Fragment {

	private BroadcastReceiver resultReceiver;
	private HeaderListView listRequests;
	private TextView emptySubscriptions;
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
	}
	
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

		View rootView = inflater.inflate(
				com.gumino.pushetta.R.layout.fragment_subscriptions, container,
				false);

		/*
		 * VERSIONE SENZA XML 
		listRequests = new HeaderListView(container.getContext());
		container.addView(listRequests);
		*/
		
		listRequests = (HeaderListView) rootView.findViewById(R.id.listRequests);
		emptySubscriptions = (TextView) rootView
				.findViewById(R.id.emptySubscriptions);

		getSubscriptions();

		return rootView;
	}

	public void getSubscriptions() {

		Activity main = getActivity();
		if (main instanceof MainActivity) {
			((MainActivity) main).showProgress();
		}

		IPushettaServiceClient client = PushettaClientFactory.getClient();

		client.getChannelsRequests(
				Helpers.getUniquePsuedoID(),
				new PushettaClientResponseHandler<List<ChannelSubscribeRequest>>() {

					@Override
					public void onSuccess(List<ChannelSubscribeRequest> result) {
						Activity main = getActivity();
						if (main != null) {
							RequestsAdapter adapter = new RequestsAdapter(main,
									result);

							listRequests.setAdapter(adapter);

							if (result.size() > 0) {
								emptySubscriptions.setVisibility(View.GONE);
								listRequests.setVisibility(View.VISIBLE);
							} else {
								emptySubscriptions.setVisibility(View.VISIBLE);
								listRequests.setVisibility(View.GONE);
							}

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
						}
						Activity main = getActivity();
						if (main instanceof MainActivity) {
							((MainActivity) main).hideProgress();
						}
					}
				});

	}

	/**
	 * Receiver per l'intent broadcast (locale) usato per il refresh delle
	 * subscriptions
	 */

	private BroadcastReceiver createBroadcastReceiver() {
		return new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				getSubscriptions();
			}

		};
	}
}
