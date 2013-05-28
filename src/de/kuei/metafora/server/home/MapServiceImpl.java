package de.kuei.metafora.server.home;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.Vector;

import de.kuei.metafora.client.login.handler.server.MapService;
import de.kuei.metafora.server.home.mysql.MysqlConnector;

public class MapServiceImpl extends RemoteServiceServlet implements MapService{

	@Override
	public Vector<String> getMapnames(String user, String groupId, Boolean all, String token) {
		return MysqlConnector.getInstance().loadMapnames(user, groupId, true);
	}
}
