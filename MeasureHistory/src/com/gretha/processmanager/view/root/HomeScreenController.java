package com.gretha.processmanager.view.root;

import java.net.URL;
import java.util.ResourceBundle;

import com.gretha.processmanager.Main;

import javafx.fxml.Initializable;
import javafx.stage.Stage;

/**
 * 
 * @author Markus Thaler
 */
public class HomeScreenController implements Initializable {

	private ResourceBundle resources;

	private Main main;
	private Stage dialogStage;

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public HomeScreenController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param main
	 */
	public void setMain(Main main) {
		this.main = main;

	}

}