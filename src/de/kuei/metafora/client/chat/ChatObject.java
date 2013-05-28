package de.kuei.metafora.client.chat;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.chat.serverlink.ChatService;
import de.kuei.metafora.client.chat.serverlink.ChatServiceAsync;

public class ChatObject extends HTML implements ClickHandler, MouseOverHandler,
		MouseOutHandler {

	private ChatServiceAsync chatLink = GWT.create(ChatService.class);
	private Languages languages = GWT.create(Languages.class);

	private String url;
	private String urltext;
	private String user;
	private String msg;
	private String viewurl;
	private Frame popupFrame = null;
	private String time;

	public ChatObject(String msg) {
		this(msg, null);
	}

	@SuppressWarnings("deprecation")
	public ChatObject(String msg, String time) {
		String[] parts = msg.split("\n");
		if (parts.length < 4) {
			Window.alert("Invalid Chat Object.");
		} else {
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				if (part.startsWith("urltext:")) {
					urltext = part.substring(8, part.length());
				} else if (part.startsWith("url:")) {
					url = part.substring(4, part.length());
				} else if (part.startsWith("user:")) {
					user = part.substring(5, part.length());
				} else if (part.startsWith("viewurl:")) {
					viewurl = part.substring(8, part.length());
				}
			}
		}

		this.msg = msg;

		String text = "Object";
		if (urltext != null) {
			text = urltext;
		}

		if (time == null) {
			Date d = new Date();

			time = "";
			if (d.getHours() < 10) {
				time += "0" + d.getHours();
			} else {
				time += d.getHours();
			}

			time += ":";

			if (d.getMinutes() < 10) {
				time += "0" + d.getMinutes();
			} else {
				time += d.getMinutes();
			}
		}

		this.time = time;

		String html = "<p><span style=\"color: blue;\">(" + time + ") " + "<b>"
				+ user + ": </b>" + text + "</span></p>";
		super.setHTML(html);
		getElement().getStyle().setCursor(Cursor.POINTER);

		if ((viewurl != null) && (viewurl.length() > 0)) {
			popupFrame = new Frame(viewurl);
			RootPanel.get().add(popupFrame);
			popupFrame.getElement().getStyle().setBackgroundColor("white");
			popupFrame.setVisible(false);
			popupFrame.setSize("100px", "100px");
			popupFrame.getElement().getStyle().setZIndex(1000);
			addMouseOverHandler(this);
			addMouseOutHandler(this);
		}

		addClickHandler(this);
	}

	@Override
	public void onClick(ClickEvent event) {
		if (url != null && url.length() > 0) {
			String toolname = "Tool";
			String color = "";

			if (url.toLowerCase().contains("lasad")) {
				toolname = languages.Lasad();
				color = "blue";
			} else if (url.toLowerCase().contains("expresser")) {
				toolname = languages.Expresser();
				color = "green";
			} else if (url.toLowerCase().contains("suscity")) {
				toolname = languages.Suscity();
				color = "military";
			} else if (url.toLowerCase().contains("physt")) {
				toolname = languages.Jugger();
				color = "orange";
			} else if (url.toLowerCase().contains("planningtool")) {
				toolname = languages.PlanningTool();
				color = "yellow";
			} else if (url.toLowerCase().contains("piki")) {
				toolname = languages.Piki();
				color = "darkred";
			} else if (url.toLowerCase().contains("malt")) {
				toolname = languages.Math();
				color = "lightblue";
			}
			Home.lastFrame.addContentFrame(url, toolname, true, color, true,
					true);
		}

		chatLink.openChatObject(msg, Home.userName, Home.groupName,
				Home.challengeId, Home.challengeName, Home.token,
				new AsyncCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Open chat object failed!\nChatObject chatLink.openChatObject:\n"
								+ caught.getMessage());
					}
				});
	}

	@Override
	public void onMouseOver(MouseOverEvent event) {
		int x = event.getClientX();
		int y = event.getClientY();
		RootPanel.get().setWidgetPosition(popupFrame, x + 5, y + 5);
		popupFrame.setVisible(true);
	}

	@Override
	public void onMouseOut(MouseOutEvent event) {
		popupFrame.setVisible(false);
	}

	public String getTime() {
		return time;
	}
}
