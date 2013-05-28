package de.kuei.metafora.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Interface for i18n (internationalization)
 * 
 * @author Kerstin Pfahler
 * @methods returns a string, whose value depends on the requested language
 * 
 *          add "&locale=en" to the end of the URL -> words appear in English
 *          add "&locale=hbr" to URL -> words appear in Hebrew add "&locale=gr"
 *          to URL -> words appear in Greek add "&locale=de" to URL -> words
 *          appear in German i18n default value = English
 */
public interface Languages extends Messages {
	String Lasad();

	String Google();
	
	String Back();

	String Expresser();

	String Suscity();

	String Jugger();

	String Math();

	String Notes();

	String Overview();

	String Chat();

	String Tools();

	String PlanningTool();

	String Workbench();

	String Challenge();

	String BuildingDiscussion();

	String ComparingDiscussion();

	String DiscussingMicroworlds();

	String ComparingMicroworlds();

	String LocalUser();

	String Name();

	String Password();

	String Member();

	String Send();

	String Login();

	String LoginSmallletters();

	String SetName();

	String Group();

	String NewGroup();

	String Select();

	String Demo();

	String Editor();

	String Map();

	String Challenges();

	String Help();

	String NewMap();

	String Groupmembers();

	String askHelp();

	String cancel();

	String Continue();

	String LoginTitle();

	String ChallengeTitle();

	String MapTitle();

	String Piki();

	String ImportantFeedbackMessages();

	String HelpRequest();

	String AskHelpFrom();

	String GroupWithPopup();

	String GroupWithChatMessage();

	String OtherHelper();

	String AboutYourWorkIn();

	String Other();

	String Why();

	String AsksForHelp();

	String Feedback();

	String Remote();

	String Logout();

	String ChangeGroup();

	String JoinGroupDescription(); // "Press <Enter> to join or create a group."

	String LoginDescription(); // "Press <Enter> to login or register."

	String GroupInfo();

	String JoinGroup();
	
	String TypeFilterAlert();
	String FilterYouTyped();
	String FilterTheCharacters();
	String StringFilterAlert();
	String HelpMessage();
	String ChatMessageCoudNotBeAdded();
	String HelpRequestFailed();
	String TheChallengeCouldNotBeSelected();
	String PleaseSelectYourChallenge();
	String PleaseSelectYourGroup();
	String YourPasswordWasWrong();
	String LoginFailed();
	String PleaseEnterYourUsername();
	String PleaseEnterYourPassword();
	String CouldNotVerifyYourPassword();
	String YourUsernameIsUnknown();
	String IDontWantAUseraccount();
	String NewUserCoudNotBeRegistered();
	String YouAreNowAProudOwnerOfUseraccount();
	String PleaseSelectAMap();
	String PleaseEnterTheNameOfYourNewMap();
	String PlanningToolMapName();
	String CreatingANewMapFailed();
	String PleaseEnterYourGroupName();
	String CreatingNewGroupFailed();
	String TheGroupnameCoudNotBeSet();
	String TheChallengeDescriptionCanNotBeShown();
	String YourRecentChallenges();
	String YourRecentGroups();
	String YourRecentMaps();
	String LogoutFailed();
	String YouAreLoggedIn();
	String ToolDataIsInvalid();
	String StartingMetaforaSessionFailed();
	String SelectYourGroup();
}