package de.kuei.metafora.client.login.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.ChallengeManager;
import de.kuei.metafora.client.login.GroupManager;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;

public class TeamCallback implements AsyncCallback<Integer> {

	final static Languages language = GWT.create(Languages.class);
	private UserLinkAsync userLink = GWT.create(UserLink.class);

	private String groupname;

	public TeamCallback(String groupname) {
		this.groupname = groupname;
	}

	public void onFailure(Throwable caught) {
		Window.alert(language.TheGroupnameCoudNotBeSet()+" "+language.LoginFailed()+"\nLoginHandler userLink.setTeamname():\n"
				+ caught.getMessage() + "\n" + caught.getCause());
	}

	public void onSuccess(Integer result) {
		userLink.sendLoginXML(Home.userName, groupname, Home.token,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Void result) {
					}
				});
		GroupManager.getInstance().hideUI();
		Home.lastFrame.setGroupInformation(groupname);
		ChallengeManager.getInstance().main();
	}
}