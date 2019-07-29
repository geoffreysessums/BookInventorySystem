package controllers;


import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
//import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
//import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import models.BookGhost;
import controllers.MasterViewController;
import controllers.ViewType;

public class BookListViewController implements Initializable {
	private static Logger logger = LogManager.getLogger();
	@FXML private TextField tfSearch;
	@FXML private Button buttonDelete;
	@FXML private Button buttonFirst;
	@FXML private Button buttonPrev;
	@FXML private Button buttonNext;
	@FXML private Button buttonLast;
	@FXML private Button buttonSearch;
	@FXML private Label labelRecordRange;
	@FXML private ListView<BookGhost> bookListView;
	private String bookTitle;
	private List<BookGhost> bookList;
	private BookGateway bookGateway;
	private int totalRecords;
	private int firstResult;
	private int lastRecord;
	private int pageSize;
	private int currentPage;
	private int totalPages;
	private boolean searchClicked;
	
	public BookListViewController(List<BookGhost> bookList) {
		this.bookGateway = MasterViewController.getInstance().getBookGateway();
		this.bookList = bookList;
		this.bookTitle = "";
		// initially count all book records
		this.totalRecords = bookGateway.getTotalRecordCount(); 
		this.firstResult = 0;
		this.lastRecord = this.totalRecords;
		this.pageSize = 50;
		this.currentPage = 0;
		this.totalPages = (int) Math.ceil(totalRecords / pageSize);
		this.searchClicked = false;
	}
	
