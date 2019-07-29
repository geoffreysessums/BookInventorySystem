package models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthorBook {
	// fields
	private static Logger logger = LogManager.getLogger();
    private Author author;
    private Book book;
    private int royalty;
    private double royaltyPercent;
    private Boolean newRecord;
    
    // constructor(s)
    public AuthorBook() {
    	author = new Author();
    	book = new Book();
    	royalty = 0;
    	newRecord = true;
    }
    
	public AuthorBook(Author author, Book book, int royalty, double royaltyPercent) {
		this.author = author;
		this.book = book;
		this.royalty = royalty;
		this.royaltyPercent = royaltyPercent;
		this.newRecord = false;
	}

	// methods
	/**
	 * @return the author
	 */
	public Author getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(Author author) {
		this.author = author;
	}

	/**
	 * @return the book
	 */
	public Book getBook() {
		return book;
	}

	/**
	 * @param book the book to set
	 */
	public void setBook(Book book) {
		this.book = book;
	}

	/**
	 * @return the royalty
	 */
	public int getRoyalty() {
		return royalty;
	}
	
	/**
	 * @param royalty the royalty to set
	 */
	public void setRoyalty(int royalty) {
		this.royalty = royalty;
	}
	

	/**
	 * @return the royalty
	 */
	public String getRoyaltyPercent() {
		return royaltyPercent + "%";
	}
	
	/**
	 * @param royalty the royalty to set
	 */
	public void setRoyaltyPercent(double royaltyPercent) {
		this.royaltyPercent = royaltyPercent;
	}
	/**
	 * @return the newRecord
	 */
	public Boolean getNewRecord() {
		return newRecord;
	}

	/**
	 * @param newRecord the newRecord to set
	 */
	public void setNewRecord(Boolean newRecord) {
		this.newRecord = newRecord;
	}
	
	/**
	 * @return author's name and royalty
	 */
	public String toString() {
		double r = (double) (getRoyalty() / 1_000.0);
		return author + " " + r + "%";
	}
	
	/**
	 * 
	 */
	public Boolean isValidRoyalty() {
		if (royalty >= 0 && royalty <= 100_000) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 */
	public void save() throws Exception {
		// validate royalty field before insertion into db
		// validate author fields before insertion into db
		if (!isValidRoyalty()) {
			throw new Exception("Invalid royalty. "
					          + "Must be a value between 0.0 and 1.0");
		}
		author.validateFields();
		if (this.getAuthor().getId() == 0) {
			book.getBookGateway().insertAuthor(this);
		} else {
		    book.getBookGateway().updateAuthor(this);
		}
	}
	
	/**
	 * 
	 */
	public void removeAuthor() throws Exception {
		book.getBookGateway().deleteAuthorBook(this);
	}
}
