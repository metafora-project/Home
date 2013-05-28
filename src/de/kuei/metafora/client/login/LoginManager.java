package de.kuei.metafora.client.login;

import java.util.Vector;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.ColoredButton;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.SuggestBox;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.handler.LoginHandler;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;

/**
 * LoginManager shows UI and executes actions for user login.
 */
public class LoginManager {

	// i18n 
	final static Languages language = GWT.create(Languages.class);

	private static LoginManager loginManager = null;

	/**
	 * Returns the only existing instance of LoginManager.
	 * 
	 * @return LoginManager instance
	 */
	public static LoginManager getInstance() {
		if (loginManager == null) {
			loginManager = new LoginManager();
		}
		return loginManager;
	}

	private UserLinkAsync userLink = GWT.create(UserLink.class);

	private MultiWordSuggestOracle oracle;
	private SuggestBox sbxUsername;
	private PasswordTextBox txtbxPassword;
	private ColoredButton btnLogin;
	private LayoutPanel layout;

	/**
	 * UI for login screen.
	 */
	public void main() {
		final LoginHandler handler = new LoginHandler();

		layout = new LayoutPanel();

		HTML image = new HTML(
				"<div style='margin-left:0px;margin-top:0px;'><img src='gxt/images/logo_metafora.png' alt='logo'/></div>");
		layout.add(image);
		layout.setWidgetLeftWidth(image, 5, Unit.PX, 197, Unit.PX);
		layout.setWidgetTopHeight(image, 10, Unit.PX, 34, Unit.PX);

		LayoutPanel content = new LayoutPanel();
		content.getElement().getStyle().setBorderWidth(1, Unit.PX);
		content.getElement().getStyle().setBorderColor("#AABBCC");
		content.getElement().getStyle().setBorderStyle(BorderStyle.SOLID);

		HTML title = new HTML(
				"<div style='font-size: 14pt; text-align: center;'>"
						+ language.LoginTitle() + "</div>");
		content.add(title);
		content.setWidgetLeftRight(title, 5, Unit.PX, 5, Unit.PX);
		content.setWidgetTopHeight(title, 5, Unit.PX, 70, Unit.PX);

		HTML nameLabel = new HTML(language.Name());
		content.add(nameLabel);
		content.setWidgetLeftWidth(nameLabel, 10, Unit.PX, 100, Unit.PX);
		content.setWidgetTopHeight(nameLabel, 87, Unit.PX, 25, Unit.PX);

		createOracle();
		sbxUsername = new SuggestBox(oracle);
		sbxUsername.setWidth("95%");
		sbxUsername.setText("");
		sbxUsername.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if ((sbxUsername.getText() != null)
						&& (sbxUsername.getText().length() > 0)
						&& (sbxUsername.getText() != "")
						&& (txtbxPassword.getText() != null)
						&& (txtbxPassword.getText().length() > 0)
						&& (txtbxPassword.getText() != "")) {
					btnLogin.enable();
				}
			}
		});

		content.add(sbxUsername);
		content.setWidgetLeftRight(sbxUsername, 120, Unit.PX, 10, Unit.PX);
		content.setWidgetTopHeight(sbxUsername, 80, Unit.PX, 25, Unit.PX);

		HTML passwordLabel = new HTML(language.Password());
		content.add(passwordLabel);
		content.setWidgetLeftWidth(passwordLabel, 10, Unit.PX, 100, Unit.PX);
		content.setWidgetTopHeight(passwordLabel, 127, Unit.PX, 25, Unit.PX);

		txtbxPassword = new PasswordTextBox();
		txtbxPassword.setText("");
		txtbxPassword.setWidth("95%");
		txtbxPassword.addKeyPressHandler(handler);
		txtbxPassword.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if ((sbxUsername.getText() != null)
						&& (sbxUsername.getText().length() > 0)
						&& (sbxUsername.getText() != "")
						&& (txtbxPassword.getText() != null)
						&& (txtbxPassword.getText().length() > 0)
						&& (txtbxPassword.getText() != "")) {
					btnLogin.enable();
				}
			}
		});

		content.add(txtbxPassword);
		content.setWidgetLeftRight(txtbxPassword, 120, Unit.PX, 10, Unit.PX);
		content.setWidgetTopHeight(txtbxPassword, 120, Unit.PX, 25, Unit.PX);

		btnLogin = new ColoredButton(language.Login().toUpperCase(), "green");
		btnLogin.setWidth("100%");
		btnLogin.disable();
		content.add(btnLogin);
		content.setWidgetBottomHeight(btnLogin, 10, Unit.PX, 30, Unit.PX);
		content.setWidgetLeftWidth(btnLogin, 30, Unit.PCT, 40, Unit.PCT);

		Listener<ButtonEvent> loginListener = new Listener<ButtonEvent>() {
			public void handleEvent(ButtonEvent ce) {
				handler.onClick(null);
			}
		};
		btnLogin.addListener(Events.OnClick, loginListener);

		layout.add(content);
		layout.setWidgetLeftWidth(content, 15, Unit.PCT, 70, Unit.PCT);
		layout.setWidgetTopHeight(content, 50, Unit.PX, 300, Unit.PX);

		layout.setHeight("100%");
		layout.setWidth("100%");
		RootLayoutPanel.get().add(layout);
	}

	/**
	 * Returns the entered username from the TextBox.
	 * 
	 * @return String
	 */
	public String getUsername() {
		return sbxUsername.getText();
	}

	/**
	 * Returns the entered password from the TextBox.
	 * 
	 * @return String
	 */
	public String getPassword() {
		return txtbxPassword.getText();
	}

	/**
	 * Clears username and password TextBox.
	 */
	public void clearFields() {
		this.sbxUsername.setText("");
		this.txtbxPassword.setText("");
	}

	/**
	 * Hides UI of LoginManager.
	 */
	public void hideUI() {
		layout.setVisible(false);
	}

	private void createOracle() {
		oracle = new MultiWordSuggestOracle();

		userLink.getUsernames(Home.token, new AsyncCallback<Vector<String>>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(Vector<String> result) {
				for (String uname : result) {
					oracle.add(uname);
				}
			}
		});
	}
}