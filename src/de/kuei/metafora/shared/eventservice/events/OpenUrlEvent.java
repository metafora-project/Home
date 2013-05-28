package de.kuei.metafora.shared.eventservice.events;

import de.novanic.eventservice.client.event.Event;

public class OpenUrlEvent implements Event {

	private String token;
	private String url;

	public OpenUrlEvent() {

	}

	public OpenUrlEvent(String url, String token) {
		this.url = url;
		this.token = token;

	}

	public String getUrl() {
		return url;
	}

	public String getToken() {
		return token;
	}
}
