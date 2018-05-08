package com.gretha.processmanager.view.root;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.shared.db.util.EDatabase;
import com.gretha.shared.util.ApplicationProperties;
import com.gretha.shared.util.Constants;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 
 * 
 * @author Markus Thaler
 */
public class SettingsController implements Initializable {

	private static final Logger logger = Logger.getLogger(SettingsController.class);
	@FXML
	private TextField hostField;
	@FXML
	private ComboBox<EDatabase> dbVendorCb;
	@FXML
	private TextField timeoutField;
	@FXML
	private TextField userField;
	@FXML
	private TextField passwordField;
	@FXML
	private TextField portField;
	@FXML
	private TextField instanceField;
	@FXML
	private ComboBox<EStartWindow> startScreenCb;
	@FXML
	private ComboBox<EOption> minimizeOnCloseCb;
	@FXML
	private ComboBox<EOption> demoModeCb;

	private Stage dialogStage;

	private boolean okClicked = false;
	private ResourceBundle resources;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resources = resources;

		dbVendorCb.setItems(FXCollections.observableArrayList(EDatabase.values()));
		EventHandler<ActionEvent> dbVendorCbActionEvent = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (dbVendorCb.getSelectionModel().getSelectedItem() == EDatabase.MYSQL) {
					setDatabase(EDatabase.MYSQL.getVendor());
				}

