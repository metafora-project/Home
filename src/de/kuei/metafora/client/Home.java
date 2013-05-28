package de.kuei.metafora.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.extjs.gxt.themes.client.Slate;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.util.ThemeManager;
import com.extjs.gxt.ui.client.widget.ColoredTabItem;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ColoredButton;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.MarginData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout.VBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayoutData;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import de.kuei.metafora.client.chat.ChatAwarenessCounter;
import de.kuei.metafora.client.chat.ChatList;
import de.kuei.metafora.client.eventservice.impl.ChatListenerImpl;
import de.kuei.metafora.client.eventservice.impl.FrameworkListenerImpl;
import de.kuei.metafora.client.eventservice.impl.UserListenerImpl;
import de.kuei.metafora.client.feedback.FeedbackAwarenessCounter;
import de.kuei.metafora.client.feedback.FeedbackDialog;
import de.kuei.metafora.client.feedback.FeedbackList;
import de.kuei.metafora.client.help.HelpClickHandler;
import de.kuei.metafora.client.login.LoginManager;
import de.kuei.metafora.client.server.SessionLink;
import de.kuei.metafora.client.server.SessionLinkAsync;
import de.kuei.metafora.client.team.Logout;
import de.kuei.metafora.client.team.TeamWidget;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;
import de.kuei.metafora.client.util.EncodingUrlBuilder;
import de.kuei.metafora.shared.eventservice.DomainNames;
import de.novanic.eventservice.client.ClientHandler;
import de.novanic.eventservice.client.event.RemoteEventService;
import de.novanic.eventservice.client.event.RemoteEventServiceFactory;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

public class Home implements EntryPoint {

	// object needed to implement i18n through Languages interface
	final static Languages language = GWT.create(Languages.class);

	private static Logger logger = Logger.getLogger("Home.Home");

	public static Home lastFrame;
	public static String token = null;
	public static ContentPanel feedbackPanel;
	private static ContentPanel chatPanel, west4;

	private static RemoteEventService remoteEventService;

	private static Domain chatDomain = DomainFactory
			.getDomain(DomainNames.CHATDOMAIN);
	private static Domain userDomain = DomainFactory
			.getDomain(DomainNames.USERDOMAIN);
	private static Domain frameworkDomain = DomainFactory
			.getDomain(DomainNames.FRAMEWORKDOMAIN);

	private UserLinkAsync userLink = GWT.create(UserLink.class);
	private SessionLinkAsync sessionLink = GWT.create(SessionLink.class);

	private String lang = "en";
	public static String mapName = "default";
	public static String userName = "";
	private static String shamd5hash;
	public static String groupName = "";
	public static String challengeId;
	public static String challengeName;
	public static String challengeUrl = null;

	private Viewport viewport;
	private ChatList chat;
	private FeedbackList feedback;
	private Vector<DialogBox> messages;
	private AccordionLayout accordion;
	private TabPanel mainTabPanel;
	private ContentPanel north;
	private HTML connectionState = new HTML(
			"<span style=\"color: yellow;\">unknown</span>");

	private HashMap<String, ArrayList<String>> urlMap;
	private boolean openOldTabs = false;
	public boolean wasReload = false;

	private boolean toolDataInitalized = false;
	private HashMap<String, String[]> toolData;
	private ArrayList<String> remoteTeamMembers = new ArrayList<String>();

	private HashMap<String, Widget> openWidgets;
	private HashMap<Widget, TabItem> openTabs;

	public HTML getConnectionState() {
		return connectionState;
	}

