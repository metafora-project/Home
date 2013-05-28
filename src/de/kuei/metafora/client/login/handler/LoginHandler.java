package de.kuei.metafora.client.login.handler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.googlecode.gwt.crypto.util.SecureRandom;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.GroupManager;
import de.kuei.metafora.client.login.LoginManager;
import de.kuei.metafora.client.team.server.UserLink;
import de.kuei.metafora.client.team.server.UserLinkAsync;
import de.kuei.metafora.client.util.InputFilter;
import de.kuei.metafora.client.util.MD5Algo;

public class LoginHandler implements ClickHandler, KeyPressHandler {

	private static final int ITERATION_NUMBER = 1000;
	final static Languages language = GWT.create(Languages.class);
	private UserLinkAsync userLink = GWT.create(UserLink.class);
	private LoginManager loginManager;
	private boolean processing = false;

	public LoginHandler() {
		loginManager = LoginManager.getInstance();
	}

	@Override
	public void onClick(ClickEvent event) {
		login();
	}

	private void login() {
		if (!processing) {
			final String user = loginManager.getUsername();
			final String password = loginManager.getPassword();

			if (!(user.length() > 0)) {
				Window.alert(language.PleaseEnterYourUsername());
			} else if (!(password.length() > 0)) {
				Window.alert(language.PleaseEnterYourPassword());
			} else {
				userLink.getSalt(user, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(language.CouldNotVerifyYourPassword());
						caught.printStackTrace();
					}

					@Override
					public void onSuccess(String result) {
						processing = true;

						if (result.equals("noSalt")) {
							SecureRandom random = SecureRandom
									.getInstance("SHA-256");
							byte[] bSalt = new byte[10];
							random.nextBytes(bSalt);
							final String salt2 = convertToHexString(bSalt);
							final String shaPassword2 = sha(password, salt2);
							String md5Password = MD5Algo.md5(password);

							userLink.login(user, md5Password, shaPassword2,
									"Metafora", null, null, Home.token,
									new LoginCallback(user, md5Password,
											shaPassword2));
						} else if (result.equals("noUser")) {
							SecureRandom random = SecureRandom
									.getInstance("SHA-256");
							byte[] bSalt = new byte[10];
							random.nextBytes(bSalt);
							final String salt2 = convertToHexString(bSalt);
							final String shaPassword2 = sha(password, salt2);

							String input = Window.prompt(
									language.YourUsernameIsUnknown(),
									language.IDontWantAUseraccount());
							if (input.toLowerCase().startsWith("yes")) {
								String filteredUser = InputFilter
										.filterString(user);

								String md5Password = MD5Algo.md5(password);

								registerUser(filteredUser, md5Password,
										shaPassword2, salt2, "Metafora");
							}
						} else {
							SecureRandom random = SecureRandom
									.getInstance("SHA-256");
							byte[] bSalt = new byte[10];
							random.nextBytes(bSalt);
							final String salt2 = convertToHexString(bSalt);
							final String shaPassword2 = sha(password, salt2);
							String md5Password = MD5Algo.md5(password);

							userLink.login(user, md5Password, shaPassword2,
									"Metafora", null, null, Home.token,
									new LoginCallback(user, md5Password,
											shaPassword2));
						}
					}
				});
			}
		}
	}

	private void registerUser(String _user, String md5Password,
			String _shaPassword, String _salt, String _groupname) {
		final String user = _user;
		final String shaPassword = _shaPassword;
		final String groupname = _groupname;
		final String salt = _salt;
		final String md5Pass = md5Password;

		String filteredUser = InputFilter.filterString(user);

		userLink.register(filteredUser, md5Pass, shaPassword, salt, groupname,
				Home.token, new AsyncCallback<Integer>() {
					@Override
					public void onFailure(Throwable caught) {
						Window.alert(language.NewUserCoudNotBeRegistered()+"\nLoginHandler userLink.register():"
								+ caught.getMessage()
								+ "\n"
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
						Window.alert(language.YouAreNowAProudOwnerOfUseraccount());
						/*
						 * // LASAD not ready to receive SHA, automated login
						 * will fail Home.lastFrame.setLoginInformation(user,
						 * groupname, shaPassword);
						 */
						Home.lastFrame.setLoginInformation(user, md5Pass,
								shaPassword);
						loginManager.clearFields();
						loginManager.hideUI();
						GroupManager.getInstance().main();
					}
				});
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		if (((int) event.getCharCode()) == 13
				|| (((int) event.getCharCode()) == 0 && ((int) event
						.getNativeEvent().getKeyCode()) == 13)) {
			login();
		}
	}

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
