package de.kuei.metafora.server.home;

import java.util.Vector;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.server.HelpServerLink;
import de.kuei.metafora.server.home.manager.Usermanager;
import de.kuei.metafora.server.home.xml.Classification;
import de.kuei.metafora.server.home.xml.CommonFormatCreator;
import de.kuei.metafora.server.home.xml.Role;
import de.kuei.metafora.server.home.xml.XMLException;
import de.kuei.metafora.server.home.xml.XMLUtils;
import de.kuei.metafora.shared.eventservice.events.HelpEvent;

public class HelpService extends RemoteServiceServlet implements HelpServerLink {

	@Override
	public void help(String username, String groupId, String challengeId,
			String challengeName, String message, String selectedUrl,
			Vector<String> openUrls, String token, boolean group, boolean others) {
		String team = Usermanager.getInstance().getTeamName(token);

		Vector<String> users = Usermanager.getInstance().getLocalUsers(
				Usermanager.getInstance().getIpForUser(username));
		String allUsers = "";
		for (String u : users) {
			if (allUsers.length() < 1)
				allUsers += u;
			else
				allUsers += ", " + u;
		}

		if (team != null) {
			if (users.isEmpty()) {
				allUsers = team;
			}

			if (group) {
				HelpEvent helpEvent = new HelpEvent(allUsers, groupId, message,
						token);
				Usermanager.getInstance().sendMessage(helpEvent,
						Usermanager.chatDomain);
			}
		}

		try {
			CommonFormatCreator creator = null;
			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.other, "HELP_REQUEST", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL", StartupServlet.toolname);

			for (String a : users) {
				creator.addUser(a, token, Role.originator);
			}

			creator.setObject("0", "HELP_REQUEST");
			String msg = message;
			creator.addProperty("MESSAGE", msg);
			creator.addProperty("SELECTED_URL", selectedUrl);
			for (String url : openUrls) {
				creator.addProperty("OPEN_URL", url);
			}
			creator.addContentProperty("GROUP_ID", groupId);
			creator.addContentProperty("CHALLENGE_ID", challengeId);
			creator.addContentProperty("CHALLENGE_NAME", challengeName);

			StartupServlet.sendToLogger(creator.getDocument());
		} catch (XMLException e) {
			e.printStackTrace();
		}

		sendIndicator(users, message, token, selectedUrl, openUrls, groupId,
				challengeId, challengeName, allUsers);
		sendLandmark(users, message, token, selectedUrl, openUrls, groupId,
				challengeId, challengeName, allUsers);

		// TODO: test
		System.err.println("Others: " + others);
		if (others) {
			String helpMessage = allUsers + " of group " + groupId
					+ " need help and says: " + message;
			String feedback = generateFeedbackMessage(helpMessage);
			StartupServlet.sendToCommand(feedback);
			System.err.println(feedback);
		}
	}

