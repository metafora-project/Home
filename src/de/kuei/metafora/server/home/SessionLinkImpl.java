package de.kuei.metafora.server.home;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.server.SessionLink;
import de.kuei.metafora.server.home.manager.Usermanager;

public class SessionLinkImpl extends RemoteServiceServlet implements
		SessionLink {

	@Override
	public String startSession(String token, String oldToken) {
		if (oldToken == null) {
			Usermanager.getInstance().registerSession(token);
		} else {
			Usermanager.getInstance().refreshSession(token, oldToken);
		}
		return token;
	}
}
