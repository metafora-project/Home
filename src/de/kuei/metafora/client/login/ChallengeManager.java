package de.kuei.metafora.client.login;

import java.util.Vector;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ColoredButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.handler.ChallengeHandler;
import de.kuei.metafora.client.login.handler.server.LoginService;
import de.kuei.metafora.client.login.handler.server.LoginServiceAsync;
import de.kuei.metafora.client.team.Logout;

public class ChallengeManager implements ClickHandler {

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	private static ChallengeManager challengeManager = null;

	private LoginServiceAsync loginService = GWT.create(LoginService.class);

	/**
	 * Returns the only existing instance of ChallengeManager.
	 * 
	 * @return ChallengeManager instance
	 */
	public static ChallengeManager getInstance() {
		if (challengeManager == null) {
			challengeManager = new ChallengeManager();
		}
		return challengeManager;
	}

	private ListBox lbxChallenge;
	private Button btnSelect;
	private Button btnBack;
	private Frame fChallenge;
	private Logout logout;

	private Vector<String> availableChallenges;

	private LayoutPanel layout;

	/**
	 * Executes ChallengeManager.
	 */
	public void main() {
		availableChallenges = new Vector<String>();

		RecentChallengesGui recent = new RecentChallengesGui();

		final ChallengeHandler handler = new ChallengeHandler();

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

		HTML group = new HTML(language.Group() + ": " + Home.groupName);
		layout.add(group);
		layout.setWidgetLeftWidth(group, 60, Unit.PCT, 250, Unit.PX);
		layout.setWidgetTopHeight(group, 17, Unit.PX, 17, Unit.PX);

		LayoutPanel content = new LayoutPanel();
		content.getElement().getStyle().setBorderWidth(1, Unit.PX);
		content.getElement().getStyle().setBorderColor("#AABBCC");
		content.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);

		HTML title = new HTML(
				"<div style='font-size: 14pt; text-align: center;'>"
						+ language.ChallengeTitle() + "</div>");
		content.add(title);
		content.setWidgetLeftRight(title, 5, Unit.PX, 5, Unit.PX);
		content.setWidgetTopHeight(title, 5, Unit.PX, 70, Unit.PX);

		LayoutPanel leftSide = new LayoutPanel();
		content.add(leftSide);
		content.setWidgetTopBottom(leftSide, 90, Unit.PX, 50, Unit.PX);
		content.setWidgetLeftWidth(leftSide, 0, Unit.PX, 50, Unit.PCT);

		HTML challengeTitle = new HTML("<b>" + language.Challenge() + ":</b>");
		leftSide.add(challengeTitle);
		leftSide.setWidgetTopHeight(challengeTitle, 0, Unit.PX, 25, Unit.PX);
		leftSide.setWidgetLeftRight(challengeTitle, 10, Unit.PX, 10, Unit.PX);

