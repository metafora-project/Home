package de.kuei.metafora.client.login.handler;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.MapManager;
import de.kuei.metafora.client.login.handler.server.LoginService;
import de.kuei.metafora.client.login.handler.server.LoginServiceAsync;

public class MapHandler implements ClickHandler, KeyPressHandler {

	final static Languages language = GWT.create(Languages.class);
	private LoginServiceAsync loginService = GWT.create(LoginService.class);

	private MapManager mapManager;
	private boolean processing = false;

	public MapHandler() {
		mapManager = MapManager.getInstance();
	}

	@Override
	public void onClick(ClickEvent event) {
		handleEvent();
	}

	private void handleEvent() {
		if (!processing) {
			String mapname = mapManager.getMap();

			if (mapname.length() > 1) {
				// open selected Map
				Home.mapName = mapname;

				Window.setStatus("Map: " + mapname + " (" + Home.mapName + ")");

				Date now = new Date();
				long nowLong = now.getTime();
				nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);// seven days
				now.setTime(nowLong);

				Cookies.setCookie("metaforaMap", Home.mapName, now);
				processing = true;

				MapManager.getInstance().hideUI();
				Home.lastFrame.main();

				loginService.updateLoginData(Home.token, Home.userName,
						Home.groupName, Home.challengeId, mapname,
						new AsyncCallback<Void>() {

							@Override
							public void onSuccess(Void result) {
							}

							@Override
							public void onFailure(Throwable caught) {
							}
						});
			} else {
				Window.alert(language.PleaseSelectAMap());
			}
		}
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		if (((int) event.getCharCode()) == 13
				|| (((int) event.getCharCode()) == 0 && ((int) event
						.getNativeEvent().getKeyCode()) == 13)) {
			handleEvent();
		}
	}
}
