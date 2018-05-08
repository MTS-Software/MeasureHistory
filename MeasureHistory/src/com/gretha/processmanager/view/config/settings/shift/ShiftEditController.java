package com.gretha.processmanager.view.config.settings.shift;

import com.gretha.shared.model.Settings;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ShiftEditController {

	@FXML
	private TextField valueShift1StartTime;
	@FXML
	private TextField valueShift1EndTime;
	@FXML
	private TextField valueShift2StartTime;
	@FXML
	private TextField valueShift2EndTime;
	@FXML
	private TextField valueShift3StartTime;
	@FXML
	private TextField valueShift3EndTime;

	private Stage dialogStage;
	private Settings settings;
	private boolean okClicked = false;

	@FXML
	private void initialize() {

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	public void setData(Settings settings) {
		this.settings = settings;
		valueShift1StartTime.setText(settings.getShift1StartTime());
		valueShift1EndTime.setText(settings.getShift1EndTime());
		valueShift2StartTime.setText(settings.getShift2StartTime());
		valueShift2EndTime.setText(settings.getShift2EndTime());
		valueShift3StartTime.setText(settings.getShift3StartTime());
		valueShift3EndTime.setText(settings.getShift3EndTime());

	}

	public boolean isOkClicked() {
		return okClicked;
	}

	@FXML
	private void handleOk() {
		if (isInputValid()) {
			settings.setShift1StartTime(valueShift1StartTime.getText());
			settings.setShift1EndTime(valueShift1EndTime.getText());
			settings.setShift2StartTime(valueShift2StartTime.getText());
			settings.setShift2EndTime(valueShift2EndTime.getText());
			settings.setShift3StartTime(valueShift3StartTime.getText());
			settings.setShift3EndTime(valueShift3EndTime.getText());

			okClicked = true;
			dialogStage.close();

		}
	}

	@FXML
	private void handleCancel() {
		dialogStage.close();
	}

	private boolean isInputValid() {
		String errorMessage = "";
		valueShift1StartTime.setId("textfield-input-default");
		valueShift1EndTime.setId("textfield-input-default");
		valueShift2StartTime.setId("textfield-input-default");
		valueShift2EndTime.setId("textfield-input-default");
		valueShift3StartTime.setId("textfield-input-default");
		valueShift3EndTime.setId("textfield-input-default");

		// Only Matching if Timeformat hh:mm:ss
		String timePattern = "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]$";

		if (valueShift1StartTime.getText() == null || !valueShift1StartTime.getText().matches(timePattern)) {
			valueShift1StartTime.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Schicht 1 Startzeit) !\n";
		}

		if (valueShift1EndTime.getText() == null || !valueShift1EndTime.getText().matches(timePattern)) {
			valueShift1EndTime.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Schicht 1 Endzeit) !\n";
		}

		if (valueShift2StartTime.getText() == null || !valueShift2StartTime.getText().matches(timePattern)) {
			valueShift2StartTime.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Schicht 2 Startzeit) !\n";
		}

		if (valueShift2EndTime.getText() == null || !valueShift2EndTime.getText().matches(timePattern)) {
			valueShift2EndTime.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Schicht 2 Endzeit) !\n";
		}

		if (valueShift3StartTime.getText() == null || !valueShift3StartTime.getText().matches(timePattern)) {
			valueShift3StartTime.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Schicht 3 Startzeit) !\n";
		}

		if (valueShift3EndTime.getText() == null || !valueShift3EndTime.getText().matches(timePattern)) {
			valueShift3StartTime.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Schicht 3 Endzeit) !\n";
		}

		if (errorMessage.length() == 0) {
			return true;
		} else {

			Alert alert = new Alert(AlertType.ERROR);
			alert.initOwner(dialogStage);
			alert.setTitle("Falsche Eingabe");
			alert.setHeaderText("Bitte die Eingabe korrigieren.");
			alert.setContentText(errorMessage);

			alert.showAndWait();

			return false;
		}
	}
}