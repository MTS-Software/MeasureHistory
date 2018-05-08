package com.gretha.processmanager.view.config.plc;

import com.gretha.shared.model.Plc;
import com.gretha.shared.util.Constants;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PlcEditController {

	@FXML
	private TextField valueName;
	@FXML
	private TextField valueIp;
	@FXML
	private TextField valueRack;
	@FXML
	private TextField valueSlot;
	@FXML
	private ComboBox<String> cbType;
	@FXML
	private TextField valueTimeout;

	private Stage dialogStage;
	private Plc plc;
	private boolean okClicked = false;

	@FXML
	private void initialize() {

		cbType.setItems(Constants.PLC_TYPES);

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	public void setData(Plc plc) {
		this.plc = plc;

		valueName.setText(plc.getName());
		valueIp.setText(plc.getIp());
		valueRack.setText(String.valueOf(plc.getRack()));
		valueSlot.setText(String.valueOf(plc.getSlot()));
		cbType.getSelectionModel().select(plc.getType());
		valueTimeout.setText(String.valueOf(plc.getTimeout()));

	}

	public boolean isOkClicked() {
		return okClicked;
	}

	@FXML
	private void handleOk() {
		if (isInputValid()) {

			plc.setName(valueName.getText());
			plc.setIp(valueIp.getText());
			plc.setRack(Integer.valueOf(valueRack.getText()));
			plc.setSlot(Integer.valueOf(valueSlot.getText()));
			plc.setType(cbType.getSelectionModel().getSelectedIndex());
			plc.setTimeout(Integer.valueOf(valueTimeout.getText()));

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
		valueName.setId("textfield-input-default");
		valueIp.setId("textfield-input-default");
		valueRack.setId("textfield-input-default");
		valueSlot.setId("textfield-input-default");
		valueTimeout.setId("textfield-input-default");

		String ipAdressPattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
				+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

		if (valueName.getText() == null || valueName.getText().length() == 0 || valueName.getText().length() > 45) {
			valueName.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Name) !\n";
		}

		if (valueIp.getText() == null || !valueIp.getText().matches(ipAdressPattern)
				|| valueIp.getText().length() > 45) {
			valueIp.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (IP-Adresse) !\n";
		}

		if (valueRack.getText() == null || valueRack.getText().length() == 0
				|| !valueRack.getText().matches("[0-9]*")) {
			valueRack.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Rack) !\n";
		}

		if (valueSlot.getText() == null || valueSlot.getText().length() == 0
				|| !valueSlot.getText().matches("[0-9]*")) {
			valueSlot.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Slot) !\n";
		}

		if (valueTimeout.getText() == null || !valueTimeout.getText().matches("[0-9]*")
				|| valueTimeout.getText().length() < 4 || valueTimeout.getText().length() > 5) {
			System.out.println(valueTimeout.getText().length());
			valueTimeout.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Timeout) !\n";
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