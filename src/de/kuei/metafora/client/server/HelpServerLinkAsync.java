package de.kuei.metafora.client.server;

import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HelpServerLinkAsync {

	void help(String username, String groupId, String challengeId, String challengeName, String message, String selectedUrl, Vector<String> openUrls, String token, boolean group, boolean others, AsyncCallback<Void> callback);

}
