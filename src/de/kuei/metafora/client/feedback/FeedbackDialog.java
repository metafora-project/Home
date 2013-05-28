package de.kuei.metafora.client.feedback;

import com.extjs.gxt.ui.client.widget.ColoredDialog;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;

import de.kuei.metafora.client.Home;

public class FeedbackDialog extends ColoredDialog implements ClickHandler {

	private Timer timer;
	private boolean highPriority = false;

	public FeedbackDialog(String text, boolean highPriority) {
		super("lightblue");

		this.highPriority = highPriority;

		HTML html;
		if (highPriority) {
			setWidth("225px");
			setModal(true);
			setClosable(false);
			setHideOnButtonClick(true);
			setButtons(Dialog.OK);

			html = new HTML(
					"<br/><table border='0' align='center' width='215' height='100%' cellpadding='10'><tr>"
							+ "<td width='60' height='40' align='center' valign='middle'>"
							+ "<img src='exclamation_mark.png' alt='!'/>"
							+ "</td>"
							+ "<td height='100%' align='center' valign='top' style='font-size: 12pt;'>"
							+ "<b>" + text + "</b></td></tr></table>");
		} else {
			setWidth("150px");
			setClosable(true);
			setHideOnButtonClick(true);
			setHeaderVisible(true);
			setBodyBorder(false);
			setModal(false);
			getButtonById("ok").hide();

			html = new HTML(
					"<table width='100%' height='100%' cellpadding='4'><tr><td width='100%' height='100%' align='center' valign='middle' style='font-size: 10pt;'>"
							+ text + "</td></tr></table>");
		}

		add(html);

		if (!highPriority) {
			timer = new Timer() {

				@Override
				public void run() {
					Home.lastFrame
							.hideLowInteruptiveMessage(FeedbackDialog.this);
				}
			};

			timer.schedule(30000);
		}

	}

	@Override
	public void onClick(ClickEvent event) {
		if (highPriority) {
			hide();
		} else {
			Home.lastFrame.hideLowInteruptiveMessage(FeedbackDialog.this);
		}
	}

}