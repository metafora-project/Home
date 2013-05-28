package de.kuei.metafora.client.login.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.GroupManager;
import de.kuei.metafora.client.login.LoginManager;

public class LoginCallback implements AsyncCallback<Integer> {

	final static Languages language = GWT.create(Languages.class);
	private String user;
	private String shaPassword;
	private String md5Password;

	public LoginCallback(String user, String md5Password, String shaPassword) {
		this.user = user;
		this.shaPassword = shaPassword;
		this.md5Password = md5Password;
	}

	public void onSuccess(Integer result) {
		if (result == -1) {
			Window.alert(language.YourPasswordWasWrong());
		} else {
			LoginManager.getInstance().clearFields();
			LoginManager.getInstance().hideUI();
			Home.lastFrame.setLoginInformation(user, md5Password, shaPassword);
			GroupManager.getInstance().main();
		}
	}

	public void onFailure(Throwable caught) {
		Window.alert(language.LoginFailed()+"\nLoginHandler userLink.login():\n"
				+ caught.getMessage() + "\n" + caught.getCause());
	}
}
