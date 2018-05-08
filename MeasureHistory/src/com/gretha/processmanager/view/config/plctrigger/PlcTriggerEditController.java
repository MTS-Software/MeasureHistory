package com.gretha.processmanager.view.config.plctrigger;

import com.gretha.processmanager.Main;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Plc;
import com.gretha.shared.model.PlcTrigger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class PlcTriggerEditController {

	@FXML
	private TextField valueDb;
	@FXML
	private TextField valueStrtAdr;
	@FXML
	private CheckBox cbActivated;
	@FXML
	private ComboBox<Plc> plcComboBox;
	@FXML
	private Button plcOverviewButton;

	private Stage dialogStage;
	private PlcTrigger plctrigger;
	private Main main;
	private boolean okClicked = false;

	@FXML
	private void initialize() {

		plcOverviewButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				main.showPlcOverviewDialog();
				setPlcComboBox();

			}
		});
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	public void setMain(Main main) {
		this.main = main;
	}

	public void setData(PlcTrigger plctrigger) {
		this.plctrigger = plctrigger;

		valueDb.setText(String.valueOf(plctrigger.getDb()));
		valueStrtAdr.setText(String.valueOf(plctrigger.getStrtAdr()));
		cbActivated.setSelected(plctrigger.isActivated());

		setPlcComboBox();

	}

	private void setPlcComboBox() {

		ObservableList<Plc> plcs = FXCollections.observableArrayList(Service.getInstance().getPlcs());
		plcComboBox.setItems(plcs);
		plcComboBox.setConverter(new StringConverter<Plc>() {

			@Override
			public Plc fromString(String string) {
				return null;
			}

			@Override
			public String toString(Plc object) {
				return object.getName();
			}
		});

		for (Plc plc : plcs) {
			if (plctrigger != null)
				if (plc.getId() == plctrigger.getPlcId()) {
					plcComboBox.getSelectionModel().select(plc);
					break;
				}
		}

	}

	public boolean isOkClicked() {
		return okClicked;
	}

	@FXML
	private void handleOk() {
		if (isInputValid()) {

			plctrigger.setDb(Integer.valueOf(valueDb.getText()));
			plctrigger.setStrtAdr(Integer.valueOf(valueStrtAdr.getText()));
			plctrigger.setActivated(cbActivated.isSelected());

			if (plcComboBox.getSelectionModel().getSelectedItem() != null) {
				plctrigger.setPlcId(plcComboBox.getSelectionModel().getSelectedItem().getId());
				plctrigger.setPlc(plcComboBox.getSelectionModel().getSelectedItem());
			}

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
		valueDb.setId("textfield-input-default");
		valueStrtAdr.setId("textfield-input-default");

		if (valueDb.getText() == null || valueDb.getText().length() == 0 || valueDb.getText().contentEquals("0")
				|| !valueDb.getText().matches("[0-9]*")) {
			valueDb.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (DB Nummer) !\n";
		}

		if (valueStrtAdr.getText() == null || valueStrtAdr.getText().length() == 0
				|| !valueStrtAdr.getText().matches("[0-9]*")) {
			valueStrtAdr.setId("textfield-input-invalid");
			errorMessage += "Ungültige Eingabe (Startadresse) !\n";
		}

		if (plcComboBox.getSelectionModel().getSelectedItem() == null) {
			errorMessage += "Ungültige Auswahl (Steuerung) !\n";
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