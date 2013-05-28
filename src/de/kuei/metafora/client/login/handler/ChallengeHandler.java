package de.kuei.metafora.client.login.handler;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.kuei.metafora.client.Home;
import de.kuei.metafora.client.Languages;
import de.kuei.metafora.client.login.ChallengeManager;
import de.kuei.metafora.client.login.MapManager;
import de.kuei.metafora.client.login.handler.server.LoginService;
import de.kuei.metafora.client.login.handler.server.LoginServiceAsync;

public class ChallengeHandler implements ClickHandler, KeyPressHandler {

	private ChallengeManager challengeManager;
	private LoginServiceAsync loginService = GWT.create(LoginService.class);
	private boolean processing = false;
	final static Languages language = GWT.create(Languages.class);

	public ChallengeHandler() {
		challengeManager = ChallengeManager.getInstance();
	}

	@Override
	public void onClick(ClickEvent event) {
		if (!processing) {
			final String challenge = challengeManager.getChallenge();

			if (challenge.length() > 1) {
				loginService.getChallengeId(challenge, Home.token,
						Home.userName, Home.groupName,
						new AsyncCallback<String>() {

							@Override
							public void onFailure(Throwable caught) {
								Window.alert(language.TheChallengeCouldNotBeSelected()+"\nChallengeHandler loginService.getChallengeId():\n"
										+ caught.getMessage()
										+ "\n"
										+ caught.getCause());
							}

							@Override
							public void onSuccess(String result) {
								processing = true;
								Home.lastFrame.setChallengeId(result);
								Home.lastFrame.setChallengeName(challenge);
								challengeManager.hideUI();
								MapManager.getInstance().main();
							}
						});

				loginService.getChallengeUrl(challenge, Home.token,
						new AsyncCallback<String>() {

							@Override
							public void onFailure(Throwable caught) {
							}

							@Override
							public void onSuccess(String result) {
								Home.challengeUrl = result;
								Date now = new Date();
								long nowLong = now.getTime();
								nowLong = nowLong + (1000 * 60 * 60 * 20);
								now.setTime(nowLong);
								Cookies.setCookie("metaforaChallengeUrl",
										Home.challengeUrl, now);
							}
						});
			} else {
				Window.alert(language.PleaseSelectYourChallenge());
			}
		}
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		if (((int) event.getCharCode()) == 13
				|| (((int) event.getCharCode()) == 0 && ((int) event
						.getNativeEvent().getKeyCode()) == 13)) {
			if (!processing) {
				final String challenge = challengeManager.getChallenge();

				if (challenge.length() > 1) {
					loginService.getChallengeId(challenge, Home.token,
							Home.userName, Home.groupName,
							new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
									Window.alert(language.TheChallengeCouldNotBeSelected()+"\nChallengeHandler loginService.getChallengeId():\n"
											+ caught.getMessage()
											+ "\n"
											+ caught.getCause());
								}

								@Override
								public void onSuccess(String result) {
									processing = true;
									Home.lastFrame.setChallengeId(result);
									Home.lastFrame.setChallengeName(challenge);
									challengeManager.hideUI();
									MapManager.getInstance().main();
								}
							});

					loginService.getChallengeUrl(challenge, Home.token,
							new AsyncCallback<String>() {

								@Override
								public void onFailure(Throwable caught) {
								}

								@Override
								public void onSuccess(String result) {
									Home.challengeUrl = result;
									Date now = new Date();
									long nowLong = now.getTime();
									nowLong = nowLong + (1000 * 60 * 60 * 20);
									now.setTime(nowLong);
									Cookies.setCookie("metaforaChallengeUrl",
											Home.challengeUrl, now);
								}
							});
				} else {
					Window.alert(language.PleaseSelectYourChallenge());
				}
			}
		}
	}
}