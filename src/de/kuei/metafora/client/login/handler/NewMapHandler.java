package de.kuei.metafora.client.login.handler;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.MapManager;
import de.kuei.metafora.client.util.InputFilter;

public class NewMapHandler implements ClickHandler {

	final static Languages language = GWT.create(Languages.class);
	
	public NewMapHandler() {
	}

	@Override
	public void onClick(ClickEvent event) {
		String mapname = Window.prompt(
				language.PleaseEnterTheNameOfYourNewMap(),
				language.PlanningToolMapName());

		if ((mapname != null) && mapname.length() > 0) {
			mapname = InputFilter.filterString(mapname);

			Home.mapName = mapname;
			Window.setStatus("Map: " + mapname + " (" + Home.mapName + ")");

			Date now = new Date();
			long nowLong = now.getTime();
			nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);// seven days
			now.setTime(nowLong);

			Cookies.setCookie("metaforaMap", Home.mapName, now);

			MapManager.getInstance().hideUI();
			Home.lastFrame.main();
		} else {
			Window.alert(language.CreatingANewMapFailed());
		}
	}
}
