package de.kuei.metafora.client.team.server;

import java.util.HashMap;
import java.util.Vector;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface UserLinkAsync {

	void login(String name, String md5Password, String passwordEncrypted,
			String groupId, String challengeId, String map, String token,
			AsyncCallback<Integer> callback);

	void logout(String name, String groupId, String challengeId,
			String challengeName, String token, AsyncCallback<Void> callback);

	void sendLoginXML(String name, String groupId, String token,
			AsyncCallback<Void> callback);

	void setTeamname(String newName, String token, String challengeId,
			String map, Boolean login, AsyncCallback<Integer> callback);

	void getUsernames(String token, AsyncCallback<Vector<String>> callback);

	void getTeamnames(String token, AsyncCallback<Vector<String>> callback);

	void register(String name, String md5Password, String shaPassword,
			String salt, String groupId, String token,
			AsyncCallback<Integer> callback);

	void getTeam(String token, AsyncCallback<Vector<Vector<String>>> callback);

	void getSalt(String user, AsyncCallback<String> callback);

	void replacePassword(String user, String salt, String shaPassword,
			AsyncCallback<Void> callback);

	void logoutClient(String groupId, String challengeId, String challengeName,
			String token, AsyncCallback<Void> callback);

	void getToolData(AsyncCallback<HashMap<String, String[]>> callback);
}
