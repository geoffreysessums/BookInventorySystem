package application;
	
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controllers.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// CS 4743 Assignment 5 by Geoffrey Sessums
public class Launcher extends Application {
	private static Logger logger = LogManager.getLogger(Launcher.class);
	
	@Override
	public void init() throws Exception {
		super.init();
				
		//create car gateway
		logger.info("Calling init");
		MasterViewController.getInstance();
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		logger.info("Initializing MasterViewController");
		URL url = this.getClass().getResource("../views/MainView.fxml");
		FXMLLoader loader = new FXMLLoader(url);
		
		MenuController menuController = new MenuController();
		loader.setController(menuController);

		Parent rootViewNode = loader.load();
		MasterViewController.getInstance().setBorderPane((BorderPane) rootViewNode);
		
		stage.setScene(new Scene(rootViewNode));
		
		stage.setTitle("BookInventorySystem");
		stage.setWidth(600);
		stage.setHeight(500);
		stage.show();

	}
	
	@Override
	public void stop() throws Exception {
		super.stop();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}