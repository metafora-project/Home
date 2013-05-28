package de.kuei.metafora.client.chat.serverlink;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ChatServiceAsync {

	void sendChatMessage(String msg, String challengeId, String challengeName, String token, AsyncCallback<Void> callback);

	void openChatObject(String message, String user, String groupId, String challengeId, String challengeName, String token, AsyncCallback<Void> callback);

	void sendHIFMessage(String msg, String challengeId, String challengeName,
			String token, AsyncCallback<Void> callback);

	void sendLIFMessage(String msg, String challengeId, String challengeName,
			String token, AsyncCallback<Void> callback);

}
