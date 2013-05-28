package de.kuei.metafora.shared.eventservice.events;

import de.novanic.eventservice.client.event.Event;

public class ChatObjectEvent implements Event {

	private String group;
	private String text;
	private String url;
	private String tool;
	private String user;
	private String viewurl;

	public ChatObjectEvent() {

	}

	public ChatObjectEvent(String group, String text, String url,
			String tool, String user, String viewurl) {
		this.group = group;
		this.text = text;
		this.url = url;
		this.tool = tool;
		this.user = user;
		this.viewurl = viewurl;
	}

	public String getGroup() {
		return group;
	}

	public String getText() {
		return text;
	}

	public String getUrl() {
		return url;
	}

	public String getTool() {
		return tool;
	}

	public String getUser() {
		return user;
	}

	public String getViewUrl() {
		return viewurl;
	}
}
