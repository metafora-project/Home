package de.kuei.metafora.client.login;

import java.util.Date;
import java.util.Vector;

import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.shared.HistoryData;

public class RecentChallengesGui extends VerticalPanel implements ChangeHandler {
	// i18n 
	final static Languages language = GWT.create(Languages.class);
	private Vector<String> challenges;
	private ListBox recentChallenges;

	public RecentChallengesGui() {
		super();

		challenges = new Vector<String>();

		add(new HTML("<b>"+language.YourRecentChallenges()+"</b>"));

		recentChallenges = new ListBox();
		recentChallenges.setVisibleItemCount(5);
		recentChallenges.addChangeHandler(this);
		add(new ScrollPanel(recentChallenges));

		buildGui();
	}

	private void buildGui() {

		Vector<HistoryData> history = GroupManager.getInstance().getHistory();

		if (history != null) {
			for (HistoryData historyData : history) {
				String challengeName = historyData.getChallengeName();
				challenges.add(challengeName);

				String listEntry = challengeName + " (";

				Date time = historyData.getTime();
				String date = DateTimeFormat.getFormat("dd.MM.yy").format(time);
				listEntry += date + ", ";

				for (String map : historyData.getMaps()) {
					listEntry += map + ", ";
				}
				listEntry = listEntry.substring(0, listEntry.length() - 2);
				listEntry += ")";

				recentChallenges.addItem(listEntry);
			}
		} else {
			recentChallenges.setVisible(false);
		}
	}

	@Override
	public void onChange(ChangeEvent event) {
		int index = recentChallenges.getSelectedIndex();
		String challenge = challenges.get(index);
		ChallengeManager.getInstance().selectChallenge(challenge);
	}

}
