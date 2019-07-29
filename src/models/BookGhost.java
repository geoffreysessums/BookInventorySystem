package models;

import java.time.LocalDateTime;

public class BookGhost {
    // fields
	protected int id; // unique book id
	protected int yearPublished; // range: from 1455 to present
	protected String isbn; // International Standard Book Number
	protected String title;
	protected String summary;	
	protected Publisher publisher;	
	
	protected LocalDateTime lastModified;

	// constructor(s)
    public BookGhost() {
		// default values
		id = 0;
		title = "";
		summary = "";
		isbn = "";
		yearPublished = 0;
		publisher = new Publisher();
		lastModified = null;
    }
    
	public BookGhost(int id, String title, String summary, int year,
		        Publisher publisher, String isbn, LocalDateTime lastModified) {
		this();
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.yearPublished = year;
		this.publisher = publisher;
		this.isbn = isbn;
		this.lastModified = lastModified;
	}
	
	// methods
	public boolean isValidTitle(String title) {
		if ((title.length() > 0) && (title.length() < 256)) {
			return true;
		}
		return false;
	}
	
	public boolean isValidSummary(String summary) {
		if (summary.length() >= 0 && summary.length() < 65536) {
			return true;
		}
		return false;
	}
	
	public boolean isValidYear(int year) {
		if (year >= 1455 && year <= 2019) {
			return true;
		}
		return false;
	}
	
	public boolean isValidISBN(String isbn) {
		if (isbn.length() < 0 || isbn.length() > 13) {
		    return false;
		}
		return true;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getISBN() {
		return isbn;
	}
	
	public void setISBN(String isbn) {
		this.isbn = isbn;
	}
	
	public int getYearPublished() {
		return yearPublished;
	}
	
	public void setYearPublished(int year) {
		this.yearPublished = year;
	}
	
	public Publisher getPublisher() {
		return this.publisher;
	}
	
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}
	
	public int getPublisherId() {
		return this.publisher.getId();
	}
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String toString() {
		return id + ": " + title;
	}
	
	public LocalDateTime getLastModified() {
		return this.lastModified;
	}
	
	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}
}