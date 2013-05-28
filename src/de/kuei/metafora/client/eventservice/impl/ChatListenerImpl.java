package de.kuei.metafora.client.eventservice.impl;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.chat.ChatList;
import de.kuei.metafora.client.eventservice.ChatListener;
import de.kuei.metafora.client.feedback.FeedbackDialog;
import de.kuei.metafora.client.feedback.FeedbackList;
import de.kuei.metafora.client.feedback.server.FeedbackService;
import de.kuei.metafora.client.feedback.server.FeedbackServiceAsync;
import de.kuei.metafora.shared.eventservice.events.ChatEvent;
import de.kuei.metafora.shared.eventservice.events.ChatObjectEvent;
import de.kuei.metafora.shared.eventservice.events.FeedbackEvent;
import de.kuei.metafora.shared.eventservice.events.HelpEvent;
import de.novanic.eventservice.client.event.Event;

public class ChatListenerImpl implements ChatListener {

	private static Logger logger = Logger.getLogger("Home.ChatListenerImpl");
	private static final FeedbackServiceAsync feedbackService = GWT
			.create(FeedbackService.class);

	final static Languages language = GWT.create(Languages.class);

	public ChatListenerImpl() {
		logger.setLevel(Level.WARNING);
	}

	@Override
	public void apply(Event anEvent) {
		if (anEvent instanceof ChatEvent) {
			chatEvent((ChatEvent) anEvent);
		} else if (anEvent instanceof ChatObjectEvent) {
			chatObjectEvent((ChatObjectEvent) anEvent);
		} else if (anEvent instanceof FeedbackEvent) {
			feedbackEvent((FeedbackEvent) anEvent);
		} else if (anEvent instanceof HelpEvent) {
			helpEvent((HelpEvent) anEvent);
		}
	}

	@Override
	public void chatEvent(ChatEvent event) {
		if (event.getGroup().equals(Home.groupName)) {
			String chatmsg = event.getName() + ": " + event.getMessage();
			if (ChatList.instance != null) {
				ChatList.instance.chatMessage(chatmsg);

				logger.log(Level.INFO, Home.token
						+ " Home.ChatListenerImpl: New chat message: "
						+ chatmsg);
			}
		}
	}

	@Override
	public void chatObjectEvent(ChatObjectEvent event) {
		if (event.getGroup().equals(Home.groupName)) {

			String msg = "";
			if (event.getText() != null) {
				msg += "urltext:" + event.getText();
			}
			if (event.getUrl() != null) {
				msg += "\nurl:" + event.getUrl();
			}

			if (event.getTool() != null) {
				msg += "\ntool:" + event.getTool();
			}
			if (event.getUser() != null) {
				msg += "\nuser:" + event.getUser();
			}
			if ((event.getViewUrl() != null)
					&& (event.getViewUrl().length() > 0)) {
				msg += "\nviewurl:" + event.getViewUrl();
			}

			if (ChatList.instance != null) {
				ChatList.instance.chatObject(msg);

				logger.log(Level.INFO,
						Home.token
								+ " Home.ChatListenerImpl: New chat object: "
								+ event.getUser() + ": " + event.getUrl()
								+ ", " + event.getText());
			}
		}
	}

	@Override
	public void feedbackEvent(FeedbackEvent event) {
		if (event.getToken().equals(Home.token)) {

			String alert = event.getMessage();
			String priority = event.getInterruption();

			String htmlstart = "<span style='color: #007000; '>";
			String htmlend = "</span>";

			if (priority.equals("HIGH_INTERRUPTION")) {
				htmlstart = "<span style='color: #CC0000;'>";
				htmlend = "</span>";
			} else if (priority.equals("LOW_INTERRUPTION")) {
				htmlstart = "<span style='color: #A08000;'>";
				htmlend = "</span>";
			}

			if (FeedbackList.instance != null) {
				FeedbackList.instance.feedbackMessage(htmlstart + alert
						+ htmlend);

				logger.log(Level.INFO, Home.token
						+ " Home.ChatListenerImpl: New feedback message: "
						+ alert);
			} else {
				logger.log(Level.INFO, Home.token
						+ " Home.ChatListenerImpl: Feedback list ist null!");
			}

			String openedTool = Home.lastFrame.getSelectedTabTitle();
			if ((openedTool.equals(language.Suscity()))
					|| (openedTool.equals(language.Math()))
					|| (openedTool.equals(language.Jugger()))
					|| (openedTool.equals(language.Piki()))) {

				logger.log(
						Level.INFO,
						Home.token
								+ " Home.ChatListenerImpl: Forward feedback message to tool "
								+ openedTool);

				feedbackService.sendFeedbackMessage(alert, Home.userName,
						openedTool, priority, new AsyncCallback<Void>() {
							@Override
							public void onFailure(Throwable caught) {
							}

							@Override
							public void onSuccess(Void result) {
							}
						});
			} else {
				if (priority.equals("HIGH_INTERRUPTION")) {
					Home.lastFrame.showFeedbackMessage(alert, true);
				} else if (priority.equals("LOW_INTERRUPTION")) {
					Home.lastFrame.showFeedbackMessage(alert, false);
				}
			}
		}
	}

	@Override
	public void helpEvent(HelpEvent event) {
		if (event.getGroup().equals(Home.groupName)) {
			FeedbackDialog dialog = new FeedbackDialog(event.getName() + ":\n "
					+ event.getMessage(), true);
			dialog.setPosition(0, 0);
			dialog.setTitle(language.HelpMessage());
			dialog.show();

			logger.log(Level.INFO,
					Home.token + " Home.ChatListenerImpl: New help request: "
							+ event.getName() + ": " + event.getMessage());
		}
	}
}
