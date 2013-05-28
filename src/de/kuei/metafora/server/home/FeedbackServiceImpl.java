package de.kuei.metafora.server.home;

import java.util.Vector;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import de.kuei.metafora.client.feedback.server.FeedbackService;
import de.kuei.metafora.server.home.manager.Usermanager;
import de.kuei.metafora.server.home.xml.Classification;
import de.kuei.metafora.server.home.xml.CommonFormatCreator;
import de.kuei.metafora.server.home.xml.Role;

public class FeedbackServiceImpl extends RemoteServiceServlet implements
		FeedbackService {

	@Override
	public void sendFeedbackMessage(String msg, String user, String tool,
			String interruptionType) {
		CommonFormatCreator creator = null;
		Vector<String> users = null;
		try {
			if (tool.equals("PiKI"))
				tool = StartupServlet.pikiName;
			else if (tool.equals("Sus-X"))
				tool = StartupServlet.suscityName;
			else if (tool.equals("Juggler"))
				tool = StartupServlet.jugglerName;
			else if (tool.equals("3D-Math"))
				tool = StartupServlet.mathName;

			creator = new CommonFormatCreator(System.currentTimeMillis(),
					Classification.create, "FEEDBACK", StartupServlet.logged);
			creator.addContentProperty("SENDING_TOOL", StartupServlet.toolname);
			creator.addContentProperty("RECEIVING_TOOL", tool);

			users = Usermanager.getInstance().getLocalUsers(
					Usermanager.getInstance().getIpForUser(user));
			for (String u : users)
				creator.addUser(u,
						Usermanager.getInstance().getIpForUser(user),
						Role.receiver);

			creator.setObject("0", "MESSAGE");
			creator.addProperty("TEXT", msg);
			creator.addProperty("INTERRUPTION_TYPE", interruptionType);

			StartupServlet.sendToCommand(creator.getDocument());
		} catch (Exception e) {
			System.err
					.println("FeedbackServiceImpl.sendFeedbackMessage() exception: "
							+ e.toString());
		}
	}
}
