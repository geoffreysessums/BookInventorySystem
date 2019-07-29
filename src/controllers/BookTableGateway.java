package controllers;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import controllers.BookGateway;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import models.AuditTrailEntry;
import models.Author;
import models.AuthorBook;
import models.Book;
import models.BookGhost;

/*
 * <description>
 * @author Geoffrey Sessums
 */
public class BookTableGateway implements BookGateway {
	private static final boolean SKIP_BOOK_CACHE = false;
	private static Logger logger = LogManager.getLogger();
    private Connection connection;
    private ResultSet resultSet = null;
    private PreparedStatement preparedStatement = null;
    
    // identity map hash map
	private HashMap<Integer, BookGhost> bookGhostCache;
	private HashMap<Integer, Book> bookCache;
    
    public BookTableGateway() throws Exception {
    	connection = null;
    	bookGhostCache = new HashMap<Integer, BookGhost>();
    	bookCache = new HashMap<Integer, Book>();
    	//connect to database and create connection instance
    	Properties properties = new Properties();
    	FileInputStream fileInput = null;
    	try {
    	    fileInput = new FileInputStream("db.properties");
    	    properties.load(fileInput);
    	    fileInput.close();

    	    // create data source
    	    MysqlDataSource dataSource = new MysqlDataSource();
            dataSource.setURL(properties.getProperty("MYSQL_DB_URL"));
            dataSource.setUser(properties.getProperty("MYSQL_DB_USERNAME"));
            dataSource.setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));

