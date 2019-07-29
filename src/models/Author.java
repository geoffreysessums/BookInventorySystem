package models;

import java.time.LocalDate;

public class Author {
    // fields
	private int id;
	private String firstName;
	private String lastName;
	private LocalDate dateOfBirth;
	private char gender;
	private String webSite;
	
	// constructor(s)
	public Author() {
		this.id = 0;
		this.firstName = "";
		this.lastName = "";
		this.dateOfBirth = null;
		this.gender = '\0';
		this.webSite = null;
	}
	
	public Author(int id, String firstName, String lastName, 
			      LocalDate dateOfBirth, char gender, String webSite) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.dateOfBirth = dateOfBirth;
		this.gender = gender;
		this.webSite = webSite;
	}
	
	// methods
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}
	
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}
	
	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * @return the dateOfBirth
	 */
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}
	
	/**
	 * @param localDate the dateOfBirth to set
	 */
	public void setDateOfBirth(LocalDate localDate) {
		this.dateOfBirth = localDate;
	}
	
	/**
	 * @return the gender
	 */
	public char getGender() {
		return gender;
	}
	
	/**
	 * @param gender the gender to set
	 */
	public void setGender(char gender) {
		this.gender = gender;
	}
	
	/**
	 * @return the webSite
	 */
	public String getWebSite() {
		return webSite;
	}
	
	/**
	 * @param webSite the webSite to set
	 */
	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return firstName + " " + lastName;
	}
	
	/**
	 * 
	 * @param firstName
	 * @return
	 */
	public Boolean isValidFirstName(String firstName) {
		if (firstName.length() > 0 && firstName.length() <= 100) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 */
	public Boolean isValidLastName(String lastName) {
		if (lastName.length() > 0 && lastName.length() <= 100) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 */
	public Boolean isValidGender(char gender) {
		if(gender == 'M' || gender == 'm' || gender == 'F' || gender == 'f') {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 */
	public Boolean isValidWebSite(String webSite) {
		if (webSite.length() <= 100 || webSite == null) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 */
	public void validateFields() throws Exception {
		if(!isValidFirstName(getFirstName()))
			throw new Exception("Invalid first name: "  + getFirstName());
		if(!isValidLastName(getLastName()))
			throw new Exception("Invalid last name: "  + getLastName());
		if(!isValidGender(getGender()))
			throw new Exception("Invalid gender: "  + getGender());
		if(!isValidWebSite(getWebSite()))
			throw new Exception("Invalid website: "  + getWebSite());
	}
}
