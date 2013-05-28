package de.kuei.metafora.client.team;

import java.util.Collection;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;

public class LocalUserGui extends LayoutPanel implements ClickHandler {

	// i18n 
	final static Languages language = GWT.create(Languages.class);

	private UserLinkAsync userLink = GWT.create(UserLink.class);

	private Button logout;
	private HTML name;
	private boolean local;

	private LayoutPanel userList;

	private String userName;

	public LocalUserGui(String name, LayoutPanel userList, boolean local) {
		this.local = local;

		this.userName = name;

		if (!local) {
			name += " (" + language.Remote() + ") ";
		}

		this.name = new HTML(name + " ");
		add(this.name);
		setWidgetLeftRight(this.name, 5, Unit.PX, 30, Unit.PX);
		setWidgetTopHeight(this.name, 5, Unit.PX, 20, Unit.PX);

		logout = new Button("X");
		logout.setTitle(language.Logout() + " " + name);
		logout.addClickHandler(this);
		logout.setWidth("100%");
		logout.setHeight("100%");
		if (local) {
			add(logout);
			setWidgetTopHeight(logout, 1, Unit.PX, 28, Unit.PX);
			setWidgetRightWidth(logout, 5, Unit.PX, 25, Unit.PX);
		}

		this.userList = userList;
	}

	private void remove() {
		userList.remove(this);
	}

	@Override
	public void onClick(ClickEvent event) {
		userLink.logout(userName, Home.groupName, Home.challengeId,
				Home.challengeName, Home.token, new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(language.LogoutFailed()+"\nLocalUserGui userLink.logout():\n"
								+ caught.getMessage()
								+ "\n"
								+ caught.getCause());
					}

					@Override
					public void onSuccess(Void result) {
						remove();
						if (TeamWidget.getInstance().getFirstUser() == null) {
							Collection<String> cookies = Cookies
									.getCookieNames();
							for (String cookie : cookies) {
								if (cookie.startsWith("metafora")) {
									Cookies.removeCookie(cookie);
								}
							}

							// reload page
							String url = Window.Location.getHref();
							Window.Location.assign(url);
						} else {
							Collection<String> cookies = Cookies
									.getCookieNames();
							for (String cookie : cookies) {
								if (cookie != null) {
									if (cookie.equals("metaforaUserOther"
											+ userName)
											|| cookie
													.equals("metaforaPasswordOther"
															+ userName)) {
										Cookies.removeCookie(cookie);
									} else if (userName.equals(Cookies
											.getCookie("metaforaUser"))) {
										Cookies.removeCookie(cookie);

										for (String abc : cookies) {
											if (abc != null
													&& abc.equals("metaforaPassword")) {
												Cookies.removeCookie(abc);
											}
										}

										String otherUser = null;
										String otherPassword = null;

										for (String cn : cookies) {
											String firstUser = TeamWidget
													.getInstance()
													.getFirstUser();
											if (cn != null
													&& cn.startsWith("metaforaUserOther")
													&& firstUser.equals(Cookies
															.getCookie(cn))) {
												otherUser = Cookies
														.getCookie(cn);
											}
										}
										for (String cook : cookies) {
											if (cook != null
													&& cook.equals("metaforaPasswordOther"
															+ otherUser)) {
												otherPassword = Cookies
														.getCookie(cook);
											}
										}

										Date now = new Date();
										long nowLong = now.getTime();
										nowLong = nowLong
												+ (1000 * 60 * 60 * 20);// 20
																		// hours
										now.setTime(nowLong);

										Cookies.setCookie("metaforaUser",
												otherUser, now);
										Cookies.setCookie("metaforaPassword",
												otherPassword, now);
										Home.lastFrame.setLoginInformation(
												otherUser, otherPassword,
												otherPassword);
										Cookies.removeCookie("metaforaUserOther"
												+ otherUser);
										Cookies.removeCookie("metaforaPasswordOther"
												+ otherUser);
									}
								}
							}
						}
					}
				});
	}

	public boolean isLocalUser() {
		return local;
	}
}
