package de.kuei.metafora.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import de.kuei.metafora.client.team.TeamWidget;

public class UserUrlHandler implements ClickHandler {

	private Home planning;
	private String title;
	private String url;

	public UserUrlHandler(Home planning, String url, String title) {
		this.planning = planning;
		this.url = url;
		this.title = title;
	}

	@Override
	public void onClick(ClickEvent event) {
		String user = TeamWidget.getInstance().getFirstUser();

		if (user == null)
			user = "Alice";

		if (url.contains("USERNAME")) {
			url = url.replaceAll("USERNAME", user);
		}
		planning.addContentFrame(url, title, true, true);
	}

}
