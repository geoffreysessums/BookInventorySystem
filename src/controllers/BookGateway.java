package controllers;

import models.AuditTrailEntry;
import models.AuthorBook;
import models.Book;
import models.BookGhost;

import java.time.LocalDateTime;
import java.util.List;
public interface BookGateway {
	public void close();
	public List<BookGhost> getBooks(int start, int end);
	public void updateBook(Book book) throws Exception;
	public void insertBook(Book book) throws Exception;
	public void deleteBook(BookGhost book) throws Exception;
	//public Book getBookById(int id);
	public List<AuditTrailEntry> getAuditTrail(Book book) throws Exception;
	public List<AuthorBook> getAuthorsForBook(Book book) throws Exception;
	public LocalDateTime getBookLastModifiedById(int id) throws Exception;
	public void insertAuditTrailEntry(int bookId, String entryMessage);
	public void insertAuthor(AuthorBook authorBook);
	public void deleteAuthorBook(AuthorBook author) throws Exception;
	public void updateAuthor(AuthorBook authorBook) throws Exception;
	public int getRecordCountByTitle(String column);
	public int getTotalRecordCount();
	public List<BookGhost> search(String bookTitle, int index, int pageSize) throws Exception;
	public BookGhost getBookGhostById(int count);
	public Book getBookById(int bookId);
	//void deleteBookById(int bookId) throws Exception;
}
