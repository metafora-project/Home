package de.kuei.metafora.server.home.xmpp;

import java.util.Collection;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.packet.DelayInfo;
import org.jivesoftware.smackx.packet.DelayInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.kuei.metafora.server.home.ChatServiceImpl;
import de.kuei.metafora.server.home.StartupServlet;
import de.kuei.metafora.server.home.manager.Usermanager;
import de.kuei.metafora.server.home.xml.XMLException;
import de.kuei.metafora.server.home.xml.XMLUtils;
import de.kuei.metafora.shared.eventservice.events.ChatEvent;
import de.kuei.metafora.shared.eventservice.events.ChatObjectEvent;
import de.kuei.metafora.shared.eventservice.events.FeedbackEvent;
import de.kuei.metafora.shared.eventservice.events.LoginErrorEvent;
import de.kuei.metafora.shared.eventservice.events.OpenUrlEvent;

public class XMPPListener implements PacketListener, HttpSessionListener {

	public static String highInteruptive = "HIGH_INTERRUPTION";
	public static String lowInteruptive = "LOW_INTERRUPTION";
	public static String noneInteruptive = "NO_INTERRUPTION";

	private static XMPPListener instance = null;

	public static XMPPListener getInstance() {
		if (instance == null) {
			instance = new XMPPListener();
		}
		return instance;
	}

	public XMPPListener() {
		instance = this;
	}

