package com.gumino.pushetta.core;

public interface PushettaClientResponseHandler<T> {
	void onSuccess(T result);

	void onError(int httpCode, String message);
}
