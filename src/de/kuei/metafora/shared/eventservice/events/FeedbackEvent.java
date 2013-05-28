package de.kuei.metafora.shared.eventservice.events;

import de.novanic.eventservice.client.event.Event;

public class FeedbackEvent implements Event {

	private String token;
	private String interruption;
	private String group;
	private String message;

	public FeedbackEvent() {

	}

	public FeedbackEvent(String interruption, String group, String message,
			String token) {
		this.interruption = interruption;
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

	public String getInterruption() {
		return interruption;
	}

	public String getMessage() {
		return message;
	}

}
