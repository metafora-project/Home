package de.kuei.metafora.shared.eventservice.events;

import de.novanic.eventservice.client.event.Event;

public class ChatEvent implements Event {

	private String token;
	private String name;
	private String group;
	private String message;

	public ChatEvent() {

	}

	public ChatEvent(String name, String group, String message, String token) {
		this.name = name;
		this.group = group;
		this.message = message;
		this.token = token;

	}

	public String getGroup() {
		return group;
	}

	public String getToken() {
		return token;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}

}
