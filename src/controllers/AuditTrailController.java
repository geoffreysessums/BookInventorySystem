package controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import models.AuditTrailEntry;
import models.Book;

public class AuditTrailController implements Initializable {
	private static Logger logger = LogManager.getLogger();
	//private AuditTrailEntry entry = null;
	private List<AuditTrailEntry> auditTrailList;
	@FXML private Button buttonBack;
	@FXML private Label auditTrailLabel;
	@FXML private ListView<AuditTrailEntry> auditTrailListView;
	private Book book = null;

	
	public AuditTrailController(Book book, List<AuditTrailEntry> auditTrailList) {
		this.book = book;
		this.auditTrailList = auditTrailList;
	}
	
	public void initialize(URL location, ResourceBundle resources) throws NullPointerException {
		logger.info("Initializing the audit trail entry list");
		auditTrailLabel.setText("Audit Trail for " + book.getTitle());
	    ObservableList<AuditTrailEntry> items = FXCollections.observableArrayList();
		items.addAll(auditTrailList);
		auditTrailListView.setItems(items);
	}
		
	@FXML
    void onMouseClicked(ActionEvent action) throws Exception {
    	Object button = action.getSource();
    	if (button == buttonBack) {
    		// log action to console
        	logger.info("clicked Back");
        	// switch back to the book detail view
			MasterViewController.getInstance().switchView(ViewType.BOOK_DETAIL_VIEW, book);
    	}
    }
}
