package de.kuei.metafora.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class UrlHandler implements ClickHandler {

	private Home planning;
	private String title;
	private String url;

	public UrlHandler(Home planning, String url, String title) {
		this.planning = planning;
		this.url = url;
		this.title = title;
	}

	@Override
	public void onClick(ClickEvent event) {
		planning.addContentFrame(url, title, true, true);
	}

}
