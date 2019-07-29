package controllers;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import models.Publisher;

public class PublisherTableGateway {
	// fields
	private static Logger logger = LogManager.getLogger();
    private Connection connection;
    private ResultSet resultSet = null;
    private PreparedStatement preparedStatement = null;
    
    // constructor
    public PublisherTableGateway() throws Exception {
    	logger.info("Connecting publisher table gateway");
    	connection = null;
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
    
    /*
     * Return a list of Publishers from the database
     * @return list of publishers
     */
    public List<Publisher> getPublishers() {
    	List<Publisher> publishers = new ArrayList<Publisher>();
    	logger.info("Retrieving publisher list");
    	try {
    		// Select all the publishers from the database
    		preparedStatement = connection.prepareStatement(
    				"SELECT * FROM vzu944.publisher");
    		resultSet = preparedStatement.executeQuery();
    		/* 
    		 * Iterate over each tuple in result set
    		 * Create publisher objects from the result set
    		 * Add each publisher object to the list of publishers
    		 */
    		while(resultSet.next()) {
    			int id = resultSet.getInt("id"); // not null
    			String publisherName = resultSet.getString("publisher_name"); // not null
    			Timestamp timeStamp = resultSet.getTimestamp("date_added");
    			LocalDateTime dateAdded = timeStamp.toLocalDateTime();
    			Publisher publisher = new Publisher(id, publisherName, dateAdded);
    			publisher.setPublisherTableGateway(this);
    			publishers.add(publisher);
    		}
    	} catch (SQLException e) {
    		logger.error("Failed to retrieve publisher list");
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
    	// return list of publishers
    	return publishers;
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
}
