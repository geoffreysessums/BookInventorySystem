package controllers;

import java.net.URL;

import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import models.AuthorBook;

public class AuthorDialogViewController implements Initializable {
    // fields
	private static Logger logger = LogManager.getLogger();
	@FXML private TextField tfFirstName;	
	@FXML private TextField tfLastName;
	@FXML private TextField tfGender;
	@FXML private TextField tfWebsite;
	@FXML private TextField tfRoyalty;
	@FXML private DatePicker dpDOB;
	@FXML private Button buttonSave;
	
    private AuthorBook authorBook;
	//private Author author;
	
	public AuthorDialogViewController(AuthorBook authorBook) {
		this.authorBook = authorBook;
		//this.author = authorBook.getAuthor();
	}
	
	// methods
	@FXML
    void onMouseClicked(ActionEvent action) {
    	Object button = action.getSource();
    	if (button == buttonSave) {
    		if (authorBook.getNewRecord()) {
        		try {
    			    // save author and author-book relationship data
        		    logger.info("Adding new author");
    			    authorBook.getAuthor().setFirstName(tfFirstName.getText());
    			    authorBook.getAuthor().setLastName(tfLastName.getText());
    			    authorBook.getAuthor().setGender(tfGender.getText().charAt(0));
    			    authorBook.getAuthor().setWebSite(tfWebsite.getText());
    			    authorBook.getAuthor().setDateOfBirth(dpDOB.getValue());

    			    // convert the text field string to double, scale it so as to
    			    // loose precision, then cast to int
    			    int r = (int) (Double.parseDouble(tfRoyalty.getText()) * 100_000);
    			    logger.info("royalty = " + r);
    			    authorBook.setRoyalty(r);
    			    authorBook.setNewRecord(false);
    			    authorBook.save();
        		} catch (Exception e) {
        			logger.error("Changes cannot be saved: " + e.getMessage());
        			Alert alert = new Alert(AlertType.INFORMATION);
        			alert.setTitle("ERROR");
        			alert.setHeaderText(null);
        			alert.setContentText(e.getMessage());
        			alert.showAndWait();
        			return;
        		}
        		Alert alert = new Alert(AlertType.INFORMATION);
    			alert.setTitle("Changes Saved");
    			alert.setHeaderText(null);
    			alert.setContentText("Changes saved successfully!");
    			alert.showAndWait();
    			MasterViewController.getInstance().switchView(ViewType.BOOK_DETAIL_VIEW, authorBook.getBook());
    			closeStage(action);
    			return;
    		}
    		/*
    		try {
                // update existing author record
    		    // update existing author_book royalty NOTE: check timesstamps on author_book record
    		    LocalDateTime currentTimeStamp = bookGateway.getBookLastModifiedById(book.getId());
    		    if(!currentTimeStamp.equals(bookLocalDateTime)) {
    		    	Alert alert = new Alert(AlertType.INFORMATION);
    			    alert.setTitle("ALERT");
    			    alert.setHeaderText(null);
    			    alert.setContentText("Record has changed since this "
        				+ "view loaded.\n\nReturn to the Book List to "
    					+ "fetch a fresh copy of the book.");
    			    alert.showAndWait();
        		    return;
        		}
    		} catch (Exception e) {
    			
    		}
    		*/
    		if (hasChanged()) {
    			// if changes are saved successfully, update audit book trail
    			if (saveChanges()) {
    				updateBookAuditTrail();
        			MasterViewController.getInstance().switchView(ViewType.BOOK_DETAIL_VIEW, authorBook.getBook());
        			closeStage(action);
        			return;
    			}
    		}
    	}
		closeStage(action);
	}
	
    private void closeStage(ActionEvent event) {
        Node  source = (Node)  event.getSource(); 
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//  if author book record exits, then initialize fields
		if (!authorBook.getNewRecord()) {
		    logger.info("Initializing author dialog box fields");
		    tfFirstName.setText(authorBook.getAuthor().getFirstName());	
		    tfLastName.setText(authorBook.getAuthor().getLastName());;
		    tfGender.setText(Character.toString(authorBook.getAuthor().getGender()));;
		    tfWebsite.setText(authorBook.getAuthor().getWebSite());;
		    double royalty = (double) authorBook.getRoyalty() / 100_000;
		    tfRoyalty.setText(String.valueOf(royalty));;
		    dpDOB.setValue(authorBook.getAuthor().getDateOfBirth());
		}
	}
	
