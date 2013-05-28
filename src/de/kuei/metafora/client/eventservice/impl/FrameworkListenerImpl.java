package de.kuei.metafora.client.eventservice.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.eventservice.FrameworkListener;
import de.kuei.metafora.shared.eventservice.events.OpenUrlEvent;
import de.novanic.eventservice.client.event.Event;

public class FrameworkListenerImpl implements FrameworkListener {

	private static Logger logger = Logger
			.getLogger("Home.FrameworkListenerImpl");

	final static Languages language = GWT.create(Languages.class);

	public FrameworkListenerImpl() {
		logger.setLevel(Level.WARNING);
	}

	@Override
	public void apply(Event anEvent) {
		if (anEvent instanceof OpenUrlEvent) {
			openUrlEvent((OpenUrlEvent) anEvent);
		}
	}

	@Override
	public void openUrlEvent(OpenUrlEvent event) {
		if (event.getToken().equals(Home.token)) {
			String url = event.getUrl();

			Window.setStatus("OpenUrlEvent: " + url);

			if (url != null && url.length() > 0) {
				String toolname = "Tool";
				String color = "";

				if (url.toLowerCase().contains("lasad")) {
					toolname = language.Lasad();
					color = "blue";
				} else if (url.toLowerCase().contains("expresser")) {
					toolname = language.Expresser();
					color = "green";
				} else if (url.toLowerCase().contains("suscity")) {
					toolname = language.Suscity();
					color = "military";
				} else if (url.toLowerCase().contains("physt")) {
					toolname = language.Jugger();
					color = "orange";
				} else if (url.toLowerCase().contains("planningtool")) {
					toolname = language.PlanningTool();
					color = "yellow";
				} else if (url.toLowerCase().contains("piki")) {
					toolname = language.Piki();
					color = "darkred";
				} else if (url.toLowerCase().contains("malt")) {
					toolname = language.Math();
					color = "lightblue";
				}

				if (Home.lastFrame != null) {
					Home.lastFrame.addContentFrame(url, toolname, true, color,
							true, true);

					logger.log(Level.INFO, Home.token
							+ " Home.FrameworkListenerImpl: Open url " + url);

				} else {
					Window.setStatus("OpenUrlEvent: no home found!");
				}
			}
		}
	}

}
