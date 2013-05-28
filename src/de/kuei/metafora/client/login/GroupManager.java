package de.kuei.metafora.client.login;

import java.util.Collections;
import java.util.Vector;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ColoredButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.handler.GroupHandler;
import de.kuei.metafora.client.login.handler.SetGroupNameHandler;
import de.kuei.metafora.client.team.Logout;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;
import de.kuei.metafora.shared.HistoryData;

public class GroupManager {

	// i18n 
	final static Languages language = GWT.create(Languages.class);

	private static GroupManager instance = null;

	public static GroupManager getInstance() {
		if (instance == null) {
			instance = new GroupManager();
		}
		return instance;
	}

	private UserLinkAsync userLink = GWT.create(UserLink.class);

	private Vector<String> availableGroups;

	private ListBox lbxGroup;
	private Button btnNewGroup;
	private ColoredButton btnLogin;
	private ColoredButton btnBack;
	private Logout logout;

	private RecentGroupsGui recent = null;

	private LayoutPanel layout;

	private GroupManager() {

	}

	public Vector<HistoryData> getHistory() {
		return recent.getHistory();
	}

	public void main() {
		recent = new RecentGroupsGui();

		availableGroups = new Vector<String>();

		final GroupHandler handler = new GroupHandler();

		layout = new LayoutPanel();

		HTML image = new HTML(
				"<div style='margin-left:0px;margin-top:0px;'><img src='gxt/images/logo_metafora.png' alt='logo'/></div>");
		layout.add(image);
		layout.setWidgetLeftWidth(image, 5, Unit.PX, 197, Unit.PX);
		layout.setWidgetTopHeight(image, 10, Unit.PX, 34, Unit.PX);

		logout = new Logout();
		layout.add(logout);
		layout.setWidgetRightWidth(logout, 5, Unit.PX, 100, Unit.PX);
		layout.setWidgetTopHeight(logout, 2, Unit.PX, 20, Unit.PX);

		HTML name = new HTML(language.Name() + ": " + Home.userName);
		layout.add(name);
		layout.setWidgetLeftWidth(name, 60, Unit.PCT, 250, Unit.PX);
		layout.setWidgetTopHeight(name, 0, Unit.PX, 17, Unit.PX);

		LayoutPanel content = new LayoutPanel();
		content.getElement().getStyle().setBorderWidth(1, Unit.PX);
		content.getElement().getStyle().setBorderColor("#AABBCC");
		content.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);

		HTML title = new HTML(
				"<div style='font-size: 14pt; text-align: center;'>"
						+ language.SelectYourGroup()+ "</div>");
		content.add(title);
		content.setWidgetLeftRight(title, 5, Unit.PX, 5, Unit.PX);
		content.setWidgetTopHeight(title, 5, Unit.PX, 70, Unit.PX);

		LayoutPanel leftSide = new LayoutPanel();
		content.add(leftSide);
		content.setWidgetLeftWidth(leftSide, 0, Unit.PX, 50, Unit.PCT);
		content.setWidgetTopHeight(leftSide, 70, Unit.PX, 60, Unit.PX);

		createOracleTeam();

		HTML groupLabel = new HTML(language.Group());
		leftSide.add(groupLabel);
		leftSide.setWidgetLeftWidth(groupLabel, 10, Unit.PX, 100, Unit.PX);
		leftSide.setWidgetTopHeight(groupLabel, 14, Unit.PX, 25, Unit.PX);

		lbxGroup = new ListBox();
		lbxGroup.addKeyPressHandler(handler);
		lbxGroup.setWidth("95%");
		leftSide.add(lbxGroup);
		leftSide.setWidgetLeftRight(lbxGroup, 100, Unit.PX, 10, Unit.PX);
		leftSide.setWidgetTopHeight(lbxGroup, 10, Unit.PX, 25, Unit.PX);

		btnNewGroup = new ColoredButton(language.NewGroup(), "");
		btnNewGroup.setText(language.NewGroup());
		content.add(btnNewGroup);
		content.setWidgetLeftWidth(btnNewGroup, 10, Unit.PX, 160, Unit.PX);
		content.setWidgetTopHeight(btnNewGroup, 140, Unit.PX, 30, Unit.PX);

		content.add(recent);
		content.setWidgetLeftRight(recent, 55, Unit.PCT, 10, Unit.PX);
		content.setWidgetTopBottom(recent, 80, Unit.PX, 50, Unit.PX);

		Listener<ButtonEvent> groupListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				new SetGroupNameHandler(getInstance()).onClick(null);
			}
		};
		btnNewGroup.addListener(Events.OnMouseUp, groupListener);

		btnLogin = new ColoredButton(language.Continue().toUpperCase() + " >",
				"green");
		btnLogin.setWidth("100%");
		content.add(btnLogin);
		content.setWidgetBottomHeight(btnLogin, 10, Unit.PX, 30, Unit.PX);
		content.setWidgetLeftWidth(btnLogin, 60, Unit.PCT, 30, Unit.PCT);

		Listener<ButtonEvent> loginListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				handler.onClick(null);
			}
		};
		btnLogin.addListener(Events.OnClick, loginListener);

		btnBack = new ColoredButton("< " + language.Back().toUpperCase(),
				"green");
		btnBack.setWidth("100%");
		content.add(btnBack);
		content.setWidgetBottomHeight(btnBack, 10, Unit.PX, 30, Unit.PX);
		content.setWidgetLeftWidth(btnBack, 10, Unit.PCT, 30, Unit.PCT);

		Listener<ButtonEvent> logoutListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				logout.logoutUser();
			}
		};
		btnBack.addListener(Events.OnClick, logoutListener);

		layout.add(content);
		layout.setWidgetLeftWidth(content, 15, Unit.PCT, 70, Unit.PCT);
		layout.setWidgetTopBottom(content, 50, Unit.PX, 10, Unit.PX);

		layout.setHeight("100%");
		layout.setWidth("100%");
		RootLayoutPanel.get().add(layout);
	}

	public void hideUI() {
		RootLayoutPanel.get().setWidgetLeftWidth(layout, -10000, Unit.PX, 100,
				Unit.PCT);
		layout.setVisible(false);
	}

	public void showUI() {
		RootLayoutPanel.get().setWidgetLeftWidth(layout, 0, Unit.PX, 100,
				Unit.PCT);
		layout.setVisible(true);
	}

	private void createOracleTeam() {
		userLink.getTeamnames(Home.token, new AsyncCallback<Vector<String>>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(Vector<String> result) {
				Collections.sort(result);
				for (String name : result) {
					lbxGroup.addItem(name);
					availableGroups.add(name);
				}
				lbxGroup.setVisibleItemCount(1);
			}
		});
	}

	public void selectGroup(String group) {
		int index = availableGroups.indexOf(group);
		if (index >= 0 && index < availableGroups.size()) {
			lbxGroup.setSelectedIndex(index);
			btnLogin.SetEnable(true);
		}
	}

	/**
	 * Returns the entered group name from the TextBox.
	 * 
	 * @return String
	 */
	public String getGroupName() {
		return lbxGroup.getItemText(lbxGroup.getSelectedIndex());
	}

	/**
	 * Adds committed String to ListBox with group names.
	 * 
	 * @param groupname
	 */
	public void addGroupName(String group) {
		lbxGroup.addItem(group);
	}

}
