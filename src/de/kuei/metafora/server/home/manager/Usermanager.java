package de.kuei.metafora.server.home.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.kuei.metafora.server.home.StartupServlet;
import de.kuei.metafora.server.home.mysql.MysqlConnector;
import de.kuei.metafora.server.home.xml.Classification;
import de.kuei.metafora.server.home.xml.CommonFormatCreator;
import de.kuei.metafora.server.home.xml.Role;
import de.kuei.metafora.server.home.xml.XMLException;
import de.kuei.metafora.server.home.xml.XMLUtils;
import de.kuei.metafora.shared.eventservice.DomainNames;
import de.kuei.metafora.shared.eventservice.events.GroupEvent;
import de.kuei.metafora.shared.eventservice.events.UserLoginEvent;
import de.kuei.metafora.shared.eventservice.events.UserLogoutEvent;
import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;
import de.novanic.eventservice.service.registry.EventRegistryFactory;

public class Usermanager {

	private static Usermanager instance = null;

	public static final Domain userDomain = DomainFactory
			.getDomain(DomainNames.USERDOMAIN);
	public static final Domain chatDomain = DomainFactory
			.getDomain(DomainNames.CHATDOMAIN);
	public static final Domain framworkDomain = DomainFactory
			.getDomain(DomainNames.FRAMEWORKDOMAIN);

	public static Usermanager getInstance() {
		if (instance == null) {
			instance = new Usermanager();
		}

		return instance;
	}

	private Map<String, String> users;
	private Map<String, String> salts;
	private Vector<String> teams;

	private Map<String, String> tokenToTeam;
	private Map<String, String> userToIp;
	private Vector<String> active;

	private Usermanager() {
		users = Collections.synchronizedMap(new HashMap<String, String>());
		salts = Collections.synchronizedMap(new HashMap<String, String>());
		teams = new Vector<String>();
		tokenToTeam = Collections
				.synchronizedMap(new HashMap<String, String>());
		userToIp = Collections.synchronizedMap(new HashMap<String, String>());
		active = new Vector<String>();
	}

	public Vector<String> getActiveUsers(){
		return active;
	}
	
	public void loadUsers() {
		Vector<String[]> ul = MysqlConnector.getInstance().loadUsers();
		for (String[] u : ul) {
			users.put(u[0], u[1]);
		}
	}

	public void loadSalts() {
		Vector<String[]> ul = MysqlConnector.getInstance().loadSalts();
		for (String[] u : ul) {
			salts.put(u[0], u[1]);
		}
	}

	public void loadTeams() {
		teams = MysqlConnector.getInstance().loadTeams();
	}

	public void sendMessage(Event event, Domain domain) {
		EventRegistryFactory.getInstance().getEventRegistry()
				.addEvent(domain, event);
	}

	public int login(String user, String md5Password, String shaPassword,
			String groupId, String token) {
		if (users.get(user).equals(shaPassword)) {
			if (active.contains(user))
				logout(user, "", "", "");

			active.add(user);
			userToIp.put(user, token);

			if (tokenToTeam.get(token) == null)
				setTeam(token, groupId, true);

			UserLoginEvent event = new UserLoginEvent(user, token, groupId);

			sendMessage(event, Usermanager.userDomain);

			return 1;
		} else {
			return -1;
		}
	}

