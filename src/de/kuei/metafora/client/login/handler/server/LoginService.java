package de.kuei.metafora.client.login.handler.server;

import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import de.kuei.metafora.shared.HistoryData;

@RemoteServiceRelativePath("loginservice")
public interface LoginService extends RemoteService {
	public Vector<String> getMapnames(String token, String username,
			String groupId, Boolean all);

	public Vector<String> getChallenges(String token);

	public String getChallengeId(String challengeName, String token,
			String userName, String groupId);

	public String getChallengeUrl(String challengeName, String token);

	public Vector<HistoryData> getHistory(String user);

	public void updateLoginData(String token, String user, String group,
			String challenge, String map);
}
