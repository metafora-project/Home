package de.kuei.metafora.client.feedback;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

import de.kuei.metafora.client.Home;

public class FeedbackAwarenessCounter implements Listener<ComponentEvent> {

	private static FeedbackAwarenessCounter instance = null;

	private static Logger logger = Logger
			.getLogger("Home.FeedbackAwarenesCounter");

	public static FeedbackAwarenessCounter getInstance() {
		if (instance == null) {
			instance = new FeedbackAwarenessCounter();
		}
		return instance;
	}

	private ContentPanel feedbackPanel = null;
	private int count = 0;
	private boolean isInitialized = false;
	private String title = "";

	public boolean highlighted = false;

	private boolean enabled = true;

	private FeedbackAwarenessCounter() {
		logger.setLevel(Level.WARNING);
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean areThereUnreadMessages() {
		return (count > 0);
	}

	public void setPanel(ContentPanel chatPanel, String title) {
		this.feedbackPanel = chatPanel;
		this.title = title;
	}

	public void increaseCount() {
		if (enabled) {
			if (feedbackPanel != null && !feedbackPanel.isExpanded()) {
				count++;
				updateHeader();
				logger.log(
						Level.INFO,
						Home.token
								+ " Home.FeedbackAwarenessCounter: increaseCount(): New count "
								+ count);
			}
		} else {
			count = 0;
		}
		if (isInitialized) {
			Cookies.setCookie("metaforafeedbackcount", "" + count);
		}
	}

	@Override
	public void handleEvent(ComponentEvent be) {
		count = 0;
		Cookies.setCookie("metaforafeedbackcount", "" + count);
		updateHeader();
		logger.log(
				Level.INFO,
				Home.token
						+ " Home.FeedbackAwarenessCounter: handleEvent(): Panel opened, reset count.");
	}

	private void updateHeader() {
		if (feedbackPanel != null) {
			if (count == 0) {
				feedbackPanel.setHeading(title);
			} else {
				String headText = title + " <span>[" + count + "]</span>";

				String arrow = "<img src='arrow.gif' alt='' align='top'>";

				int arrowNum = (count > 2) ? 2 : count;
				for (int i = 0; i < arrowNum; i++) {
					headText += arrow;
				}

				feedbackPanel.setHeading(headText);
			}
		}
	}

	public void initCounter() {
		if (!isInitialized) {
			if (Cookies.getCookie("metaforafeedbackcount") != null) {
				count = Integer.parseInt(Cookies
						.getCookie("metaforafeedbackcount"));
			}
			isInitialized = true;
			updateHeader();
		}
	}
}
