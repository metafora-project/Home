package de.kuei.metafora.client.team;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.TextDecoration;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;

public class Logout extends Anchor implements ClickHandler {

	private static final UserLinkAsync userLink = GWT.create(UserLink.class);

	// i18n 
	final static Languages language = GWT.create(Languages.class);

	public Logout() {
		super(language.Logout());
		addClickHandler(this);

		getElement().getStyle().setFontWeight(FontWeight.BOLD);
		getElement().getStyle().setTextDecoration(TextDecoration.UNDERLINE);
		getElement().getStyle().setColor("#0076B0");
	}

	public void logoutUser() {
		Collection<String> cookies = Cookies.getCookieNames();
		for (String cookie : cookies) {
			if (cookie.startsWith("metafora")) {
				Cookies.removeCookie(cookie);
			}
		}

		userLink.logoutClient(Home.groupName, Home.challengeId,
				Home.challengeName, Home.token, new AsyncCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
						// reload page
						String url = Window.Location.getHref();
						Window.Location.assign(url);
					}

					@Override
					public void onFailure(Throwable caught) {
						// reload page
						String url = Window.Location.getHref();
						Window.Location.assign(url);
					}
				});
	}

	@Override
	public void onClick(ClickEvent event) {
		logoutUser();
	}
}
