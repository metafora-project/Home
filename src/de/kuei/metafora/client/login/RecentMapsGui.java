package de.kuei.metafora.client.login;

import java.util.Vector;

import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;

import de.kuei.metafora.client.Languages;
import de.kuei.metafora.shared.HistoryData;

public class RecentMapsGui extends VerticalPanel implements ChangeHandler {

	// i18n 
	final static Languages language = GWT.create(Languages.class);
	private Vector<String> maps;
	private ListBox recentMaps;

	public RecentMapsGui() {
		super();

		maps = new Vector<String>();

		add(new HTML("<b>"+language.YourRecentMaps()+"</b>"));

		recentMaps = new ListBox();
		recentMaps.setVisibleItemCount(5);
		recentMaps.addChangeHandler(this);
		add(new ScrollPanel(recentMaps));

		buildGui();
	}

	private void buildGui() {

		Vector<HistoryData> history = GroupManager.getInstance().getHistory();

		if (history != null) {
			for (HistoryData historyData : history) {
				for (String map : historyData.getMaps()) {
					maps.add(map);

					String listEntry = map + " (";

					listEntry += historyData.getChallengeName() + ", ";

					for (String user : historyData.getOtherUsers()) {
						listEntry += user + ", ";
					}
					listEntry = listEntry.substring(0, listEntry.length() - 2);
					listEntry += ")";

					recentMaps.addItem(listEntry);
				}
			}
		} else {
			recentMaps.setVisible(false);
		}
	}

	@Override
	public void onChange(ChangeEvent event) {
		int index = recentMaps.getSelectedIndex();
		String map = maps.get(index);
		MapManager.getInstance().selectMap(map);
	}

}
