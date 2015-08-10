package com.gumino.pushetta.core.dto;

import com.gumino.pushetta.core.enums.ChannelSubscribeStatus;

public class ChannelSubscribeRequest {
	private Channel channel;

	private ChannelSubscribeStatus status;

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public ChannelSubscribeStatus getStatus() {
		return status;
	}

	public void setStatus(ChannelSubscribeStatus status) {
		this.status = status;
	}
}
