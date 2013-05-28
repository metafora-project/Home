package de.kuei.metafora.shared.eventservice.events;

import de.novanic.eventservice.client.event.Event;

public class UserLogoutEvent implements Event {

	private String user;
	private String group;

	public UserLogoutEvent() {

	}

	public UserLogoutEvent(String user, String group) {
		this.user = user;
		this.group = group;
	}

	public String getUser() {
		return user;
	}

	public String getGroup() {
		return group;
	}
}
