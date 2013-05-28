package de.kuei.metafora.client;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.google.gwt.user.client.Cookies;

public class TabCloseListener implements Listener<TabPanelEvent> {

	private String cookieName;

	public TabCloseListener(String cookieName) {
		this.cookieName = cookieName;
	}

	@Override
	public void handleEvent(TabPanelEvent be) {
		Cookies.removeCookie(cookieName);

		TabItem item = be.getItem();
		Home.lastFrame.tabClosed(item);
	}

}
