package de.kuei.metafora.shared;

import java.io.Serializable;
import java.util.Date;
import java.util.Vector;

public class HistoryData implements Serializable {

	private String id = null;
	private String token = null;
	private String challengeId = null;
	private String challengeName = null;
	private Vector<String> groups;
	private Vector<String> otherUsers;
	private Date time = null;
	private Date logout = null;
	private Vector<String> maps;

	public HistoryData() {
		groups = new Vector<String>();
		otherUsers = new Vector<String>();
		maps = new Vector<String>();
	}

	public void setTime(long time) {
		this.time = new Date(time);
	}

	public void setLogout(long time) {
		this.logout = new Date(time);
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setChallengeName(String challengeName) {
		this.challengeName = challengeName;
	}

	public void setChallengeId(String challengeId) {
		this.challengeId = challengeId;
	}

	public void addMap(String map) {
		if (map != null && !maps.contains(map))
			maps.add(map);
	}

	public void addOtherUser(String user) {
		if (user != null && !otherUsers.contains(user))
			otherUsers.add(user);
	}

	public void addGroup(String group) {
		if (group != null && !groups.contains(group))
			groups.add(group);
	}

	public String getChallengeName() {
		return challengeName;
	}

	public String getChallengeId() {
		return challengeId;
	}

	public Vector<String> getGroups() {
		return groups;
	}

	public Vector<String> getOtherUsers() {
		return otherUsers;
	}

	public Vector<String> getMaps() {
		return maps;
	}

	public Date getTime() {
		return time;
	}

	public Date getLogout() {
		return logout;
	}
}