				if (dbVendorCb.getSelectionModel().getSelectedItem() == EDatabase.SQLSERVER) {
					setDatabase(EDatabase.SQLSERVER.getVendor());
				}

			}

		};
		dbVendorCb.setOnAction(dbVendorCbActionEvent);

		startScreenCb.setItems(FXCollections.observableArrayList(EStartWindow.values()));
		minimizeOnCloseCb.setItems(FXCollections.observableArrayList(EOption.values()));
		demoModeCb.setItems(FXCollections.observableArrayList(EOption.values()));

	}

	/**
	 * Sets the stage of this dialog.
	 * 
	 * @param dialogStage
	 */
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
		setData();
	}

	private void setDatabase(String dbVendor) {

		if (dbVendor.equalsIgnoreCase(EDatabase.MYSQL.getVendor())) {
			dbVendorCb.getSelectionModel().select(EDatabase.MYSQL);
			userField.setText(ApplicationProperties.getInstance().getProperty("db_mysql_user"));
			passwordField.setText(ApplicationProperties.getInstance().getProperty("db_mysql_password"));
			portField.setText(ApplicationProperties.getInstance().getProperty("db_mysql_port"));
			portField.setDisable(false);
			instanceField.setDisable(true);
			timeoutField.setDisable(false);

		}

		if (dbVendor.equalsIgnoreCase(EDatabase.SQLSERVER.getVendor())) {
			dbVendorCb.getSelectionModel().select(EDatabase.SQLSERVER);
			userField.setText(ApplicationProperties.getInstance().getProperty("db_sqlserver_user"));
			passwordField.setText(ApplicationProperties.getInstance().getProperty("db_sqlserver_password"));
			portField.setText(ApplicationProperties.getInstance().getProperty("db_sqlserver_port"));
			portField.setDisable(true);
			instanceField.setText(ApplicationProperties.getInstance().getProperty("db_sqlserver_instance"));
			instanceField.setDisable(false);
			timeoutField.setDisable(true);
		}

	}

	public void setData() {

		// Datenbank
		hostField.setText(ApplicationProperties.getInstance().getProperty("db_host"));
		timeoutField.setText(ApplicationProperties.getInstance().getProperty("db_timeout"));
		setDatabase(ApplicationProperties.getInstance().getProperty("db_vendor"));

		// Application
		if (ApplicationProperties.getInstance().getProperty("start_window").equalsIgnoreCase(EStartWindow.DEFAULT.nr)) {
			startScreenCb.getSelectionModel().select(EStartWindow.DEFAULT);

		} else if (ApplicationProperties.getInstance().getProperty("start_window")
				.equalsIgnoreCase(EStartWindow.MINIMIZED.nr)) {
			startScreenCb.getSelectionModel().select(EStartWindow.MINIMIZED);

		} else if (ApplicationProperties.getInstance().getProperty("start_window")
				.equalsIgnoreCase(EStartWindow.MAXIMIZED.nr)) {
			startScreenCb.getSelectionModel().select(EStartWindow.MAXIMIZED);
		}

		if (ApplicationProperties.getInstance().getProperty("minimize_on_close").equalsIgnoreCase(EOption.JA.label)) {
			minimizeOnCloseCb.getSelectionModel().select(EOption.JA);
		} else
			minimizeOnCloseCb.getSelectionModel().select(EOption.NEIN);

		if (ApplicationProperties.getInstance().getProperty("demomode").equalsIgnoreCase(EOption.JA.label)) {
			demoModeCb.getSelectionModel().select(EOption.JA);
		} else
			demoModeCb.getSelectionModel().select(EOption.NEIN);
	}

	/**
	 * Returns true if the user clicked OK, false otherwise.
	 * 
	 * @return
	 */
	public boolean isOkClicked() {
		return okClicked;
	}

	/**
	 * Called when the user clicks ok.
	 */
	@FXML
	private void handleOk() {

		if (isInputValid()) {

			// Datenbank
			ApplicationProperties.getInstance().edit("db_host", hostField.getText());
			ApplicationProperties.getInstance().edit("db_timeout", timeoutField.getText());

			if (dbVendorCb.getSelectionModel().getSelectedItem() == EDatabase.MYSQL) {
				ApplicationProperties.getInstance().edit("db_vendor", EDatabase.MYSQL.getVendor());
				ApplicationProperties.getInstance().edit("db_mysql_user", userField.getText());
				ApplicationProperties.getInstance().edit("db_mysql_password", passwordField.getText());
				ApplicationProperties.getInstance().edit("db_mysql_port", portField.getText());

			}

			if (dbVendorCb.getSelectionModel().getSelectedItem() == EDatabase.SQLSERVER) {
				ApplicationProperties.getInstance().edit("db_vendor", EDatabase.SQLSERVER.getVendor());
				ApplicationProperties.getInstance().edit("db_sqlserver_user", userField.getText());
				ApplicationProperties.getInstance().edit("db_sqlserver_password", passwordField.getText());
				ApplicationProperties.getInstance().edit("db_sqlserver_port", portField.getText());
				ApplicationProperties.getInstance().edit("db_sqlserver_instance", instanceField.getText());
			}

			// Application
			if (startScreenCb.getSelectionModel().getSelectedItem() == EStartWindow.DEFAULT)
				ApplicationProperties.getInstance().edit("start_window", EStartWindow.DEFAULT.nr);
			else if (startScreenCb.getSelectionModel().getSelectedItem() == EStartWindow.MINIMIZED)
				ApplicationProperties.getInstance().edit("start_window", EStartWindow.MINIMIZED.nr);
			else if (startScreenCb.getSelectionModel().getSelectedItem() == EStartWindow.MAXIMIZED)
				ApplicationProperties.getInstance().edit("start_window", EStartWindow.MAXIMIZED.nr);

			if (minimizeOnCloseCb.getSelectionModel().getSelectedItem() == EOption.JA)
				ApplicationProperties.getInstance().edit("minimize_on_close", EOption.JA.label);
			else
				ApplicationProperties.getInstance().edit("minimize_on_close", EOption.NEIN.label);

			if (demoModeCb.getSelectionModel().getSelectedItem() == EOption.JA)
				ApplicationProperties.getInstance().edit("demomode", EOption.JA.label);
			else
				ApplicationProperties.getInstance().edit("demomode", EOption.NEIN.label);

			ApplicationProperties.getInstance().save();

			okClicked = true;

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Hinweis");
			alert.setHeaderText("Änderungen werden erst nach Neustart vorgenommen");
			alert.setContentText("Wenn Sie Änderungen vorgenommen haben, starten Sie das Programm bitte neu.");
			DialogPane dialogPane = alert.getDialogPane();
			dialogPane.getStylesheets().addAll(getClass().getResource(Constants.STYLESHEET).toExternalForm());
			Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			stage.getIcons().add(new Image(Constants.ICON_ABOUT));
			alert.showAndWait();
			dialogStage.close();
		}

	}

	@FXML
	private void handleCancel() {
		dialogStage.close();

	}

	/**
	 * Validates the user input in the text fields.
	 * 
	 * @return true if the input is valid
	 */
	private boolean isInputValid() {
		String errorMessage = "";

		if (portField.getText() == null || portField.getText().length() == 0) {
			errorMessage += "Ungültige Eingabe der Portnummer!\n";
		}

		if (timeoutField.getText() == null || timeoutField.getText().length() == 0) {
			errorMessage += "Ungültige Eingabe des Timeout!\n";
		}

		if (errorMessage.length() == 0) {
			return true;
		} else {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Fehlerhafte Eingabe!");
			alert.setContentText(errorMessage);
			DialogPane dialogPane = alert.getDialogPane();
			dialogPane.getStylesheets().addAll(getClass().getResource(Constants.STYLESHEET).toExternalForm());
			alert.show();
			return false;
		}
	}

	public enum EOption {

		NEIN("false", "Nein"), JA("true", "Ja");

		private String label;
		private String name;

		EOption(String label, String name) {
			this.label = label;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	public enum EStartWindow {

		DEFAULT("default", "0"), MINIMIZED("minimiert", "1"), MAXIMIZED("maximiert", "2");

		private String label;
		private String nr;

		EStartWindow(String label, String nr) {
			this.label = label;
			this.nr = nr;
		}

		public String getNr() {
			return nr;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}