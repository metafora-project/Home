package de.kuei.metafora.shared.eventservice.events;

import de.novanic.eventservice.client.event.Event;

public class UserLoginEvent implements Event {

	private String user;
	private String token;
	private String group;

	public UserLoginEvent() {

	}

	public UserLoginEvent(String user, String token, String group) {
		this.user = user;
		this.token = token;
		this.group = group;
	}

	public String getUser() {
		return user;
	}

	public String getGroup() {
		return group;
	}

	public String getToken() {
		return token;
	}

}
