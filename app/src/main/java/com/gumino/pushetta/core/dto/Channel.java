package com.gumino.pushetta.core.dto;

import com.gumino.pushetta.core.enums.ChannelKind;

public class Channel {
	private String name;

	private String image;

	private String description;

	private ChannelKind kind;

	private Boolean hidden;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ChannelKind getKind() {
		return kind;
	}

	public void setKind(ChannelKind kind) {
		this.kind = kind;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
}
