package de.kuei.metafora.shared.eventservice.events;

import de.novanic.eventservice.client.event.Event;

public class GroupEvent implements Event {

	private String token;
	private String group;

	public GroupEvent() {

	}

	public GroupEvent(String group, String token) {
		this.token = token;
		this.group = group;
	}

	public String getGroup() {
		return group;
	}

	public String getToken() {
		return token;
	}

}
