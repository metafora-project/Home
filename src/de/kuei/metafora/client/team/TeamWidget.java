package de.kuei.metafora.client.team;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.googlecode.gwt.crypto.util.SecureRandom;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;
import de.kuei.metafora.client.util.InputFilter;
import de.kuei.metafora.client.util.MD5Algo;

public class TeamWidget extends LayoutPanel implements KeyPressHandler,
		ClickHandler {

	// i18n
	final static Languages language = GWT.create(Languages.class);

	private final static int ITERATION_NUMBER = 1000;

	private static TeamWidget instance = null;

	public static TeamWidget getInstance() {
		if (instance == null) {
			instance = new TeamWidget();
		}
		return instance;
	}

	private UserLinkAsync userLink = GWT.create(UserLink.class);

	private SuggestBox user;
	private PasswordTextBox password;
	private SuggestBox teamName;
	private MultiWordSuggestOracle oracle;
	private MultiWordSuggestOracle oracleTeam;
	private Button btnLogin;
	private Button btnSetGroupName;
	private LayoutPanel localUsers;
	private Map<String, LocalUserGui> users;
	private String team = language.Group();

	private HTML teamHtml;

	private TeamWidget() {
		super();
		localUsers = new LayoutPanel();
		add(localUsers);

		users = new HashMap<String, LocalUserGui>();
		createOracle();
		createOracleTeam();

		teamHtml = new HTML(team);
		teamHtml.setTitle(team);

		setWidth("100%");
		setHeight("400px");
	}

	public void main() {

		HTML challengeLabel = new HTML("<b>"
				+ language.Challenge().toUpperCase() + ":</b>");
		add(challengeLabel);
		setWidgetTopHeight(challengeLabel, 5, Unit.PX, 20, Unit.PX);
		setWidgetLeftWidth(challengeLabel, 2, Unit.PX, 90, Unit.PX);

		HTML challenge = new HTML(Home.challengeName);
		challenge.setTitle(Home.challengeName);
		add(challenge);
		setWidgetTopHeight(challenge, 5, Unit.PX, 20, Unit.PX);
		setWidgetLeftRight(challenge, 100, Unit.PX, 2, Unit.PX);

		HTML groupLabel = new HTML("<b>" + language.Group().toUpperCase()
				+ ":</b>");
		add(groupLabel);
		setWidgetLeftWidth(groupLabel, 2, Unit.PX, 90, Unit.PX);
		setWidgetTopHeight(groupLabel, 30, Unit.PX, 20, Unit.PX);

		add(teamHtml);
		setWidgetLeftRight(teamHtml, 100, Unit.PX, 2, Unit.PX);
		setWidgetTopHeight(teamHtml, 30, Unit.PX, 20, Unit.PX);

		HTML changeLabel = new HTML(language.ChangeGroup()+":");
		add(changeLabel);
		setWidgetTopHeight(changeLabel, 57, Unit.PX, 20, Unit.PX);
		setWidgetLeftWidth(changeLabel, 2, Unit.PX, 95, Unit.PX);

		teamName = new SuggestBox(oracleTeam);
		teamName.addKeyPressHandler(this);
		teamName.setTitle(language.JoinGroupDescription());
		add(teamName);
		setWidgetTopHeight(teamName, 55, Unit.PX, 25, Unit.PX);
		setWidgetLeftRight(teamName, 97, Unit.PX, 2, Unit.PX);

		btnSetGroupName = new Button();
		btnSetGroupName.setText(language.SetName());
		btnSetGroupName.addClickHandler(this);
		btnSetGroupName.setWidth("100%");
		btnSetGroupName.setHeight("100%");
		add(btnSetGroupName);
		setWidgetTopHeight(btnSetGroupName, 85, Unit.PX, 25, Unit.PX);
		setWidgetLeftRight(btnSetGroupName, 97, Unit.PX, 2, Unit.PX);

		HTML userTitle = new HTML("<b>" + language.Groupmembers().toUpperCase()
				+ ":</b>");
		add(userTitle);
		setWidgetTopHeight(userTitle, 115, Unit.PX, 20, Unit.PX);
		setWidgetLeftRight(userTitle, 2, Unit.PX, 2, Unit.PX);

		setWidgetLeftRight(localUsers, 2, Unit.PX, 2, Unit.PX);
		setWidgetTopHeight(localUsers, 140, Unit.PX, 30, Unit.PX);

		HTML localUser = new HTML("<b>" + language.LocalUser().toUpperCase()
				+ ":</b>");
		add(localUser);
		setWidgetLeftRight(localUser, 2, Unit.PX, 2, Unit.PX);
		setWidgetBottomHeight(localUser, 85, Unit.PX, 20, Unit.PX);

		HTML name = new HTML(language.Name() + ": ");
		add(name);
		setWidgetLeftWidth(name, 2, Unit.PX, 68, Unit.PX);
		setWidgetBottomHeight(name, 60, Unit.PX, 20, Unit.PX);
		user = new SuggestBox(oracle);
		add(user);
		setWidgetLeftRight(user, 70, Unit.PX, 2, Unit.PX);
		setWidgetBottomHeight(user, 60, Unit.PX, 25, Unit.PX);

		HTML passwordLabel = new HTML(language.Password() + ": ");
		add(passwordLabel);
		setWidgetLeftWidth(passwordLabel, 2, Unit.PX, 68, Unit.PX);
		setWidgetBottomHeight(passwordLabel, 30, Unit.PX, 20, Unit.PX);

		password = new PasswordTextBox();
		password.addKeyPressHandler(this);
		password.setTitle(language.LoginDescription());
		add(password);
		setWidgetLeftRight(password, 70, Unit.PX, 2, Unit.PX);
		setWidgetBottomHeight(password, 30, Unit.PX, 25, Unit.PX);

		btnLogin = new Button();
		btnLogin.setText(language.LoginSmallletters());
		btnLogin.addClickHandler(this);
		btnLogin.setWidth("100%");
		btnLogin.setHeight("100%");
		add(btnLogin);
		setWidgetLeftRight(btnLogin, 70, Unit.PX, 2, Unit.PX);
		setWidgetBottomHeight(btnLogin, 2, Unit.PX, 25, Unit.PX);

		userLink.getTeam(Home.token,
				new AsyncCallback<Vector<Vector<String>>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Vector<Vector<String>> result) {
						String teamname = result.get(0).get(0);
						setTeamName(teamname);

						Vector<String> remote = result.get(1);
						for (String user : remote) {
							addUser(user, false);
						}

						Vector<String> local = result.get(2);
						for (String user : local) {
							addUser(user, true);
						}
					}
				});
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

	private void createOracleTeam() {
		oracleTeam = new MultiWordSuggestOracle();
		userLink.getTeamnames(Home.token, new AsyncCallback<Vector<String>>() {

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(Vector<String> result) {
				for (String uname : result) {
					oracleTeam.add(uname);
				}
			}
		});
	}

	private void updateTitle() {

		String titleText = language.GroupInfo() + ": [" + team + "]<br/>";

		Set<String> teamUserNames = users.keySet();
		Iterator<String> userIter = teamUserNames.iterator();
		if (users.size() > 0) {
			while (userIter.hasNext()) {
				titleText += userIter.next() + ", ";
			}
			titleText = titleText.substring(0, titleText.length() - 2);

		}
		Home.setLoginTitle(titleText);

	}

	public void addUser(String name, boolean local) {

		if (users.get(name) != null) {
			LocalUserGui lu = users.get(name);
			localUsers.remove(lu);
			users.remove(lu);
		}

		LocalUserGui ugui = new LocalUserGui(name, localUsers, local);
		users.put(name, ugui);

		localUsers.add(ugui);

		localUsers.setWidgetLeftRight(ugui, 0, Unit.PX, 0, Unit.PX);

		setWidgetTopHeight(localUsers, 160, Unit.PX, users.size() * 30, Unit.PX);

		oracle.add(name);

		layoutUsers();

		updateTitle();

		if (local) {
			Date now = new Date();
			long nowLong = now.getTime();
			nowLong = nowLong + (1000 * 60 * 60 * 20);// 20 hours
			now.setTime(nowLong);

			Collection<String> cookies = Cookies.getCookieNames();
			for (String cn : cookies) {
				if (cn != null && cn.startsWith("metaforaUser")) {
					if (!name.equals(Cookies.getCookie(cn))) {
						Cookies.setCookie("metaforaUserOther" + name, name, now);
					}
				}
			}
		}
	}

	private void layoutUsers() {
		int pos = 0;
		for (String key : users.keySet()) {
			LocalUserGui gui = users.get(key);
			localUsers.setWidgetTopHeight(gui, pos * 30, Unit.PX, 30, Unit.PX);
			pos++;
		}
		localUsers.setHeight(pos * 30 + "px");
		setHeight((270 + users.size() * 30) + "px");
	}

	public void removeUser(String name) {
		LocalUserGui ugui = users.get(name);
		users.remove(name);

		Cookies.removeCookie("metaforaUserOther" + name);

		if (ugui != null) {
			localUsers.remove(ugui);
			layoutUsers();
		}

		if (users.size() > 0)
			updateTitle();
		else
			Home.setLoginTitle(team);
	}

	public void setTeamName(String name) {
		team = name;
		teamHtml.setText(team);
		teamHtml.setTitle(team);

		oracleTeam.add(name);

		updateTitle();

		Date now = new Date();
		long nowLong = now.getTime();
		nowLong = nowLong + (1000 * 60 * 60 * 24 * 7);
		now.setTime(nowLong);

		Cookies.setCookie("metaforaGroup", name, now);
	}

	private void clearFields() {
		user.setText("");
		password.setText("");
		teamName.setText("");
	}

	@Override
	public void onClick(ClickEvent event) {
		if (event.getSource().equals(btnLogin)) {
			final String user = this.user.getText();

			String map = Home.mapName;
			final String mapname = map;

			if (user.length() <= 0)
				Window.alert(language.PleaseEnterYourUsername());
			else if (password.getText().length() <= 0)
				Window.alert(language.PleaseEnterYourPassword());
			else {
				userLink.getSalt(user, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(language.CouldNotVerifyYourPassword());
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(String result) {
						final String salt = result;

						if (result.equals("noSalt")) {
							SecureRandom random = SecureRandom
									.getInstance("SHA-256");
							byte[] bSalt = new byte[10];
							random.nextBytes(bSalt);
							final String salt2 = convertToHexString(bSalt);
							final String shaPassword2 = sha(password.getText(),
									salt2);
							String md5Password = MD5Algo.md5(password.getText());

							userLink.login(user, md5Password, shaPassword2,
									Home.groupName, Home.challengeId, mapname,
									Home.token, new AsyncCallback<Integer>() {

										public void onSuccess(Integer result) {
											if (result == -1) {
												Window.alert(language
														.YourPasswordWasWrong());
											} else {
												// send LOGIN XML with groupId
												// to logger
												// channel for PlaTO
												userLink.sendLoginXML(
														user,
														Home.groupName,
														Home.token,
														new AsyncCallback<Void>() {

															@Override
															public void onFailure(
																	Throwable caught) {
															}

															@Override
															public void onSuccess(
																	Void result) {
															}
														});
												clearFields();
												Window.alert(language
														.YouAreLoggedIn());
											}
										}

										public void onFailure(Throwable caught) {
											Window.alert(language.LoginFailed()
													+ "\nTeamWidget userLink.login():\n"
													+ caught.getMessage()
													+ "\n" + caught.getCause());
										}
									});
						} else if (result.equals("noUser")) {
							SecureRandom random = SecureRandom
									.getInstance("SHA-256");
							byte[] bSalt = new byte[10];
							random.nextBytes(bSalt);
							final String salt2 = convertToHexString(bSalt);
							final String shaPassword2 = sha(password.getText(),
									salt2);
							String md5Password = MD5Algo.md5(password.getText());

							String input = Window.prompt(
									language.YourUsernameIsUnknown(),
									language.IDontWantAUseraccount());
							if (input.toLowerCase().startsWith("yes")) {
								String filteredUser = InputFilter
										.filterString(user);
								registerUser(filteredUser, md5Password,
										shaPassword2, salt2, Home.groupName);
								clearFields();
							}
						} else {
							final String shaPassword = sha(password.getText(),
									salt);
							String md5Password = MD5Algo.md5(password.getText());

							userLink.login(user, md5Password, shaPassword,
									Home.groupName, Home.challengeId, mapname,
									Home.token, new AsyncCallback<Integer>() {

										public void onFailure(Throwable caught) {
											Window.alert(language.LoginFailed()
													+ "\nTeamWidget userLink.login():\n"
													+ caught.getMessage()
													+ "\n" + caught.getCause());
										}

										public void onSuccess(Integer result) {
											if (result == -1) {
												Window.alert(language
														.YourPasswordWasWrong());
											} else {
												// send LOGIN XML with groupId
												// to logger
												// channel for PlaTO
												userLink.sendLoginXML(
														user,
														Home.groupName,
														Home.token,
														new AsyncCallback<Void>() {

															@Override
															public void onFailure(
																	Throwable caught) {
															}

															@Override
															public void onSuccess(
																	Void result) {
															}
														});
												clearFields();
												Window.alert(language
														.YouAreLoggedIn());
											}
										}

									});
						}
					}
				});
			}
		} else if (event.getSource().equals(btnSetGroupName)) {
			changeGroup();
		}
	}

	private void changeGroup() {
		String team = teamName.getText();
		if (team.length() > 0) {
			team = InputFilter.filterString(team);

			String map = Home.mapName;

			userLink.setTeamname(team, Home.token, Home.challengeId, map,
					false, new AsyncCallback<Integer>() {

						@Override
						public void onFailure(Throwable caught) {
							Window.alert(language.TheGroupnameCoudNotBeSet()
									+ "\nTeamWidget userLink.setTeamname():\n"
									+ caught.getMessage() + "\n"
									+ caught.getCause());
						}

						@Override
						public void onSuccess(Integer result) {
							clearFields();
						}
					});
		} else
			Window.alert(language.PleaseEnterYourGroupName());
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		if (((int) event.getCharCode()) == 13
				|| (((int) event.getCharCode()) == 0 && ((int) event
						.getNativeEvent().getKeyCode()) == 13)) {
			if (event.getSource().equals(password)) {
				final String user = this.user.getText();

				String map = Home.mapName;
				final String mapname = map;

				if (user.length() <= 0)
					Window.alert(language.PleaseEnterYourUsername());
				else if (password.getText().length() <= 0)
					Window.alert(language.PleaseEnterYourPassword());
				else {
					userLink.getSalt(user, new AsyncCallback<String>() {

						@Override
						public void onFailure(Throwable caught) {
							Window.alert(language.CouldNotVerifyYourPassword());
							caught.printStackTrace();
						}

						@Override
						public void onSuccess(String result) {
							final String salt = result;

							if (result.equals("noSalt")) {
								SecureRandom random = SecureRandom
										.getInstance("SHA-256");
								byte[] bSalt = new byte[10];
								random.nextBytes(bSalt);
								final String salt2 = convertToHexString(bSalt);
								final String shaPassword2 = sha(
										password.getText(), salt2);
								String md5Password = MD5Algo.md5(password
										.getText());

								userLink.login(user, md5Password, shaPassword2,
										Home.groupName, Home.challengeId,
										mapname, Home.token,
										new AsyncCallback<Integer>() {

											public void onSuccess(Integer result) {
												if (result == -1) {
													Window.alert(language
															.YourPasswordWasWrong());
												} else {
													// send LOGIN XML with
													// groupId to logger
													// channel for PlaTO
													userLink.sendLoginXML(
															user,
															Home.groupName,
															Home.token,
															new AsyncCallback<Void>() {

																@Override
																public void onFailure(
																		Throwable caught) {
																}

																@Override
																public void onSuccess(
																		Void result) {
																}
															});
													clearFields();
													Window.alert(language
															.YouAreLoggedIn());
												}
											}

											public void onFailure(
													Throwable caught) {
												Window.alert(language
														.LoginFailed()
														+ "\nTeamWidget userLink.login():\n"
														+ caught.getMessage()
														+ "\n"
														+ caught.getCause());
											}
										});
							} else if (result.equals("noUser")) {
								SecureRandom random = SecureRandom
										.getInstance("SHA-256");
								byte[] bSalt = new byte[10];
								random.nextBytes(bSalt);
								final String salt2 = convertToHexString(bSalt);
								final String shaPassword2 = sha(
										password.getText(), salt2);

								String input = Window.prompt(
										language.YourUsernameIsUnknown(),
										language.IDontWantAUseraccount());
								if (input.toLowerCase().startsWith("yes")) {
									final String filteredUser = InputFilter
											.filterString(user);

									String md5Password = MD5Algo.md5(password
											.getText());

									registerUser(filteredUser, md5Password,
											shaPassword2, salt2, Home.groupName);
									clearFields();
								}
							} else {
								final String shaPassword = sha(
										password.getText(), salt);
								String md5Password = MD5Algo.md5(password
										.getText());

								userLink.login(user, md5Password, shaPassword,
										Home.groupName, Home.challengeId,
										mapname, Home.token,
										new AsyncCallback<Integer>() {

											public void onFailure(
													Throwable caught) {
												Window.alert(language
														.LoginFailed()
														+ "\nTeamWidget userLink.login():\n"
														+ caught.getMessage()
														+ "\n"
														+ caught.getCause());
											}

											public void onSuccess(Integer result) {
												if (result == -1) {
													Window.alert(language
															.YourPasswordWasWrong());
												} else {
													// send LOGIN XML with
													// groupId to logger
													// channel for PlaTO
													userLink.sendLoginXML(
															user,
															Home.groupName,
															Home.token,
															new AsyncCallback<Void>() {

																@Override
																public void onFailure(
																		Throwable caught) {
																}

																@Override
																public void onSuccess(
																		Void result) {
																}
															});
													clearFields();
													Window.alert(language
															.YouAreLoggedIn());
												}
											}

										});
							}
						}
					});
				}
			} else if (event.getSource().equals(teamName)) {
				changeGroup();
			} else {
				Window.alert("Event source not found!");
			}
		}
	}

	private void registerUser(String _user, String md5Password,
			String _shaPassword, String _salt, String _groupname) {
		final String user = _user;
		final String shaPassword = _shaPassword;
		final String groupname = _groupname;
		final String salt = _salt;

		userLink.register(user, md5Password, shaPassword, salt, groupname,
				Home.token, new AsyncCallback<Integer>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert(language.NewUserCoudNotBeRegistered()
								+ "\nLoginHandler userLink.register():"
								+ caught.getMessage() + "\n"
								+ caught.getCause());
					}

					@Override
					public void onSuccess(Integer result) {
						// send LOGIN XML with groupId to logger channel
						// for
						// PlaTO
						userLink.sendLoginXML(user, groupname, Home.token,
								new AsyncCallback<Void>() {

									@Override
									public void onFailure(Throwable caught) {
									}

									@Override
									public void onSuccess(Void result) {
									}
								});
						Window.alert(language
								.YouAreNowAProudOwnerOfUseraccount());
					}
				});
	}

	public String getFirstUser() {
		for (String user : users.keySet()) {
			if (users.get(user).isLocalUser()) {
				return user;
			}
		}
		return null;
	}

	public static native String md5(String string) /*-{

		function RotateLeft(lValue, iShiftBits) {
			return (lValue << iShiftBits) | (lValue >>> (32 - iShiftBits));
		}

		function AddUnsigned(lX, lY) {
			var lX4, lY4, lX8, lY8, lResult;
			lX8 = (lX & 0x80000000);
			lY8 = (lY & 0x80000000);
			lX4 = (lX & 0x40000000);
			lY4 = (lY & 0x40000000);
			lResult = (lX & 0x3FFFFFFF) + (lY & 0x3FFFFFFF);
			if (lX4 & lY4) {
				return (lResult ^ 0x80000000 ^ lX8 ^ lY8);
			}
			if (lX4 | lY4) {
				if (lResult & 0x40000000) {
					return (lResult ^ 0xC0000000 ^ lX8 ^ lY8);
				} else {
					return (lResult ^ 0x40000000 ^ lX8 ^ lY8);
				}
			} else {
				return (lResult ^ lX8 ^ lY8);
			}
		}

		function F(x, y, z) {
			return (x & y) | ((~x) & z);
		}
		function G(x, y, z) {
			return (x & z) | (y & (~z));
		}
		function H(x, y, z) {
			return (x ^ y ^ z);
		}
		function I(x, y, z) {
			return (y ^ (x | (~z)));
		}

		function FF(a, b, c, d, x, s, ac) {
			a = AddUnsigned(a, AddUnsigned(AddUnsigned(F(b, c, d), x), ac));
			return AddUnsigned(RotateLeft(a, s), b);
		}
		;

		function GG(a, b, c, d, x, s, ac) {
			a = AddUnsigned(a, AddUnsigned(AddUnsigned(G(b, c, d), x), ac));
			return AddUnsigned(RotateLeft(a, s), b);
		}
		;

		function HH(a, b, c, d, x, s, ac) {
			a = AddUnsigned(a, AddUnsigned(AddUnsigned(H(b, c, d), x), ac));
			return AddUnsigned(RotateLeft(a, s), b);
		}
		;

		function II(a, b, c, d, x, s, ac) {
			a = AddUnsigned(a, AddUnsigned(AddUnsigned(I(b, c, d), x), ac));
			return AddUnsigned(RotateLeft(a, s), b);
		}
		;

		function ConvertToWordArray(string) {
			var lWordCount;
			var lMessageLength = string.length;
			var lNumberOfWords_temp1 = lMessageLength + 8;
			var lNumberOfWords_temp2 = (lNumberOfWords_temp1 - (lNumberOfWords_temp1 % 64)) / 64;
			var lNumberOfWords = (lNumberOfWords_temp2 + 1) * 16;
			var lWordArray = Array(lNumberOfWords - 1);
			var lBytePosition = 0;
			var lByteCount = 0;
			while (lByteCount < lMessageLength) {
				lWordCount = (lByteCount - (lByteCount % 4)) / 4;
				lBytePosition = (lByteCount % 4) * 8;
				lWordArray[lWordCount] = (lWordArray[lWordCount] | (string
						.charCodeAt(lByteCount) << lBytePosition));
				lByteCount++;
			}
			lWordCount = (lByteCount - (lByteCount % 4)) / 4;
			lBytePosition = (lByteCount % 4) * 8;
			lWordArray[lWordCount] = lWordArray[lWordCount]
					| (0x80 << lBytePosition);
			lWordArray[lNumberOfWords - 2] = lMessageLength << 3;
			lWordArray[lNumberOfWords - 1] = lMessageLength >>> 29;
			return lWordArray;
		}
		;

		function WordToHex(lValue) {
			var WordToHexValue = "", WordToHexValue_temp = "", lByte, lCount;
			for (lCount = 0; lCount <= 3; lCount++) {
				lByte = (lValue >>> (lCount * 8)) & 255;
				WordToHexValue_temp = "0" + lByte.toString(16);
				WordToHexValue = WordToHexValue
						+ WordToHexValue_temp.substr(
								WordToHexValue_temp.length - 2, 2);
			}
			return WordToHexValue;
		}
		;

		function Utf8Encode(string) {
			string = string.replace(/\r\n/g, "\n");
			var utftext = "";

			for ( var n = 0; n < string.length; n++) {

				var c = string.charCodeAt(n);

				if (c < 128) {
					utftext += String.fromCharCode(c);
				} else if ((c > 127) && (c < 2048)) {
					utftext += String.fromCharCode((c >> 6) | 192);
					utftext += String.fromCharCode((c & 63) | 128);
				} else {
					utftext += String.fromCharCode((c >> 12) | 224);
					utftext += String.fromCharCode(((c >> 6) & 63) | 128);
					utftext += String.fromCharCode((c & 63) | 128);
				}
			}

			return utftext;
		}
		;

		var x = Array();
		var k, AA, BB, CC, DD, a, b, c, d;
		var S11 = 7, S12 = 12, S13 = 17, S14 = 22;
		var S21 = 5, S22 = 9, S23 = 14, S24 = 20;
		var S31 = 4, S32 = 11, S33 = 16, S34 = 23;
		var S41 = 6, S42 = 10, S43 = 15, S44 = 21;

		string = Utf8Encode(string);

		x = ConvertToWordArray(string);

		a = 0x67452301;
		b = 0xEFCDAB89;
		c = 0x98BADCFE;
		d = 0x10325476;

		for (k = 0; k < x.length; k += 16) {
			AA = a;
			BB = b;
			CC = c;
			DD = d;
			a = FF(a, b, c, d, x[k + 0], S11, 0xD76AA478);
			d = FF(d, a, b, c, x[k + 1], S12, 0xE8C7B756);
			c = FF(c, d, a, b, x[k + 2], S13, 0x242070DB);
			b = FF(b, c, d, a, x[k + 3], S14, 0xC1BDCEEE);
			a = FF(a, b, c, d, x[k + 4], S11, 0xF57C0FAF);
			d = FF(d, a, b, c, x[k + 5], S12, 0x4787C62A);
			c = FF(c, d, a, b, x[k + 6], S13, 0xA8304613);
			b = FF(b, c, d, a, x[k + 7], S14, 0xFD469501);
			a = FF(a, b, c, d, x[k + 8], S11, 0x698098D8);
			d = FF(d, a, b, c, x[k + 9], S12, 0x8B44F7AF);
			c = FF(c, d, a, b, x[k + 10], S13, 0xFFFF5BB1);
			b = FF(b, c, d, a, x[k + 11], S14, 0x895CD7BE);
			a = FF(a, b, c, d, x[k + 12], S11, 0x6B901122);
			d = FF(d, a, b, c, x[k + 13], S12, 0xFD987193);
			c = FF(c, d, a, b, x[k + 14], S13, 0xA679438E);
			b = FF(b, c, d, a, x[k + 15], S14, 0x49B40821);
			a = GG(a, b, c, d, x[k + 1], S21, 0xF61E2562);
			d = GG(d, a, b, c, x[k + 6], S22, 0xC040B340);
			c = GG(c, d, a, b, x[k + 11], S23, 0x265E5A51);
			b = GG(b, c, d, a, x[k + 0], S24, 0xE9B6C7AA);
			a = GG(a, b, c, d, x[k + 5], S21, 0xD62F105D);
			d = GG(d, a, b, c, x[k + 10], S22, 0x2441453);
			c = GG(c, d, a, b, x[k + 15], S23, 0xD8A1E681);
			b = GG(b, c, d, a, x[k + 4], S24, 0xE7D3FBC8);
			a = GG(a, b, c, d, x[k + 9], S21, 0x21E1CDE6);
			d = GG(d, a, b, c, x[k + 14], S22, 0xC33707D6);
			c = GG(c, d, a, b, x[k + 3], S23, 0xF4D50D87);
			b = GG(b, c, d, a, x[k + 8], S24, 0x455A14ED);
			a = GG(a, b, c, d, x[k + 13], S21, 0xA9E3E905);
			d = GG(d, a, b, c, x[k + 2], S22, 0xFCEFA3F8);
			c = GG(c, d, a, b, x[k + 7], S23, 0x676F02D9);
			b = GG(b, c, d, a, x[k + 12], S24, 0x8D2A4C8A);
			a = HH(a, b, c, d, x[k + 5], S31, 0xFFFA3942);
			d = HH(d, a, b, c, x[k + 8], S32, 0x8771F681);
			c = HH(c, d, a, b, x[k + 11], S33, 0x6D9D6122);
			b = HH(b, c, d, a, x[k + 14], S34, 0xFDE5380C);
			a = HH(a, b, c, d, x[k + 1], S31, 0xA4BEEA44);
			d = HH(d, a, b, c, x[k + 4], S32, 0x4BDECFA9);
			c = HH(c, d, a, b, x[k + 7], S33, 0xF6BB4B60);
			b = HH(b, c, d, a, x[k + 10], S34, 0xBEBFBC70);
			a = HH(a, b, c, d, x[k + 13], S31, 0x289B7EC6);
			d = HH(d, a, b, c, x[k + 0], S32, 0xEAA127FA);
			c = HH(c, d, a, b, x[k + 3], S33, 0xD4EF3085);
			b = HH(b, c, d, a, x[k + 6], S34, 0x4881D05);
			a = HH(a, b, c, d, x[k + 9], S31, 0xD9D4D039);
			d = HH(d, a, b, c, x[k + 12], S32, 0xE6DB99E5);
			c = HH(c, d, a, b, x[k + 15], S33, 0x1FA27CF8);
			b = HH(b, c, d, a, x[k + 2], S34, 0xC4AC5665);
			a = II(a, b, c, d, x[k + 0], S41, 0xF4292244);
			d = II(d, a, b, c, x[k + 7], S42, 0x432AFF97);
			c = II(c, d, a, b, x[k + 14], S43, 0xAB9423A7);
			b = II(b, c, d, a, x[k + 5], S44, 0xFC93A039);
			a = II(a, b, c, d, x[k + 12], S41, 0x655B59C3);
			d = II(d, a, b, c, x[k + 3], S42, 0x8F0CCC92);
			c = II(c, d, a, b, x[k + 10], S43, 0xFFEFF47D);
			b = II(b, c, d, a, x[k + 1], S44, 0x85845DD1);
			a = II(a, b, c, d, x[k + 8], S41, 0x6FA87E4F);
			d = II(d, a, b, c, x[k + 15], S42, 0xFE2CE6E0);
			c = II(c, d, a, b, x[k + 6], S43, 0xA3014314);
			b = II(b, c, d, a, x[k + 13], S44, 0x4E0811A1);
			a = II(a, b, c, d, x[k + 4], S41, 0xF7537E82);
			d = II(d, a, b, c, x[k + 11], S42, 0xBD3AF235);
			c = II(c, d, a, b, x[k + 2], S43, 0x2AD7D2BB);
			b = II(b, c, d, a, x[k + 9], S44, 0xEB86D391);
			a = AddUnsigned(a, AA);
			b = AddUnsigned(b, BB);
			c = AddUnsigned(c, CC);
			d = AddUnsigned(d, DD);
		}

		var result = WordToHex(a) + WordToHex(b) + WordToHex(c) + WordToHex(d);

		return result.toLowerCase();
	}-*/;

	public static String sha(String password, String salt) {
		String s = salt + password;
		System.out.println(s);

		String output = digest(s);
		System.out.println(output);

		for (int i = 0; i < ITERATION_NUMBER; i++) {
			output = digest(output);
		}

		return convertToHexString(convertToArray(output));
	}

	public static native String digest(String s)/*-{

		var chrsz = 8;
		var hexcase = 0;

		function safe_add(x, y) {
			var lsw = (x & 0xFFFF) + (y & 0xFFFF);
			var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
			return (msw << 16) | (lsw & 0xFFFF);
		}

		function S(X, n) {
			return (X >>> n) | (X << (32 - n));
		}
		function R(X, n) {
			return (X >>> n);
		}
		function Ch(x, y, z) {
			return ((x & y) ^ ((~x) & z));
		}
		function Maj(x, y, z) {
			return ((x & y) ^ (x & z) ^ (y & z));
		}
		function Sigma0256(x) {
			return (S(x, 2) ^ S(x, 13) ^ S(x, 22));
		}
		function Sigma1256(x) {
			return (S(x, 6) ^ S(x, 11) ^ S(x, 25));
		}
		function Gamma0256(x) {
			return (S(x, 7) ^ S(x, 18) ^ R(x, 3));
		}
		function Gamma1256(x) {
			return (S(x, 17) ^ S(x, 19) ^ R(x, 10));
		}

		function core_sha256(m, l) {
			var K = new Array(0x428A2F98, 0x71374491, 0xB5C0FBCF, 0xE9B5DBA5,
					0x3956C25B, 0x59F111F1, 0x923F82A4, 0xAB1C5ED5, 0xD807AA98,
					0x12835B01, 0x243185BE, 0x550C7DC3, 0x72BE5D74, 0x80DEB1FE,
					0x9BDC06A7, 0xC19BF174, 0xE49B69C1, 0xEFBE4786, 0xFC19DC6,
					0x240CA1CC, 0x2DE92C6F, 0x4A7484AA, 0x5CB0A9DC, 0x76F988DA,
					0x983E5152, 0xA831C66D, 0xB00327C8, 0xBF597FC7, 0xC6E00BF3,
					0xD5A79147, 0x6CA6351, 0x14292967, 0x27B70A85, 0x2E1B2138,
					0x4D2C6DFC, 0x53380D13, 0x650A7354, 0x766A0ABB, 0x81C2C92E,
					0x92722C85, 0xA2BFE8A1, 0xA81A664B, 0xC24B8B70, 0xC76C51A3,
					0xD192E819, 0xD6990624, 0xF40E3585, 0x106AA070, 0x19A4C116,
					0x1E376C08, 0x2748774C, 0x34B0BCB5, 0x391C0CB3, 0x4ED8AA4A,
					0x5B9CCA4F, 0x682E6FF3, 0x748F82EE, 0x78A5636F, 0x84C87814,
					0x8CC70208, 0x90BEFFFA, 0xA4506CEB, 0xBEF9A3F7, 0xC67178F2);
			var HASH = new Array(0x6A09E667, 0xBB67AE85, 0x3C6EF372,
					0xA54FF53A, 0x510E527F, 0x9B05688C, 0x1F83D9AB, 0x5BE0CD19);
			var W = new Array(64);
			var a, b, c, d, e, f, g, h, i, j;
			var T1, T2;

			m[l >> 5] |= 0x80 << (24 - l % 32);
			m[((l + 64 >> 9) << 4) + 15] = l;

			for ( var i = 0; i < m.length; i += 16) {
				a = HASH[0];
				b = HASH[1];
				c = HASH[2];
				d = HASH[3];
				e = HASH[4];
				f = HASH[5];
				g = HASH[6];
				h = HASH[7];

				for ( var j = 0; j < 64; j++) {
					if (j < 16)
						W[j] = m[j + i];
					else
						W[j] = safe_add(safe_add(safe_add(Gamma1256(W[j - 2]),
								W[j - 7]), Gamma0256(W[j - 15])), W[j - 16]);

					T1 = safe_add(safe_add(safe_add(safe_add(h, Sigma1256(e)),
							Ch(e, f, g)), K[j]), W[j]);
					T2 = safe_add(Sigma0256(a), Maj(a, b, c));

					h = g;
					g = f;
					f = e;
					e = safe_add(d, T1);
					d = c;
					c = b;
					b = a;
					a = safe_add(T1, T2);
				}

				HASH[0] = safe_add(a, HASH[0]);
				HASH[1] = safe_add(b, HASH[1]);
				HASH[2] = safe_add(c, HASH[2]);
				HASH[3] = safe_add(d, HASH[3]);
				HASH[4] = safe_add(e, HASH[4]);
				HASH[5] = safe_add(f, HASH[5]);
				HASH[6] = safe_add(g, HASH[6]);
				HASH[7] = safe_add(h, HASH[7]);
			}
			return HASH;
		}

		function str2binb(str) {
			var bin = Array();
			var mask = (1 << chrsz) - 1;
			for ( var i = 0; i < str.length * chrsz; i += chrsz) {
				bin[i >> 5] |= (str.charCodeAt(i / chrsz) & mask) << (24 - i % 32);
			}
			return bin;
		}

		function Utf8Encode(string) {
			string = string.replace(/\r\n/g, "\n");
			var utftext = "";

			for ( var n = 0; n < string.length; n++) {

				var c = string.charCodeAt(n);

				if (c < 128) {
					utftext += String.fromCharCode(c);
				} else if ((c > 127) && (c < 2048)) {
					utftext += String.fromCharCode((c >> 6) | 192);
					utftext += String.fromCharCode((c & 63) | 128);
				} else {
					utftext += String.fromCharCode((c >> 12) | 224);
					utftext += String.fromCharCode(((c >> 6) & 63) | 128);
					utftext += String.fromCharCode((c & 63) | 128);
				}

			}

			return utftext;
		}

		function binb2hex(binarray) {
			var hex_tab = hexcase ? "0123456789ABCDEF" : "0123456789abcdef";
			var str = "";
			for ( var i = 0; i < binarray.length * 4; i++) {
				str += hex_tab
						.charAt((binarray[i >> 2] >> ((3 - i % 4) * 8 + 4)) & 0xF)
						+ hex_tab
								.charAt((binarray[i >> 2] >> ((3 - i % 4) * 8)) & 0xF);
			}
			return str;
		}

		s = Utf8Encode(s);
		return binb2hex(core_sha256(str2binb(s), s.length * chrsz));

	}-*/;

	public static byte[] convertToArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

	public static String convertToHexString(byte[] b) {
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(0xFF & b[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
