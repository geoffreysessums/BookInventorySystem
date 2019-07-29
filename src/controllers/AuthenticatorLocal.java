package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Session;
import models.User;

/**
 * 
 * Don't use this code in real apps!!! 
 * The app MUST have a way to prove it is the owner of the given session id
 * This can be accomplished by giving the logged in app a session id and a salt
 * 
 * @author marcos
 *
 */
public class AuthenticatorLocal extends Authenticator {
	private RBACPolicyAuth accessPolicy;
	
	/**
	 * fake server session states
	 */
	private List<Session> sessions;
	
	/**
	 * users authenticator knows about
	 */
	private List<User> credentials;
	
	public AuthenticatorLocal() {
		//init the session list and credentials list
		sessions = new ArrayList<Session>();
		credentials = new ArrayList<User>();
		
		//create a default ABAC policy and add some permissions for user bob
		accessPolicy = new RBACPolicyAuth();

		//create bob as valid user
		//NOTE: we don't store the user's password in our credential store
		//User hashes the password in its constructor
		User u = new User("wilma", "arugula", "Wilma");
		credentials.add(u);
		//bob can access everything
		accessPolicy.createSimpleUserACLEntry(u.getLogin(), true, true, true);

		//create sue as valid user
		u = new User("leeroy", "wipeout", "Leroy");
		credentials.add(u);
		//sue can access choices 1 and 2
		accessPolicy.createSimpleUserACLEntry(u.getLogin(), true, true, false);

		//create sue as valid user
		u = new User("sasquatch", "jerky", "Sasquatch");
		credentials.add(u);
		//ragnar can only access choice 3
		accessPolicy.createSimpleUserACLEntry(u.getLogin(), false, false, true);
		
		/*
		u = new User("abc", "efg", "New User");
		credentials.add(u);
		//ragnar can only access choice 3
		accessPolicy.createSimpleUserACLEntry(u.getLogin(), false, true, false);
		*/
	}
		
	/**
	 * determine if session user has access to function f 
	 * @param sessionId id of session to lookup in user reference identifier in policy
	 * @param f function for which permission is being asked
	 * @return
	 * @throws LoginException
	 */
	public boolean hasAccess(int sessionId, String f) {
		for(Session s : sessions) {
			if(s.getSessionId() == sessionId) {
				//session id matches, use serverSession for user object for access control
				return accessPolicy.canUserAccessFunction(s.getSessionUser().getLogin(), f);
			}
		}
		return false;
	}
		
	/**
	 * login and create a new session if credentials match using Sha-256 hash of user's password
	 * @param l
	 * @param pwHash Sha256 hash of password
	 * @return id of newly created session
	 * @throws LoginException
	 */
	public int loginSha256(String l, String pwHash) throws Exception {
		//iterate through user credentials and see if l and pwHash match. if so, make a session and returns its id
		for(User u : credentials) {
			if(u.getLogin().equals(l) && u.getPasswordHash().equals(pwHash)) {
				//create a fake server-side session from the user object given to us
				//in reality this makes no sense. authentication happens on the server side
				Session s = new Session(u);
				sessions.add(s);
				return s.getSessionId();
			}
		}
		throw new Exception("Authentication failed");
	}
	
	/**
	 * remove the session object with id of sessionId
	 * @param sessionId
	 */
	public void logout(int sessionId) {
		for(int i = sessions.size() - 1; i >= 0; i--) {
			Session s = sessions.get(i);
			if(s.getSessionId() == sessionId) {
				sessions.remove(i);
			}
		}
	}
	
	/**
	 * client does not store any session or user info other than sessionId
	 * so must ask authenticator for user info
	 * 
	 * @param sessionId
	 * @return
	 */
	public String getUserNameFromSessionId(int sessionId) {
		if(sessionId == Authenticator.INVALID_SESSION) 
			return "Not logged in"; 
		for(Session s : sessions) {
			if(s.getSessionId() == sessionId)
				return s.getSessionUser().getUserName();
		}
		return "";
	}
}