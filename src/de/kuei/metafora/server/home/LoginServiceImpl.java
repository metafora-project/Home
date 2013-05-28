package de.kuei.metafora.server.home;

import java.util.Vector;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.login.handler.server.LoginService;
import de.kuei.metafora.server.home.manager.Usermanager;
import de.kuei.metafora.server.home.mysql.MysqlConnector;
import de.kuei.metafora.server.home.xml.Classification;
import de.kuei.metafora.server.home.xml.CommonFormatCreator;
import de.kuei.metafora.server.home.xml.Role;
import de.kuei.metafora.server.home.xml.XMLException;
import de.kuei.metafora.shared.HistoryData;

public class LoginServiceImpl extends RemoteServiceServlet implements
		LoginService {

	@Override
	public Vector<String> getMapnames(String token, String username,
			String groupId, Boolean all) {
		return MysqlConnector.getInstance()
				.loadMapnames(username, groupId, all);
	}

	@Override
	public Vector<String> getChallenges(String token) {
		return MysqlConnector.getInstance().loadChallenges();
	}

	@Override
	public String getChallengeId(String challengeName, String token,
			String userName, String groupId) {
		String challengeId = MysqlConnector.getInstance().getChallengeId(
				challengeName);

		MysqlConnector.getInstance().saveUserData(token, userName, groupId,
				challengeId, null, false, false);

		CommonFormatCreator creator;
		try {
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "SET_CHALLENGE",
					StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL", StartupServlet.toolname);

			creator.addUser(userName, token, Role.originator);
			Vector<String> party = Usermanager.getInstance().getLocalUsers(
					token);
			if (party.size() > 0) {
				for (String otherUser : party) {
					if (!otherUser.equals(userName))
						creator.addUser(otherUser, token, Role.originator);
				}
			}
			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			StartupServlet.sendToCommand(creator.getDocument());
			StartupServlet.sendToLogger(creator.getDocument());
		} catch (XMLException exc) {
			System.err.println(exc.getMessage());
		}

		return challengeId;
	}

	@Override
	public String getChallengeUrl(String challengeName, String token) {
		return MysqlConnector.getInstance().getChallengeUrl(challengeName);
	}

	@Override
	public Vector<HistoryData> getHistory(String user) {
		return MysqlConnector.getInstance().getHistory(user);
	}

	@Override
	public void updateLoginData(String token, String user, String group,
			String challenge, String map) {
		MysqlConnector.getInstance().saveUserData(token, user, group,
				challenge, map, false, false);
	}

}