	/**
	 * Sends LOGIN XML to XMPP channels for PlaTO.
	 * 
	 * @param user
	 *            username
	 * @param groupId
	 *            groupname
	 * @param token
	 */
	public void sendLoginXML(String user, String groupId, String token) {
		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "LOGIN", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL", StartupServlet.toolname);

			creator.addUser(user, token, Role.originator);
			creator.addContentProperty("GROUP_ID", groupId);

			StartupServlet.sendToLogger(creator.getDocument());
			StartupServlet.sendToCommand(creator.getDocument());

			// send indicator to analysis channel
			Document doc = XMLUtils.createDocument();
			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");
			actiontype.setAttribute("type", "INDICATOR");
			actiontype.setAttribute("classification", "other");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			Element userElement = doc.createElement("user");
			userElement.setAttribute("id", user);
			userElement.setAttribute("role", "originator");
			action.appendChild(userElement);

			Element object = doc.createElement("object");
			object.setAttribute("id", user + "`s login");
			object.setAttribute("type", "LOGIN");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property;
			property = doc.createElement("property");
			property.setAttribute("name", "SENDING_TOOL");
			property.setAttribute("value", StartupServlet.toolname);
			properties.appendChild(property);

			property = doc.createElement("property");
			property.setAttribute("name", "GROUP_ID");
			property.setAttribute("value", groupId);
			properties.appendChild(property);

			Element content = doc.createElement("content");
			action.appendChild(content);

			Element description = doc.createElement("description");
			CDATASection cdata = doc.createCDATASection(user + " logged in!");
			description.appendChild(cdata);
			content.appendChild(description);

			Element contentProperties = doc.createElement("properties");
			content.appendChild(contentProperties);

			Element property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "INDICATOR_TYPE");
			property2.setAttribute("value", "ACTIVITY");

