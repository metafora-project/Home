package de.kuei.metafora.client.eventservice;

import de.kuei.metafora.shared.eventservice.events.ChatEvent;
import de.kuei.metafora.shared.eventservice.events.ChatObjectEvent;
import de.kuei.metafora.shared.eventservice.events.FeedbackEvent;
import de.kuei.metafora.shared.eventservice.events.HelpEvent;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface ChatListener extends RemoteEventListener {

	public void chatEvent(ChatEvent event);

	public void chatObjectEvent(ChatObjectEvent event);

	public void feedbackEvent(FeedbackEvent event);

	public void helpEvent(HelpEvent event);

}