	/*
	 * 
	 */
	public boolean hasChanged() {
		String gender = Character.toString(authorBook.getAuthor().getGender());
	    double royalty = (double) authorBook.getRoyalty() / 100_000;
	    double royaltyField = Double.parseDouble(tfRoyalty.getText());
		if(!authorBook.getAuthor().getFirstName().contentEquals(tfFirstName.getText())) {
			return true;
		} else if (!authorBook.getAuthor().getLastName().contentEquals(tfLastName.getText())) {
			return true;
		} else if (!authorBook.getAuthor().getDateOfBirth().isEqual(dpDOB.getValue())) {
			return true;
		} else if (!gender.equalsIgnoreCase(tfGender.getText())) {
			return true;
		} else if (!authorBook.getAuthor().getWebSite().contentEquals(tfWebsite.getText())) {
			return true;
		} else if (0 != Double.compare(royalty, royaltyField)) {
			return true;
		}
		// fall through: has not changed
		return false;
	}
	
	/**
	 * 
	 */
	public boolean saveChanges() {
		try {
		    // save author and author-book relationship data
			authorBook.getAuthor().setFirstName(tfFirstName.getText());
			authorBook.getAuthor().setLastName(tfLastName.getText());
			authorBook.getAuthor().setGender(tfGender.getText().charAt(0));
			authorBook.getAuthor().setWebSite(tfWebsite.getText());
			authorBook.getAuthor().setDateOfBirth(dpDOB.getValue());
		    
		    //authorBook.setAuthor(author);
		    // convert the text field string to double, scale it so as to
		    // loose precision, then cast to int
		    int r = (int) (Double.parseDouble(tfRoyalty.getText()) * 100_000);
		    authorBook.setRoyalty(r);
		    //authorBook.setNewRecord(false);
		    
		    authorBook.save();
		} catch (Exception e) {
			logger.error("Changes cannot be saved: " + e.getMessage());
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Changes Not Saved");
			alert.setHeaderText(null);
			alert.setContentText(e.getMessage());
			alert.showAndWait();
			// changes unsuccessful
			return false;
		}
		// changes successful
		return true;
	}
	
	/**
	 * 
	 */
	public void updateBookAuditTrail() {
		logger.info("Inserting audit trail record(s) for " + authorBook.getBook().getTitle());
		int bookId = authorBook.getBook().getId();
		BookGateway bookGateway = authorBook.getBook().getBookGateway();
		String entryMessage = null;
		String gender = Character.toString(authorBook.getAuthor().getGender());
	    double royalty = (double) authorBook.getRoyalty() / 100_000;
	    double royaltyField = Double.parseDouble(tfRoyalty.getText());
		if(!(authorBook.getAuthor().getFirstName().contentEquals(tfFirstName.getText()))) {
			entryMessage = "Author's first name changed from " 
		                 + authorBook.getAuthor().getFirstName()
		                 + " to " + tfFirstName.getText();
			bookGateway.insertAuditTrailEntry(bookId, entryMessage);
		} else if (!(authorBook.getAuthor().getLastName().contentEquals(tfLastName.getText()))) {
			entryMessage = "Author's last name changed from " 
	                 + authorBook.getAuthor().getLastName()
	                 + " to " + tfLastName.getText();
		    bookGateway.insertAuditTrailEntry(bookId, entryMessage);
		} else if (!(authorBook.getAuthor().getDateOfBirth().isEqual(dpDOB.getValue()))) {
			entryMessage = "Author's DOB changed from " 
	                 + authorBook.getAuthor().getDateOfBirth()
	                 + " to " + dpDOB.getValue();
		    bookGateway.insertAuditTrailEntry(bookId, entryMessage);
		} else if (!(gender.equalsIgnoreCase(tfGender.getText()))) {
			entryMessage = "Author's gender changed from " 
	                 + authorBook.getAuthor().getGender()
	                 + " to " + tfGender.getText();
		    bookGateway.insertAuditTrailEntry(bookId, entryMessage);
		} else if (!(authorBook.getAuthor().getWebSite().contentEquals(tfWebsite.getText()))) {
			entryMessage = "Author's website changed from " 
	                 + authorBook.getAuthor().getWebSite()
	                 + " to " + tfWebsite.getText();
		    bookGateway.insertAuditTrailEntry(bookId, entryMessage);
		} else if (0 != Double.compare(royalty, royaltyField)) {
			entryMessage = "Author's royalty changed from " 
	                 + royalty
	                 + " to " + royaltyField;
		    bookGateway.insertAuditTrailEntry(bookId, entryMessage);
		}
	}
}