	@Override
	public void processPacket(Packet packet) {
		try {
			handlePacket(packet);
		} catch (Exception e) {
			System.err.println("Home: XMPPListener: Handle packet exception! "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	public void handlePacket(Packet packet) {
		if (packet instanceof Message) {
			Message msg = (Message) packet;

			if (msg.getBody() == null) {
				return;
			}

			String from = msg.getFrom();
			String name = "";
			String chat = from;

			int splitPos = from.indexOf('/');
			if (splitPos > 0) {
				name = from.substring(splitPos + 1, from.length());
				chat = from.substring(0, splitPos);
			}

			Date time = new Date();

			Collection<PacketExtension> extensions = packet.getExtensions();
			for (PacketExtension e : extensions) {
				if (e instanceof DelayInfo) {
					DelayInfo d = (DelayInfo) e;
					time = d.getStamp();
					break;
				} else if (e instanceof DelayInformation) {
					DelayInformation d = (DelayInformation) e;
					time = d.getStamp();
					break;
				}
			}

			newMessage(name, msg.getBody(), chat, time);
		}
	}

	public void newMessage(String user, String message, String chat, Date time) {
		try {
			if ((chat.contains("logger")) || (chat.contains("analysis")))
				return;

			try {
				message = message.replaceAll("\n", "");
			} catch (Exception e) {
				System.err
						.println("Home: XMPPListener.newMessage(): exception: "
								+ e.toString());
			}

			if (message == null)
				return;

			if (message
					.matches(".*[<][ ]*[Aa][Cc][Tt][Ii][Oo][Nn][ ]*time.*[<][/][ ]*[Aa][Cc][Tt][Ii][Oo][Nn][ ]*[>].*")) {
				try {
					Document doc = XMLUtils.parseXMLString(message, false);
					// --- referable Objects in Chat ----
					boolean chatobject = false;
					boolean chatmessage = false;
					String tool = null;

					NodeList properties = doc.getElementsByTagName("property");
					for (int i = 0; i < properties.getLength(); i++) {
						Node propertyNode = properties.item(i);
						String name = propertyNode.getAttributes()
								.getNamedItem("name").getTextContent();
						if (name.toLowerCase().equals("receiving_tool")) {
							tool = propertyNode.getAttributes()
									.getNamedItem("value").getTextContent();
						}
					}

					if ((tool != null)
							&& (tool.toLowerCase()
									.equals(StartupServlet.toolname
											.toLowerCase()))) {

						NodeList actiontypenl = doc
								.getElementsByTagName("actiontype");
						if (actiontypenl.getLength() > 0) {
							Node actiontype = actiontypenl.item(0);
							String type = actiontype.getAttributes()
									.getNamedItem("type").getTextContent();
							if (type.toLowerCase().equals(
									"CREATE_REFERABLE_OBJECT".toLowerCase())) {
								chatobject = true;
								System.err
										.println("Home: XMPPListener.newMessage(): Chat Referable Object found.");
							} else if (type.toLowerCase().equals(
									"CHATMESSAGE".toLowerCase())) {
								chatmessage = true;
								System.err
										.println("Home: XMPPListener.newMessage(): Chat Message found.");
							} else if (type.toLowerCase().matches("feedback")) {
								System.err
										.println("Home: XMPPListener.newMessage(): new feedback message found: "
												+ message);
								String priority = highInteruptive;
								Vector<String> tokens = new Vector<String>();
								String msg = null;
								Vector<String> users = new Vector<String>();

								NodeList userList = doc
										.getElementsByTagName("user");
								if (userList.getLength() > 0) {
									for (int i = 0; i < userList.getLength(); i++) {
										Node userNode = userList.item(i);
										String name = userNode.getAttributes()
												.getNamedItem("id")
												.getTextContent();
										String role = userNode.getAttributes()
												.getNamedItem("role")
												.getTextContent();
										if ((role.toLowerCase()
												.equals("receiver"))
												&& (name != null)) {
											users.add(name);
										}
									}
								} else {
									System.err
											.println("Home: XMPPListener.newMessage(): No user found in feedback message. Sending message to group.");
								}

								String groupId = null;

								NodeList propertiesList = doc
										.getElementsByTagName("property");
								for (int k = 0; k < propertiesList.getLength(); k++) {
									Node propNode = propertiesList.item(k);
									String name = propNode.getAttributes()
											.getNamedItem("name")
											.getTextContent();
									String value = propNode.getAttributes()
											.getNamedItem("value")
											.getTextContent();
									if (name.toLowerCase().equals("text")) {
										msg = value;
									} else if (name.toLowerCase().equals(
											"interruption_type")) {
										if (value.toLowerCase().equals(
												"no_interruption")) {
											priority = noneInteruptive;
										} else if (value.toLowerCase().equals(
												"low_interruption")) {
											priority = lowInteruptive;
										}
									} else if (name.toLowerCase().equals(
											"group_id")) {
										groupId = value;
									}
								}

								System.err
										.println("Home.XMPPListener: Group ID: "
												+ groupId
												+ ", Users: "
												+ users.size());

								if (groupId != null && users.size() == 0) {
									Vector<String> groupTokens = Usermanager
											.getInstance().getTokensForTeam(
													groupId);
									tokens.addAll(groupTokens);
									System.err
											.println("Home.XMPPListener: groupTokens: "
													+ groupTokens.size());
								} else if (users.size() > 0) {
									for (int j = 0; j < users.size(); j++) {
										String token = Usermanager
												.getInstance().getIpForUser(
														users.get(j));
										if (token != null
												&& !tokens.contains(token)) {
											tokens.add(token);
										} else {
											System.err
													.println("Home: XMPPListener.newMessage(): no token for user "
															+ users.get(j)
															+ " found.");
										}
									}
								}

								if ((msg != null) && (!msg.equals(""))
										&& (tokens.size() > 0)) {
									for (String token : tokens) {
										String group = Usermanager
												.getInstance().getTeamName(
														token);

										FeedbackEvent feedbackEvent = new FeedbackEvent(
												priority, group, msg, token);
										Usermanager.getInstance().sendMessage(
												feedbackEvent,
												Usermanager.chatDomain);
									}
								}
							} else if (type.toLowerCase().equals(
									"CREATE_USER".toLowerCase())) {
								String succeed = actiontype.getAttributes()
										.getNamedItem("succeed")
										.getTextContent();
								if (succeed.toLowerCase().equals("false")) {
									NodeList propertiesList = doc
											.getElementsByTagName("property");
									for (int i = 0; i < propertiesList
											.getLength(); i++) {
										Node propertyNode = propertiesList
												.item(i);
										String name = propertyNode
												.getAttributes()
												.getNamedItem("name")
												.getTextContent();
										String value = propertyNode
												.getAttributes()
												.getNamedItem("value")
												.getTextContent();
										if (name.toLowerCase().equals(
												"username")) {
											String token = Usermanager
													.getInstance()
													.getIpForUser(value);
											if (token != null) {
												String msg = "Creating new user "
														+ value
														+ " in LASAD failed! No login to LASAD possible.";
												LoginErrorEvent loginErrorEvent = new LoginErrorEvent(
														msg, token);
												Usermanager
														.getInstance()
														.sendMessage(
																loginErrorEvent,
																Usermanager.userDomain);

												System.out
														.println(token
																+ ": loginerroralert: creating new user "
																+ value
																+ " in LASAD failed!");
											} else
												System.err
														.println("Home: XMPPListener.newMessage(): no token for user "
																+ value
																+ " found.");
										}
									}
								}
							} else if (type.toLowerCase().equals(
									"DISPLAY_STATE_URL".toLowerCase())) {
								Vector<String> ips = new Vector<String>();
								String ip = null;
								String url = null;

								NodeList prop = doc
										.getElementsByTagName("property");
								for (int i = 0; i < prop.getLength(); i++) {
									Node propertyNode = prop.item(i);
									String name = propertyNode.getAttributes()
											.getNamedItem("name")
											.getTextContent();
									if (name.toLowerCase().equals(
											"reference_url")) {
										url = propertyNode.getAttributes()
												.getNamedItem("value")
												.getTextContent();
									}
								}

								NodeList userNL = doc
										.getElementsByTagName("user");
								for (int j = 0; j < userNL.getLength(); j++) {
									Node userNode = userNL.item(j);
									String uname = userNode.getAttributes()
											.getNamedItem("id")
											.getTextContent();
									ip = Usermanager.getInstance()
											.getIpForUser(uname);

									if (ip != null && !ips.contains(ip)) {
										ips.add(ip);
									}
								}

								System.err
										.println("Home: XMPPListener: DIPLAY_STATE_URL: URL: "
												+ url + ", IPs: " + ips.size());

								if ((url != null) && (ips.size() > 0)) {
									if (url.contains("$USERNAME(")
											| url.contains("$Username(")
											| url.contains("$username(")) {
										String parts[] = url
												.split("[$][Uu][Ss][Ee][Rr][Nn][Aa][Mm][Ee][(]");

										for (int i = 1; i < parts.length; i++) {
											int e = parts[i].indexOf(')');
											if (e > 0) {
												String ipsub = parts[i]
														.substring(0, e);
												if (!ips.contains(ipsub))
													ips.add(ipsub);
											}
										}
										for (String ipPart : ips) {
											String username = Usermanager
													.getInstance()
													.getUserForIP(ip);
											ipPart = ipPart.replaceAll("[.]",
													"[.]");
											if (username != null) {
												url = url.replaceAll(
														"[$][Uu][Ss][Ee][Rr][Nn][Aa][Mm][Ee][(]"
																+ ip + "[)]",
														username);
											} else {
												url = url.replaceAll(
														"[$][Uu][Ss][Ee][Rr][Nn][Aa][Mm][Ee][(]"
																+ ip + "[)]",
														"unknown");
											}
										}
									}

									OpenUrlEvent openUrlEvent = new OpenUrlEvent(
											url, ip);
									Usermanager.getInstance().sendMessage(
											openUrlEvent,
											Usermanager.framworkDomain);
								} else
									System.err
											.println("url or ip is null, DISPLAY_STATE_URL not possible. URL: "
													+ url + ", IP: " + ip);
							}
						}
					}

					if (chatobject) {
						String username = null;
						String groupName = null;

						NodeList userType = doc.getElementsByTagName("user");
						Node userNode = userType.item(0);
						username = userNode.getAttributes().getNamedItem("id")
								.getTextContent();

						if (username != null) {
							groupName = Usermanager.getInstance()
									.getTeamForUser(username);
						}

						if (groupName != null) {
							String url = null;
							String viewurl = null;
							String text = null;
							String objectHomeTool = null;
							String challengeId = null;
							String challengeName = null;

							NodeList propeties = doc
									.getElementsByTagName("property");
							for (int i = 0; i < propeties.getLength(); i++) {
								Node property = propeties.item(i);
								String propname = property.getAttributes()
										.getNamedItem("name").getTextContent();
								if (propname.toLowerCase().equals(
										"reference_url")) {
									url = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (propname.toLowerCase().equals(
										"view_url")) {
									viewurl = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (propname.toLowerCase()
										.equals("text")) {
									text = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (propname.toLowerCase().equals(
										"object_home_tool")) {
									objectHomeTool = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (propname.toLowerCase().equals(
										"challenge_id")) {
									challengeId = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								} else if (propname.toLowerCase().equals(
										"challenge_name")) {
									challengeName = property.getAttributes()
											.getNamedItem("value")
											.getTextContent();
								}
							}

							System.err
									.println("Home: XMPPListener: Chat referable object. Url: "
											+ url);

							ChatObjectEvent chatObjectEvent = new ChatObjectEvent(
									groupName, text, url, objectHomeTool,
									username, viewurl);
							Usermanager.getInstance().sendMessage(
									chatObjectEvent, Usermanager.chatDomain);

							ChatServiceImpl.sendChatObjectLandmark(url,
									viewurl, text, username, groupName,
									challengeId, challengeName, objectHomeTool);
						} else {
							System.err
									.println("Home: XMPPListener.newMessage(): Chat message dropped! Invalid groupname.");
						}
					} else if (chatmessage) {
						Vector<String> users = new Vector<String>();

						NodeList userType = doc.getElementsByTagName("user");
						for (int i = 0; i < userType.getLength(); i++) {
							Node userNode = userType.item(i);
							String username = userNode.getAttributes()
									.getNamedItem("id").getTextContent();
							users.add(username);
						}

						Vector<String> groups = new Vector<String>();

						for (String username : users) {
							String groupName = Usermanager.getInstance()
									.getTeamForUser(username);
							if (!groups.contains(groupName)) {
								groups.add(groupName);
							}
						}

						if (groups.size() > 0) {

							String msg = null;

							NodeList propertiesList = doc
									.getElementsByTagName("property");
							for (int k = 0; k < propertiesList.getLength(); k++) {
								Node propNode = propertiesList.item(k);
								String name = propNode.getAttributes()
										.getNamedItem("name").getTextContent();
								String value = propNode.getAttributes()
										.getNamedItem("value").getTextContent();
								if (name.toLowerCase().equals("text")) {
									msg = value;
								}
							}

							if (msg != null) {
								for (String group : groups) {
									ChatEvent chatEvent = new ChatEvent(
											"Deep Thought", group, msg,
											"DEEP-THOUGHT-TOKEN");
									Usermanager.getInstance().sendMessage(
											chatEvent, Usermanager.chatDomain);
								}
							}
						} else {
							System.err
									.println("Home: XMPPListener.newMessage(): Chat message dropped! Invalid groupname.");
						}
					}
				} catch (XMLException e) {
					System.err
							.println("Home: XMPPListener.newMessage(): message dropped: "
									+ message + "\n" + e.getMessage());
				}
			}

		} catch (Exception e) {
			System.err.println("Home XMPPListener.newMessage(): "
					+ e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {

	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {

	}
}
