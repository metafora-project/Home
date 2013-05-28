package de.kuei.metafora.client.login.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.GroupManager;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;
import de.kuei.metafora.client.util.InputFilter;

public class SetGroupNameHandler implements ClickHandler {

	final static Languages language = GWT.create(Languages.class);
	private UserLinkAsync userLink = GWT.create(UserLink.class);
	private GroupManager groupManager = null;

	public SetGroupNameHandler(GroupManager _groupManager) {
		groupManager = _groupManager;
	}

	@Override
	public void onClick(ClickEvent event) {
		final String newName = Window.prompt(language.PleaseEnterYourGroupName()+" ",
				"unnamed");
		if ((newName == null) || (newName.equals("unnamed"))
				|| (newName.isEmpty())) {
			Window.alert(language.CreatingNewGroupFailed());
		} else {
			final String filteredName = InputFilter.filterString(newName);

			String map = Home.mapName;

			userLink.setTeamname(filteredName, Home.token, Home.challengeId,
					map, false, new AsyncCallback<Integer>() {

						@Override
						public void onFailure(Throwable caught) {
							Window.alert(language.TheGroupnameCoudNotBeSet()+"\nSetGroupNameHandler userLink.setTeamname():\n"
									+ caught.getMessage()
									+ "\n"
									+ caught.getCause());
						}

						@Override
						public void onSuccess(Integer result) {
							groupManager.addGroupName(newName);
						}
					});
		}
	}
}