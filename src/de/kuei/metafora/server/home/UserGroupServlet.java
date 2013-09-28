package de.kuei.metafora.server.home;

import java.io.IOException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.kuei.metafora.server.home.manager.Usermanager;
import de.kuei.metafora.server.home.xml.Classification;
import de.kuei.metafora.server.home.xml.CommonFormatCreator;
import de.kuei.metafora.server.home.xml.Role;
import de.kuei.metafora.server.home.xml.XMLException;

public class UserGroupServlet extends HttpServlet {

	protected void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {

		String query = req.getQueryString();

		HashMap<String, String> parameter = new HashMap<String, String>();

		String[] parts = null;
		if (query != null) {
			parts = query.split("&");

			for (String part : parts) {
				String[] kv = part.split("=");
				if (kv.length == 2) {
					kv[0] = URLDecoder.decode(kv[0], "UTF-8");
					kv[1] = URLDecoder.decode(kv[1], "UTF-8");
					parameter.put(kv[0], kv[1]);
				} else {
					System.err.println("Invalid query: " + part);
				}
			}
		}

		if (!parameter.containsKey("groupId")) {
			String groupId = getGroupId(parameter);
			if (groupId == null) {
				groupId = "Metafora";
			}
			parameter.put("groupId", groupId);
		}

		if (parameter.containsKey("url")) {
			String url = parameter.get("url");

			//TODO: fix problem!
			if (!url.startsWith("http://"))
				url = URLDecoder.decode(url, "UTF-8");

			parameter.remove("url");
			parameter = getUrlExtension(parameter);

			for (String key : parameter.keySet()) {
				if (url.contains("?")) {
					url += "&";
				} else {
					url += "?";
				}

				url += URLEncoder.encode(key, "UTF-8") + "="
						+ URLEncoder.encode(parameter.get(key), "UTF-8");
			}

			res.sendRedirect(url);
		} else {

			String response = null;
			try {
				response = getXMLResponse(parameter);
			} catch (Exception e) {
				response = "Java Exception: " + e.getClass().getName() + ": "
						+ e.getMessage();
				System.err.println("Java Exception: " + e.getClass().getName()
						+ ": " + e.getMessage());
			}

			res.setCharacterEncoding("UTF-8");
			res.setContentType("text/plain");

			Writer writer = res.getWriter();
			writer.write(response);
			writer.flush();
			writer.close();
		}
	}

	private String getGroupId(HashMap<String, String> parameter) {
		String groupId = null;

		if (parameter.containsKey("token")) {
			groupId = Usermanager.getInstance().getGroupForToken(
					parameter.get("token"));
		}

		if (groupId == null && parameter.containsKey("user")) {
			groupId = Usermanager.getInstance().getGroupForToken(
					parameter.get("user"));
		}

		return groupId;
	}

	private HashMap<String, String> getUrlExtension(
			HashMap<String, String> parameter) {
		String groupId = parameter.get("groupId");

		Vector<String> users = getUsers(groupId);

		if (parameter.containsKey("messaging")
				&& parameter.get("messaging").toLowerCase().equals("true")) {
			String receiverIds = "";
			for (String user : users) {
				receiverIds += user + "|";
			}
			if (receiverIds.length() > 0)
				receiverIds = receiverIds
						.substring(0, receiverIds.length() - 1);
			parameter.put("receiverIDs", receiverIds);
		} else {
			for (int i = 0; i < users.size(); i++) {
				parameter.put("groupUser" + (i + 1), users.get(i));
			}
		}

		return parameter;
	}

	private String getXMLResponse(HashMap<String, String> parameter) {
		String answer = "";

		String token = "";
		if (parameter.containsKey("token")) {
			token = parameter.get("token");
		}

		String groupId = parameter.get("groupId");

		if (groupId == null) {
			answer = "Please add groupId, token or user to group!";
		} else {
			Vector<String> users = getUsers(groupId);

			try {
				CommonFormatCreator cfc = new CommonFormatCreator(
						System.currentTimeMillis(), Classification.other,
						"GROUP_OVERVIEW", false);

				cfc.setDescription("Users for group " + groupId);

				for (String user : users) {
					cfc.addUser(user, token, Role.originator);
				}

				answer = cfc.getDocument();
			} catch (XMLException e) {
				e.printStackTrace();
			}
		}

		return answer;
	}

	private Vector<String> getUsers(String groupId) {
		Vector<String> tokens = Usermanager.getInstance().getTokensForGroup(
				groupId);
		if (tokens != null && tokens.size() > 0) {
			return Usermanager.getInstance().getUsersForTokens(tokens);
		}
		return new Vector<String>();
	}
}