	public void initialize(URL location, ResourceBundle resources) throws NullPointerException {
		ObservableList<BookGhost> bookObservableList;
	    bookObservableList = FXCollections.observableArrayList();
		labelRecordRange.setText("Fetched records " + (firstResult + 1) + " to " + pageSize + " out of " + totalRecords);
		bookObservableList.addAll(bookList);
		bookListView.setItems(bookObservableList);
			
		//add double-click handler to load detail view
		bookListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
		    @Override
		    public void handle(MouseEvent click) {
		        if(click.getClickCount() == 2) {
		            // Use ListView's getSelected Item
		            BookGhost bookItem = bookListView.getSelectionModel().getSelectedItem();
		            logger.info("double-clicked " + bookItem);
		            MasterViewController.getInstance().switchView(ViewType.BOOK_DETAIL_VIEW, bookItem);
		        }
		    }
		});
	}

	/**
	 * @param action
	 * @throws Exception
	 */
	@FXML
    void onMouseClicked(ActionEvent action) throws Exception {
		ObservableList<BookGhost> bookObservableList;
	    bookObservableList = FXCollections.observableArrayList();
    	Object button = action.getSource();
    	if (button == buttonDelete) {
    		// log action to console
			BookGhost book = bookListView.getSelectionModel().getSelectedItem();
    		//book.delete();
			if (book != null) {
				Alert deleteBookAlert = new Alert(AlertType.CONFIRMATION);
				deleteBookAlert.getButtonTypes().clear();
				ButtonType yesButton = new ButtonType("Yes");
				ButtonType noButton = new ButtonType("No");
			    deleteBookAlert.getButtonTypes().setAll(yesButton, noButton);	
				
				deleteBookAlert.setTitle("Delete Book?");
				deleteBookAlert.setHeaderText(null);
				deleteBookAlert.setContentText("This action cannot be undone. Delete book?");
				Optional<ButtonType> result = deleteBookAlert.showAndWait();
				
				if (result.get().getText().equalsIgnoreCase("Yes")) {
					logger.info("Removing book: " + book);
					MasterViewController.getInstance().getBookGateway().deleteBook(book);
				    bookObservableList = bookListView.getItems();
				    bookObservableList.remove(book);
				} else {
					logger.info("Delete Aborted.");
				}
			} else {
				logger.info("No book selected for deletion.");
			}
			//MasterViewController.getInstance().switchView(ViewType.BOOK_LIST_VIEW, null);
    	}
    	if (button == buttonFirst) {
    	    // if on first page, do nothing
    		if (currentPage != 0) {
    			firstResult = 0;
    		    currentPage = 0;	
				labelRecordRange.setText("Fetched records " + (firstResult + 1) + " to " + pageSize + " out of " + totalRecords);
				if (searchClicked) {
					bookObservableList.addAll(bookGateway.search(bookTitle, firstResult, pageSize));
				} else {
					bookObservableList.addAll(bookGateway.getBooks(firstResult, pageSize));
				}
				bookListView.setItems(bookObservableList);
    		}
    	}
    	if (button == buttonPrev) {
    	    // if on first page, do nothing
    		if (currentPage != 0) {
    			currentPage--;
				firstResult = currentPage * pageSize;
				labelRecordRange.setText("Fetched records " + (firstResult + 1) + " to " + (firstResult + pageSize)+ " out of " + totalRecords);
				if (searchClicked) {
					bookObservableList.addAll(bookGateway.search(bookTitle, firstResult, pageSize));
				} else {
					bookObservableList.addAll(bookGateway.getBooks(firstResult, pageSize));
				}
				bookListView.setItems(bookObservableList);
    		}
    	}
    	if (button == buttonNext) {
    		// if on last page, do nothing
    		if (currentPage != totalPages) {
    	    	currentPage++;
    	    	firstResult = currentPage * pageSize;
    	    	if (currentPage == totalPages) {
					int lastPageSize = lastRecord - firstResult;
					labelRecordRange.setText("Fetched records " + (firstResult + 1) + " to " + (firstResult + lastPageSize)+ " out of " + totalRecords);
					if (searchClicked) {
						bookObservableList.addAll(bookGateway.search(bookTitle, firstResult, pageSize));
					} else {
						bookObservableList.addAll(bookGateway.getBooks(firstResult, pageSize));
					}
    	    	} else {
					labelRecordRange.setText("Fetched records " + (firstResult + 1) + " to " + (firstResult + pageSize)+ " out of " + totalRecords);
					if (searchClicked) {
						bookObservableList.addAll(bookGateway.search(bookTitle, firstResult, pageSize));
					} else {
						bookObservableList.addAll(bookGateway.getBooks(firstResult, pageSize));
					}
    	    	}
				bookListView.setItems(bookObservableList);
    	    } 

    	}
    	if (button == buttonLast) {
    		// if on last page, do nothing
    		if (currentPage < totalRecords) {
    			logger.info("currentPage: " + currentPage);
    			logger.info("totalPages: " + totalPages);
				currentPage = totalPages;
				firstResult = currentPage * pageSize;
				int lastPageSize = lastRecord - firstResult;
				labelRecordRange.setText("Fetched records " + (firstResult + 1) + " to " + (firstResult + lastPageSize)+ " out of " + totalRecords);
				if (searchClicked) {
					bookObservableList.addAll(bookGateway.search(bookTitle, firstResult, lastPageSize));
				} else {
					bookObservableList.addAll(bookGateway.getBooks(firstResult, lastPageSize));
				}
				bookListView.setItems(bookObservableList);
    		}
    		//logger.info("Total pages: " + totalPages);
    	}
    	if (button == buttonSearch) {
    		//List<BookGhost> searchResults = null;
    		searchClicked = true;
    		bookTitle = tfSearch.getText().trim();
            if (bookTitle.isEmpty()) {
            	logger.info("Search field is empty");
            	searchClicked = false;
            	// Show alert box stating that search field is empty
            } else {
            	//searchResults = bookGateway.search(bookTitle);
            	int count = bookGateway.getRecordCountByTitle(bookTitle);
            	if (count != 0) {
					this.bookList = bookGateway.search(bookTitle, 0, 50);
            	} else {
            		// display found no matches
            	}
            	if (count < 50) {
            		setPageSize(count);
            	} else {
					setPageSize(50);
            	}
            	setFirstResult(0);
            	setTotalRecords(count);
            	setLastRecord(this.totalRecords);
            	setCurrentPage(0);
            	// compute the total number of pages as a function on total records
            	setTotalPages(this.totalRecords);
            	initialize(null, null);
            }
    	}
    }
    
	/**
	 * @param i
	 */
    private void setTotalRecords(int i) {
	    this.totalRecords = i;	
	}

	/**
	 * @param i 
     */
    public void setFirstResult(int i) {
        this.firstResult = i;
    }
    
    /**
     * 
     */
    public int getFirstResult() {
        return this.getFirstResult();	
    }
    
    /**
     * 
     */
    public void setLastRecord(int i) {
    	this.lastRecord = i;
    }
    
    /**
     *  
     */
    public int getLastRecord() {
        return this.lastRecord;	
    }
    
    /**
     * @param i 
     *  
     */
    public void setPageSize(int i) {
        this.pageSize = i;  	
    }
    
    /**
     * 
     */
    public int getPageSize() {
        return this.pageSize;	
    }
    
    /**
     * @param i 
     * 
     */
    public void setCurrentPage(int i) {
        this.currentPage = i;  	
    }
    
    /**
     * 
     */
    public int getCurrentPage() {
        return this.currentPage;	
    }
    
    /**
     * 
     */
    public void setTotalPages(int i) {
    	this.totalPages = (int) Math.ceil(i / pageSize);
    }
    
    /**
     * 
     */
    public int getTotalPages() {
        return this.totalPages;	
    }
    
    /**
     * 
     */
    public BookGateway getBookGateway() {
        return MasterViewController.getInstance().getBookGateway();	
    }
}