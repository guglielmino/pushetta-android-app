package com.gumino.pushetta.core;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.gumino.pushetta.MainActivity;
import com.gumino.pushetta.R;
import com.gumino.pushetta.core.enums.ChannelSubscribeResult;
import com.gumino.pushetta.util.UIHelpers;

/**
 * Domain layer API
 * 
 * @author fabrizio
 *
 */
public final class DomainLayer {

	private static DomainLayer istanza;

	private DomainLayer() {
	}

	public static DomainLayer getInstance() {
		if (istanza == null) {
			istanza = new DomainLayer();
		}

		return istanza;
	}

	public void SubscribeChannel(final Context context,
			final String channel_name) {
		IPushettaServiceClient client = PushettaClientFactory.getClient();
		client.subscribeChannel(channel_name, Helpers.getUniquePsuedoID(),
				PushettaConfigs.getRegId(),
				new PushettaClientResponseHandler<ChannelSubscribeResult>() {

					@Override
					public void onSuccess(ChannelSubscribeResult result) {
						String message = "";
						switch (result) {
						case Success:
							message = String.format(
									context.getResources().getString(
											R.string.gen_channel_subscribed),
									channel_name);
							break;
						case RequestSent:
							message = String.format(
									context.getResources().getString(
											R.string.gen_channel_request_sent),
									channel_name);
							break;
						default:
							message = context.getResources().getString(
									R.string.error_cant_subscribe_channel);
							break;
						}

						UIHelpers.showInfoMessage(context, message,
								new OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								});
					}

					@Override
					public void onError(int httpCode, String message) {
						UIHelpers.showErrorMessage(context, message);
					}
				});
	}
}
