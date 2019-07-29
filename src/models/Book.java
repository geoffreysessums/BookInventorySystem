package models;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.BookGateway;


public class Book extends BookGhost {
    // fields
	private static Logger logger = LogManager.getLogger();

	private BookGateway bookGateway;

	// constructor(s)
	public Book() {
		super();
        bookGateway = null;
		lastModified = null;
	}
	
	public Book(int id, String title, String summary, int year,
		    Publisher publisher, String isbn, LocalDateTime lastModified) {
		super(id, title, summary, year, publisher, isbn, lastModified);

	}

	// method(s)
	public BookGateway getBookGateway() {
		return this.bookGateway;
	}

	public void setBookGateway(BookGateway bookGateway) {
		this.bookGateway = bookGateway;
	}
	
	public void save() throws Exception {
		if (!isValidTitle(getTitle()))
			throw new Exception("Invalid title: " + getTitle());
		if (!isValidSummary(getSummary()))
			throw new Exception("Invalid summary: " + getSummary());
		if (!isValidYear(getYearPublished()))
			throw new Exception("Invalid published year: " + getYearPublished());
		if (!isValidISBN(getISBN()))
			throw new Exception("Invalid ISBN: " + getISBN());
        // if book is a new record (i.e. id = 0), then insert it
		// otherwise book record exits and must be updated
		if (getId() == 0) {
			bookGateway.insertBook(this);
		} else {
	        bookGateway.updateBook(this);
		}
	}
	
	/**
	 * 
	 */
	public void delete() throws Exception {
		bookGateway.deleteBook(this);
	}
	
	/**
	 * 
	 */
	public List<AuthorBook> getAuthors() {
		List<AuthorBook> authors = null;
		try {
			authors = this.getBookGateway().getAuthorsForBook(this);
		} catch(Exception e) {
			e.printStackTrace();
		}
		logger.info("Returning authors for \"" + this.getTitle() + "\"");
		return authors;
	}

	/**
	 * 
	 * @return
	 */
	public List<AuditTrailEntry> getAuditTrail() {
		List<AuditTrailEntry> auditTrail = null;
		try { 
			auditTrail = this.getBookGateway().getAuditTrail(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("Returning audit trail for \"" + this.getTitle() + "\"");
		return auditTrail;
	}
}