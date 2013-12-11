package de.kuei.metafora.server.home;

import java.util.Vector;

import javax.servlet.http.HttpServlet;

import de.kuei.metafora.server.home.manager.Usermanager;
import de.kuei.metafora.server.home.mysql.ChannelDescription;
import de.kuei.metafora.server.home.mysql.MysqlConnector;
import de.kuei.metafora.server.home.mysql.MysqlInitConnector;
import de.kuei.metafora.server.home.mysql.ServerDescription;
import de.kuei.metafora.server.home.xmpp.XMPPListener;
import de.kuei.metafora.xmppbridge.xmpp.NameConnectionMapper;
import de.kuei.metafora.xmppbridge.xmpp.ServerConnection;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUC;
import de.kuei.metafora.xmppbridge.xmpp.XmppMUCManager;

public class StartupServlet extends HttpServlet {

	public static String commonformat = "http://static.metafora-project.info/dtd/commonformat.dtd";
	public static String toolname = "METAFORA";
	public static boolean logged = true;
	public static String lasadName = "LASAD";
	public static String pikiName = "PIKI";
	public static String suscityName = "SUS_CITY";
	public static String jugglerName = "JUGGLER";
	public static String mathName = "MATH";

	public static String[] planningTool = new String[] { "https",
			"metafora-project.de", "planningtoolsolo/" };
	public static String[] lasad = new String[] { "http",
			"adapterrex.hcii.cs.cmu.edu", "lasad/", "8090" };
	public static String[] piki = new String[] {"http", "test.silentbaystudios.com", "metafora_piki/test/piki.php"};
	
	public static String[] messaging = new String[] {"http", "web.lkldev.ioe.ac.uk", "MonitorInterventionMetafora/?userType=METAFORA_USER&receiver=METAFORA_TEST"};

	public static String[] susCity = new String[] { "http", "etl.ppp.uoa.gr",
			"suscity/index.html" };
	public static String[] expresser = new String[] { "http",
			"web-expresser.appspot.com" };
	public static String[] juggler = new String[] { "http", "etl.ppp.uoa.gr",
			"physt/" };
	public static String[] math = new String[] { "http", "etl.ppp.uoa.gr",
			"malt/index.html" };
	public static String[] workbench = new String[] { "https",
			"metafora-project.de", "workbench/" };

	private static XmppMUC logger;
	private static XmppMUC analysis;
	private static XmppMUC command;

	public static void sendToLogger(String message) {
		if (logger != null) {
			logger.sendMessage(message);
		} else {
			System.err
					.println("Logger channel was not initalized! Message lost:\n"
							+ message);
		}
	}

	public static void sendToCommand(String message) {
		if (command != null) {
			command.sendMessage(message);
		} else {
			System.err
					.println("Command channel was not initalized! Message lost:\n"
							+ message);
		}
	}

	public static void sendToAnalysis(String message) {
		if (analysis != null) {
			analysis.sendMessage(message);
		} else {
			System.err
					.println("Analysis channel was not initalized! Message lost:\n"
							+ message);
		}
	}

	private String[] splitServer(String server, String pathExtension) {
		int pos = server.indexOf(":", 0);
		String proto = server.substring(0, pos);
		server = server.substring(pos + 3, server.length());

		String port = null;
		String domain = server;
		String path = null;
		if (server.contains(":")) {
			pos = server.indexOf(':');
			int pos2 = server.indexOf('/');
			if (pos2 > 0) {
				port = server.substring(pos + 1, pos2);
				domain = server.substring(0, pos);
				path = server.substring(pos2 + 1, server.length());
			}
		} else if (server.contains("/")) {
			pos = server.indexOf('/');
			if (pos == server.length() - 1) {
				domain = server.substring(0, pos);
			} else {
				domain = server.substring(0, pos);
				path = server.substring(pos + 1, server.length());
			}
		}

		if (pathExtension != null) {
			if (path == null) {
				path = pathExtension;
			} else {
				path += pathExtension;
			}
		}

		int len = 2;
		if (port != null) {
			len++;
		}
		if (path != null) {
			len++;
		}

		String[] serverParts = new String[len];
		serverParts[0] = proto;
		serverParts[1] = domain;
		if (path != null)
			serverParts[2] = path;
		if (port != null)
			serverParts[3] = port;

		return serverParts;
	}

	private void printServer(String[] server, String name) {
		System.err.println(name + ": ");
		for (int i = 0; i < server.length; i++) {
			System.err.print(server[i] + ", ");
		}
		System.err.println();
	}

