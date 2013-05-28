package de.kuei.metafora.client.login.handler.server;

import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.shared.HistoryData;

public interface LoginServiceAsync {
	void getMapnames(String token, String username, String groupId,
			Boolean all, AsyncCallback<Vector<String>> callback);

	void getChallenges(String token, AsyncCallback<Vector<String>> callback);

	void getChallengeId(String challengeName, String token, String userName,
			String groupId, AsyncCallback<String> callback);

	void getChallengeUrl(String challengeName, String token,
			AsyncCallback<String> callback);

	void updateLoginData(String token, String user, String group,
			String challenge, String map, AsyncCallback<Void> callback);

	void getHistory(String user, AsyncCallback<Vector<HistoryData>> callback);
}
