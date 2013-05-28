package de.kuei.metafora.client.chat;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.chat.serverlink.ChatService;
import de.kuei.metafora.client.chat.serverlink.ChatServiceAsync;
import de.kuei.metafora.client.util.InputFilter;

public class ChatList extends LayoutPanel implements ClickHandler,
		KeyPressHandler {

	private static Logger logger = Logger.getLogger("Home.ChatList");

	public static ChatList instance = null;

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	private static final int messageCount = 100;

	private Vector<Widget> chatEntries;
	private TextBox box;
	private Button send;
	private VerticalPanel messages;

	private ChatServiceAsync chatLink = GWT.create(ChatService.class);

	private int cookieCount = 0;

	private ScrollPanel scroll;

	public ChatList() {
		ChatList.instance = this;

		setWidth("100%");
		setHeight("100%");

		messages = new VerticalPanel();
		messages.setWidth("100%");
		messages.setHeight("100%");
		scroll = new ScrollPanel(messages);
		add(scroll);
		setWidgetBottomHeight(scroll, 40, Unit.PX, 40, Unit.PX);
		setWidgetLeftRight(scroll, 0, Unit.PX, 0, Unit.PX);

		chatEntries = new Vector<Widget>();

		box = new TextBox();
		box.addKeyPressHandler(this);
		box.addKeyPressHandler(new InputFilter(box));
		box.setWidth("95%");
		add(box);
		setWidgetLeftRight(box, 5, Unit.PX, 50, Unit.PX);
		setWidgetBottomHeight(box, 5, Unit.PX, 25, Unit.PX);

		if (logger.getLevel().equals(Level.INFO)) {
			box.addKeyPressHandler(new KeyPressHandler() {

				@Override
				public void onKeyPress(KeyPressEvent event) {
					logger.log(Level.INFO, Home.token
							+ " Home.ChatList: Key pressed. New box content: "
							+ box.getText());
				}
			});
		}

		send = new Button(language.Send());
		send.addClickHandler(this);
		send.setWidth("100%");
		send.setHeight("100%");
		add(send);
		setWidgetRightWidth(send, 5, Unit.PX, 40, Unit.PX);
		setWidgetBottomHeight(send, 5, Unit.PX, 25, Unit.PX);

		while (Cookies.getCookie("metaforachat" + cookieCount) != null) {
			String text = Cookies.getCookie("metaforachat" + cookieCount);

			if (text.startsWith("o")) {
				text = text.substring(1, text.length());
				int pos = text.indexOf("&");
				if (pos > 0) {
					String time = text.substring(0, pos);
					String message = text.substring(pos + 1, text.length());
					ChatObject object = new ChatObject(message, time);
					chatObject(object, message);
				} else {
					chatObject(text);
				}
			} else {
				int pos = text.indexOf("&");
				if (pos > 0) {
					String time = text.substring(0, pos);
					String message = text.substring(pos + 1, text.length());
					chatMessageTime(message, time);
				} else {
					chatMessage(text);
				}
			}
		}
		layoutMessages();
	}

	public void chatObject(String text) {
		ChatObject obj = new ChatObject(text);
		chatObject(obj, text);
	}

	public void chatObject(ChatObject obj, String text) {
		messages.add(obj);
		chatEntries.add(obj);

		scroll.setVerticalScrollPosition(messages.getOffsetHeight());

		layoutMessages();

		while (chatEntries.size() > messageCount) {
			Widget widget = chatEntries.firstElement();
			chatEntries.remove(widget);
			messages.remove(widget);
		}

		ChatAwarenessCounter.getInstance().increaseCount();

		Cookies.setCookie("metaforachat" + cookieCount, "o" + obj.getTime()
				+ "&" + text);
		cookieCount++;
	}

	@SuppressWarnings("deprecation")
	public void chatMessage(String text) {
		Date d = new Date();

		String time = "";
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

		chatMessageTime(text, time);
	}

	public void chatMessageTime(String text, String time) {
		String[] split = text.split(": ");
		String usr = split[0];
		String message = split[1];
		if (split[0] == null) {
			if (Home.userName != null)
				usr = Home.userName;
			else
				usr = Home.groupName;
		}

		if (split[1] == null) {
			message = "";
		}

		String html = "<p>(" + time + ") " + "<b>" + usr + ": </b>" + message
				+ "</p>";
		HTML htmlElement = new HTML(html);

		messages.add(htmlElement);
		chatEntries.add(htmlElement);

		scroll.setVerticalScrollPosition(messages.getOffsetHeight());

		layoutMessages();

		while (chatEntries.size() > messageCount) {
			Widget widget = chatEntries.firstElement();
			chatEntries.remove(widget);
			messages.remove(widget);
		}

		ChatAwarenessCounter.getInstance().increaseCount();

		Cookies.setCookie("metaforachat" + cookieCount, time + "&" + text);
		cookieCount++;
	}

	public void layoutMessages() {
		if (messages.getAbsoluteTop() > 5) {
			if (getOffsetHeight() - 45 - messages.getOffsetHeight() > 0) {
				setWidgetBottomHeight(scroll, 40, Unit.PX,
						messages.getOffsetHeight() + 5, Unit.PX);

			} else {
				setWidgetTopBottom(scroll, 1, Unit.PX, 40, Unit.PX);
				scroll.setVerticalScrollPosition(messages.getOffsetHeight());
			}
		}
	}

	private void sendChatMessage() {
		// send as chat message
		chatLink.sendChatMessage(box.getText(), Home.challengeId,
				Home.challengeName, Home.token, new AsyncCallback<Void>() {

					@Override
					public void onSuccess(Void result) {
					}

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(language.ChatMessageCoudNotBeAdded()+"\nChatList chatLink.sendChatMessage():\n"
								+ caught.getMessage());
					}
				});
		box.setText("");
	}

	/**
	 * Sends text in Textbox to chat when clicking "send" button.
	 */
	@Override
	public void onClick(ClickEvent event) {
		sendChatMessage();
	}

	/**
	 * Sends text in Textbox to chat when pressing "enter".
	 */
	@Override
	public void onKeyPress(KeyPressEvent event) {
		if (((int) event.getCharCode()) == 13
				|| (((int) event.getCharCode()) == 0 && ((int) event
						.getNativeEvent().getKeyCode()) == 13)) {
			sendChatMessage();
		}
	}
}