	public void init() {

		MysqlInitConnector.getInstance().loadData("Home");

		System.err.println("Loading mysql init parameter....");

		toolname = MysqlInitConnector.getInstance().getParameter("METAFORA");
		lasadName = MysqlInitConnector.getInstance().getParameter("LASAD");
		pikiName = MysqlInitConnector.getInstance().getParameter("PIKI");
		suscityName = MysqlInitConnector.getInstance().getParameter("SUS_CITY");
		jugglerName = MysqlInitConnector.getInstance().getParameter("JUGGLER");
		mathName = MysqlInitConnector.getInstance().getParameter("MATH");

		System.err.println("Home name: " + toolname);
		System.err.println("Lasad name: " + lasadName);
		System.err.println("Piki name: " + pikiName);
		System.err.println("Sus-City name: " + suscityName);
		System.err.println("Juggler name: " + jugglerName);
		System.err.println("3D-Math name: " + mathName);

		if (MysqlInitConnector.getInstance().getParameter("logged")
				.toLowerCase().equals("false")) {
			logged = false;
		}

		System.err.println("Config servers...");

		ServerDescription tomcatServer = MysqlInitConnector.getInstance()
				.getAServer("tomcat");
		if (tomcatServer != null && tomcatServer.getServer() != null) {
			planningTool = splitServer(tomcatServer.getServer(),
					"planningtoolsolo/");
			workbench = splitServer(tomcatServer.getServer(), "workbench/");
		} else {
			System.err.println("Tomcat server not found!");
		}

		printServer(planningTool, "PlanningTool");

		ServerDescription pikiServer = MysqlInitConnector.getInstance()
				.getAServer("piki");
		if (pikiServer != null && pikiServer.getServer() != null) {
			piki = splitServer(pikiServer.getServer(), "/piki.php");
		} else {
			System.err.println("Piki server not found!");
		}

		printServer(piki, "Piki");
		
		ServerDescription messagingServer = MysqlInitConnector.getInstance()
				.getAServer("messaging");
		if (messagingServer != null && messagingServer.getServer() != null) {
			messaging = splitServer(messagingServer.getServer(), null);
		} else {
			System.err.println("Messaging server not found!");
		}

		printServer(messaging, "Messaging");

		ServerDescription serverDesc = MysqlInitConnector.getInstance()
				.getAServer("lasad");
		if (serverDesc != null && serverDesc.getServer() != null) {
			lasad = splitServer(serverDesc.getServer(), null);
		} else {
			System.err.println("Lasad server not found!");
		}

		printServer(lasad, "Lasad");

		serverDesc = MysqlInitConnector.getInstance().getAServer("suscity");
		if (serverDesc != null && serverDesc.getServer() != null) {
			susCity = splitServer(serverDesc.getServer(), null);
		} else {
			System.err.println("Sus-City server not found!");
		}

		printServer(susCity, "Sus-City");

		serverDesc = MysqlInitConnector.getInstance().getAServer("expresser");
		if (serverDesc != null && serverDesc.getServer() != null) {
			expresser = splitServer(serverDesc.getServer(), null);
		} else {
			System.err.println("Web-eXpresser server not found!");
		}

		printServer(expresser, "Web-eXpresser");

		serverDesc = MysqlInitConnector.getInstance().getAServer("juggler");
		if (serverDesc != null && serverDesc.getServer() != null) {
			juggler = splitServer(serverDesc.getServer(), null);
		} else {
			System.err.println("Juggler server not found!");
		}

		printServer(juggler, "Juggler");

		serverDesc = MysqlInitConnector.getInstance().getAServer("3dmath");
		if (serverDesc != null && serverDesc.getServer() != null) {
			math = splitServer(serverDesc.getServer(), null);
		} else {
			System.err.println("3D-Math server not found!");
		}

		printServer(math, "3D-Math");

		System.err.println("Config XMPP...");

		// configure xmpp
		Vector<ServerDescription> xmppServers = MysqlInitConnector
				.getInstance().getServer("xmpp");

		for (ServerDescription xmppServer : xmppServers) {
			System.err.println("XMPP server: " + xmppServer.getServer());
			System.err.println("XMPP user: " + xmppServer.getUser());
			System.err.println("XMPP password: " + xmppServer.getPassword());
			System.err.println("XMPP device: " + xmppServer.getDevice());
			System.err.println("Modul: " + xmppServer.getModul());

			System.err.println("Starting XMPP connection...");

			NameConnectionMapper.getInstance().createConnection(
					xmppServer.getConnectionName(), xmppServer.getServer(),
					xmppServer.getUser(), xmppServer.getPassword(),
					xmppServer.getDevice());

			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName())
					.addPacketListener(XMPPListener.getInstance());

			NameConnectionMapper.getInstance()
					.getConnection(xmppServer.getConnectionName()).login();
		}

		Vector<ChannelDescription> channels = MysqlInitConnector.getInstance()
				.getXMPPChannels();

		for (ChannelDescription channeldesc : channels) {
			ServerConnection connection = NameConnectionMapper.getInstance()
					.getConnection(channeldesc.getConnectionName());

			if (connection == null) {
				System.err.println("StartupServlet: Unknown connection: "
						+ channeldesc.getUser());
				continue;
			}

			System.err.println("Joining channel " + channeldesc.getChannel()
					+ " as " + channeldesc.getAlias());

			XmppMUC muc = XmppMUCManager.getInstance().getMultiUserChat(
					channeldesc.getChannel(), channeldesc.getAlias(),
					connection);
			muc.join(0);

			if (channeldesc.getChannel().equals("logger")) {
				System.err.println("StartupServlet: logger configured.");
				logger = muc;
			} else if (channeldesc.getChannel().equals("analysis")) {
				System.err.println("StartupServlet: analysis configured.");
				analysis = muc;
			} else if (channeldesc.getChannel().equals("command")) {
				System.err.println("StartupServlet: command configured.");
				command = muc;
			}
		}

		System.err.println("Config MySQL...");

		// init mysql
		ServerDescription mysqlServer = MysqlInitConnector.getInstance()
				.getAServer("mysql");

		MysqlConnector.url = "jdbc:mysql://" + mysqlServer.getServer()
				+ "/metafora?useUnicode=true&characterEncoding=UTF-8";
		MysqlConnector.user = mysqlServer.getUser();
		MysqlConnector.password = mysqlServer.getPassword();

		Usermanager.getInstance().loadUsers();
		Usermanager.getInstance().loadSalts();
		Usermanager.getInstance().loadTeams();
	}

}
