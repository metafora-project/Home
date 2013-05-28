package de.kuei.metafora.client.login;

import java.util.Date;
import java.util.Vector;

import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.handler.server.LoginService;
import de.kuei.metafora.client.login.handler.server.LoginServiceAsync;
import de.kuei.metafora.shared.HistoryData;

public class RecentGroupsGui extends VerticalPanel implements ChangeHandler {

	// i18n 
	final static Languages language = GWT.create(Languages.class);
	private static final LoginServiceAsync login = GWT
			.create(LoginService.class);

	private Vector<String> groups;
	private ListBox recentGroups;

	private Vector<HistoryData> history = null;

	public RecentGroupsGui() {
		super();

		groups = new Vector<String>();

		add(new HTML("<b>"+language.YourRecentGroups()+"</b>"));

		recentGroups = new ListBox();
		recentGroups.setVisibleItemCount(5);
		recentGroups.addChangeHandler(this);
		add(new ScrollPanel(recentGroups));

		buildGui();
	}

	public Vector<HistoryData> getHistory() {
		return history;
	}

	private void buildGui() {

		login.getHistory(Home.userName,
				new AsyncCallback<Vector<HistoryData>>() {

					@Override
					public void onSuccess(Vector<HistoryData> result) {
						history = result;

						for (HistoryData historyData : result) {
							for (String groupName : historyData.getGroups()) {

								groups.add(groupName);

								String listEntry = groupName + " (";

								Date time = historyData.getTime();
								String date = DateTimeFormat.getFormat(
										"dd.MM.yy").format(time);

								listEntry += date + ", ";
								listEntry += historyData.getChallengeName()
										+ ", ";
								listEntry += historyData.getMaps()
										.firstElement();

								listEntry += ")";

								recentGroups.addItem(listEntry);
							}
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						recentGroups.setVisible(false);
					}
				});
	}

	@Override
	public void onChange(ChangeEvent event) {
		int index = recentGroups.getSelectedIndex();
		String group = groups.get(index);
		GroupManager.getInstance().selectGroup(group);
	}

}
