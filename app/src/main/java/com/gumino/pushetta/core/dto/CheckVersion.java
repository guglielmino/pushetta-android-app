package com.gumino.pushetta.core.dto;

public class CheckVersion {
	private String message;
	private Boolean need_update;

	public Boolean getNeed_update() {
		return need_update;
	}

	public void setNeed_update(Boolean need_update) {
		this.need_update = need_update;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
