package controllers;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;

import java.util.List;
import java.util.ResourceBundle;

import java.time.LocalDateTime;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import models.AuditTrailEntry;
import models.AuthorBook;
import models.Book;
import models.Publisher;

public class BookDetailViewController implements Initializable {
	private static Logger logger = LogManager.getLogger();
	@FXML private TextField tfTitle;
	@FXML private TextField tfSummary;
	@FXML private TextField tfYear;
	@FXML private TextField tfISBN;
	@FXML private ComboBox<Publisher> cbPublisher;
	@FXML private Button buttonSave;
	@FXML private Button buttonAuditTrail;
	@FXML private Button buttonAddAuthor;
	@FXML private Button buttonEdit;
	@FXML private Button buttonRemove;
	@FXML private TableView<AuthorBook> authorTableView;
	@FXML private TableColumn<?, ?> colName;
	@FXML private TableColumn<?, ?> colRoyalty;
	private ObservableList<AuthorBook> authorList;
	private List<Publisher> publisherList;
	private Book book;
	private BookGateway bookGateway;
	private LocalDateTime bookLocalDateTime;
	private AuthorBook authorBook;

    public BookDetailViewController(Book book, List<Publisher> publisherList) {
    	this.book = book;
    	this.bookGateway = book.getBookGateway();
    	this.bookLocalDateTime = book.getLastModified();
    	this.publisherList = publisherList;
	}

	@FXML
    void onMouseClicked(ActionEvent action) {
    	Object button = action.getSource();
    	if (button == buttonAuditTrail) {
        	logger.info("Audit button clicked for " + book.getTitle());
        	//  If switch view is unsuccessful, then stay on current view
			if(!MasterViewController.getInstance().switchView(ViewType.BOOK_AUDIT_TRAIL_VIEW, book)) {
				return;
			}
    	}
    	if (button == buttonAddAuthor) {
    		logger.info("Add Author button clicked for " + book.getTitle());
    		AuthorBook newAuthorBook = new AuthorBook();
    		newAuthorBook.setBook(book);
            displayAuthorBox(newAuthorBook);
            return;
    	}
    	if (button == buttonEdit) {
    		if (authorBook == null) {
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("Alert");
    			alert.setHeaderText(null);
    			alert.setContentText("You must select an author.");
    			alert.showAndWait();
    			return;
    		}
            displayAuthorBox(authorBook);
    	}
    	if (button == buttonRemove) {
    		logger.info("clicked remove author " + authorBook);
    		if (authorBook == null) {
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("ALERT");
    			alert.setHeaderText(null);
    			alert.setContentText("You must select an author.");
    			alert.showAndWait();
    			return;
    		}
    		try {
				authorBook.removeAuthor();
			} catch (Exception e) {
				e.printStackTrace();
			}
        	//  If switch view is unsuccessful, then stay on current view
			if(!MasterViewController.getInstance().switchView(ViewType.BOOK_DETAIL_VIEW, book)) {
				return;
			}
    	}
    	if (button == buttonSave) {
        	logger.info("Save button clicked");
        	// Check if book record exists:
        	// If book id is 0 (new book record), then insert the new book record
        	if (book.getId() == 0) {
        		try {
        			//update the model data
        			book.setTitle(tfTitle.getText());
        			book.setSummary(tfSummary.getText());
        			book.setYearPublished(Integer.parseInt(tfYear.getText()));
        			book.setPublisher(cbPublisher.getValue());
        			book.setISBN(tfISBN.getText());
        		    book.save();
            	} catch (Exception e) {
        			logger.error("Changes cannot be saved: " + e.getMessage());
        			Alert alert = new Alert(AlertType.INFORMATION);
        			alert.setTitle("ERROR.");
        			alert.setHeaderText(null);
        			alert.setContentText(e.getMessage());
        			alert.showAndWait();
        			return;
            	}
        		// enable audit trail button upon successful save
        		logger.info("Enabling audit trail button");
        		buttonAuditTrail.setDisable(false);
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("Changes Saved");
    			alert.setHeaderText(null);
    			alert.setContentText("Changes saved successfully!");
    			alert.showAndWait();
    			MasterViewController.getInstance().switchView(ViewType.BOOK_DETAIL_VIEW, book);
        	    return;	
        	}
        	// Update existing book record
        	// Implement Optimistic locking of book record
        	try {
        		// Check if current book db record's timestamp matches the
        		// the timestamp of the book we are attempting to insert.
        		// If the timestamps DO NOT MATCH, then show an error and
        		// request user refresh their view
            	LocalDateTime currentTimeStamp = bookGateway.getBookLastModifiedById(book.getId());
            	if(!currentTimeStamp.equals(bookLocalDateTime)) {
        			Alert alert = new Alert(AlertType.INFORMATION);
        			alert.setTitle("ERROR");
        			alert.setHeaderText(null);
        			alert.setContentText("Record has changed since this "
            				+ "view loaded.\n\nReturn to the Book List to "
        					+ "fetch a fresh copy of the book.");
        			alert.showAndWait();
            		return;
            	}
        	} catch (Exception e) {
                logger.error("Book gateway failed to retrieve timestamp " + e.getMessage());
    			Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("UNEXPECTED ERROR.");
    			alert.setHeaderText(null);
    			alert.setContentText("Refresh the view and try again.");
    			alert.showAndWait();
                return;
        	}
        	// Timestamp's MATCH: Update book record
    		// Check book detail view fields
    		// If the view fields have changed, then save those changes
    		if (hasChanged()) {
        	    saveChanges();
    		}
    		return;
    	}
    }
    