		    //create the connection
		    connection = dataSource.getConnection();
    	} catch (Exception e) {
    		e.printStackTrace();
    		throw new Exception(e);
    	}
    }
    
	/**
	 * return a list of Book objects from the book table
	 * 1. get all book ids and last_modified values 
	 * 2. look in cache for each, if book in cache and has not changed then skip
	 * 3. else, fetch the book from the db and put/replace it into the cache
     * @return list of books
     */
    public List<BookGhost> getBooks(int index, int pageSize) {
    	List<BookGhost> books = new ArrayList<BookGhost>();
    	PreparedStatement  preparedStatement = null;
    	ResultSet resultSet = null;
    	try {
    		// get all the books from the database
    		preparedStatement = connection.prepareStatement(
    				  "SELECT b.id, b.last_modified "
    				+ "FROM vzu944.Book b "
    				+ "ORDER BY b.id LIMIT ?, ?");
    		preparedStatement.setInt(1, index);
    		preparedStatement.setInt(2, pageSize);
    		resultSet = preparedStatement.executeQuery();
    		
    		/* 
    		 * Iterate over each tuple in result set.
    		 * Check cache for each book.
    		 * If the book is new or has changed then fetch the book from the 
    		 * DB and insert/replace in the cache
    		 */
    		while(resultSet.next()) {
    			int bookId = resultSet.getInt("id");
    			if (!bookGhostCache.containsKey(bookId)) {
    				// book in DB is new so fetch it and add to cache
    				BookGhost book = getBookGhostById(bookId);
    				books.add(book);
    				// add book to cache
    				bookGhostCache.put(book.getId(), book);
    			} else {
    				// check last_modified time stamps
    				LocalDateTime timeStampFromDB = resultSet.getTimestamp("last_modified").toLocalDateTime();
    				BookGhost bookOld = bookGhostCache.get(bookId);
    			    if (timeStampFromDB.isAfter(bookOld.getLastModified()))	{
    			    	BookGhost book = getBookGhostById(bookId);
    			    	books.add(book);
    			    	// add book to cache
    			    	bookGhostCache.put(book.getId(), book);
    			    } else {
    			    	// add the book from the cache in the list
    			    	books.add(bookGhostCache.get(bookId));
    			    }
    			}
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	// Close resultSet, preparedStatement, and connect
    	} finally {
    		try {
    			if (resultSet != null)
    				resultSet.close();
    			if (preparedStatement != null)
    				preparedStatement.close();
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	// return list of books
    	return books;
    }
   
	/**
	 * fetches bookGhost record from DB 
	 * assumes not already in bookGhost cache
	 * @param bookId
	 * @return
	 */
    public BookGhost getBookGhostById(int bookId) {
    	PreparedStatement preparedStatement = null;
		ResultSet resultSet = null; 
		BookGhost book = null;
		try {
			preparedStatement = connection.prepareStatement("SELECT * from vzu944.Book where id = ?");
			preparedStatement.setInt(1, bookId);
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			book = new Book();
			book.setId(resultSet.getInt("id"));
			book.setTitle(resultSet.getString("title"));
			book.setSummary(resultSet.getString("summary"));
			book.setYearPublished(resultSet.getInt("year_published"));
			book.setISBN(resultSet.getString("isbn"));
			//book.setBookGateway(this);
			
			Timestamp timeStamp = resultSet.getTimestamp("last_modified");
			book.setLastModified(timeStamp.toLocalDateTime());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if(preparedStatement != null)
					preparedStatement.close();
				if(resultSet != null)
					resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
        // return book		
		return book;
	}

	/**
     * 
     */
	public void updateBook(Book book) throws Exception {
		PreparedStatement preparedStatement = null;
		try {
			connection.setAutoCommit(false);

			preparedStatement = connection.prepareStatement(
			    "UPDATE vzu944.Book "
				+ "SET title = ?"
				+ ", summary = ?"
				+ ", year_published = ?"
				+ ", publisher_id = ?"
				+ ", isbn = ? " 
				+ "where id = ?");
			preparedStatement.setString(1, book.getTitle());
			preparedStatement.setString(2, book.getSummary());
			preparedStatement.setInt(3, book.getYearPublished());
			preparedStatement.setInt(4, book.getPublisherId());
			preparedStatement.setString(5, book.getISBN());
			preparedStatement.setInt(6, book.getId());
			preparedStatement.executeUpdate();
			// update model lastModified timestamp
			book.setLastModified(this.getBookLastModifiedById(book.getId()));
			connection.commit();
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			throw new Exception(e);
		} finally {
			try {
				if(preparedStatement != null) {
					preparedStatement.close();
				}
				connection.setAutoCommit(true);
				
			} catch (SQLException e) {
				throw new Exception("SQL Error: " + e.getMessage());
			}
		}
	}

	/**
	 * Note: Since the database is configured to handle the timestamp on update
	 * and the lastModified member in the Book object is initialized to null, 
	 * there is no need to set the current timestamp here. 
	 */
	@Override
	public void insertBook(Book book) throws SQLException {
		//create a prepared statement using an SQL query
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatementAuditTrail = null;
		PreparedStatement preparedStatementQuery = null;
		ResultSet resultSetQuery = null;
		ResultSet resultSet = null;
		int recordId = 0;
		try {
			logger.info("Inserting book");
			// Try to insert book
			String query = "insert into vzu944.Book "
					+ "(title, summary, year_published, publisher_id, isbn) "
					+ "values (?, ?, ?, ?, ?)";
			preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, book.getTitle());
			preparedStatement.setString(2, book.getSummary());
			preparedStatement.setInt(3, book.getYearPublished());
			preparedStatement.setInt(4, book.getPublisher().getId());
			preparedStatement.setString(5, book.getISBN());
			preparedStatement.executeUpdate();
			
			// get the generated key of the new record
			// jdbc returns a resultset of field data containing the new key(s)
			resultSet = preparedStatement.getGeneratedKeys();
			// make sure you don't call getInt() on an empty result set
			// next() also moves the result set to the first record
			if(resultSet != null && resultSet.next()) {
				// returned keys don't have column names unfortunately
				// so just ask for the value of the first column in the returned key set
				recordId = resultSet.getInt(1);
				logger.info("New record id is " + recordId);
				// set inserted book's id
				book.setId(recordId);
			}
			// get the timestamp of the book that was just inserted
			preparedStatementQuery = connection.prepareStatement("select * from vzu944.Book where id = ?");
			preparedStatementQuery.setInt(1, recordId);
			resultSetQuery = preparedStatementQuery.executeQuery();
			resultSetQuery.next();
			Timestamp timeStamp = resultSetQuery.getTimestamp("last_modified");
			
		    // Try inserting audit trail record with entry_msg = "Book added"
			logger.info("Creating audit trail");
			String auditQuery = "INSERT INTO vzu944.book_audit_trail "
					            + "(book_id, date_added, entry_msg) "
					            + "VALUES (?, ?, ?)";
			preparedStatementAuditTrail = connection.prepareStatement(auditQuery);
			preparedStatementAuditTrail.setInt(1, recordId);
			preparedStatementAuditTrail.setTimestamp(2, timeStamp);
			preparedStatementAuditTrail.setString(3, "Book Added");
			preparedStatementAuditTrail.executeUpdate();
			
		} catch (SQLException e) {
			logger.error("SQL Exception: " + e.getMessage());
			throw new SQLException("SQL Error: " + e.getMessage());
		} finally {
			// be sure to close things properly if they are open, regardless of exception
			try {
				if(resultSet != null) {
					resultSet.close();
					resultSetQuery.close();
				}
				if(preparedStatement != null) {
					preparedStatement.close();
				    preparedStatementAuditTrail.close();
			    }
			} catch (SQLException e) {
				logger.error("SQL Exception: " + e.getMessage());
				throw new SQLException("SQL Error: " + e.getMessage());
			}
		}	
	}
    
	@Override
	public void deleteBook(BookGhost book) throws Exception {
		// create a prepared statement using an SQL query
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatementAuditTrail = null;
		ResultSet resultSet = null;
		int bookId = book.getId();
		logger.info("Deleting book_id: " + bookId);
		try {
    		preparedStatement = connection.prepareStatement(
    				"delete from vzu944.Book "
    				+ "where id = ?");
            preparedStatement.setInt(1, bookId);
			preparedStatement.executeUpdate();
			// delete audit trail entries for book
			preparedStatementAuditTrail = null;
    		preparedStatementAuditTrail = connection.prepareStatement(
    				"delete from vzu944.book_audit_trail "
    				+ "where book_id = ?");
            preparedStatementAuditTrail.setInt(1, bookId);
			preparedStatementAuditTrail.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL Exception: " + e.getMessage());
			//throw new Exception("SQL Error: " + e.getMessage());
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("ALERT");
			alert.setHeaderText(null);
			alert.setContentText(e.getMessage());
			alert.showAndWait();
		} finally {
			// be sure to close things properly if they are open, regardless of exception
			try {
				if(resultSet != null)
					resultSet.close();
				if(preparedStatement != null) {
					preparedStatement.close();
					preparedStatementAuditTrail.close();
				}
			} catch (SQLException e) {
				logger.error("SQL Exception: " + e.getMessage());
				throw new Exception("SQL Error: " + e.getMessage());
			}
		}	
	}
	
	public LocalDateTime getBookLastModifiedById(int id) throws Exception {
		LocalDateTime localDateTime = null;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement("select * from vzu944.Book where id = ?");
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			Timestamp timeStamp = resultSet.getTimestamp("last_modified");
			localDateTime = timeStamp.toLocalDateTime();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception(e);
		} finally {
			try {
				if(preparedStatement != null)
					preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new Exception(e);
			}
		}

		return localDateTime;
	}
	
	/**
	 * check book cache for book first. if there, check last modified. if current then return it
	 * else fetch from DB, put in cache and return
	 * @param bookId
	 * @return
	 */
	public Book getBookById(int bookId) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		Book book = null;
		
		/* determine whether or not it's necessary to fetch car from DB and
		 * put in cache
		 */
		boolean fetchFromDB = false;

		try {
			if (SKIP_BOOK_CACHE || !bookCache.containsKey(bookId)) {
				// book in DB is new so fetch and add to cache
				fetchFromDB = true;
			} else {
				// check last_modified time stamps
				preparedStatement = connection.prepareStatement(
						  "SELECT b.id, b.last_modified "
						+ "FROM vzu944.Book b "
						+ "WHERE b.id = ?");
				preparedStatement.setInt(1, bookId);
				resultSet = preparedStatement.executeQuery();
				resultSet.next();	
				LocalDateTime timeStampDB = resultSet.getTimestamp("last_modified").toLocalDateTime();
				Book bookOld = bookCache.get(bookId);
				if (timeStampDB.isAfter(bookOld.getLastModified())) {
					fetchFromDB = true;
					resultSet.close();
				    preparedStatement.close();	
				}
			}
			if (fetchFromDB) {
				preparedStatement = connection.prepareStatement(
						"SELECT * FROM vzu944.Book WHERE id = ?");
				preparedStatement.setInt(1, bookId);
				resultSet = preparedStatement.executeQuery();
				resultSet.next();	
				book = new Book();
				book.setId(resultSet.getInt("id"));
				book.setTitle(resultSet.getString("title"));
				book.setSummary(resultSet.getString("summary"));
				book.setYearPublished(resultSet.getInt("year_published"));
				book.setISBN(resultSet.getString("isbn"));
				book.setLastModified(resultSet.getTimestamp("last_modified").toLocalDateTime());
				book.setBookGateway(this);
				// add book to cache
				bookCache.put(book.getId(), book);
			} else {
				// add the book from the cache in the list
				book = bookCache.get(bookId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			//throw new Exception(e);
		} finally {
			try {
				if(preparedStatement != null)
					preparedStatement.close();
				if (resultSet != null) 
					resultSet.close();
			} catch (SQLException e) {
				e.printStackTrace();
				//throw new Exception(e);
			}
		}
	    // return book	
		return book;
	}
	
	public List<AuditTrailEntry> getAuditTrail(Book book) throws Exception {
        List<AuditTrailEntry> auditTrail = new ArrayList<AuditTrailEntry>();
    	
    	try {
    		// Select the audit trail from the database using the book id
    		preparedStatement = connection.prepareStatement(
    				"SELECT * "
    				+ "FROM book_audit_trail "
    				+ "INNER JOIN Book b ON b.id = book_id "
    				+ "WHERE b.id = ? "
    				+ "ORDER BY date_added ASC");
    		preparedStatement.setInt(1, book.getId());
    		resultSet = preparedStatement.executeQuery();
    		
    		
    		/* 
    		 * Iterate over each tuple in result set
    		 * Create audit trail entry objects from the result set
    		 * Add each entry object to the list entries
    		 */
    		while(resultSet.next()) {
    			int entryId = resultSet.getInt("entry_id");
    			int bookId = resultSet.getInt("book_id");
    			Timestamp entryTimeStamp = resultSet.getTimestamp("date_added");
    			LocalDateTime entryLocalDateTime = entryTimeStamp.toLocalDateTime();
    			String message = resultSet.getString("entry_msg");
    			AuditTrailEntry entry = new AuditTrailEntry(entryId, bookId, entryLocalDateTime, message);
    			auditTrail.add(entry);
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	// Close resultSet, preparedStatement, and connect
    	} finally {
    		try {
    			if (resultSet != null)
    				resultSet.close();
    			if (preparedStatement != null)
    				preparedStatement.close();
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	return auditTrail;
	}
	
	public void insertAuditTrailEntry(int bookId, String entryMessage) {
		//create a prepared statement using an SQL query
		PreparedStatement preparedStatement = null;
		try {
		    // Insert audit trail record with an entry message
			String query = "INSERT INTO vzu944.book_audit_trail "
					            + "(book_id, entry_msg) "
					            + "VALUES (?, ?)";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setInt(1, bookId);
			preparedStatement.setString(2, entryMessage);
			preparedStatement.executeUpdate();
			
		} catch (SQLException e) {
			logger.error("Unable to insert audit trail entry " + e.getMessage());
		} finally {
			// be sure to close things properly if they are open, regardless of exception
			try {
				if(preparedStatement != null) {
					preparedStatement.close();
			    }
			} catch (SQLException e) {
				logger.error("SQL Exception: " + e.getMessage());
			}
		}
	}
	
    public void close() {
    	if (connection != null) {
    		try {
    			connection.close();
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    }

	public List<AuthorBook> getAuthorsForBook(Book book) throws Exception {
		List<AuthorBook> authors = new ArrayList<AuthorBook>();
		Book bookRecord = null;
		Author author = null;
		try {
		    preparedStatement = connection.prepareStatement(
			    	"SELECT * "
			    	+ "FROM author_book "
			    	+ "WHERE book_id = ? ");
		    preparedStatement.setInt(1,  book.getId());
		    resultSet = preparedStatement.executeQuery();
		    
		    /*
		     * 
		     */
		    while(resultSet.next()) {
		    	int authorId = resultSet.getInt("author_id");
		    	int bookId = resultSet.getInt("book_id");
		    	bookRecord = getBookById(bookId);
		    	author = getAuthorById(authorId);
		    	// convert the db royalty decimal value in x.xxxxx to int
		    	int royalty = (int) (resultSet.getDouble("royalty") * 100_000);
		    	double royaltyPercent = resultSet.getDouble("royalty") * 100;
		    	AuthorBook authorBook = new AuthorBook(author, bookRecord, royalty, royaltyPercent);
		    	authors.add(authorBook);
		    }
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (resultSet != null)
					resultSet.close();
				if (preparedStatement != null)
					preparedStatement.close();
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}
		return authors;
	}

	private Author getAuthorById(int authorId) throws Exception {
		Author author = null;
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = connection.prepareStatement("select * from vzu944.author where id = ?");
			preparedStatement.setInt(1, authorId);
			ResultSet resultSet = preparedStatement.executeQuery();
			resultSet.next();
			author = new Author();
			author.setId(resultSet.getInt("id"));
			author.setFirstName(resultSet.getString("first_name"));
			author.setLastName(resultSet.getString("last_name"));
			Date date = resultSet.getDate("dob");
			LocalDate localDate = date.toLocalDate();
			author.setDateOfBirth(localDate);
			author.setGender(resultSet.getString("gender").charAt(0));
			author.setWebSite(resultSet.getString("web_site"));
			//author.setBookGateway(this);
			
			//Timestamp timeStamp = resultSet.getTimestamp("last_modified");
			//author.setLastModified(timeStamp.toLocalDateTime());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new Exception(e);
		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new Exception(e);
			}
		}
		return author;
	}
	
	/**
	 * 
	 */
	public void insertAuthor(AuthorBook authorBook) {
		// create a prepared statement using SQL query
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatementAuthorBook = null;
		PreparedStatement preparedStatementQuery = null;
		ResultSet resultSet = null;
		ResultSet resultSetQuery = null;
		int recordId;
		try {
			// Insert author record into author table
			logger.info("Inserting author record");
			String authorQuery = "INSERT INTO vzu944.author "
					     + "(first_name, last_name, dob, gender, web_site) "
					     + "VALUES (?, ?, ?, ?, ?)";
			preparedStatement = connection.prepareStatement(authorQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(1, authorBook.getAuthor().getFirstName());
			preparedStatement.setString(2, authorBook.getAuthor().getLastName());
			LocalDate localDate = authorBook.getAuthor().getDateOfBirth();
			Date date = Date.valueOf(localDate);
			preparedStatement.setDate(3, date);
			preparedStatement.setString(4, Character.toString(authorBook.getAuthor().getGender()));
			preparedStatement.setString(5, authorBook.getAuthor().getWebSite());
			preparedStatement.executeUpdate();

			// get the generated key of the new record
			// jdbc returns a resultset of field data containing the new key(s)
			resultSet = preparedStatement.getGeneratedKeys();
			// make sure you don't call getInt() on an empty result set
			// next() also moves the result set to the first record
			if(resultSet != null && resultSet.next()) {
				// returned keys don't have column names unfortunately
				// so just ask for the value of the first column in the returned key set
				recordId = resultSet.getInt(1);
				// set inserted author's id
				authorBook.getAuthor().setId(recordId);
			}
			// insert author-book record into db
			String authorBookQuery = "INSERT INTO vzu944.author_book "
					               + "(author_id, book_id, royalty) "
					               + "VALUES (?, ?, ?)";
			preparedStatementAuthorBook = connection.prepareStatement(authorBookQuery);
			preparedStatementAuthorBook.setInt(1, authorBook.getAuthor().getId());
			preparedStatementAuthorBook.setInt(2, authorBook.getBook().getId());
			double royalty = (double) authorBook.getRoyalty() / 100_000;
			preparedStatementAuthorBook.setDouble(3, royalty);
			preparedStatementAuthorBook.executeUpdate();
			
			//preparedStatementAuthorBook.setTimestamp(4, new Timestamp(System.currentTimeMillis()));

			
			// get the timestamp of the author-book record just inserted
			String timeStampQuery = "SELECT last_modified "
                                  + "FROM vzu944.author_book "
					              + "WHERE author_id = ? AND book_id = ?";
			preparedStatementQuery = connection.prepareStatement(timeStampQuery);
			preparedStatementQuery.setInt(1, authorBook.getAuthor().getId());
			preparedStatementQuery.setInt(2, authorBook.getBook().getId());
			resultSetQuery = preparedStatementQuery.executeQuery();
			resultSetQuery.next();

			// insert audit trail record with entry_msg "Author added"
            String entryMessage = "Added author " + authorBook.getAuthor();
            insertAuditTrailEntry(authorBook.getBook().getId(), entryMessage);

            /*
            Timestamp timeStamp = resultSetQuery.getTimestamp("last_modified");
			logger.info("Inserting audit trail entry");
			String auditQuery = "INSERT INTO vzu944.book_audit_trail "
					          + "(book_id, date_added, entry_msg) "
					          + "VALUES (?, ?, ?)";
			preparedStatementAuditTrail = connection.prepareStatement(auditQuery);
			preparedStatementAuditTrail.setInt(1, authorBook.getBook().getId());
			preparedStatementAuditTrail.setTimestamp(2,  timeStamp);
			preparedStatementAuditTrail.setString(3, "Author added");
			preparedStatementAuditTrail.executeUpdate();
			*/
		} catch (SQLException e) {
			logger.error("Unable to insert author record " + e.getMessage());
		} finally {
			// close
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				logger.error("SQL Exception: " + e.getMessage());
			}
		}
	}

	@Override
	public void deleteAuthorBook(AuthorBook authorBook) throws Exception {
		// create a prepared statement
        PreparedStatement preparedStatement = null;
        int bookId = authorBook.getBook().getId();
        int authorId = authorBook.getAuthor().getId();
        String entryMessage = "Removed author " + authorBook.getAuthor();
        logger.info("Removinng author_book record");
        try {
        	preparedStatement = connection.prepareStatement(
        			  "DELETE FROM vzu944.author_book "
        			+ "WHERE author_id = ? AND book_id = ?");
        	preparedStatement.setInt(1,  authorId);
        	preparedStatement.setInt(2,  bookId);
        	preparedStatement.executeUpdate();
			// insert audit trail record
        	insertAuditTrailEntry(bookId, entryMessage);
		} catch (SQLException e) {
			logger.error("SQL Exception: " + e.getMessage());
			throw new Exception("SQL Error: " + e.getMessage());
		} finally {
			// be sure to close things properly if they are open, regardless of exception
			try {
				if(resultSet != null)
					resultSet.close();
				if(preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {
				logger.error("SQL Exception: " + e.getMessage());
				throw new Exception("SQL Error: " + e.getMessage());
			}
		}
	}

	@Override
	public void updateAuthor(AuthorBook authorBook) throws Exception {
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatementAuthorBook = null;
		Author author = authorBook.getAuthor();
		int bookId = authorBook.getBook().getId();
		String entryMessage = "Updated author record";
		try {
			//connection.setAutoCommit(false);

			preparedStatement = connection.prepareStatement(
			    "UPDATE vzu944.author "
				+ "SET first_name = ?"
				+ ", last_name = ?"
				+ ", dob = ?"
				+ ", gender = ?"
				+ ", web_site = ? " 
				+ "where id = ?");
			preparedStatement.setString(1, author.getFirstName());
			preparedStatement.setString(2, author.getLastName());
			LocalDate localDate = authorBook.getAuthor().getDateOfBirth();
			Date date = Date.valueOf(localDate);
			preparedStatement.setDate(3, date);
			preparedStatement.setString(4, Character.toString(authorBook.getAuthor().getGender()));
			preparedStatement.setString(5, author.getWebSite());
			preparedStatement.setInt(6, author.getId());
			preparedStatement.executeUpdate();
			
			preparedStatementAuthorBook = connection.prepareStatement(
					"UPDATE vzu944.author_book "
					+ "SET royalty = ?"
					+ "WHERE author_id = ? AND book_id = ?");
			preparedStatementAuthorBook.setDouble(1, authorBook.getRoyalty() / 100_000.0);
			preparedStatementAuthorBook.setInt(2,  author.getId());
			preparedStatementAuthorBook.setInt(3,  authorBook.getBook().getId());
			preparedStatementAuthorBook.executeUpdate();
			
			// insert audit trail record
        	insertAuditTrailEntry(bookId, entryMessage);
			// update model lastModified timestamp
			//book.setLastModified(this.getBookLastModifiedById(book.getId()));
			//connection.commit();
		} catch (SQLException e) {
			/*
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			*/
			throw new Exception(e);
		} finally {
			try {
				if(preparedStatement != null) {
					preparedStatement.close();
				}
				//connection.setAutoCommit(true);
				
			} catch (SQLException e) {
				throw new Exception("SQL Error: " + e.getMessage());
			}
		}
	}
	
	/**
	 * 
	 */
	public int getRecordCountByTitle(String title) {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		int count = 0;
		try {
			preparedStatement = connection.prepareStatement(
					  "SELECT COUNT(b.title) AS count "
					+ "FROM vzu944.Book b "
					+ "WHERE b.title LIKE ?");
		    preparedStatement.setString(1, title);
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			count = resultSet.getInt("count");
		} catch (Exception e) {
		    logger.error("SQL exception : " + e.getMessage());
		} finally {
			try {
				if (resultSet != null) 
					resultSet.close();
				if (preparedStatement != null)
					preparedStatement.close();
			} catch (SQLException e) {
				logger.error("SQL Exception: " + e.getMessage());
			}
		}
		logger.info("Returning record count: " + count);
		return count;
	}
	
	/**
	 * 
	 */
	public int getTotalRecordCount() {
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		int count = 0;
		try {
			preparedStatement = connection.prepareStatement(
					  "SELECT COUNT(*) AS count "
					+ "FROM vzu944.Book b");
			resultSet = preparedStatement.executeQuery();
			resultSet.next();
			count = resultSet.getInt("count");
		} catch (Exception e) {
		    logger.error("SQL exception : " + e.getMessage());
		} finally {
			try {
				if (resultSet != null) 
					resultSet.close();
				if (preparedStatement != null)
					preparedStatement.close();
			} catch (SQLException e) {
				logger.error("SQL Exception: " + e.getMessage());
			}
		}
		logger.info("Returning total record count: " + count);
		return count;
	}
	
	/**
	 * 
	 */
	public List<BookGhost> search(String title, int index, int pageSize) throws Exception {
		List<BookGhost> books = new ArrayList<BookGhost>();
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
		    preparedStatement = connection.prepareStatement(
		    		  "SELECT b.id, b.last_modified "
		    		+ "FROM vzu944.Book b "
		    	    + "WHERE b.title LIKE ? "
		    		+ "ORDER BY b.id LIMIT ?,?");
		    preparedStatement.setString(1, title);
		    preparedStatement.setInt(2, index);
		    preparedStatement.setInt(3, pageSize);
		    resultSet = preparedStatement.executeQuery();
    		while(resultSet.next()) {
    			int bookId = resultSet.getInt("id");
    			if (!bookGhostCache.containsKey(bookId)) {
    				// book in DB is new so fetch it and add to cache
    				BookGhost book = getBookGhostById(bookId);
    				books.add(book);
    				// add book to cache
    				bookGhostCache.put(book.getId(), book);
    			} else {
    				// check last_modified time stamps
    				LocalDateTime timeStampFromDB = resultSet.getTimestamp("last_modified").toLocalDateTime();
    				BookGhost bookOld = bookGhostCache.get(bookId);
    			    if (timeStampFromDB.isAfter(bookOld.getLastModified()))	{
    			    	BookGhost book = getBookGhostById(bookId);
    			    	books.add(book);
    			    	// add book to cache
    			    	bookGhostCache.put(book.getId(), book);
    			    } else {
    			    	// add the book from the cache in the list
    			    	books.add(bookGhostCache.get(bookId));
    			    }
    			}
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	// Close resultSet, preparedStatement, and connect
    	} finally {
    		try {
    			if (resultSet != null)
    				resultSet.close();
    			if (preparedStatement != null)
    				preparedStatement.close();
    		} catch (SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	// return list of books
    	return books;
	}

	/*
	@Override
	public void deleteBookById(int bookId) throws Exception {
		// create a prepared statement using an SQL query
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatementAuditTrail = null;
		ResultSet resultSet = null;
		logger.info("Deleting book_id: " + bookId);
		try {
    		preparedStatement = connection.prepareStatement(
    				"delete from vzu944.Book "
    				+ "where id = ?");
            preparedStatement.setInt(1, bookId);
			preparedStatement.executeUpdate();
			// delete audit trail entries for book
			preparedStatementAuditTrail = null;
    		preparedStatementAuditTrail = connection.prepareStatement(
    				"delete from vzu944.book_audit_trail "
    				+ "where book_id = ?");
            preparedStatementAuditTrail.setInt(1, bookId);
			preparedStatementAuditTrail.executeUpdate();
		} catch (SQLException e) {
			logger.error("SQL Exception: " + e.getMessage());
			//throw new Exception("SQL Error: " + e.getMessage());
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("ALERT");
			alert.setHeaderText(null);
			alert.setContentText(e.getMessage());
			alert.showAndWait();
		} finally {
			// be sure to close things properly if they are open, regardless of exception
			try {
				if(resultSet != null)
					resultSet.close();
				if(preparedStatement != null) {
					preparedStatement.close();
					preparedStatementAuditTrail.close();
				}
			} catch (SQLException e) {
				logger.error("SQL Exception: " + e.getMessage());
				throw new Exception("SQL Error: " + e.getMessage());
			}
		}	
	}
	*/
}