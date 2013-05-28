package de.kuei.metafora.client.feedback.server;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface FeedbackServiceAsync {

	void sendFeedbackMessage(String msg, String user, String tool, String interruptionType, AsyncCallback<Void> callback);
}
