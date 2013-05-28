package de.kuei.metafora.client.login.handler.server;

import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface MapServiceAsync {
	void getMapnames(String user, String groupId, Boolean all, String token, AsyncCallback<Vector<String>> callback);
}
