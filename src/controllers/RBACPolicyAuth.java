package controllers;

import java.util.HashMap;

public class RBACPolicyAuth {
	//fine-grained functional access
	public static final String CAN_ACCESS_CHOICE_1 = "choice1";
	public static final String CAN_ACCESS_CHOICE_2 = "choice2";
	public static final String CAN_ACCESS_CHOICE_3 = "choice3";

	/**
	 * access control list for users and functions (see above)
	 */
	private HashMap<String, HashMap<String, Boolean>> acl;
	
	public RBACPolicyAuth() {
		//create default fall-through policy where person has no access
		acl = new HashMap<String, HashMap<String, Boolean>>();
		
		//always good to have a fall through ACL entry configured with default permissions
		createSimpleUserACLEntry("default", false, false, false);
	}
	
	/*
	 * convenience function that creates a set of ACL entries specific to this demo app
	 * a general function is below; call it once for each permission 
	 */
	public void createSimpleUserACLEntry(String login, boolean ... entries) {
		HashMap<String, Boolean> userTable = new HashMap<String, Boolean>();
		userTable.put(CAN_ACCESS_CHOICE_1, entries[0]);
		userTable.put(CAN_ACCESS_CHOICE_2, entries[1]);
		userTable.put(CAN_ACCESS_CHOICE_3, entries[2]);
		//add user table to acl
		acl.put(login, userTable);
	}
	
	/**
	 * Change user's permission for function f
	 * @param uName user whose permission will change
	 * @param f the application function
	 * @param val new permission 
	 */
	public void setUserACLEntry(String uName, String f, boolean val) throws Exception {
		if(!acl.containsKey(uName))
			throw new Exception(uName + " does not exist in ACL");
		
		HashMap<String, Boolean> userTable = acl.get(uName);
		if(!userTable.containsKey(f))
			throw new Exception(f + " does not exist in user table");
		
		//change the permission
		userTable.put(f, val);
	}
	
	public boolean canUserAccessFunction(String userName, String functionName) {
		//make sure our ACL has a default entry and fail deny if default entry does not exist 
		if(!acl.containsKey("default"))
			return false;
		HashMap<String, Boolean> userTable = acl.get("default");
		
		//get user's table if it is in acl. otherwise, use default
		if(acl.containsKey(userName))
			userTable = acl.get(userName);
		
		//is permission for function in table? if so return that permission
		if(userTable.containsKey(functionName))
			return userTable.get(functionName);

		//otherwise return false (permission does not exist)
		return false;
	}
}
