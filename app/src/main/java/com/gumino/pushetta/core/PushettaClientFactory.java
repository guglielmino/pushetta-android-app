package com.gumino.pushetta.core;

public class PushettaClientFactory {

	public static IPushettaServiceClient getClient() {
		return new PushettaServiceClient();

	}

}