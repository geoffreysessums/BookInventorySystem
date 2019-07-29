package models;

import java.time.LocalDateTime;

import controllers.PublisherTableGateway;

public class Publisher {
	// fields
    private int id;
    private String publisherName;
	private LocalDateTime dateAdded;
	private PublisherTableGateway publisherGateway;
    
    // constructors
    public Publisher() {
    	id = 1;
    	publisherName = "Unknown";
    	dateAdded = null;
    }
    
    // methods
    public Publisher(int id, String publisherName, LocalDateTime dateAdded) {
    	this.id = id;
    	this.publisherName = publisherName;
    	this.dateAdded = dateAdded;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPublisherName() {
		return publisherName;
	}

	public void setPublisherName(String publisherName) {
		this.publisherName = publisherName;
	}

	@Override
	public String toString() {
		return publisherName;
	}

	public PublisherTableGateway getPublisherTableGateway() {
		return this.publisherGateway;
	}

	public void setPublisherTableGateway(PublisherTableGateway publisherGateway) {
		this.publisherGateway = publisherGateway;
	}
	
	public LocalDateTime getDateAdded() {
		return this.dateAdded;
	}
	
	public void setDateAdded(LocalDateTime dateAdded) {
		this.dateAdded = dateAdded;
	}
}