	private void sendIndicator(Vector<String> users, String message,
			String token, String selectedUrl, Vector<String> openUrls,
			String groupId, String challengeId, String challengeName,
			String allUsers) {
		try {
			String allU = null;
			for (String u : users) {
				if (allU == null)
					allU += u;
				else
					allU += "'s, " + u;
			}

			// send indicator to analysis channel
			Document doc = XMLUtils.createDocument();

			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");
			actiontype.setAttribute("type", "INDICATOR");
			actiontype.setAttribute("classification", "other");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			for (String b : users) {
				Element userElement = doc.createElement("user");
				userElement.setAttribute("id", b);
				userElement.setAttribute("role", "originator");
				action.appendChild(userElement);
			}

			Element object = doc.createElement("object");
			object.setAttribute("id", allU + "'s Help Request");
			object.setAttribute("type", "HELP_REQUEST");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "SENDING_TOOL");
			property.setAttribute("value", StartupServlet.toolname);

			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "TEXT");
			property.setAttribute("value", message);

			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "SELECTED_URL");
			property.setAttribute("value", selectedUrl);

			for (String url : openUrls) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "OPEN_URL");
				property.setAttribute("value", url);
			}

			Element content = doc.createElement("content");
			action.appendChild(content);

			if (users.size() > 1) {
				Element description = doc.createElement("description");
				CDATASection cdata = doc.createCDATASection(allUsers
						+ " need help and says: " + message);
				description.appendChild(cdata);
				content.appendChild(description);
			} else {
				Element description = doc.createElement("description");
				CDATASection cdata = doc.createCDATASection(allUsers
						+ " needs help and says: " + message);
				description.appendChild(cdata);
				content.appendChild(description);
			}

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
			property2.setAttribute("value", "HELP_REQUEST");

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

	private void sendLandmark(Vector<String> users, String message,
			String token, String selectedUrl, Vector<String> openUrls,
			String groupId, String challengeId, String challengeName,
			String allUsers) {
		try {

			String allU = null;
			for (String u : users) {
				if (allU == null)
					allU += u;
				else
					allU += "'s, " + u;
			}

			// send indicator to analysis channel
			Document doc = XMLUtils.createDocument();

			Element action = doc.createElement("action");
			action.setAttribute("time", System.currentTimeMillis() + "");
			doc.appendChild(action);

			Element actiontype = doc.createElement("actiontype");
			actiontype.setAttribute("type", "LANDMARK");
			actiontype.setAttribute("classification", "other");
			actiontype.setAttribute("logged", StartupServlet.logged + "");
			action.appendChild(actiontype);

			for (String b : users) {
				Element userElement = doc.createElement("user");
				userElement.setAttribute("id", b);
				userElement.setAttribute("role", "originator");
				action.appendChild(userElement);
			}

			Element object = doc.createElement("object");
			object.setAttribute("id", allU + "'s Help Request");
			object.setAttribute("type", "HELP_REQUEST");
			action.appendChild(object);

			Element properties = doc.createElement("properties");
			object.appendChild(properties);

			Element property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "SENDING_TOOL");
			property.setAttribute("value", StartupServlet.toolname);

			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "TEXT");
			property.setAttribute("value", message);

			property = doc.createElement("property");
			properties.appendChild(property);
			property.setAttribute("name", "SELECTED_URL");
			property.setAttribute("value", selectedUrl);

			for (String url : openUrls) {
				property = doc.createElement("property");
				properties.appendChild(property);
				property.setAttribute("name", "OPEN_URL");
				property.setAttribute("value", url);
			}

			Element content = doc.createElement("content");
			action.appendChild(content);

			if (users.size() > 1) {
				Element description = doc.createElement("description");
				CDATASection cdata = doc.createCDATASection(allUsers
						+ " need help and says: " + message);
				description.appendChild(cdata);
				content.appendChild(description);
			} else {
				Element description = doc.createElement("description");
				CDATASection cdata = doc.createCDATASection(allUsers
						+ " needs help and says: " + message);
				description.appendChild(cdata);
				content.appendChild(description);
			}

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
			property2.setAttribute("value", "HELP_REQUEST");

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

	public static String generateFeedbackMessage(String message) {

		String interruptionType = "no_interruption";

		try {
			CommonFormatCreator cfc = new CommonFormatCreator(
					System.currentTimeMillis(), Classification.create,
					"FEEDBACK", true);

			for (String user : Usermanager.getInstance().getActiveUsers()) {
				cfc.addUser(user, "", Role.receiver);
			}

			cfc.setObject("0", "MESSAGE");
			cfc.addProperty("INTERRUPTION_TYPE", interruptionType);
			cfc.addProperty("TEXT", message);

			cfc.setDescription(message);

			cfc.addContentProperty("SENDING_TOOL", StartupServlet.toolname);
			cfc.addContentProperty("RECEIVING_TOOL", StartupServlet.toolname);

			return cfc.getDocument();

		} catch (XMLException e) {
			// ignore
		}
		return null;
	}
}
