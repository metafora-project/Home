package de.kuei.metafora.client.eventservice;

import de.kuei.metafora.shared.eventservice.events.GroupEvent;
import de.kuei.metafora.shared.eventservice.events.LoginErrorEvent;
import de.kuei.metafora.shared.eventservice.events.UserLoginEvent;
import de.kuei.metafora.shared.eventservice.events.UserLogoutEvent;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface UserListener extends RemoteEventListener {

	public void groupEvent(GroupEvent event);

	public void loginErrorEvent(LoginErrorEvent event);

	public void userLoginEvent(UserLoginEvent event);

	public void userLogoutEvent(UserLogoutEvent event);

}
