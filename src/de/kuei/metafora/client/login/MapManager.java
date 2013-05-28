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
import de.kuei.metafora.client.login.handler.MapHandler;
import de.kuei.metafora.client.login.handler.NewMapHandler;
import de.kuei.metafora.client.login.handler.server.LoginService;
import de.kuei.metafora.client.login.handler.server.LoginServiceAsync;
import de.kuei.metafora.client.team.Logout;

public class MapManager implements ClickHandler {

	// i18n 
	final static Languages language = GWT.create(Languages.class);

	private static MapManager mapManager = null;

	/**
	 * Returns the only existing instance of MapManager.
	 * 
	 * @return MapManager instance
	 */
	public static MapManager getInstance() {
		if (mapManager == null) {
			mapManager = new MapManager();
		}
		return mapManager;
	}

	private LoginServiceAsync mapService = GWT.create(LoginService.class);

	private Frame fMap;
	private ListBox lbxMap;
	private Button btnSelect;
	private Button btnBack;
	private Button btnNewMap;
	private Logout logout;

	private Vector<String> availableMaps;

	private LayoutPanel layout;

	/**
	 * Executes MapManager.
	 */
	public void main() {
		availableMaps = new Vector<String>();

		final MapHandler handler = new MapHandler();

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

		HTML challenge = new HTML(language.Challenge() + ": "
				+ Home.challengeName);
		layout.add(challenge);
		layout.setWidgetLeftWidth(challenge, 60, Unit.PCT, 250, Unit.PX);
		layout.setWidgetTopHeight(challenge, 34, Unit.PX, 17, Unit.PX);

		LayoutPanel content = new LayoutPanel();
		content.getElement().getStyle().setBorderWidth(1, Unit.PX);
		content.getElement().getStyle().setBorderColor("#AABBCC");
		content.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);

		HTML title = new HTML(
				"<div style='font-size: 14pt; text-align: center;'>"
						+ language.MapTitle() + "</div>");
		content.add(title);
		content.setWidgetLeftRight(title, 5, Unit.PX, 5, Unit.PX);
		content.setWidgetTopHeight(title, 5, Unit.PX, 70, Unit.PX);

		HTML challengeTitle = new HTML(language.Map() + ":");
		content.add(challengeTitle);
		content.setWidgetTopHeight(challengeTitle, 90, Unit.PX, 25, Unit.PX);
		content.setWidgetLeftWidth(challengeTitle, 10, Unit.PX, 45, Unit.PCT);

		lbxMap = new ListBox();
		lbxMap.setWidth("95%");
		lbxMap.setVisibleItemCount(20);
		lbxMap.addKeyPressHandler(handler);
		lbxMap.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				btnSelect.enable();
			}
		});
		lbxMap.addClickHandler(this);

		content.add(lbxMap);
		content.setWidgetLeftWidth(lbxMap, 10, Unit.PX, 45, Unit.PCT);
		content.setWidgetTopBottom(lbxMap, 120, Unit.PX, 85, Unit.PX);

		btnNewMap = new ColoredButton(language.NewMap(), "orange");
		Listener<ButtonEvent> mapListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				new NewMapHandler().onClick(null);
			}
		};
		btnNewMap.addListener(Events.OnMouseUp, mapListener);
		content.add(btnNewMap);
		content.setWidgetLeftWidth(btnNewMap, 10, Unit.PX, 45, Unit.PCT);
		content.setWidgetBottomHeight(btnNewMap, 50, Unit.PX, 30, Unit.PX);

		mapService.getMapnames(Home.token, Home.userName, Home.groupName,
				false, new AsyncCallback<Vector<String>>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("MapManager.main(): " + caught.toString());
					}

					@Override
					public void onSuccess(Vector<String> result) {
						for (int i = 0; i < result.size(); i++) {
							lbxMap.addItem(result.get(i));
							availableMaps.add(result.get(i));
						}
						lbxMap.setVisibleItemCount(20);
						lbxMap.setSelectedIndex(0);
					}

				});

		fMap = new Frame(
				"http://metafora.ku-eichstaett.de/images/resources/PlanningTool");
		fMap.setHeight("98%");
		fMap.setWidth("98%");

		RecentMapsGui recent = new RecentMapsGui();
		content.add(recent);
		content.setWidgetLeftRight(recent, 50, Unit.PCT, 10, Unit.PX);
		content.setWidgetTopBottom(recent, 120, Unit.PX, 50, Unit.PX);

		btnSelect = new ColoredButton(language.Continue() + " >", "green");
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
				ChallengeManager.getInstance().main();
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

	/**
	 * Returns selected Map.
	 * 
	 * @return String
	 */
	public String getMap() {
		return lbxMap.getItemText(lbxMap.getSelectedIndex());
	}

	/**
	 * Hides UI of MapManager.
	 */
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

	@Override
	public void onClick(ClickEvent event) {
		btnSelect.enable();
	}

	public void selectMap(String map) {
		int index = availableMaps.indexOf(map);
		if (index >= 0 && index < availableMaps.size()) {
			lbxMap.setSelectedIndex(index);
			btnSelect.setEnabled(true);
		}else{
			lbxMap.addItem(map);
			availableMaps.add(map);
			index = availableMaps.indexOf(map);
			lbxMap.setSelectedIndex(index);
			btnSelect.setEnabled(true);
		}
	}
}
