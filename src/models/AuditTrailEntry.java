package models;

import java.time.LocalDateTime;

public class AuditTrailEntry {
    // fields
	private int id;
	private int bookId;
	private LocalDateTime dateAdded;
	private String message;
	
	// constructors
	public AuditTrailEntry() {
		id = 0;
		bookId = 0;
		dateAdded = null;
		message = "";
	}
	
	// methods
	public AuditTrailEntry(int id, int bookId, LocalDateTime dateAdded, String message) {
		this.id = id;
		this.bookId = bookId;
		this.dateAdded = dateAdded;
		this.message = message;
	}

	@Override
	public String toString() {
		return dateAdded + ": " + message;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}
	
	public LocalDateTime getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(LocalDateTime dateAdded) {
		this.dateAdded = dateAdded;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}