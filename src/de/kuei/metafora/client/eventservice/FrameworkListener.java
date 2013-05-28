package de.kuei.metafora.client.eventservice;

import de.kuei.metafora.shared.eventservice.events.OpenUrlEvent;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface FrameworkListener extends RemoteEventListener {

	public void openUrlEvent(OpenUrlEvent event);

}
