package de.kuei.metafora.client.login.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.GroupManager;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;
import de.kuei.metafora.client.util.InputFilter;

public class GroupHandler implements ClickHandler, KeyPressHandler {

	private UserLinkAsync userLink = GWT.create(UserLink.class);
	final static Languages language = GWT.create(Languages.class);

	public GroupHandler() {

	}

	@Override
	public void onClick(ClickEvent event) {
		checkGroup();
	}

	private void checkGroup() {
		String groupname = GroupManager.getInstance().getGroupName();

		if (!(groupname.length() > 0)) {
			Window.alert(language.PleaseSelectYourGroup());
		} else {
			updateGroupName(groupname);
		}
	}

	private void updateGroupName(String group) {
		group = InputFilter.filterString(group);

		String map = Home.mapName;

		userLink.setTeamname(group, Home.token, Home.challengeId, map, true,
				new TeamCallback(group));
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		if (((int) event.getCharCode()) == 13
				|| (((int) event.getCharCode()) == 0 && ((int) event
						.getNativeEvent().getKeyCode()) == 13)) {
			checkGroup();
		}
	}
}
