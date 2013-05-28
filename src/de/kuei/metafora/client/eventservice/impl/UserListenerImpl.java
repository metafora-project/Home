package de.kuei.metafora.client.eventservice.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Window;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.eventservice.UserListener;
import de.kuei.metafora.client.team.TeamWidget;
import de.kuei.metafora.shared.eventservice.events.GroupEvent;
import de.kuei.metafora.shared.eventservice.events.LoginErrorEvent;
import de.kuei.metafora.shared.eventservice.events.UserLoginEvent;
import de.kuei.metafora.shared.eventservice.events.UserLogoutEvent;
import de.novanic.eventservice.client.event.Event;

public class UserListenerImpl implements UserListener {

	private static Logger logger = Logger.getLogger("Home.UserListenerImpl");

	public UserListenerImpl() {
		logger.setLevel(Level.WARNING);
	}

	@Override
	public void apply(Event event) {
		if (event instanceof GroupEvent) {
			groupEvent((GroupEvent) event);
		} else if (event instanceof LoginErrorEvent) {
			loginErrorEvent((LoginErrorEvent) event);
		} else if (event instanceof UserLoginEvent) {
			userLoginEvent((UserLoginEvent) event);
		} else if (event instanceof UserLogoutEvent) {
			userLogoutEvent((UserLogoutEvent) event);
		}
	}

	@Override
	public void groupEvent(GroupEvent event) {
		if (event.getToken().equals(Home.token)) {
			TeamWidget.getInstance().setTeamName(event.getGroup());
		}
	}

	@Override
	public void loginErrorEvent(LoginErrorEvent event) {
		Window.alert(event.getMessage());
	}

	@Override
	public void userLoginEvent(UserLoginEvent event) {
		if (event.getGroup().equals(Home.groupName)) {
			if (event.getToken().equals(Home.token)) {
				TeamWidget.getInstance().addUser(event.getUser(), true);
			} else {
				TeamWidget.getInstance().addUser(event.getUser(), false);
				Home.lastFrame.addRemoteUser(event.getUser());
			}

			logger.log(Level.INFO,
					Home.token + " Home.UserListenerImpl: User login of "
							+ event.getUser());
		}
	}

	@Override
	public void userLogoutEvent(UserLogoutEvent event) {
		TeamWidget.getInstance().removeUser(event.getUser());
		Home.lastFrame.removeRemoteUser(event.getUser());

		logger.log(Level.INFO, Home.token
				+ " Home.UserListenerImpl: User logout of " + event.getUser());

	}

}
