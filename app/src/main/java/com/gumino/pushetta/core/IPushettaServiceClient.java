package com.gumino.pushetta.core;

import java.util.List;

import com.gumino.pushetta.core.dto.Channel;
import com.gumino.pushetta.core.dto.ChannelSubscribeRequest;
import com.gumino.pushetta.core.dto.CheckVersion;
import com.gumino.pushetta.core.dto.PushMessage;
import com.gumino.pushetta.core.enums.ChannelSubscribeResult;

/**
 * Created by fabrizio on 27/03/14.
 */
public interface IPushettaServiceClient {

	public void checkVersion(PushettaClientResponseHandler<CheckVersion> responseHandler);
	
	public void subscribeDevice(String deviceId, String token, String name,
			PushettaClientResponseHandler<Boolean> responseHandler);

	public void subscribeChannel(String channelName, String deviceId,
			String token, PushettaClientResponseHandler<ChannelSubscribeResult> responseHandler);

	public void unsubscribeChannel(String channelName, String deviceId,
			PushettaClientResponseHandler<Boolean> responseHandler);

	public void getMyMessages(String deviceId, String token,
			PushettaClientResponseHandler<List<PushMessage>> responseHandler);

	public void getPushMessage(Integer msgId,
			PushettaClientResponseHandler<PushMessage> responseHandler);

	public void getSubscribedChannels(String deviceId,
			PushettaClientResponseHandler<List<Channel>> responseHandler);

	public void searchPublicChannel(String query,
			PushettaClientResponseHandler<List<Channel>> responseHandler);

	public PushMessage getPushMessageSync(Integer msgId);

	public void messageReadFeedback(String deviceId, Integer[] msgId);
	
	public void getSuggestions(String deviceId, PushettaClientResponseHandler<List<Channel>> responseHandler);
	
	public void getChannelsRequests(String deviceId,
			PushettaClientResponseHandler<List<ChannelSubscribeRequest>> responseHandler);
	
}
