package de.kuei.metafora.server.home.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import de.kuei.metafora.shared.HistoryData;

public class MysqlConnector {

	public static String url = "jdbc:mysql://metafora.ku-eichstaett.de/metafora?useUnicode=true&characterEncoding=UTF-8";
	public static String user = "meta";
	public static String password = "didPfM";

	private static MysqlConnector instance = null;

	public static synchronized MysqlConnector getInstance() {
		if (instance == null) {
			instance = new MysqlConnector();
		}
		return instance;
	}

	private Connection connection;

	private MysqlConnector() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			try {
				connection = DriverManager.getConnection(MysqlConnector.url,
						MysqlConnector.user, MysqlConnector.password);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Vector<String[]> loadUsers() {
		Vector<String[]> users = new Vector<String[]>();

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM Users");
			ResultSet rs = pst.executeQuery();

			if (rs.first()) {
				do {
					String[] user = new String[2];
					user[0] = rs.getString("name");
					user[1] = rs.getString("shapassword");
					users.add(user);
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return users;
	}

	/**
	 * Returns all graphnames which are in the DB.
	 * 
	 * @return Vector<String>
	 */
	public Vector<String> loadMapnames(String username, String groupId,
			Boolean all) {
		Vector<String> maps = new Vector<String>();

		if (!all) {
			try {
				PreparedStatement pst = getConnection()
						.prepareStatement(
								"SELECT graphName, name FROM DirectedGraph JOIN (SELECT MapOwners.MapId, MapOwners.UserId, Users.name FROM MapOwners JOIN Users ON MapOwners.UserId = Users.id) AS Owners ON DirectedGraph.graphId = Owners.MapId WHERE name IN (?)");
				pst.setString(1, username);
				ResultSet rs = pst.executeQuery();

				if (rs.first()) {
					do {
						String graphName = rs.getString("graphName");
						if (!maps.contains(graphName))
							maps.add(graphName);
					} while (rs.next());
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}

			try {
				PreparedStatement pst = getConnection()
						.prepareStatement(
								"SELECT graphName, name FROM DirectedGraph JOIN (SELECT MapTeams.MapId, MapTeams.TeamId, Teams.name FROM MapTeams JOIN Teams ON MapTeams.TeamId = Teams.id) AS Teams ON DirectedGraph.graphId = Teams.MapId WHERE name = ?");
				pst.setString(1, groupId);
				ResultSet rs = pst.executeQuery();

				if (rs.first()) {
					do {
						String graphName = rs.getString("graphName");
						if (!maps.contains(graphName))
							maps.add(graphName);
					} while (rs.next());
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		} else {
			try {
				PreparedStatement pst = getConnection().prepareStatement(
						"SELECT * FROM DirectedGraph");
				ResultSet rs = pst.executeQuery();

				if (rs.first()) {
					do {
						String map = rs.getString("graphName");
						maps.add(map);
					} while (rs.next());
				}

				rs.close();
				pst.close();
				Collections.sort(maps);

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return maps;
	}

	public Vector<String> loadTeams() {
		Vector<String> teams = new Vector<String>();

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM Teams");
			ResultSet rs = pst.executeQuery();

			if (rs.first()) {
				do {
					String team = rs.getString("name");
					teams.add(team);
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return teams;
	}

	public Vector<String> loadChallenges() {
		Vector<String> challenges = new Vector<String>();

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM challenge");
			ResultSet rs = pst.executeQuery();
			if (rs.first()) {
				do {
					String challenge = rs.getString("challengeName");
					challenges.add(challenge);
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return challenges;
	}

	public synchronized String getChallengeId(String challengeName) {
		int challengeId = 0;

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM challenge WHERE challengeName LIKE ?");
			pst.setString(1, challengeName);
			ResultSet rs = pst.executeQuery();

			if (getRowCount(rs) == 0) {
				System.err.println("Challenge not found in database.");
				rs.close();
				pst.close();
				return null;
			} else if (getRowCount(rs) > 1) {
				System.err.println("Multiple challenges with name "
						+ challengeName + " found in database!");
			}

			if (rs.first()) {
				challengeId = rs.getInt("challengeId");
			} else {
				return null;
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return challengeId + "";
	}

	public synchronized String getChallengeUrl(String challengeName) {
		String challengeUrl = null;
		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM challenge WHERE challengeName LIKE ?");
			pst.setString(1, challengeName);
			ResultSet rs = pst.executeQuery();

			if (getRowCount(rs) == 0) {
				System.err.println("Challenge not found in database.");
				rs.close();
				pst.close();
				return null;
			} else if (getRowCount(rs) > 1) {
				System.err.println("Multiple challenges with name "
						+ challengeName + " found in database!");
			}

			if (rs.first()) {
				challengeUrl = rs.getString("challengeUrl");
			} else {
				return null;
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return challengeUrl;
	}

	private int getRowCount(ResultSet rs) {
		int rowCount = 0;
		try {
			if (rs.last()) {
				rowCount = rs.getRow();
				rs.first();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rowCount;
	}

	private Connection getConnection() {
		try {
			if (connection == null || !connection.isValid(5)) {
				connection = DriverManager.getConnection(MysqlConnector.url,
						MysqlConnector.user, MysqlConnector.password);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return connection;
	}

	public void createUser(String user, String shaPassword, String salt,
			String md5Password) {
		try {
			PreparedStatement pst = getConnection()
					.prepareStatement(
							"INSERT INTO Users(name, salt, shapassword, password) VALUES (?, ?, ?, ?)");
			pst.setString(1, user);
			pst.setString(2, salt);
			pst.setString(3, shaPassword);
			pst.setString(4, md5Password);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void createTeam(String team) {
		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"INSERT INTO Teams(name) VALUES (?)");
			pst.setString(1, team);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Vector<String[]> loadSalts() {
		Vector<String[]> users = new Vector<String[]>();

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM Users");
			ResultSet rs = pst.executeQuery();

			if (rs.first()) {
				do {
					String[] user = new String[2];
					user[0] = rs.getString("name");
					user[1] = rs.getString("salt");
					users.add(user);
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return users;
	}

	public Vector<String[]> loadMD5Users() {
		Vector<String[]> users = new Vector<String[]>();

		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"SELECT * FROM Users");
			ResultSet rs = pst.executeQuery();

			if (rs.first()) {
				do {
					String[] user = new String[2];
					user[0] = rs.getString("name");
					user[1] = rs.getString("password");
					users.add(user);
				} while (rs.next());
			}

			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return users;
	}

	public void replacePassword(String user, String salt, String shaPassword) {
		try {
			PreparedStatement pst = getConnection().prepareStatement(
					"UPDATE Users SET salt=?, shapassword=? WHERE name = ?");
			pst.setString(1, salt);
			pst.setString(2, shaPassword);
			pst.setString(3, user);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveJoinedTeam(String user, String team) {
		long time = System.currentTimeMillis();
		try {
			String sql = "INSERT INTO RecentGroups(`username`, `group`, `time`) VALUES ('"
					+ user + "', '" + team + "', " + time + ")";
			PreparedStatement pst = getConnection().prepareStatement(sql);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveSelectedChallenge(String user, String team, String challenge) {
		long time = System.currentTimeMillis();
		try {
			String sql = "INSERT INTO RecentChallenges(`username`, `group`, `challenge`, `time`) VALUES ('"
					+ user
					+ "', '"
					+ team
					+ "', "
					+ challenge
					+ " ,"
					+ time
					+ ")";
			PreparedStatement pst = getConnection().prepareStatement(sql);
			pst.execute();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveUserData(String token, String username, String group,
			String challengeId, String map, boolean login, boolean logout) {
		long now = System.currentTimeMillis();

		// look for active session
		String sql = "SELECT * FROM  `History` WHERE  `username` LIKE  '"
				+ username
				+ "' AND  `logouttime` IS NULL ORDER BY `logintime` DESC";

		boolean activeSession = false;
		int dbId = -1;
		String dbToken = null;
		String dbGroup = null;
		String dbChallengeId = null;
		String dbMap = null;

		try {
			PreparedStatement pst = getConnection().prepareStatement(sql);
			ResultSet rs = pst.executeQuery();

			if (rs.first()) {
				activeSession = true;
				dbId = rs.getInt("id");
				dbToken = rs.getString("token");
				dbGroup = rs.getString("group");
				dbChallengeId = rs.getString("challenge");
				dbMap = rs.getString("map");
			}

			// close old open sessions
			while (rs.next()) {
				int id = rs.getInt("id");
				String sqlCloseSession = "UPDATE `History` SET `logouttime` = '"
						+ now + "' WHERE id = " + id;

				PreparedStatement pstCloseSession = getConnection()
						.prepareStatement(sqlCloseSession);
				pstCloseSession.execute();
				pstCloseSession.close();
			}

			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (dbToken != null && token != null && !dbToken.equals(token)) {
			// new token => handle as login
			login = true;
		}

		if (!activeSession) {
			// if there is no running session, handle as login
			login = true;
		}

		if ((dbGroup != null && group != null && !dbGroup.equals(group))
				|| (dbMap != null && map != null && !dbMap.equals(map))
				|| (dbChallengeId != null && challengeId != null && !dbChallengeId
						.equals(challengeId))) {
			// handle as new session
			login = true;
		}

		if ((logout || login) && activeSession) {
			// close session on logout, close open old session on login
			try {
				String sqlCloseSession = "UPDATE `History` SET `logouttime` = "
						+ now + " WHERE id = " + dbId;

				PreparedStatement pstCloseSession = getConnection()
						.prepareStatement(sqlCloseSession);
				pstCloseSession.execute();
				pstCloseSession.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		if (login) {
			String sqlLogin1 = "INSERT INTO `History` (`token`, `username`, `logintime`";
			String sqlLogin2 = ") VALUES ('" + token + "', '" + username
					+ "', " + now;

			if (group != null) {
				sqlLogin1 += ", `group`";
				sqlLogin2 += (", '" + group + "'");
			}

			if (challengeId != null) {
				sqlLogin1 += ", `challenge`";
				sqlLogin2 += (", " + challengeId);
			}

			if (map != null) {
				sqlLogin1 += ", `map`";
				sqlLogin2 += (", '" + map + "'");
			}

			String sqlLogin = sqlLogin1 + sqlLogin2 + ")";

			try {
				PreparedStatement pstLogin = getConnection().prepareStatement(
						sqlLogin);
				pstLogin.execute();
				pstLogin.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		if (!login && !logout && activeSession) {
			// update session
			boolean update = false;

			String sqlUpdateSession = "UPDATE `History` SET ";

			if (group != null && dbGroup == null) {
				update = true;
				sqlUpdateSession += "`group` = '" + group + "' ";
			}

			if (challengeId != null && dbChallengeId == null) {
				if (update) {
					sqlUpdateSession += ", ";
				}
				update = true;
				sqlUpdateSession += "`challenge` = " + challengeId + " ";
			}

			if (map != null && dbMap == null) {
				if (update) {
					sqlUpdateSession += ", ";
				}
				update = true;
				sqlUpdateSession += "`map` = '" + map + "' ";
			}

			sqlUpdateSession += " WHERE id = " + dbId;

			if (update) {
				try {
					PreparedStatement pstLogin = getConnection()
							.prepareStatement(sqlUpdateSession);
					pstLogin.execute();
					pstLogin.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public Vector<HistoryData> getHistory(String user) {
		HashMap<String, HistoryData> history = new HashMap<String, HistoryData>();

		String sql = "SELECT `History`.`id`, `History`.`token`, `History`.`username`, `History`.`group`,"
				+ " `History`.`challenge`, `History`.`map`, `History`.`logintime`, `History`.`logouttime`, `History`.`logouttime`,"
				+ " `challenge`.`challengeName` FROM `History` LEFT JOIN `challenge` ON  `History`.`challenge` = `challenge`.`challengeId`"
				+ " WHERE `map` IS NOT NULL AND `challenge` IS NOT NULL AND `username` LIKE '"
				+ user + "' ORDER BY `logintime` DESC LIMIT 100";

		try {
			PreparedStatement pstLogin = getConnection().prepareStatement(sql);
			ResultSet rs = pstLogin.executeQuery();

			if (rs.first()) {
				do {
					String challengeId = rs.getString("challenge");
					String group = rs.getString("group");
					String map = rs.getString("map");

					if (!history.containsKey(challengeId)) {

						String challengeName = rs.getString("challengeName");
						String id = rs.getString("id");
						String token = rs.getString("token");
						long logintime = rs.getLong("logintime");
						long logouttime = rs.getLong("logouttime");

						HistoryData historyData = new HistoryData();
						historyData.setChallengeId(challengeId);
						historyData.setChallengeName(challengeName);
						historyData.setTime(logintime);
						historyData.setLogout(logouttime);
						historyData.setId(id);
						historyData.setToken(token);
						historyData.addMap(map);
						historyData.addGroup(group);

						history.put(challengeId, historyData);
					} else {
						history.get(challengeId).addGroup(group);
						history.get(challengeId).addMap(map);
					}
				} while (rs.next() && history.size() <= 5);
			}

			pstLogin.close();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		for (String key : history.keySet()) {
			HistoryData historyData = history.get(key);

			// find other local users
			String sqlUser = "SELECT `username` FROM `History` WHERE `token` LIKE '"
					+ historyData.getToken()
					+ "' AND `group` LIKE '"
					+ historyData.getGroups().firstElement()
					+ "' AND `challenge` LIKE '"
					+ historyData.getChallengeId()
					+ "' AND `map` LIKE '"
					+ historyData.getMaps().firstElement()
					+ "' AND `username` NOT LIKE '"
					+ user
					+ "' ORDER BY `logintime` DESC LIMIT 100";

			try {
				PreparedStatement pstLogin = getConnection().prepareStatement(
						sqlUser);
				ResultSet rs = pstLogin.executeQuery();

				if (rs.first()) {
					do {
						String otherUser = rs.getString("username");
						historyData.addOtherUser(otherUser);
					} while (rs.next());
				}

				pstLogin.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}

			// find used maps
			String sqlMaps = "SELECT `map` FROM `PlanHistory` WHERE `username` LIKE '"
					+ user
					+ "' AND `token` LIKE '"
					+ historyData.getToken()
					+ "' AND `time` >= " + historyData.getTime().getTime();
			if (historyData.getLogout() != null) {
				sqlMaps += " AND `time` <= "
						+ historyData.getLogout().getTime();
			}
			sqlMaps += " ORDER BY `time` DESC LIMIT 100";

			try {
				PreparedStatement pstLogin = getConnection().prepareStatement(
						sqlMaps);
				ResultSet rs = pstLogin.executeQuery();

				if (rs.first()) {
					do {
						String map = rs.getString("map");
						historyData.addMap(map);
					} while (rs.next());
				}

				pstLogin.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}

		Vector<HistoryData> data = new Vector<HistoryData>();
		data.addAll(history.values());

		return data;
	}
}
