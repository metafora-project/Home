package de.kuei.metafora.client.help;

import java.util.Vector;

import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextArea;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.feedback.FeedbackAwarenessCounter;
import de.kuei.metafora.client.server.HelpServerLink;
import de.kuei.metafora.client.server.HelpServerLinkAsync;

public class HelpDialog extends DialogBox implements ClickHandler {

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	private HelpServerLinkAsync helpLink = GWT.create(HelpServerLink.class);

	private TextArea textarea;
	private Vector<RadioButton> radioButtons;

	RadioButton askHelpFromGroup;
	RadioButton otherHelper;
	private Button cancel;

	public HelpDialog() {
		super();
		setAutoHideEnabled(false);
		setModal(true);
		setText(language.Help());

		getElement().getStyle().setBackgroundColor("#FFFF00");

		VerticalPanel content = new VerticalPanel();
		content.getElement().getStyle().setBackgroundColor("#FFFF00");

		if (FeedbackAwarenessCounter.getInstance().areThereUnreadMessages()) {
			FlowPanel panel = new FlowPanel();
			panel.getElement().getStyle().setBackgroundColor("#FFFF00");
			HTML html = new HTML("<b>" + language.ImportantFeedbackMessages()
					+ "</b>");
			panel.add(html);
			content.add(panel);
		}

		CaptionPanel askHelp = new CaptionPanel(language.AskHelpFrom() + " ");
		askHelp.getElement().getStyle().setBackgroundColor("#FFFF00");

		VerticalPanel askHelpList = new VerticalPanel();
		askHelpList.getElement().getStyle().setBackgroundColor("#FFFF00");

		askHelpFromGroup = new RadioButton("askHelp", language.GroupWithPopup());
		askHelpFromGroup.setValue(true);
		askHelpList.add(askHelpFromGroup);

		otherHelper = new RadioButton("askHelp", language.OtherHelper());
		askHelpList.add(otherHelper);

		askHelp.add(askHelpList);
		content.add(askHelp);

		CaptionPanel aboutWork = new CaptionPanel(language.AboutYourWorkIn());
		aboutWork.getElement().getStyle().setBackgroundColor("#FFFF00");

		Vector<String> openTabs = Home.lastFrame.getOpenTabs();
		String selectedTab = Home.lastFrame.getSelectedTab();

		radioButtons = new Vector<RadioButton>();

		boolean selected = false;

		VerticalPanel tablist = new VerticalPanel();
		tablist.getElement().getStyle().setBackgroundColor("#FFFF00");

		for (String openTab : openTabs) {
			String name = null;

			String ot = openTab.substring(0, openTab.indexOf('?'));

			if (ot.toLowerCase().contains("planning")) {
				name = language.PlanningTool();
			} else if (ot.toLowerCase().contains("workbench")) {
				name = language.Workbench();
			} else if (ot.toLowerCase().contains("lasad")) {
				name = language.Lasad();
			} else if (ot.toLowerCase().contains("suscity")) {
				name = language.Suscity();
			} else if (ot.toLowerCase().contains("web-expresser")) {
				name = language.Expresser();
			} else if (ot.toLowerCase().contains("physt")) {
				name = language.Jugger();
			} else if (ot.toLowerCase().contains("malt")) {
				name = language.Math();
			} else if (ot.toLowerCase().contains("challenge")) {
				name = language.Challenge();
			}

			if (name != null) {
				RadioButton rb = new RadioButton("tool", name);
				rb.setTitle(openTab);
				if (openTab.equals(selectedTab)) {
					rb.setValue(true);
					selected = true;
				}
				tablist.add(rb);
				radioButtons.add(rb);
			}
		}

		RadioButton rb = new RadioButton("tool", language.Other());
		if (!selected) {
			rb.setTitle(selectedTab);
			rb.setValue(true);
		}
		tablist.add(rb);
		radioButtons.add(rb);

		aboutWork.add(tablist);
		content.add(aboutWork);

		CaptionPanel why = new CaptionPanel(language.Why());
		why.getElement().getStyle().setBackgroundColor("#FFFF00");

		textarea = new TextArea();
		textarea.setWidth("40em");
		textarea.setHeight("8ex");
		why.add(textarea);
		content.add(why);

		HorizontalPanel hPan = new HorizontalPanel();
		hPan.setWidth("100%");
		Button send = new Button(language.askHelp());
		send.addClickHandler(this);
		hPan.add(send);

		cancel = new Button(language.cancel());
		cancel.addClickHandler(this);
		hPan.add(cancel);
		content.add(hPan);

		setWidget(content);
		this.setPopupPosition(0, 0);

	}

	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource().equals(cancel)) {
			hide();
		} else {
			String selected = Home.lastFrame.getSelectedTab();

			for (RadioButton rb : radioButtons) {
				if (rb.getValue()) {
					selected = rb.getTitle();
				}
			}

			if (textarea.getText().isEmpty()) {
				textarea.setText(Home.userName + " " + language.AsksForHelp());
			}

			boolean others = otherHelper.getValue();
			boolean group = askHelpFromGroup.getValue();

			helpLink.help(Home.userName, Home.groupName, Home.challengeId,
					Home.challengeName, textarea.getText(), selected,
					Home.lastFrame.getOpenTabs(), Home.token, group, others,
					new AsyncCallback<Void>() {

						@Override
						public void onSuccess(Void result) {
						}

						@Override
						public void onFailure(Throwable caught) {
							Window.alert(language.HelpRequestFailed()
									+ "\nHelpClickHandler helpLink.help():\n"
									+ caught.getMessage() + "\n"
									+ caught.getCause());
						}
					});
			hide();
		}
	}
}
