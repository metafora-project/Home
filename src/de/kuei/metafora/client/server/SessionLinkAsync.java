package de.kuei.metafora.client.server;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SessionLinkAsync {

	void startSession(String token, String oldToken,
			AsyncCallback<String> callback);

}
