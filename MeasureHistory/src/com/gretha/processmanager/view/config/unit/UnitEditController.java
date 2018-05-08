package com.gretha.processmanager.view.config.unit;

import com.gretha.shared.model.Unit;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UnitEditController {

	@FXML
	private TextField valueSign;

	private Stage dialogStage;
	private Unit unit;
	private boolean okClicked = false;

	@FXML
	private void initialize() {
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	public void setData(Unit unit) {
		this.unit = unit;

		valueSign.setText(unit.getSign());

	}

	public boolean isOkClicked() {
		return okClicked;
	}

	@FXML
	private void handleOk() {
		if (isInputValid()) {
			unit.setSign(valueSign.getText());
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
		valueSign.setId("textfield-input-default");

		if (valueSign.getText() == null || valueSign.getText().length() == 0 || valueSign.getText().length() > 10) {
			valueSign.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Zeichen) !";
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