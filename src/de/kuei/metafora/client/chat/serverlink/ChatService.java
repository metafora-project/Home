package de.kuei.metafora.client.chat.serverlink;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("chat")
public interface ChatService extends RemoteService{
	
	public void sendChatMessage(String msg, String challengeId, String challengeName, String token);
	
	public void openChatObject(String message, String user, String groupId, String challengeId, String challengeName, String token);
	
	public void sendLIFMessage(String msg, String challengeId, String challengeName, String token);
	
	public void sendHIFMessage(String msg, String challengeId, String challengeName, String token);
}