			property2 = doc.createElement("property");
			property2.setAttribute("name", "SENDING_TOOL");
			property2.setAttribute("value", StartupServlet.toolname);
			contentProperties.appendChild(property2);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "ACTIVITY_TYPE");
			property2.setAttribute("value", "LOGIN");

			String xmlXMPPMessage = XMLUtils.documentToString(doc,
					"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");

			StartupServlet.sendToAnalysis(xmlXMPPMessage);
		} catch (XMLException exc) {
			System.err.println("Usermanager.login: " + exc.getMessage());
			exc.printStackTrace();
		}
	}

	public int register(String user, String md5Password, String shaPassword,
			String salt, String groupId, String ip) {
		if (users.containsKey(user)) {
			System.out.println("User already in use!");
			return -1;
		} else {
			users.put(user, shaPassword);

			// if not using SHA, use this code
			salts.put(user, salt);

			login(user, md5Password, shaPassword, groupId, ip);

			MysqlConnector.getInstance().createUser(user, shaPassword, salt,
					md5Password);

			try {
				CommonFormatCreator creator = null;
				creator = new CommonFormatCreator(System.currentTimeMillis(),
						Classification.other, "REGISTER", StartupServlet.logged);
				creator.addContentProperty("SENDING_TOOL",
						StartupServlet.toolname);

				creator.addUser(user, ip, Role.originator);
				Vector<String> party = getLocalUsers(ip);
				if (party.size() > 0) {
					for (String otherUser : party) {
						if (!otherUser.equals(user))
							creator.addUser(otherUser, ip, Role.originator);
					}
				}

				StartupServlet.sendToLogger(creator.getDocument());
				StartupServlet.sendToCommand(creator.getDocument());

				// send XML command to LASAD
				sendXMLtoLASAD(user, md5Password, ip);
				System.err.println("Home: register user " + user
						+ " with password " + md5Password + " and token " + ip);
			} catch (XMLException exc) {
				System.err.println(exc.getMessage());
			}

			return 0;
		}
	}

	private void sendXMLtoLASAD(String user, String md5Password, String token) {
		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "CREATE_USER", StartupServlet.logged,
					true);
			creator.addContentProperty("SENDING_TOOL", StartupServlet.toolname);
			creator.addContentProperty("RECEIVING_TOOL",
					StartupServlet.lasadName);

			creator.addUser(user, token, Role.originator);
			creator.setObject("0", "ELEMENT");
			creator.addProperty("PASSWORD_ENCRYPTED", "true");
			creator.addProperty("ROLE", "Standard");
			creator.addProperty("USERNAME", user);
			creator.addProperty("PASSWORD", md5Password);

			StartupServlet.sendToCommand(creator.getDocument());
		} catch (XMLException exc) {
			System.err.println("Home: Usermanager.sendXMLtoLASAD: "
					+ exc.getMessage());
			exc.printStackTrace();
		}
	}

	public void logoutClient(String token, String groupId, String challengeId,
			String challengeName) {
		Vector<String> users = getUsersForIp(token);

		for (String user : users) {
			logout(user, groupId, challengeId, challengeName);
		}

		tokenToTeam.remove(token);
	}

	public void logout(String user, String groupId, String challengeId,
			String challengeName) {
		String ip = userToIp.get(user);

		active.remove(user);
		userToIp.remove(user);

		UserLogoutEvent event = new UserLogoutEvent(user, groupId);

		sendMessage(event, Usermanager.userDomain);

		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "LOGOUT", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL", StartupServlet.toolname);

			creator.addUser(user, ip, Role.originator);

			if (groupId.length() > 0) {
				creator.addContentProperty("GROUP_ID", groupId);
			}
			if (challengeId.length() > 0) {
				creator.addContentProperty("CHALLENGE_ID", challengeId);
			}
			if (challengeName.length() > 0) {
				creator.addContentProperty("CHALLENGE_NAME", challengeName);
			}

			StartupServlet.sendToLogger(creator.getDocument());
			StartupServlet.sendToCommand(creator.getDocument());

			// send indicator to analysis channel
			Document doc = XMLUtils.createDocument();
			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");
			actiontype.setAttribute("type", "INDICATOR");
			actiontype.setAttribute("classification", "other");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			Element userElement = doc.createElement("user");
			userElement.setAttribute("id", user);
			userElement.setAttribute("role", "originator");
			action.appendChild(userElement);

			Element object = doc.createElement("object");
			object.setAttribute("id", user + "`s logout");
			object.setAttribute("type", "LOGOUT");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property;
			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "SENDING_TOOL");
			property.setAttribute("value", StartupServlet.toolname);

			Element content = doc.createElement("content");
			action.appendChild(content);

			Element description = doc.createElement("description");
			CDATASection cdata = doc.createCDATASection(user + " logged out!");
			description.appendChild(cdata);
			content.appendChild(description);

			Element contentProperties = doc.createElement("properties");
			content.appendChild(contentProperties);

			Element property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "INDICATOR_TYPE");
			property2.setAttribute("value", "ACTIVITY");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "SENDING_TOOL");
			property2.setAttribute("value", StartupServlet.toolname);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "ACTIVITY_TYPE");
			property2.setAttribute("value", "LOGOUT");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "GROUP_ID");
			property2.setAttribute("value", groupId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_ID");
			property2.setAttribute("value", challengeId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_NAME");
			property2.setAttribute("value", challengeName);

			String xmlXMPPMessage = XMLUtils.documentToString(doc,
					"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");

			StartupServlet.sendToAnalysis(xmlXMPPMessage);
		} catch (XMLException exc) {
			System.err.println("Usermanager.logout: " + exc.getMessage());
			exc.printStackTrace();
		}
	}

	public String getIpForUser(String user, String ip) {
		return userToIp.get(user);
	}

	private Vector<String> getUsersForIp(String ip) {
		Vector<String> users = new Vector<String>();

		for (String user : userToIp.keySet()) {
			if (userToIp.get(user).equals(ip)) {
				users.add(user);
			}
		}

		return users;
	}

	private void saveSelectedTeam(String ip, String team, String challengeId,
			String map) {
		Vector<String> users = getUsersForIp(ip);
		for (String user : users) {
			MysqlConnector.getInstance().saveUserData(ip, user, team,
					challengeId, map, false, false);
		}
	}

	public int setTeam(String ip, String team, Boolean login) {
		return setTeam(ip, team, null, null, login);
	}

	/**
	 * Sets the groupname.
	 * 
	 * @param ip
	 *            token
	 * @param team
	 *            groupname
	 * @param login
	 *            true if login (no SET_TEAMNAME will be sent because of PlaTO),
	 *            false if user does not login but switch to other group
	 * @return
	 */
	public int setTeam(String ip, String team, String challengeId, String map,
			Boolean login) {
		if (!teams.contains(team)) {
			teams.add(team);

			MysqlConnector.getInstance().createTeam(team);
		}

		saveSelectedTeam(ip, team, challengeId, map);

		String oldTeam = tokenToTeam.get(ip);

		if (oldTeam != null && oldTeam.equals(team)) {
			return 1;
		}

		tokenToTeam.put(ip, team);

		// search all users for that client
		Vector<String> party = new Vector<String>();

		for (String user : userToIp.keySet()) {
			if (userToIp.get(user).equals(ip)) {
				party.add(user);
			}
		}

		for (String user : party) {

			UserLogoutEvent logoutEvent = new UserLogoutEvent(user, oldTeam);
			UserLoginEvent loginEvent = new UserLoginEvent(user, ip, team);

			sendMessage(logoutEvent, Usermanager.userDomain);
			sendMessage(loginEvent, Usermanager.userDomain);
		}

		GroupEvent groupEvent = new GroupEvent(team, ip);
		sendMessage(groupEvent, Usermanager.userDomain);

		HashMap<String, Vector<String>> teamToUsers = new HashMap<String, Vector<String>>();
		for (String user : userToIp.keySet()) {
			String useraddress = userToIp.get(user);
			String userteam = tokenToTeam.get(useraddress);
			Vector<String> teammembers = teamToUsers.get(userteam);
			if (teammembers == null) {
				teammembers = new Vector<String>();
				teamToUsers.put(userteam, teammembers);
			}
			teammembers.add(user);
		}

		String teamover = "team overview:\n";
		for (String teamname : teamToUsers.keySet()) {
			teamover += teamname + ": ";
			Vector<String> teammembers = teamToUsers.get(teamname);
			for (String user : teammembers) {
				teamover += user + ", ";
			}
			teamover = teamover.substring(0, teamover.length() - 2);
			teamover += "\n";
		}

		if (!login) {
			try {
				CommonFormatCreator creator = null;
				creator = new CommonFormatCreator(System.currentTimeMillis(),
						Classification.other, "SET_TEAMNAME",
						StartupServlet.logged);
				creator.addContentProperty("SENDING_TOOL",
						StartupServlet.toolname);

				for (String otherUser : party) {
					creator.addUser(otherUser, ip, Role.originator);
				}

				creator.setObject("0", "ELEMENT");
				creator.addProperty("GROUP_ID", team);

				StartupServlet.sendToLogger(creator.getDocument());
				StartupServlet.sendToCommand(creator.getDocument());

				Document doc = XMLUtils.createDocument();
				Element action = doc.createElement("action");
				action.setAttribute("time", System.currentTimeMillis() + "");
				doc.appendChild(action);

				Element actiontype = doc.createElement("actiontype");
				actiontype.setAttribute("type", "INDICATOR");
				actiontype.setAttribute("classification", "other");
				actiontype.setAttribute("logged", StartupServlet.logged + "");
				action.appendChild(actiontype);

				if (party.size() > 0) {
					for (String otherUser : party) {
						Element userElement = doc.createElement("user");
						userElement.setAttribute("id", otherUser);
						userElement.setAttribute("role", "originator");
						action.appendChild(userElement);
					}
				}

				Element object = doc.createElement("object");
				object.setAttribute("id", "Party`s group switch");
				object.setAttribute("type", "GROUP_SWITCH");
				action.appendChild(object);

				Element properties = doc.createElement("properties");
				object.appendChild(properties);

				Element property;
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "SENDING_TOOL");
				property.setAttribute("value", StartupServlet.toolname);

				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "GROUP_ID");
				property.setAttribute("value", team);

				Element content = doc.createElement("content");
				action.appendChild(content);

				Element description = doc.createElement("description");
				CDATASection cdata = doc
						.createCDATASection("Party switched group!");
				description.appendChild(cdata);
				content.appendChild(description);

				Element contentProperties = doc.createElement("properties");
				content.appendChild(contentProperties);

				Element property2 = doc.createElement("property");
				contentProperties.appendChild(property2);
				property2.setAttribute("name", "INDICATOR_TYPE");
				property2.setAttribute("value", "ACTIVITY");

				property2 = doc.createElement("property");
				contentProperties.appendChild(property2);
				property2.setAttribute("name", "SENDING_TOOL");
				property2.setAttribute("value", StartupServlet.toolname);

				property2 = doc.createElement("property");
				contentProperties.appendChild(property2);
				property2.setAttribute("name", "ACTIVITY_TYPE");
				property2.setAttribute("value", "GROUP_SWITCH");

				String xmlXMPPMessage = XMLUtils
						.documentToString(doc,
								"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");

				StartupServlet.sendToAnalysis(xmlXMPPMessage);
			} catch (XMLException exc) {
				System.err.println("Usermanager.setTeam: " + exc.getMessage());
				exc.printStackTrace();
			}
		}

		return 1;
	}

	public String getTeamForUser(String user) {
		return tokenToTeam.get(userToIp.get(user));
	}

	public Vector<String> getUsers() {
		Vector<String> userNames = new Vector<String>();
		userNames.addAll(users.keySet());
		return userNames;
	}

	public Vector<String> getTeams() {
		return teams;
	}

	public String getUserForIP(String ip) {
		for (String user : active) {
			if (userToIp.get(user).equals(ip)) {
				return user;
			}
		}
		return null;
	}

	public String getTeamName(String token) {
		return tokenToTeam.get(token);
	}

	public Vector<Vector<String>> getTeam(String ip) {
		if (tokenToTeam.get(ip) == null) {
			setTeam(ip, "Metafora", false);
		}

		Vector<Vector<String>> team = new Vector<Vector<String>>();
		Vector<String> tn = new Vector<String>();
		String teamname = tokenToTeam.get(ip);
		tn.add(teamname);
		team.add(tn);

		Vector<String> remote = new Vector<String>();
		for (String address : tokenToTeam.keySet()) {
			if (!ip.equals(address) && tokenToTeam.get(address) != null
					&& tokenToTeam.get(address).equals(teamname)) {
				for (String user : userToIp.keySet()) {
					if (userToIp.get(user).equals(address)) {
						remote.add(user);
					}
				}
			}
		}
		team.add(remote);

		Vector<String> local = new Vector<String>();
		for (String user : userToIp.keySet()) {
			if (userToIp.get(user).equals(ip)) {
				local.add(user);
			}
		}
		team.add(local);

		return team;
	}

	public String getIpForUser(String username) {
		return userToIp.get(username);
	}

	public void replacePassword(String user, String salt, String shaPassword,
			String md5Password) {
		users.remove(user);
		salts.remove(user);
		users.put(user, shaPassword);
		salts.put(user, salt);

		MysqlConnector.getInstance().replacePassword(user, salt, shaPassword);
	}

	public String getSalt(String user) {
		String salt;
		if (salts.containsKey(user)) {
			if (salts.get(user) == null) {
				return "noSalt";
			}
			salt = salts.get(user).toString();
			return salt;
		} else {
			return "noUser";
		}
	}

	/**
	 * Returns all users with same token (local logged in users).
	 * 
	 * @param token
	 * @return Vector<String> All users with same token.
	 */
	public Vector<String> getLocalUsers(String token) {
		Vector<String> local = new Vector<String>();
		for (String user : userToIp.keySet()) {
			if (userToIp.get(user).equals(token)) {
				local.add(user);
			}
		}
		return local;
	}

	public void registerSession(String token) {
		tokenToTeam.put(token, "Metafora");
		System.err.println("Home: Usermanager: New session: " + token);
	}

	public void refreshSession(String token, String oldToken) {
		String team = tokenToTeam.get(oldToken);

		Vector<String> users = new Vector<String>();
		for (String user : userToIp.keySet()) {
			if (userToIp.get(user).equals(oldToken)) {
				users.add(user);
			}
		}

		tokenToTeam.put(token, team);
		for (String user : users) {
			userToIp.put(user, token);
			System.err.println("Home: Usermanager: Session refresh for user "
					+ user + ". Old token: " + oldToken + ", new token: "
					+ token);
		}

		System.err.println("Home: Usermanager: Session refreshed for "
				+ oldToken + " with new token " + token);
	}

	public Vector<String> getTokensForTeam(String group) {
		Vector<String> tokens = new Vector<String>();
		for (String token : tokenToTeam.keySet()) {
			String tokenGroup = tokenToTeam.get(token);
			System.err.println("Home.Usermanager: getTokensForTeam: Token: "
					+ token + ", Team: " + tokenGroup);
			if (tokenGroup.equals(group)) {
				tokens.add(token);
			}
		}
		return tokens;
	}

}
