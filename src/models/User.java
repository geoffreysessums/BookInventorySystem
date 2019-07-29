package models;

public class User {
	private String login;
	private String passwordHash; //hash of user's pw
	private String userName;
		
	public User(String login, String pw, String userName) {
		this.login = login;
		this.userName = userName;
		passwordHash = CryptoStuff.sha256(pw);
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
}