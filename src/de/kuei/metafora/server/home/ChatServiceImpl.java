package de.kuei.metafora.server.home;

import java.util.Vector;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.chat.serverlink.ChatService;
import de.kuei.metafora.server.home.manager.Usermanager;
import de.kuei.metafora.server.home.xml.Classification;
import de.kuei.metafora.server.home.xml.CommonFormatCreator;
import de.kuei.metafora.server.home.xml.Role;
import de.kuei.metafora.server.home.xml.XMLException;
import de.kuei.metafora.server.home.xml.XMLUtils;
import de.kuei.metafora.server.home.xmpp.XMPPListener;
import de.kuei.metafora.shared.eventservice.events.ChatEvent;
import de.kuei.metafora.shared.eventservice.events.FeedbackEvent;

public class ChatServiceImpl extends RemoteServiceServlet implements
		ChatService {

	@Override
	public void sendChatMessage(String message, String challengeId,
			String challengeName, String token) {
		String team = Usermanager.getInstance().getTeamName(token);

		Vector<String> users = Usermanager.getInstance().getLocalUsers(token);
		String user = null;
		for (String u : users) {
			if (user == null)
				user = u;
			else
				user += ", " + u;
		}

		if (team != null) {
			if (users.size() == 0) {
				user = team;
			}

			ChatEvent chatEvent = new ChatEvent(user, team, message, token);
			Usermanager.getInstance().sendMessage(chatEvent,
					Usermanager.chatDomain);
		}

		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "CHAT_MESSAGE", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL", StartupServlet.toolname);

			for (String u : users) {
				creator.addUser(u, token, Role.originator);
			}

			creator.setObject("0", "CHAT_MESSAGE");
			String msg = message;
			creator.addProperty("MESSAGE", msg);

			if (team != null) {
				creator.addContentProperty("GROUP_ID", team);
			}
			if (challengeId.length() > 0) {
				creator.addContentProperty("CHALLENGE_ID", challengeId);
			}
			if (challengeName.length() > 0) {
				creator.addContentProperty("CHALLENGE_NAME", challengeName);
			}

			StartupServlet.sendToLogger(creator.getDocument());
		} catch (XMLException e) {
			e.printStackTrace();
		}

		// send indicator to analysis channel
		Document doc;
		try {
			String userAll = null;
			for (String u : users) {
				if (userAll == null)
					userAll = u;
				else
					userAll += "'s, " + u;
			}
			doc = XMLUtils.createDocument();
			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");
			actiontype.setAttribute("type", "INDICATOR");
			actiontype.setAttribute("classification", "other");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			if (users.size() > 0) {
				for (String otherUser : users) {
					Element userElement = doc.createElement("user");
					userElement.setAttribute("id", otherUser);
					userElement.setAttribute("role", "originator");
					action.appendChild(userElement);
				}
			}

			Element object = doc.createElement("object");
			object.setAttribute("id", userAll + "'s chat message");
			object.setAttribute("type", "CHAT_MESSAGE");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property;
			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "SENDING_TOOL");
			property.setAttribute("value", StartupServlet.toolname);

			if (message != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "TEXT");
				property.setAttribute("value", message);
			}

			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "GROUP_ID");
			property.setAttribute("value", team);

			Element content = doc.createElement("content");
			action.appendChild(content);

			Element description = doc.createElement("description");
			CDATASection cdata = doc.createCDATASection(user
					+ " sent a chat message!");
			description.appendChild(cdata);
			content.appendChild(description);

			Element contentProperties = doc.createElement("properties");
			content.appendChild(contentProperties);

			Element property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "INDICATOR_TYPE");
			property2.setAttribute("value", "ACTIVITY");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "SENDING_TOOL");
			property2.setAttribute("value", StartupServlet.toolname);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "ACTIVITY_TYPE");
			property2.setAttribute("value", "CHAT_MESSAGE");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_ID");
			property2.setAttribute("value", challengeId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_NAME");
			property2.setAttribute("value", challengeName);

			String xmlXMPPMessage = XMLUtils.documentToString(doc,
					"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");

			StartupServlet.sendToAnalysis(xmlXMPPMessage);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void openChatObject(String message, String user, String groupId,
			String challengeId, String challengeName, String token) {
		String url = null, urltext = null, graphName = null, nodeId = null, objectHomeTool = null;
		Vector<String> users = Usermanager.getInstance().getLocalUsers(token);
		// "urltext:\nurl:\ngraphname:\nnodeid:\ntool:\nuser"

		String[] parts = message.split("\n");
		if (parts.length < 4) {
			Window.alert("Invalid Chat Object.");
		} else {
			for (int i = 0; i < parts.length; i++) {
				String part = parts[i];
				if (part.startsWith("urltext:")) {
					urltext = part.substring(8, part.length());
				} else if (part.startsWith("url:")) {
					url = part.substring(4, part.length());
				} else if (part.startsWith("graphname:")) {
					graphName = part.substring(10, part.length());
				} else if (part.startsWith("nodeid:")) {
					nodeId = part.substring(7, part.length());
				} else if (part.startsWith("tool:")) {
					objectHomeTool = part.substring(5, part.length());
				}
			}
		}

		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "OPEN_REFERABLE_OBJECT",
					StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL", StartupServlet.toolname);

			if (users != null) {
				for (String u : users)
					creator.addUser(u, token, Role.originator);
			}
			creator.setObject("0", "REFERABLE_OBJECT");
			creator.addProperty("TEXT", urltext);
			creator.addProperty("REFERENCE_URL", url);
			creator.addProperty("NODE_ID", nodeId);
			creator.addProperty("GRAPH_NAME", graphName);
			creator.addProperty("OBJECT_HOME_TOOL", objectHomeTool);
			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			StartupServlet.sendToLogger(creator.getDocument());
		} catch (XMLException e) {
			e.printStackTrace();
		}

		String allUsers = null;
		for (String allU : users) {
			if (allUsers == null)
				allUsers = allU;
			else
				allUsers += ", " + allU;
		}

		// send indicator to analysis channel
		Document doc;
		try {
			doc = XMLUtils.createDocument();
			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");
			actiontype.setAttribute("type", "INDICATOR");
			actiontype.setAttribute("classification", "other");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			for (String u : users) {
				Element userElement = doc.createElement("user");
				userElement.setAttribute("id", u);
				userElement.setAttribute("role", "originator");
				action.appendChild(userElement);
			}

			Element object = doc.createElement("object");
			object.setAttribute("id", nodeId);
			object.setAttribute("type", "NODE_ID");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property;
			if (url != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "URL");
				property.setAttribute("value", url);
			}

			if (urltext != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "URL_TEXT");
				property.setAttribute("value", urltext);
			}

			if (graphName != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "GRAPH_NAME");
				property.setAttribute("value", graphName);
			}

			Element content = doc.createElement("content");
			action.appendChild(content);

			Element description = doc.createElement("description");
			CDATASection cdata = doc.createCDATASection(allUsers
					+ " opened Planning Tool node from chat!");
			description.appendChild(cdata);
			content.appendChild(description);

			Element contentProperties = doc.createElement("properties");
			content.appendChild(contentProperties);

			Element property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "INDICATOR_TYPE");
			property2.setAttribute("value", "ACTIVITY");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "SENDING_TOOL");
			property2.setAttribute("value", StartupServlet.toolname);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "ACTIVITY_TYPE");
			property2.setAttribute("value", "REFERABLE_CHAT_OBJECT");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "GROUP_ID");
			property2.setAttribute("value", groupId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_ID");
			property2.setAttribute("value", challengeId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_NAME");
			property2.setAttribute("value", challengeName);

			String xmlXMPPMessage = XMLUtils.documentToString(doc,
					"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");

			StartupServlet.sendToAnalysis(xmlXMPPMessage);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	public static void sendChatObjectLandmark(String url, String viewurl,
			String text, String user, String groupId, String challengeId,
			String challengeName, String objectHomeTool) {
		// send landmark to analysis channel

		Vector<String> allUsers = Usermanager.getInstance().getLocalUsers(
				Usermanager.getInstance().getIpForUser(user));
		String users = null;
		for (String a : allUsers) {
			if (users == null)
				users = a;
			else
				users += ", " + a;
		}
		Document doc;
		try {
			doc = XMLUtils.createDocument();
			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");
			actiontype.setAttribute("type", "LANDMARK");
			actiontype.setAttribute("classification", "create");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			for (String u : allUsers) {
				Element userElement = doc.createElement("user");
				userElement.setAttribute("id", u);
				userElement.setAttribute("role", "originator");
				action.appendChild(userElement);
			}

			Element object = doc.createElement("object");
			object.setAttribute("id", "0");
			object.setAttribute("type", "REFERABLE_OBJECT");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property;
			if (url != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "REFERENCE_URL");
				property.setAttribute("value", url);
			}

			if (viewurl != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "VIEW_URL");
				property.setAttribute("value", viewurl);
			}

			if (text != null) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "TEXT");
				property.setAttribute("value", text);
			}

			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "OBJECT_HOME_TOOL");
			property.setAttribute("value", objectHomeTool);

			Element content = doc.createElement("content");
			action.appendChild(content);

			Element description = doc.createElement("description");
			CDATASection cdata = doc.createCDATASection(users
					+ " created referable object in Metafora chat: " + text
					+ ".");
			description.appendChild(cdata);
			content.appendChild(description);

			Element contentProperties = doc.createElement("properties");
			content.appendChild(contentProperties);

			Element property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "LANDMARK_TYPE");
			property2.setAttribute("value", "ACTIVITY");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "ACTIVITY_TYPE");
			property2.setAttribute("value", "Referable object in chat");

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "SENDING_TOOL");
			property2.setAttribute("value", StartupServlet.toolname);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "GROUP_ID");
			property2.setAttribute("value", groupId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_ID");
			property2.setAttribute("value", challengeId);

			property2 = doc.createElement("property");
			contentProperties.appendChild(property2);
			property2.setAttribute("name", "CHALLENGE_NAME");
			property2.setAttribute("value", challengeName);

			String xmlXMPPMessage = XMLUtils.documentToString(doc,
					"http://metafora.ku-eichstaett.de/dtd/commonformat.dtd");

			StartupServlet.sendToAnalysis(xmlXMPPMessage);
		} catch (XMLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendLIFMessage(String msg, String challengeId,
			String challengeName, String token) {
		sendIFMessage(msg, challengeId, challengeName, token, false);
	}

	@Override
	public void sendHIFMessage(String msg, String challengeId,
			String challengeName, String token) {
		sendIFMessage(msg, challengeId, challengeName, token, true);
	}

	private void sendIFMessage(String msg, String challengeId,
			String challengeName, String token, boolean highInterruptive) {

		String team = Usermanager.getInstance().getTeamName(token);

		String interruption = XMPPListener.lowInteruptive;
		if (highInterruptive) {
			interruption = XMPPListener.highInteruptive;
		}

		FeedbackEvent feedbackEvent = new FeedbackEvent(interruption, team,
				msg, token);
		Usermanager.getInstance().sendMessage(feedbackEvent,
				Usermanager.chatDomain);

		// TODO: log and analysis messages
	}

}
