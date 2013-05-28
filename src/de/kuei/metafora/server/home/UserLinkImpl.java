package de.kuei.metafora.server.home;

import java.util.HashMap;
import java.util.Vector;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.server.home.manager.Usermanager;
import de.kuei.metafora.server.home.mysql.MysqlConnector;

public class UserLinkImpl extends RemoteServiceServlet implements UserLink {

	@Override
	public int register(String name, String md5Password, String shaPassword,
			String salt, String groupId, String token) {
		return Usermanager.getInstance().register(name, md5Password,
				shaPassword, salt, groupId, token);
	}

	@Override
	public int login(String name, String md5Password, String shaPassword,
			String groupId, String challengeId, String map, String token) {

		MysqlConnector.getInstance().saveUserData(token, name, groupId,
				challengeId, map, true, false);

		return Usermanager.getInstance().login(name, md5Password, shaPassword,
				groupId, token);
	}

	@Override
	public void sendLoginXML(String name, String groupId, String token) {
		Usermanager.getInstance().sendLoginXML(name, groupId, token);
	}

	@Override
	public void logout(String name, String groupId, String challengeId,
			String challengeName, String token) {
		MysqlConnector.getInstance().saveUserData(token, name, groupId,
				challengeId, null, false, true);

		Usermanager.getInstance().logout(name, groupId, challengeId,
				challengeName);
	}

	@Override
	public int setTeamname(String newName, String token, String challengeId,
			String map, Boolean login) {

		return Usermanager.getInstance().setTeam(token, newName, challengeId,
				map, login);
	}

	@Override
	public Vector<String> getUsernames(String token) {
		return Usermanager.getInstance().getUsers();
	}

	@Override
	public Vector<String> getTeamnames(String token) {
		return Usermanager.getInstance().getTeams();
	}

	@Override
	public Vector<Vector<String>> getTeam(String token) {
		return Usermanager.getInstance().getTeam(token);
	}

	public String getSalt(String user) {
		return Usermanager.getInstance().getSalt(user);
	}

	public void replacePassword(String user, String salt, String shaPassword) {
		Usermanager.getInstance().replacePassword(user, salt, shaPassword,
				"null");
	}

	@Override
	public void logoutClient(String groupId, String challengeId,
			String challengeName, String token) {
		Usermanager.getInstance().logoutClient(token, groupId, challengeId,
				challengeName);
	}

	@Override
	public HashMap<String, String[]> getToolData() {
		HashMap<String, String[]> tooldata = new HashMap<String, String[]>();

		tooldata.put("PlanningTool", StartupServlet.planningTool);
		tooldata.put("Lasad", StartupServlet.lasad);
		tooldata.put("Piki", StartupServlet.piki);
		tooldata.put("SusCity", StartupServlet.susCity);
		tooldata.put("expresser", StartupServlet.expresser);
		tooldata.put("Juggler", StartupServlet.juggler);
		tooldata.put("3DMath", StartupServlet.math);
		tooldata.put("Workbench", StartupServlet.workbench);
		tooldata.put("messaging", StartupServlet.messaging);

		return tooldata;
	}

}