	private void displayAuthorBox(AuthorBook authorBook) {
    	String viewString = "";
		FXMLLoader loader = null;
		URL url = null;
		logger.info("Displaying author box ");
		logger.info("authorBook = " + authorBook);

		viewString = "../views/AuthorDialogView.fxml";
		url = getClass().getResource(viewString);
		loader = new FXMLLoader(url);
		loader.setController(new AuthorDialogViewController(authorBook));
	    Parent parent = null;
	    try {
		    parent = loader.load();
	    } catch (IOException e1) {
		    logger.info("Edit author failed");
		    e1.printStackTrace();
	    }
		Scene scene = new Scene(parent, 410, 250);
	    Stage stage = new Stage();
	    stage.initModality(Modality.APPLICATION_MODAL);
	    stage.setScene(scene);
	    stage.showAndWait();
	}

	public void initialize(URL location, ResourceBundle resources) throws NullPointerException {
		// initialize GUI fields
		logger.info("Initializing book detail view fields");
        tfTitle.setText(book.getTitle());
        tfSummary.setText(book.getSummary());
        tfYear.setText(Integer.toString(book.getYearPublished()));
        tfISBN.setText(book.getISBN());
        cbPublisher.getItems().addAll(publisherList);
        cbPublisher.setValue(book.getPublisher());
		// Disable Audit Trail Button if the book is new or there is no audit trail
		List<AuditTrailEntry> auditList = book.getAuditTrail();
		if (book.getId() == 0 && auditList.isEmpty()) {
			logger.info("Audit trail does not exist. Disabling audit trail button");
			buttonAuditTrail.setDisable(true);
		}
		// get authors for the given book
	    authorList = FXCollections.observableArrayList(); 
		authorList.addAll(book.getAuthors());
		colName.setCellValueFactory(new PropertyValueFactory<>("author"));
		colRoyalty.setCellValueFactory(new PropertyValueFactory<>("royaltyPercent"));
		authorTableView.setItems(authorList);
		
		// add click handler
		authorTableView.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				if (event.isPrimaryButtonDown() && event.getClickCount() == 1) {
					authorBook = authorTableView.getSelectionModel().getSelectedItem();
					logger.info("single-clicked " + authorBook);

				}
			}
		});
	}

	/**
	 * Saves the changes made to the book detail view fields by calling 
	 * insertAuditTrailEntry to record the changes made to the book within
	 * it's audit trail.
	 * @return true if save is successful, otherwise return false
	 * @throws Exception 
	 */
	public boolean saveChanges() {
		try {
			// check fields for correct input
			validateTextFields();
			// insert changes into audit trail table
			updateBookAuditTrail();
    		// update the the book model
    		book.setTitle(tfTitle.getText());
    		book.setSummary(tfSummary.getText());
    		book.setYearPublished(Integer.parseInt(tfYear.getText()));
    		book.setPublisher(cbPublisher.getValue());
    		book.setISBN(tfISBN.getText());
            // update book table entry
    		book.save();
    		// Inform user of successful save
    		Alert alert = new Alert(AlertType.INFORMATION);
    		alert.setTitle("Changes Saved");
    		alert.setHeaderText(null);
    		alert.setContentText("Changes saved successfully!");
    		alert.showAndWait();
		} catch (Exception e) {
    		logger.error("Book update failed: " + e.getMessage());
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("ERROR");
			alert.setHeaderText(null);
			alert.setContentText("Book update failed: " + e.getMessage());
			alert.showAndWait();
			// changes unsuccessful
			return false;
		}
		// changes successful
		return true;
    }

	public boolean hasChanged() {	    
		if(!(book.getISBN().contentEquals(tfISBN.getText()))) {
			return true;
		} else if(!(book.getSummary().contentEquals(tfSummary.getText()))) {
			return true;
		} else if(!(book.getTitle().contentEquals(tfTitle.getText()))) {
			return true;
		} else if(!(String.valueOf(book.getYearPublished()).contentEquals(tfYear.getText()))) {
			return true;
		} else if (book.getPublisher().getId() != cbPublisher.getValue().getId()) {
			return true;
		}
		//fall through: has not changed
		return false;
	}
	
	public void validateTextFields() throws Exception {
	    if (!book.isValidTitle(tfTitle.getText()))
	    	throw new Exception("Invalid title: " + tfTitle.getText());
	    if (!book.isValidSummary(tfSummary.getText()))
	    	throw new Exception("Invalid summary: " + tfSummary.getText());
	    if (!book.isValidYear(Integer.valueOf(tfYear.getText())))
	    	throw new Exception("Invalid published year: " + tfYear.getText());
	    if (!book.isValidISBN(tfISBN.getText()))
	    	throw new Exception("Invalid ISBN: " + tfISBN.getText());
	}
	
	public void updateBookAuditTrail() {
	    // If a book detail view field has changed, then insert those changes
	    // into the audit trail table
		int bookId = book.getId();
		String entryMessage = null;
	    logger.info("Inserting audit trail record(s) for " + book.getTitle());
	    if(!(book.getISBN().contentEquals(tfISBN.getText()))) {
		    entryMessage = "ISBN changed from " + book.getISBN() + " to " + tfISBN.getText();
		    bookGateway.insertAuditTrailEntry(bookId, entryMessage);
	    } else if(!(book.getSummary().contentEquals(tfSummary.getText()))) {
	    	entryMessage = "Summary changed from " + book.getSummary() + " to " + tfSummary.getText();
	    	bookGateway.insertAuditTrailEntry(bookId, entryMessage);
	    } else if(!(book.getTitle().contentEquals(tfTitle.getText()))) {
	    	entryMessage = "Title changed from " + book.getTitle() + " to " + tfTitle.getText();
	    	bookGateway.insertAuditTrailEntry(bookId, entryMessage);
	    } else if(!(String.valueOf(book.getYearPublished()).contentEquals(tfYear.getText()))) {
		    entryMessage = "Year Published changed from " 
	            + String.valueOf(book.getYearPublished()) + " to " 
		        + tfYear.getText();
		    bookGateway.insertAuditTrailEntry(bookId, entryMessage);
	    } else if (book.getPublisher().getId() != cbPublisher.getValue().getId()) {
	    	entryMessage = "Publisher changed from " + book.getPublisher() + " to " + cbPublisher.getValue();
	     	bookGateway.insertAuditTrailEntry(bookId, entryMessage);
	    }
	}
	
	public Book getBook() {
		return book;
	}
}