		lbxChallenge = new ListBox();
		loadChallengeItems();
		lbxChallenge.addKeyPressHandler(handler);
		lbxChallenge.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				btnSelect.enable();
			}
		});
		lbxChallenge.addClickHandler(this);
		lbxChallenge.setVisibleItemCount(calcItemCount(20));
		lbxChallenge.setWidth("95%");
		leftSide.add(lbxChallenge);
		leftSide.setWidgetLeftRight(lbxChallenge, 10, Unit.PX, 10, Unit.PX);
		leftSide.setWidgetTopHeight(lbxChallenge, 30, Unit.PX, 150, Unit.PX);

		leftSide.add(recent);
		leftSide.setWidgetLeftRight(recent, 10, Unit.PX, 10, Unit.PX);
		leftSide.setWidgetTopBottom(recent, 190, Unit.PX, 0, Unit.PX);

		fChallenge = new Frame("");
		fChallenge.setHeight("98%");
		fChallenge.setWidth("98%");
		content.add(fChallenge);
		content.setWidgetLeftRight(fChallenge, 50, Unit.PCT, 10, Unit.PX);
		content.setWidgetTopBottom(fChallenge, 120, Unit.PX, 50, Unit.PX);

		btnSelect = new ColoredButton(language.Continue().toUpperCase() + " >",
				"green");
		btnSelect.setWidth("200px");
		btnSelect.disable();
		Listener<ButtonEvent> selectListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				handler.onClick(null);
			}
		};
		btnSelect.addListener(Events.OnMouseUp, selectListener);

		content.add(btnSelect);
		btnSelect.setWidth("100%");
		content.setWidgetBottomHeight(btnSelect, 10, Unit.PX, 30, Unit.PX);
		content.setWidgetLeftWidth(btnSelect, 60, Unit.PCT, 30, Unit.PCT);

		btnBack = new ColoredButton("< " + language.Back().toUpperCase(),
				"green");
		btnBack.setWidth("100%");
		content.add(btnBack);
		content.setWidgetBottomHeight(btnBack, 10, Unit.PX, 30, Unit.PX);
		content.setWidgetLeftWidth(btnBack, 10, Unit.PCT, 30, Unit.PCT);

		Listener<ButtonEvent> logoutListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				hideUI();
				GroupManager.getInstance().showUI();
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

	private int calcItemCount(int count) {
		if (count < 8) {
			return 8;
		} else if (count > 20) {
			return 20;
		} else {
			return count;
		}
	}

	/**
	 * Returns selected Challenge.
	 * 
	 * @return String
	 */
	public String getChallenge() {
		return lbxChallenge.getItemText(lbxChallenge.getSelectedIndex());
	}

	/**
	 * Returns the selected ChallengeId.
	 * 
	 * @return Int
	 */
	public int getChallengeId() {
		return lbxChallenge.getSelectedIndex();
	}

	/**
	 * Hides UI. Sets the visibility of the ChallengeManager UI to false.
	 */
	public void hideUI() {
		RootLayoutPanel.get().setWidgetLeftWidth(layout, -10000, Unit.PX, 100,
				Unit.PCT);
		layout.setVisible(false);
	}

	@Override
	public void onClick(ClickEvent event) {
		String challenge = lbxChallenge.getItemText(lbxChallenge
				.getSelectedIndex());
		btnSelect.enable();

		loginService.getChallengeUrl(challenge, Home.token,
				new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(language.TheChallengeDescriptionCanNotBeShown()+"\nChallengeManager loginService.getChallengeUrl():\n"
								+ caught.getMessage()
								+ "\n"
								+ caught.getCause());
					}

					@Override
					public void onSuccess(String result) {
						fChallenge.setUrl(result);
					}
				});

	}

	private void loadChallengeItems() {
		loginService.getChallenges(Home.token,
				new AsyncCallback<Vector<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(language.TheGroupnameCoudNotBeSet()+"\nSetGroupNameHandler userLink.setTeamname():\n"
								+ caught.getMessage()
								+ "\n"
								+ caught.getCause());
					}

					@Override
					public void onSuccess(Vector<String> result) {
						for (int i = 0; i < result.size(); i++) {
							lbxChallenge.addItem(result.get(i));
							availableChallenges.add(result.get(i));
						}
					}
				});
	}

	public void selectChallenge(String challenge) {
		int index = availableChallenges.indexOf(challenge);
		if (index >= 0 && index < availableChallenges.size()) {
			lbxChallenge.setSelectedIndex(index);
			btnSelect.setEnabled(true);

			loginService.getChallengeUrl(challenge, Home.token,
					new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							Window.alert(language.TheChallengeDescriptionCanNotBeShown()+"\nChallengeManager loginService.getChallengeUrl():\n"
									+ caught.getMessage()
									+ "\n"
									+ caught.getCause());
						}

						@Override
						public void onSuccess(String result) {
							fChallenge.setUrl(result);
						}
					});
		}
	}
}
