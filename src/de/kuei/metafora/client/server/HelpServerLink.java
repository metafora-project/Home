package de.kuei.metafora.client.server;

import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("help")
public interface HelpServerLink extends RemoteService{

	public void help(String username, String groupId, String challengeId, String challengeName, String message, String selectedUrl, Vector<String> openUrls, String token, boolean group, boolean others);
	
}
