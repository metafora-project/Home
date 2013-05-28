package de.kuei.metafora.client.chat;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.kuei.metafora.client.Home;

public class ChatAwarenessCounter implements Listener<ComponentEvent> {

	private static ChatAwarenessCounter instance = null;

	private static Logger logger = Logger.getLogger("Home.ChatAwarenesCounter");

	public static ChatAwarenessCounter getInstance() {
		if (instance == null) {
			instance = new ChatAwarenessCounter();
		}
		return instance;
	}

	private ContentPanel chatPanel = null;
	private int count = 0;
	private String title = "";

	private boolean enabled = true;

	private ChatAwarenessCounter() {
		logger.setLevel(Level.WARNING);
	}

	public void setPanel(ContentPanel chatPanel, String title) {
		this.chatPanel = chatPanel;
		this.title = title;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void increaseCount() {
		if (enabled) {
			if (chatPanel != null && !chatPanel.isExpanded()) {
				count++;
				updateHeader();
				logger.log(
						Level.INFO,
						Home.token
								+ " Home.ChatAwarenessCounter: increaseCount(): New count "
								+ count);
			}
		} else {
			count = 0;
		}
	}

	@Override
	public void handleEvent(ComponentEvent be) {
		count = 0;
		updateHeader();
		logger.log(
				Level.INFO,
				Home.token
						+ " Home.ChatAwarenessCounter: handleEvent(): Panel opened, reset count.");
	}

	private void updateHeader() {
		if (chatPanel != null) {
			if (count == 0) {
				chatPanel.setHeading(title);
			} else {
				String headText = title + " <span>[" + count + "]</span>";
				String arrow = "<img src='arrow.gif' alt='' align='top'>";
				int arrowNum = (count > 2) ? 2 : count;
				for (int i = 0; i < arrowNum; i++) {
					headText += arrow;
				}

				chatPanel.setHeading(headText);
			}
		}
	}

}
