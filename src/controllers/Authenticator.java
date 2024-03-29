package controllers;

public abstract class Authenticator {
	public static final int INVALID_SESSION = 0;

	/**
	 * returns true if session with given sessionId has permission for function descriptor f
	 * otherwise returns false
	 * @param sessionId
	 * @param f
	 * @return
	 */
	public abstract boolean hasAccess(int sessionId, String f);

	/**
	 * checks authenticator credential table for parameters
	 * if exist, creates new session in session table and returns new session id
	 * otherwise throws security exception
	 * @param l
	 * @param pwHash
	 * @return new session id
	 * @throws SecurityException
	 */
	public abstract int loginSha256(String l, String pwHash) throws Exception;

	/**
	 * removes session with the given sessionId from the session table 
	 * @param sessionId
	 */
	public abstract void logout(int sessionId);

	//TODO: the below method should be inherited 
	public abstract String getUserNameFromSessionId(int sessionId);

}