	public void onModuleLoad() {
		logger.setLevel(Level.WARNING);

		lastFrame = this;

		openWidgets = new HashMap<String, Widget>();
		openTabs = new HashMap<Widget, TabItem>();

		lang = Window.Location.getParameter("locale");
		if (lang == null || lang.length() == 0)
			lang = "en";

		String awareness = Window.Location.getParameter("awareness");
		if (awareness != null && awareness.toLowerCase().equals("false")) {
			FeedbackAwarenessCounter.getInstance().setEnabled(false);
			ChatAwarenessCounter.getInstance().setEnabled(false);
			logger.log(Level.INFO, "Home.Home: Awareness features turned off!");
		} else {
			FeedbackAwarenessCounter.getInstance().setEnabled(true);
			ChatAwarenessCounter.getInstance().setEnabled(true);
			logger.log(Level.INFO, "Home.Home: Awareness features turned on.");
		}

		ThemeManager.register(Slate.SLATE);
		GXT.setDefaultTheme(Slate.SLATE, true);

		remoteEventService = RemoteEventServiceFactory.getInstance()
				.getRemoteEventService();

		final String oldToken = Cookies.getCookie("metaforaToken");

		if (oldToken != null) {
			String mapname = Cookies.getCookie("metaforaMap");
			String user = Cookies.getCookie("metaforaUser");
			String group = Cookies.getCookie("metaforaGroup");
			String password = Cookies.getCookie("metaforaPassword");
			String challengeId = Cookies.getCookie("metaforaChallengeId");
			String challengeName = Cookies.getCookie("metaforaChallengeName");
			String challengeUrl = Cookies.getCookie("metaforaChallengeUrl");

			if (mapname != null && user != null && group != null
					&& challengeId != null && challengeName != null
					&& challengeUrl != null) {
				if (challengeId.length() > 0) {
					Home.challengeId = challengeId;
					Home.mapName = mapname;
					Home.userName = user;
					Home.groupName = group;
					Home.shamd5hash = password;
					Home.token = oldToken;
					Home.challengeName = challengeName;
					Home.challengeUrl = challengeUrl;

					openOldTabs = true;
					wasReload = true;
				}
			}
		}

		RemoteEventServiceFactory.getInstance().requestClientHandler(
				new AsyncCallback<ClientHandler>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("requestClientHandler: "
								+ caught.getMessage());
					}

					@Override
					public void onSuccess(ClientHandler result) {
						Home.token = result.getConnectionId();

						if (Home.token == null) {
							Date date = new Date();
							Home.token = "ISNOGOODHOME" + date.getTime()
									+ Random.nextInt();
						}

						startClientSession(oldToken);
					}
				});

		remoteEventService.addListener(userDomain, new UserListenerImpl(),
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("userDomain: " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
					}
				});

		remoteEventService.addListener(chatDomain, new ChatListenerImpl(),
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("chatDomain: " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
					}
				});

		remoteEventService.addListener(frameworkDomain,
				new FrameworkListenerImpl(), new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert("frameworkDomain: " + caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
					}
				});
	}

	public void startClientSession(String oldToken) {
		sessionLink.startSession(token, oldToken, new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(language.StartingMetaforaSessionFailed());
				// reload page
				// String url = Window.Location.getHref();
				// Window.Location.assign(url);
			}

			@Override
			public void onSuccess(String result) {
				setToken(result);
			}
		});
	}

	public void setToken(String token) {
		Home.token = token;

		Date now = new Date();
		long nowLong = now.getTime();
		nowLong = nowLong + (1000 * 60 * 60 * 20);// keep logged in for 20
													// hours
		now.setTime(nowLong);
		Cookies.setCookie("metaforaToken", token, now);

		if (wasReload) {
			main();
		} else {
			LoginManager.getInstance().main();
		}
	}

	public void getToolData() {
		userLink.getToolData(new AsyncCallback<HashMap<String, String[]>>() {

			@Override
			public void onFailure(Throwable caught) {
				toolData = new HashMap<String, String[]>();
				toolDataInitalized = true;
				main();
			}

			@Override
			public void onSuccess(HashMap<String, String[]> result) {
				toolData = result;
				toolDataInitalized = true;
				main();
			}
		});
	}

	public void main() {
		if (!toolDataInitalized) {
			getToolData();
			return;
		}

		viewport = new Viewport();
		viewport.setBorders(false);
		final BorderLayout layout = new BorderLayout();
		viewport.setLayout(layout);

		initializeData();

		// initNorthPanel(viewport);
		initWestPanel(viewport);
		initCenterPanel(viewport);

		RootLayoutPanel rootLayout = RootLayoutPanel.get();
		rootLayout.add(viewport);
		rootLayout.setWidgetTopHeight(viewport, 0, Unit.PX, 100, Unit.PCT);
		rootLayout.setWidgetLeftWidth(viewport, 0, Unit.PX, 100, Unit.PCT);

		Anchor logout = new Logout();
		rootLayout.add(logout);
		rootLayout.setWidgetRightWidth(logout, 5, Unit.PX, 100, Unit.PX);
		rootLayout.setWidgetTopHeight(logout, 2, Unit.PX, 20, Unit.PX);

		HTML image = new HTML(
				"<div style='margin-left:0px;margin-top:0px;'><img src='gxt/images/logo_metafora.png' alt='logo'/></div>");
		rootLayout.add(image);
		rootLayout.setWidgetLeftWidth(image, 5, Unit.PX, 197, Unit.PX);
		rootLayout.setWidgetTopHeight(image, 10, Unit.PX, 34, Unit.PX);

		messages = new Vector<DialogBox>();

		accordion.setActiveItem(chatPanel);

		if (openOldTabs) {
			Collection<String> names = Cookies.getCookieNames();
			for (String name : names) {
				if (name != null && name.startsWith("metaforaTab")) {
					int pos = name.indexOf(':');
					if (pos > 0) {
						String url = Cookies.getCookie(name);

						Cookies.removeCookie(name);

						// to not loose color after refresh
						String toolname = "Tool";
						String color = "";

						if (url.toLowerCase().contains("lasad")) {
							toolname = language.Lasad();
							color = "blue";
						} else if (url.toLowerCase().contains("expresser")) {
							toolname = language.Expresser();
							color = "green";
						} else if (url.toLowerCase().contains("suscity")) {
							toolname = language.Suscity();
							color = "military";
						} else if (url.toLowerCase().contains("physt")) {
							toolname = language.Jugger();
							color = "orange";
						} else if (url.toLowerCase().contains(
								"planningtoolsolo")) {
							toolname = language.PlanningTool();
							color = "yellow";
						} else if (url.toLowerCase().contains("piki")) {
							toolname = language.Piki();
							color = "darkred";
						} else if (url.toLowerCase().contains("malt")) {
							toolname = language.Math();
							color = "lightblue";
						}

						addContentFrame(url, toolname, true, color, true, false);
					} else {
						Window.setStatus("Error while loading tab: " + name);
					}
				}
			}
		}

		if (wasReload) {
			Collection<String> cookies = Cookies.getCookieNames();
			for (String cn : cookies) {
				if (cn != null && cn.startsWith("metaforaUserOther")) {
					String user = Cookies.getCookie(cn);
					if (user != null)
						TeamWidget.getInstance().addUser(user, true);
				}
			}
			TeamWidget.getInstance().setTeamName(Home.groupName);
		}
	}

	private void initializeData() {
		urlMap = new HashMap<String, ArrayList<String>>();

		ArrayList<String> arrayList;
		// gets and sets the challengeUrl
		if (Home.challengeUrl != null) {
			arrayList = new ArrayList<String>();
			arrayList.add(Home.challengeUrl);
			arrayList.add("");
			urlMap.put(language.Challenge(), arrayList);
		}

		EncodingUrlBuilder planningToolUrlBuilder = new EncodingUrlBuilder();
		EncodingUrlBuilder lasadUrlBuilder = new EncodingUrlBuilder();

		// PlanningTool
		if (toolData.containsKey("PlanningTool")) {
			planningToolUrlBuilder.setToolData(toolData.get("PlanningTool"));
		}

		// Lasad
		if (toolData.containsKey("Lasad")) {
			lasadUrlBuilder.setToolData(toolData.get("Lasad"));
		}
		lasadUrlBuilder.setParameter("autologin", "true");
		lasadUrlBuilder.setParameter("isStandAlone", "false");

		// PiKI
		arrayList = new ArrayList<String>();
		EncodingUrlBuilder pikiUrlBuilder = new EncodingUrlBuilder();
		if (toolData.containsKey("Piki")) {
			pikiUrlBuilder.setToolData(toolData.get("Piki"));
		}
		arrayList.add(pikiUrlBuilder.buildString());
		arrayList.add("darkred");
		urlMap.put(language.Piki(), arrayList);

		// Planning Tool
		arrayList = new ArrayList<String>();
		arrayList.add(planningToolUrlBuilder.buildString());
		arrayList.add("yellow");
		urlMap.put(language.PlanningTool(), arrayList);

		// LASAD
		arrayList = new ArrayList<String>();
		arrayList.add(lasadUrlBuilder.buildString());
		arrayList.add("blue");
		urlMap.put(language.Lasad(), arrayList);

		// Sus-City
		arrayList = new ArrayList<String>();
		EncodingUrlBuilder susCityUrlBuilder = new EncodingUrlBuilder();
		if (toolData.containsKey("SusCity")) {
			susCityUrlBuilder.setToolData(toolData.get("SusCity"));
		}
		susCityUrlBuilder.setParameter("log_enabled", "true");
		susCityUrlBuilder.setParameter("log", "true");
		arrayList.add(susCityUrlBuilder.buildString());
		arrayList.add("military");
		urlMap.put(language.Suscity(), arrayList);

		// eXpresser
		arrayList = new ArrayList<String>();
		EncodingUrlBuilder expresserUrlBuilder = new EncodingUrlBuilder();
		if (toolData.containsKey("expresser")) {
			expresserUrlBuilder.setToolData(toolData.get("expresser"));
		}
		expresserUrlBuilder.setParameter("userKey", "JkvkM0h8U71hoRBn5z0E7b");
		expresserUrlBuilder.setParameter("project", userName);
		expresserUrlBuilder.setParameter("metafora", "1");
		expresserUrlBuilder.setParameter("logging", "0");
		expresserUrlBuilder.setParameter("indicatorposting", "1");
		arrayList.add(expresserUrlBuilder.buildString());
		arrayList.add("green");
		urlMap.put(language.Expresser(), arrayList);

		// Juggler
		arrayList = new ArrayList<String>();
		EncodingUrlBuilder jugglerUrlBuilder = new EncodingUrlBuilder();
		if (toolData.containsKey("Juggler")) {
			jugglerUrlBuilder.setToolData(toolData.get("Juggler"));
		}
		jugglerUrlBuilder.setParameter("log_enabled", "true");
		jugglerUrlBuilder.setParameter("log", "true");
		arrayList.add(jugglerUrlBuilder.buildString());
		arrayList.add("orange");
		urlMap.put(language.Jugger(), arrayList);

		// 3d-Math
		arrayList = new ArrayList<String>();
		EncodingUrlBuilder mathUrlBuilder = new EncodingUrlBuilder();
		if (toolData.containsKey("3DMath")) {
			mathUrlBuilder.setToolData(toolData.get("3DMath"));
		}
		mathUrlBuilder.setParameter("log_enabled", "true");
		mathUrlBuilder.setParameter("log", "true");
		arrayList.add(mathUrlBuilder.buildString());
		arrayList.add("lightblue");
		urlMap.put(language.Math(), arrayList);

		// Google
		arrayList = new ArrayList<String>();
		arrayList.add("http://www.google.com");
		arrayList.add("");
		urlMap.put(language.Google(), arrayList);

		// Notes
		arrayList = new ArrayList<String>();
		arrayList
				.add("https://docs.google.com/document/d/1IKYg4E-_Iz8xP6ZifgiJTJU9G3hskF4D-buwjKlvodQ/edit?hl=en_US");
		arrayList.add("black");
		urlMap.put(language.Notes(), arrayList);

		
		//Messaging Tool
		arrayList = new ArrayList<String>();
		EncodingUrlBuilder messagingUrlBuilder = new EncodingUrlBuilder();
		if (toolData.containsKey("messaging")) {
			messagingUrlBuilder.setToolData(toolData.get("messaging"));
		}
		arrayList.add(messagingUrlBuilder.buildString());
		arrayList.add("");
		urlMap.put("Messaging Tool", arrayList);
	}

	public void addRemoteUser(String user) {
		if (!remoteTeamMembers.contains(user)) {
			remoteTeamMembers.add(user);
		}
	}

	public void removeRemoteUser(String user) {
		remoteTeamMembers.remove(user);
	}

	public void initNorthPanel(LayoutContainer viewport) {

		north = new ContentPanel();
		north.setLayout(new FitLayout());
		north.setAutoHeight(true);
		north.setAutoWidth(true);
		north.setMonitorWindowResize(true);
		north.setBorders(false);
		north.setHeaderVisible(false);
		north.setLayout(new FitLayout());
		north.setWidth("100%");
		north.setHeight("20px");

		LayoutPanel layout = new LayoutPanel();
		layout.setWidth("100%");
		layout.setHeight("20px");

		Anchor logout = new Logout();
		layout.add(logout);
		layout.setWidgetRightWidth(logout, 100, Unit.PX, 90, Unit.PX);
		layout.setWidgetTopHeight(logout, 2, Unit.PX, 18, Unit.PX);

		north.add(layout);

		BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH,
				20);
		northData.setCollapsible(true);
		northData.setFloatable(true);
		northData.setHideCollapseTool(true);
		northData.setSplit(true);
		northData.setMargins(new Margins(0, 0, 0, 0));

		viewport.add(north, northData);
	}

	public void initSouthPanel(LayoutContainer viewport) {

		ContentPanel south = new ContentPanel();
		south.setLayout(new FitLayout());
		south.setMonitorWindowResize(true);
		south.setBorders(false);
		south.setHeaderVisible(false);

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH,
				10);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setHideCollapseTool(true);
		southData.setSplit(true);
		southData.setMargins(new Margins(5, 0, 0, 0));

		viewport.add(south, southData);
	}

	public void initEastPanel(LayoutContainer viewport) {

		ContentPanel east = new ContentPanel();
		east.setLayout(new FitLayout());
		east.setMonitorWindowResize(true);
		east.setBorders(false);
		east.setHeaderVisible(false);

		BorderLayoutData eastData = new BorderLayoutData(LayoutRegion.EAST, 10);
		eastData.setCollapsible(true);
		eastData.setFloatable(true);
		eastData.setHideCollapseTool(true);
		eastData.setSplit(true);
		eastData.setMargins(new Margins(0, 0, 0, 5));

		viewport.add(east, eastData);
	}

	public void initWestPanel(LayoutContainer viewport) {

		ContentPanel westParent = new ContentPanel();
		westParent.setMonitorWindowResize(true);
		westParent.setBorders(false);
		westParent.setHeaderVisible(false);
		BorderLayout bl = new BorderLayout();
		westParent.setLayout(bl);

		ContentPanel west = new ContentPanel();
		west.setMonitorWindowResize(true);
		accordion = new AccordionLayout();
		west.setLayout(accordion);
		west.setHeaderVisible(false);
		west.setBorders(false);
		west.setMinButtonWidth(200);

		ContentPanel west1 = new ContentPanel();
		west1.setScrollMode(Scroll.AUTO);
		west1.setHeading(language.Tools());
		west1.setAnimCollapse(false);
		west1.setHideCollapseTool(true);
		initTools(west1);
		west.add(west1);
		addHandler(west1, "Tools");

		west4 = new ContentPanel();
		west4.setScrollMode(Scroll.AUTO);
		west4.setHideCollapseTool(true);
		west4.setHeading(language.Login());

		TeamWidget tw = TeamWidget.getInstance();
		tw.main();
		tw.setTitle(language.Group());
		tw.setTeamName(groupName);
		tw.addUser(userName, true);
		west4.setAnimCollapse(false);
		west4.add(new ScrollPanel(tw));
		west4.collapse();
		west.add(west4);
		addHandler(west4, "Team");

		chat = new ChatList();
		chatPanel = new ContentPanel() {
			@Override
			protected void onResize(int width, int height) {
				super.onResize(width, height);
				chat.layoutMessages();
			}

		};
		chatPanel.setScrollMode(Scroll.NONE);
		chatPanel.setHideCollapseTool(true);

		chatPanel.setHeading(language.Chat());
		chatPanel.setAnimCollapse(false);
		chatPanel.add(chat);
		west.add(chatPanel);

		addHandler(chatPanel, "Chat");

		chatPanel
				.addListener(Events.Expand, ChatAwarenessCounter.getInstance());
		ChatAwarenessCounter.getInstance().setPanel(chatPanel, language.Chat());

		feedbackPanel = new ContentPanel();
		feedbackPanel.setScrollMode(Scroll.AUTO);
		feedbackPanel.setHideCollapseTool(true);

		feedbackPanel.setHeading(language.Feedback());
		feedback = new FeedbackList();
		// feedbackPanel.addText("Feedback");
		feedbackPanel.setAnimCollapse(false);
		feedbackPanel.add(feedback);
		west.add(feedbackPanel);

		addHandler(feedbackPanel, "Feedback");

		feedbackPanel.addListener(Events.Expand,
				FeedbackAwarenessCounter.getInstance());
		FeedbackAwarenessCounter.getInstance().setPanel(feedbackPanel,
				language.Feedback());

		BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 205);
		westData.setSplit(true);
		westData.setCollapsible(true);
		westData.setMargins(new Margins(50, 5, 10, 0));

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(0, 0, 10, 0));
		centerData.setCollapsible(true);
		centerData.setFloatable(true);
		centerData.setHideCollapseTool(true);
		centerData.setSplit(true);

		westParent.add(west, centerData);

		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH,
				20);
		southData.setCollapsible(true);
		southData.setFloatable(true);
		southData.setHideCollapseTool(true);
		southData.setSplit(true);
		southData.setMargins(new Margins(0, 0, 10, 0));

		// Help Button
		Listener<ButtonEvent> HelpListener = new HelpClickHandler();
		ColoredButton btn = new ColoredButton(language.Help().toUpperCase(),
				"red");
		btn.addListener(Events.OnMouseUp, HelpListener);
		westParent.add(btn, southData);

		viewport.add(westParent, westData);
		chat.layoutMessages();
	}

	private void addHandler(ContentPanel panel, String name) {
		if (logger.getLevel().equals(Level.INFO)) {
			final String panelName = name;

			panel.addListener(Events.Expand, new Listener<ComponentEvent>() {
				public void handleEvent(ComponentEvent be) {
					logger.log(Level.INFO, Home.token + " Home.Home: Panel "
							+ panelName + " expanded.");
				}
			});

			panel.addListener(Events.Collapse, new Listener<ComponentEvent>() {
				public void handleEvent(ComponentEvent be) {
					logger.log(Level.INFO, Home.token + " Home.Home: Panel "
							+ panelName + " collapsed.");
				}
			});
		}
	}

	public static void setLoginTitle(String text) {
		if (west4 != null)
			west4.setHeading(text);
		else
			Window.setStatus("Home.setLoginTitle: Login not initialized");
	}

	/**
	 * Sets the username and groupname of the first login.
	 * 
	 * @param _userName
	 * @param _groupName
	 */
	public void setLoginInformation(String _userName, String md5Password,
			String _shaPassword) {
		userName = _userName;
		shamd5hash = md5Password;

		Date now = new Date();
		long nowLong = now.getTime();
		nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);// seven days
		now.setTime(nowLong);

		Cookies.setCookie("metaforaUser", _userName, now);

		Cookies.setCookie("metaforaPassword", md5Password, now);
	}

	public void setGroupInformation(String _groupName) {
		groupName = _groupName;

		Date now = new Date();
		long nowLong = now.getTime();
		nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);// seven days
		now.setTime(nowLong);

		Cookies.setCookie("metaforaGroup", _groupName, now);
	}

	/**
	 * Sets the challengeId when first logging in.
	 * 
	 * @param _challengeId
	 */
	public void setChallengeId(String _challengeId) {
		challengeId = _challengeId;

		Date now = new Date();
		long nowLong = now.getTime();
		nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);// seven days
		now.setTime(nowLong);

		Cookies.setCookie("metaforaChallengeId", _challengeId, now);
	}

	/**
	 * Sets the challenge name when first logging in.
	 * 
	 * @param _challengeName
	 */
	public void setChallengeName(String _challengeName) {
		challengeName = _challengeName;

		Date now = new Date();
		long nowLong = now.getTime();
		nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);// seven days
		now.setTime(nowLong);

		Cookies.setCookie("metaforaChallengeName", _challengeName + "", now);
	}

	private void initTools(ContentPanel panel) {
		VBoxLayout vBox = new VBoxLayout();
		vBox.setPadding(new Padding(2));
		vBox.setVBoxLayoutAlign(VBoxLayoutAlign.STRETCH);
		panel.setLayout(vBox);

		VBoxLayoutData vBoxData = new VBoxLayoutData(new Margins(0, 0, 2, 0));
		Listener<ButtonEvent> UrlListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				Button btn = ce.<Button> getComponent();
				String btnText = btn.getText();
				ArrayList<String> elem = urlMap.get(btnText);
				String url = elem.get(0);
				String color = elem.get(1);

				addContentFrame(url, btnText, true, color, true, true);
			}
		};

		Listener<ButtonEvent> UserUrlListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				Button btn = ce.<Button> getComponent();
				String btnText = btn.getText();
				ArrayList<String> elem = urlMap.get(btnText);
				String url = elem.get(0);
				String color = elem.get(1);

				String user = TeamWidget.getInstance().getFirstUser();

				if (user == null)
					user = "Alice";

				if (url.contains("USERNAME")) {
					url = url.replaceAll("USERNAME", user);
				}
				addContentFrame(url, btnText, true, color, true, true);
			}
		};

		String text = language.PlanningTool();
		String color = urlMap.get(text).get(1);
		Button btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UrlListener);
		panel.add(btn, vBoxData);

		text = language.Lasad();
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UserUrlListener);
		panel.add(btn, vBoxData);

		text = language.Piki();
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UrlListener);
		panel.add(btn, vBoxData);

		text = language.Suscity();
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UrlListener);
		panel.add(btn, vBoxData);

		text = language.Expresser();
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UserUrlListener);
		panel.add(btn, vBoxData);

		text = language.Jugger();
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UserUrlListener);
		panel.add(btn, vBoxData);

		text = language.Math();
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UserUrlListener);
		panel.add(btn, vBoxData);

		text = language.Google();
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UrlListener);
		panel.add(btn, vBoxData);

		text = language.Notes();
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UrlListener);
		panel.add(btn, vBoxData);

		// TODO Internationalization
		text = "Messaging Tool";
		color = urlMap.get(text).get(1);
		btn = new ColoredButton(text, color);
		btn.addListener(Events.OnMouseUp, UrlListener);
		panel.add(btn, vBoxData);

	}

	public void initCenterPanel(LayoutContainer viewport) {
		ContentPanel center = new ContentPanel();
		center.setLayout(new FitLayout());
		center.setHeaderVisible(false);
		center.setBorders(false);
		center.setScrollMode(Scroll.NONE);

		BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
		centerData.setMargins(new Margins(15, 0, 10, 0));

		mainTabPanel = new TabPanel();
		mainTabPanel.setLayoutData(new CenterLayout());

		mainTabPanel.setHideMode(HideMode.OFFSETS);

		mainTabPanel.setMinTabWidth(115);
		mainTabPanel.setResizeTabs(true);
		mainTabPanel.setAnimScroll(true);
		mainTabPanel.setTabScroll(true);
		mainTabPanel.setCloseContextMenu(true);

		if (logger.getLevel().equals(Level.INFO)) {
			// search tab url if log level is info
			mainTabPanel.addListener(Events.Select,
					new Listener<ComponentEvent>() {
						public void handleEvent(ComponentEvent be) {
							TabItem item = mainTabPanel.getSelectedItem();
							for (Widget w : openTabs.keySet()) {
								if (openTabs.get(w).equals(item)) {
									for (String url : openWidgets.keySet()) {
										if (openWidgets.get(url).equals(w)) {
											logger.log(Level.INFO, Home.token
													+ " Home.Home: Tab " + url
													+ " selected.");
											return;
										}
									}
								}
							}

						}
					});
		}

		EncodingUrlBuilder workbenchUrlBuilder = new EncodingUrlBuilder();
		if (toolData.containsKey("Workbench")) {
			workbenchUrlBuilder.setToolData(toolData.get("Workbench"));
		}
		addContentFrame(workbenchUrlBuilder.buildString(),
				language.Workbench(), false, "", false, true);

		if (urlMap.get(language.Challenge()) != null) {
			ArrayList<String> elem = urlMap.get(language.Challenge());
			String url = elem.get(0);
			String color = elem.get(1);
			addContentFrame(url, Home.challengeName, false, color, false, true);
		}

		EncodingUrlBuilder ptUrlBuilder = new EncodingUrlBuilder();
		if (toolData.containsKey("PlanningTool")) {
			ptUrlBuilder.setToolData(toolData.get("PlanningTool"));
		}
		addContentFrame(ptUrlBuilder.buildString(), language.PlanningTool(),
				true, "yellow", false, true);

		center.add(mainTabPanel);
		viewport.add(center, centerData);
	}

	public void addContentFrame(String url, String title, Boolean isClosable,
			String color, boolean save, boolean select) {
		String cookieName = "";
		if (!url.equals(challengeUrl)) {
			if (url.contains("?")) {
				if (!url.contains("locale"))
					url += "&locale=" + lang;
			} else {
				if (!url.contains("locale"))
					url += "?locale=" + lang;
			}

			// set cookie url before adding URL parameters
			Date now = new Date();
			long nowLong = now.getTime();
			nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);// seven days
			now.setTime(nowLong);

			cookieName = "metaforaTab" + now.getTime() + ":" + title;
			if (save) {
				Cookies.setCookie(cookieName, url, now);
			}

			url += "&token=" + token;
			url += "&user=" + URL.encode(userName);
			url += "&pw=" + shamd5hash;
			url += "&pwEncrypted=true";
			if (!(url.contains("ptMap"))) {
				url += "&ptMap=" + URL.encode(mapName);
			}
			url += "&groupId=" + URL.encode(groupName);
			url += "&challengeId=" + challengeId;
			url += "&challengeName=" + URL.encode(challengeName);

			//TODO: Add parameter to init database!
			if (GWT.getModuleBaseURL().contains("metaforaserver")) {
				url += "&testServer=false";
			} else {
				url += "&testServer=true";
			}

			int counter = 1;
			Collection<String> cookies = Cookies.getCookieNames();
			for (String cn : cookies) {
				if (cn != null && cn.startsWith("metaforaUserOther")) {
					String user = Cookies.getCookie(cn);
					if ((user != null) && (!(user.equals(userName)))) {
						url += "&otherUser" + counter + "=" + URL.encode(user);
						counter++;
					}
				}
			}
		}

		if (openWidgets.containsKey(url)) {
			if (select) {
				Widget widget = openWidgets.get(url);
				TabItem item = openTabs.get(widget);
				mainTabPanel.setSelection(item);
			}
			return;
		}

		ContentPanel frame = new ContentPanel();
		frame.setLayout(new FitLayout());

		frame.setHeaderVisible(false);
		frame.setBorders(false);
		frame.setBodyBorder(false);

		frame.setScrollMode(Scroll.NONE);

		frame.setUrl(url);

		openWidgets.put(url, frame);

		if (color != null) {
			addContentTab(frame, title, isClosable, color, cookieName, save,
					select);
		} else {
			addContentTab(frame, title, true, "", cookieName, save, select);
		}
	}

	public void addContentFrame(String url, String title, boolean save,
			boolean select) {
		addContentFrame(url, title, true, null, save, select);
	}

	public void addContentTab(Widget w, String title, Boolean isClosable,
			String color, String cookieName, boolean save, boolean select) {

		TabItem item = null;

		if (color != null) {
			item = new ColoredTabItem(color);
		} else {
			item = new TabItem();
		}
		item.addStyleName("pad-text");

		item.setLayout(new FitLayout());

		item.setText(title);
		item.setClosable(isClosable);
		item.add(w, new MarginData(0, 15, 0, 0));

		item.setScrollMode(Scroll.NONE);

		item.addListener(Events.BeforeClose, new TabCloseListener(cookieName));

		openTabs.put(w, item);

		mainTabPanel.add(item);

		if (select)
			mainTabPanel.setSelection(item);
	}

	public void addContentTab(Widget w, String title, String cookieName,
			boolean save, boolean select) {

		addContentTab(w, title, true, null, cookieName, save, select);
	}

	public void tabClosed(TabItem item) {
		for (Widget w : openTabs.keySet()) {
			if (openTabs.get(w).equals(item)) {
				for (String url : openWidgets.keySet()) {
					if (openWidgets.get(url).equals(w)) {
						openWidgets.remove(url);
						openTabs.remove(w);

						logger.log(Level.INFO, Home.token + " Home.Home: Tab "
								+ url + " was closed.");

						return;
					}
				}
			}
		}
	}

	public Vector<String> getOpenTabs() {
		Vector<String> urls = new Vector<String>();
		urls.addAll(openWidgets.keySet());
		return urls;
	}

	public String getSelectedTabTitle() {
		return mainTabPanel.getSelectedItem().getText();
	}

	public String getSelectedTab() {
		TabItem item = mainTabPanel.getSelectedItem();
		for (Widget w : openTabs.keySet()) {
			if (openTabs.get(w).equals(item)) {
				for (String url : openWidgets.keySet()) {
					if (openWidgets.get(url).equals(w)) {
						return url;
					}
				}
			}
		}
		return "";
	}

	public void setServerPushState(boolean connected) {
		if (connected) {
			connectionState
					.setHTML("<span style=\"color: green;\">online</span>");
		} else {
			connectionState
					.setHTML("<span style=\"color: red;\">offline</span>");
		}
	}

	public void showFeedbackMessage(String text, boolean highPriority) {
		if (!getSelectedTabTitle().equals(language.Piki())) {
			if (highPriority) {
				FeedbackDialog dialog = new FeedbackDialog(text, true);

				if ((getSelectedTabTitle().equals(language.Jugger()))
						|| (getSelectedTabTitle().equals(language.Math()))) {
					dialog.setPosition(0, 0);
				} else {
					dialog.setPosition((Window.getClientWidth() - dialog
							.getOffsetWidth()) / 2 - 150,
							(Window.getClientHeight() - dialog
									.getOffsetHeight()) / 2 - 100);
				}
				dialog.show();
			} else {
				FeedbackDialog dialog = new FeedbackDialog(text, false);
				dialog.setPosition(((Window.getClientWidth() / 4) * 3) - 5,
						Window.getClientHeight() - 110);
				dialog.show();
			}
		}
	}

	public void hideLowInteruptiveMessage(FeedbackDialog dialog) {
		dialog.hide();
		messages.remove(dialog);
	}
}
