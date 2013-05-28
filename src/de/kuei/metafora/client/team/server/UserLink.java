package de.kuei.metafora.client.team.server;

import java.util.HashMap;
import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("userlink")
public interface UserLink extends RemoteService {

	public int register(String name, String md5Password, String shaPassword,
			String salt, String groupId, String token);

	public int login(String name, String md5Password, String passwordEncrypted,
			String groupId, String challengeId, String map, String token);

	public void sendLoginXML(String name, String groupId, String token);

	public void logout(String name, String groupId, String challengeId,
			String challengeName, String token);

	public int setTeamname(String newName, String token, String challengeId,
			String map, Boolean login);

	public Vector<String> getUsernames(String token);

	public Vector<String> getTeamnames(String token);

	public Vector<Vector<String>> getTeam(String token);

	public String getSalt(String user);

	public void replacePassword(String user, String salt, String shaPassword);

	public void logoutClient(String groupId, String challengeId,
			String challengeName, String token);

	public HashMap<String, String[]> getToolData();
}
