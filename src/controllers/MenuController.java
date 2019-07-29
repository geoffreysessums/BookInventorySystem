package controllers;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.text.Text;
import javafx.util.Pair;
import models.Book;
import models.CryptoStuff;

public class MenuController implements Initializable {
	private static Logger logger = LogManager.getLogger();
	
	@FXML private MenuItem quitMenuItem;
	@FXML private MenuItem loginMenuItem;
	@FXML private MenuItem logoutMenuItem;
	@FXML private MenuItem bookListMenuItem;
	@FXML private MenuItem addBookMenuItem;
	@FXML private Text textLogin, textSessionId;
	@FXML private MenuBar menuBar;

    //MenuController is the app-level controller for this program
	//so it will have an authenticator to use for logging in
	private Authenticator authenticator;
	
	//user's local session id
	//session id should not normally be an int (too easy to guess other session ids) 
	int sessionId;
	public MenuController() {
		//create an authenticator
		authenticator = new AuthenticatorLocal();

		//default to no session
		sessionId = Authenticator.INVALID_SESSION;	
	}
	
    @FXML
    void clickMenuItem(ActionEvent event) {
    	if(event.getSource() == quitMenuItem) {
    		logger.info("Menu item selected: Quit");
    		BookDetailViewController activeController;
    		activeController = MasterViewController.getInstance().getActiveController();
    		// Checks for an active book detail view
    		// If there is an active book detail view, then call switch view to
    		// check for unsaved changes.
    		if (activeController != null) {
    			// Aborts close if switch view returns error saving changes or
    			// user selects cancel
        		if (!MasterViewController.getInstance().switchView(
    					ViewType.BOOK_DETAIL_VIEW, activeController.getBook())) {
    			return;
    		    }
    		}
    		// close gateways
    		MasterViewController.getInstance().close();
    		Platform.exit();
    	}
		if(event.getSource() == loginMenuItem) {
			logger.info("Menu item selected: Login");
            login();
		}
		if(event.getSource() == logoutMenuItem) {
			logger.info("Menu item selected: Logout");
            logout();
		}
		if(event.getSource() == bookListMenuItem) {
			//get a collection of books from the gateway
			logger.info("Menu item selected: Book List");
			MasterViewController.getInstance().switchView(ViewType.BOOK_LIST_VIEW, null);
			return;
		}
		if(event.getSource() == addBookMenuItem) {
			Book book = new Book();
			logger.info("Menu item selected: Add Book");
			book.setBookGateway(MasterViewController.getInstance().getBookGateway());
			MasterViewController.getInstance().switchView(ViewType.BOOK_DETAIL_VIEW, book);
			return;
		}
    }
	
	private void logout() {
		sessionId = Authenticator.INVALID_SESSION;
		//restrict access to GUI controls based on current login session
		updateGUIAccess();
	}
	
	private void login() {
		//display login modal dialog. get login (username) and password
		//key is login, value is pw
		Pair<String, String> creds = LoginDialog.showLoginDialog();
		if(creds == null) //canceled
			return;
		
		String userName = creds.getKey();
		String pw = creds.getValue();
		
		logger.info("userName is " + userName + ", password is " + pw);
		
		//hash password
		String pwHash = CryptoStuff.sha256(pw);
		
		logger.info("sha256 hash of password is " + pwHash);
		
		//send login and hashed pw to authenticator
		try {
			//if get session id back, then replace current session
			sessionId = authenticator.loginSha256(userName, pwHash);
			
			logger.info("session id is " + sessionId);
			
		} catch (Exception e) {
			//else display login failure
			Alert alert = new Alert(AlertType.WARNING);
			alert.getButtonTypes().clear();
			ButtonType buttonTypeOne = new ButtonType("OK");
			alert.getButtonTypes().setAll(buttonTypeOne);
			alert.setTitle("Login Failed");
			alert.setHeaderText("The user name and password you provided do not match stored credentials.");
			alert.showAndWait();

			return;
		}
		//restrict access to GUI controls based on current login session
		updateGUIAccess();
	}
	
	private void updateGUIAccess() {
		//if logged in, login should be disabled
		if(sessionId == Authenticator.INVALID_SESSION)
			loginMenuItem.setDisable(false);
		else
			loginMenuItem.setDisable(true);
		
		//if not logged in, logout should be disabled
		if(sessionId == Authenticator.INVALID_SESSION)
			logoutMenuItem.setDisable(true);
		else
			logoutMenuItem.setDisable(false);
		
		//update fxml labels
		textLogin.setText(authenticator.getUserNameFromSessionId(sessionId));
		textSessionId.setText("Session id " + sessionId);
		
		//update menu info based on current user's access privileges
		if(authenticator.hasAccess(sessionId, RBACPolicyAuth.CAN_ACCESS_CHOICE_1))
			bookListMenuItem.setDisable(false);
		else 
			bookListMenuItem.setDisable(true);
		if(authenticator.hasAccess(sessionId, RBACPolicyAuth.CAN_ACCESS_CHOICE_2))
			addBookMenuItem.setDisable(false);
		else 
			addBookMenuItem.setDisable(true);
		/*
		if(authenticator.hasAccess(sessionId, RBACPolicyAuth.CAN_ACCESS_CHOICE_3))
			choice3.setDisable(false);
		else 
			choice3.setDisable(true);
			*/
	}
    
	/*
	 * create event handlers and load data from models into fields(non-Javadoc)
	 * @see javafx.fxml.Initializable#initialize(java.net.URL, java.util.ResourceBundle)
	 */
	public void initialize(URL location, ResourceBundle resources) {
		logger.info("Initializing menu controller");
		menuBar.setFocusTraversable(true);
		//restrict access to the GUI based on current session id (should be invalid session)
		updateGUIAccess();
	}
}

