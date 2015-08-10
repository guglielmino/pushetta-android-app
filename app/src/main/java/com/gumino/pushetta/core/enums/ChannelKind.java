package com.gumino.pushetta.core.enums;

import com.google.gson.annotations.SerializedName;

public enum ChannelKind {
	@SerializedName("0")
	Private(0),
	@SerializedName("1")
	Public(1);
	
	private final int value;

	public int getValue() {
		return value;
	}

	private ChannelKind(int value) {
		this.value = value;
	}
}
