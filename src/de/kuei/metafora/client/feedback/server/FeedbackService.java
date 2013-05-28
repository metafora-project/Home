package de.kuei.metafora.client.feedback.server;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("feedback")
public interface FeedbackService extends RemoteService{

	public void sendFeedbackMessage(String msg, String user, String tool, String interruptionType);
}
