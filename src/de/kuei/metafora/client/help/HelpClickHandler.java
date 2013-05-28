package de.kuei.metafora.client.help;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;


public class HelpClickHandler implements Listener<ButtonEvent> {
	
	public HelpClickHandler(){
		
	}

	@Override
	public void handleEvent(ButtonEvent be) {
		HelpDialog dialog = new HelpDialog();
		dialog.show();
	}
}
