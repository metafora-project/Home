package de.kuei.metafora.client.server;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("session")
public interface SessionLink extends RemoteService {

	public String startSession(String token, String oldToken);

}
