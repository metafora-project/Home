package de.kuei.metafora.client.login.handler.server;

import java.util.Vector;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("mapservice")
public interface MapService extends RemoteService{
	public Vector<String> getMapnames(String user, String groupId, Boolean all, String token);
}
