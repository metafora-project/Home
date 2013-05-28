package de.kuei.metafora.client.feedback;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;

public class FeedbackList extends VerticalPanel {

	// i18n
	final static Languages language = GWT.create(Languages.class);
	private static Logger logger = Logger.getLogger("Home.FeedbackList");

	public static FeedbackList instance = null;

	private Vector<Widget> chatEntries;
	private VerticalPanel messages;

	private int cookieCount = 0;

	public FeedbackList() {
		FeedbackList.instance = this;

		messages = new VerticalPanel();
		add(messages);

		chatEntries = new Vector<Widget>();

		while (Cookies.getCookie("metaforafeedback" + cookieCount) != null) {
			String text = Cookies.getCookie("metaforafeedback" + cookieCount);
			feedbackMessage(text);
		}
	}

	public void feedbackMessage(String text) {
		logger.log(Level.INFO, Home.token
				+ " Home.FeedbackList: New feedback message: " + text);

		Date d = new Date();

		String html = "<p>(" + d.getHours() + ":"
				+ (d.getMinutes() < 10 ? "0" + d.getMinutes() : d.getMinutes())
				+ ") " + text + "</p>";

		HTML htmlElement = new HTML(html);
		htmlElement.getElement().getStyle().setBorderWidth(1.0, Unit.PX);
		htmlElement.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);
		htmlElement.getElement().getStyle().setBorderColor("#0076b0");

		messages.add(htmlElement);
		chatEntries.add(htmlElement);

		FeedbackAwarenessCounter.getInstance().increaseCount();

		Cookies.setCookie("metaforafeedback" + cookieCount, text);
		cookieCount++;
	}
}
