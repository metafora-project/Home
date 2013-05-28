package de.kuei.metafora.shared.eventservice.events;

import de.novanic.eventservice.client.event.Event;

public class LoginErrorEvent implements Event {

	private String token;
	private String message;

	public LoginErrorEvent() {

	}

	public LoginErrorEvent(String message, String token) {
		this.message = message;
		this.token = token;

	}

	public String getToken() {
		return token;
	}

	public String getMessage() {
		return message;
	}

}
