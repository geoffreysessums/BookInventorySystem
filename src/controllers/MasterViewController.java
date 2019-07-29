package controllers;


import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import models.AuditTrailEntry;
import models.Book;
import models.BookGhost;
import models.Publisher;

/**
 * Uses the Singleton pattern to maintain state, gateways, and provide 
 * centralized control of different views.
 * @author Geoffrey Sessums
 */
public class MasterViewController {
	private static final Logger logger = LogManager.getLogger(MasterViewController.class);
	private static MasterViewController instance = null;
	
	private BorderPane rootPane;
    private BookTableGateway bookGateway;
    private PublisherTableGateway publisherGateway;
    private BookDetailViewController activeController;
    
	private MasterViewController() {
		try {
			bookGateway = new BookTableGateway();
			publisherGateway = new PublisherTableGateway();
			activeController = null;
		} catch (Exception e) {
			e.printStackTrace();
			Platform.exit();
		}
	}
	
	public static MasterViewController getInstance() {
		if(instance == null)
			instance = new MasterViewController();
		return instance;
	}

    public boolean switchView(ViewType viewType, Object data) {
		//check if current controller needs to save
		//if so, prompt and handle user's response
		if(activeController != null) {
			logger.info("Checking if view has changed");
			if(activeController.hasChanged()) {
				logger.info("WARNING: The active view has changed. Prompt to save.");
						
				Alert alert = new Alert(AlertType.CONFIRMATION);	
				alert.getButtonTypes().clear();
				ButtonType buttonTypeOne = new ButtonType("Yes");
				ButtonType buttonTypeTwo = new ButtonType("No");
				ButtonType buttonTypeThree = new ButtonType("Cancel");
				alert.getButtonTypes().setAll(buttonTypeOne, buttonTypeTwo, buttonTypeThree);

				alert.setTitle("Save Changes?");
				alert.setHeaderText("The current view has unsaved changes.");
				alert.setContentText("Do you wish to save them before switching to a different view?");

				Optional<ButtonType> result = alert.showAndWait();
				if(result.get().getText().equalsIgnoreCase("Yes")) {
					logger.info("Saving the view.");
					// If save changes is unsuccessful, then return false to the
					// caller
					if(!this.activeController.saveChanges())
						return false;
				} else if(result.get().getText().equalsIgnoreCase("Cancel")) {
					return false;
				}			
			}
		}
    	String viewString = "";
		FXMLLoader loader = null;
		URL url = null;
		// start view load timer
		long startTime = System.currentTimeMillis();
    	switch(viewType) {
    		case BOOK_LIST_VIEW:
    			logger.info("Displaying Book List view");
    			List<BookGhost> bookList = bookGateway.getBooks(0, 50);
    			viewString = "../views/BookListView.fxml";
        		url = getClass().getResource(viewString);
    			loader = new FXMLLoader(url);
    			activeController = null;
        		loader.setController(new BookListViewController(bookList));
    			break;
    		case BOOK_DETAIL_VIEW:
    			logger.info("Displaying Book detail view");
    			List<Publisher> publisherList = publisherGateway.getPublishers();
    			viewString = "../views/BookDetailView.fxml";
        		url = getClass().getResource(viewString);
    			loader = new FXMLLoader(url);
    			if (((BookGhost) data).getId() == 0)
					activeController = new BookDetailViewController((Book) data, publisherList); 
    			else
					activeController = new BookDetailViewController(bookGateway.getBookById(((BookGhost) data).getId()), publisherList); 
    			loader.setController(activeController);
    			break;	
    		case BOOK_AUDIT_TRAIL_VIEW:
    			logger.info("Displaying Book audit trail");
    			Book book = (Book) data;
    			List<AuditTrailEntry> auditList = book.getAuditTrail();
    			viewString = "../views/AuditTrailView.fxml";
        		url = getClass().getResource(viewString);
    			loader = new FXMLLoader(url);
    			//activeController = new AuditTrailController(auditList); 
    			//loader.setController(activeController);
    			activeController = null;
    			loader.setController(new AuditTrailController(book, auditList));
    			break;
    	}
    	Parent viewNode = null;
		try {
			viewNode = loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// attach view to app's center of border pane
		rootPane.setCenter(viewNode);
		long endTime = System.currentTimeMillis();
		logger.info("Time to load view (ms): " + (endTime - startTime));
		return true;
    }

	public BorderPane getRootPane() {
		return rootPane;
	}

	public void setBorderPane(BorderPane rootPane) {
		this.rootPane = rootPane;
	}
	
	public BookGateway getBookGateway() {
		return bookGateway;
	}
	
	public void setBookTableGateway(BookTableGateway bookGateway) {
		this.bookGateway = bookGateway;
	}
	
	public PublisherTableGateway getPublisherTableGateway() {
		return publisherGateway;
	}
	
	public void setPublisherTableGateway(PublisherTableGateway publisherGateway) {
		this.publisherGateway = publisherGateway;
	}
	
    public void close() {
       	bookGateway.close();
    	publisherGateway.close();
    }

	public BookDetailViewController getActiveController() {
		return activeController;
	}

}
