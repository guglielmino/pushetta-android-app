package com.gumino.pushetta.core.enums;

import com.google.gson.annotations.SerializedName;

public enum ChannelSubscribeStatus {
	@SerializedName("0")
	Penging(0), 
	@SerializedName("1")
	Accepted(1), 
	@SerializedName("2")
	Rejected(2);

	private final int value;

	public int getValue() {
		return value;
	}

	private ChannelSubscribeStatus(int value) {
		this.value = value;
	}
}
