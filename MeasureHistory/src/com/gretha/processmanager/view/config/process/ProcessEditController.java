package com.gretha.processmanager.view.config.process;

import com.gretha.processmanager.Main;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Plc;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Unit;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.textvalidator.TextInputValidatorPane;
import com.gretha.shared.util.textvalidator.ValidationResult;

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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ProcessEditController {

	@FXML
	private ComboBox<Integer> cbDecpoints;
	@FXML
	private TextField valueStation;
	@FXML
	private TextField valueBezeichnung;
	@FXML
	private TextField valueSetvalue;
	@FXML
	private TextField valueAnzahlAvg;
	@FXML
	private TextField valueAnzahlSpcKlassen;
	@FXML
	private TextField valueCpkLoLim1;
	@FXML
	private TextField valueCpkLoLim1Vis;
	@FXML
	private TextField valueCpkLoLim2;
	@FXML
	private Button btnOK;
	@FXML
	private Label lblUnit;
	@FXML
	private StackPane spSetvalue;
	@FXML
	private StackPane spAnzahlAvg;
	@FXML
	private StackPane spCpkLoLim1;
	@FXML
	private StackPane spCpkLoLim2;
	@FXML
	private StackPane spAnzahlSpcKlassen;
	@FXML
	private CheckBox cbSetvalueUsed;
	@FXML
	private ComboBox<Unit> unitComboBox;
	@FXML
	private ComboBox<Plc> plcComboBox;
	@FXML
	private ComboBox<PlcTrigger> triggerComboBox;
	@FXML
	private Button unitOverviewButton;
	@FXML
	private Button plcOverviewButton;
	@FXML
	private Button triggerOverviewButton;

	private Stage dialogStage;
	private Main main;
	private Process process;
	private boolean okClicked = false;

	@FXML
	private void initialize() {

		unitComboBox.setDisable(false);
		unitOverviewButton.setDisable(false);

		// PLC Auswahl ist nur bei PLC Trigger möglich --> Gefahr von Fehlauswahl
		plcComboBox.setDisable(true);

		plcOverviewButton.setDisable(false);

		triggerComboBox.setDisable(false);
		triggerOverviewButton.setDisable(false);

		btnOK.setDisable(true);

		// Fix Disablen da dies nur zum Visualisieren benötigt wird
		valueCpkLoLim1Vis.setDisable(true);

		unitComboBox.setOnAction((event) -> {
			btnOK.setDisable(false);
		});

		plcComboBox.setOnAction((event) -> {
			// btnOK.setDisable(false);
		});

		triggerComboBox.setOnAction((event) -> {
			refreshPlcComboBox();
			btnOK.setDisable(false);
		});

		unitOverviewButton.setOnAction((event) -> {
			btnOK.setDisable(false);
		});

		plcOverviewButton.setOnAction((event) -> {
			btnOK.setDisable(false);
		});

		triggerOverviewButton.setOnAction((event) -> {
			btnOK.setDisable(false);
		});

		cbDecpoints.setOnAction((event) -> {
			btnOK.setDisable(false);
		});

		valueStation.setOnKeyPressed((event) -> {
			btnOK.setDisable(false);
		});

		valueBezeichnung.setOnKeyPressed((event) -> {
			btnOK.setDisable(false);
		});

		cbSetvalueUsed.setOnAction((event) -> {
			if (cbSetvalueUsed.isSelected())
				valueSetvalue.setDisable(false);
			else
				valueSetvalue.setDisable(true);

			btnOK.setDisable(false);
		});

		initValidator();

		unitOverviewButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				main.showUnitOverviewDialog();
				setUnitComboBox();
			}
		});

		plcOverviewButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				main.showPlcOverviewDialog();
				setPlcComboBox();
				setPlcTriggerComboBox();

			}
		});

		triggerOverviewButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				main.showPlcTriggerOverviewDialog();
				setPlcComboBox();
				setPlcTriggerComboBox();

			}
		});

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	public void setMain(Main main) {
		this.main = main;
	}

	public void setData(Process process) {
		this.process = process;
		getActualSettings();

	}

	private void getActualSettings() {

		if (process != null) {

			// Auswahlfeld Dezimalstellen/Kommastellen initialisieren
			ObservableList<Integer> decPoints = FXCollections.observableArrayList(0, 1, 2, 3, 4);
			cbDecpoints.setItems(decPoints);
			// Einstellungen auslesen
			cbDecpoints.getSelectionModel().select(process.getDecimalPoints());
			valueStation.setText(process.getStation());
			valueBezeichnung.setText(process.getName());
			cbSetvalueUsed.setSelected(process.isSetvalueUsed());
			valueSetvalue.setText(
					DecimalPointFormatter.roundFloat2String(process.getSetvalue(), process.getDecimalPoints()));
			lblUnit.setText("");
			valueAnzahlAvg.setText(String.valueOf(process.getNrAvg()));
			valueAnzahlSpcKlassen.setText(String.valueOf(process.getNrSpcClass()));
			valueCpkLoLim1.setText(DecimalPointFormatter.roundFloat2String(process.getCpkLoLim1(), 2));
			valueCpkLoLim1Vis.setText(valueCpkLoLim1.getText());
			valueCpkLoLim2.setText(DecimalPointFormatter.roundFloat2String(process.getCpkLoLim2(), 2));

			if (cbSetvalueUsed.isSelected())
				valueSetvalue.setDisable(false);
			else
				valueSetvalue.setDisable(true);

			setUnitComboBox();
			setPlcComboBox();
			setPlcTriggerComboBox();
		}
	}

	private void setUnitComboBox() {

		ObservableList<Unit> units = FXCollections.observableArrayList(Service.getInstance().getUnits());
		unitComboBox.setItems(units);

		unitComboBox.setConverter(new StringConverter<Unit>() {

			@Override
			public Unit fromString(String string) {
				return null;
			}

			@Override
			public String toString(Unit object) {
				return object.getSign();
			}
		});

		for (Unit unit : units) {
			if (unit.getId() == process.getUnitId()) {
				unitComboBox.getSelectionModel().select(unit);
				break;
			}
		}

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
			if (process.getPlcTrigger() != null)
				if (plc.getId() == process.getPlcTrigger().getPlcId()) {
					plcComboBox.getSelectionModel().select(plc);
					break;
				}
		}

	}

	private void refreshPlcComboBox() {

		PlcTrigger selectedItem = triggerComboBox.getSelectionModel().getSelectedItem();

		if (selectedItem != null)
			plcComboBox.getSelectionModel().select(selectedItem.getPlc());
		else
			plcComboBox.getSelectionModel().select(null);

	}

	private void setPlcTriggerComboBox() {

		ObservableList<PlcTrigger> plcTriggersAll = FXCollections
				.observableArrayList(Service.getInstance().getPlcTriggers());

		ObservableList<PlcTrigger> plcTriggersUnused = FXCollections.observableArrayList();

		for (PlcTrigger plcTrigger : plcTriggersAll) {
			if (plcTrigger.getProcess() == null)
				plcTriggersUnused.add(plcTrigger);
		}

		triggerComboBox.setItems(plcTriggersUnused);
		triggerComboBox.setConverter(new StringConverter<PlcTrigger>() {

			@Override
			public PlcTrigger fromString(String string) {
				return null;
			}

			@Override
			public String toString(PlcTrigger object) {

				try {
					return object.getPlcTriggerInfo();

				} catch (NullPointerException e) {

				}

				return "kein";

			}
		});

		PlcTrigger plcTriggerDefault = new PlcTrigger();

		// keine Auswahl in Combo zur Verfügung stellen
		triggerComboBox.getItems().add(0, plcTriggerDefault);

		for (PlcTrigger plcTrigger : plcTriggersAll) {
			if (plcTrigger.getId() == process.getPlcTriggerId()) {

				// aktuellen verwendeten Trigger in Combo zur Verfügung stellen
				triggerComboBox.getItems().add(1, plcTrigger);

				// aktuellen verwendeten Trigger in Combo auswählen
				triggerComboBox.getSelectionModel().select(plcTrigger);

				break;
			}
		}

	}

	private void enableFileds() {
		btnOK.setDisable(false);
		cbDecpoints.setDisable(false);
		valueStation.setDisable(false);
		valueBezeichnung.setDisable(false);
		cbSetvalueUsed.setDisable(false);
		valueSetvalue.setDisable(false);
		valueAnzahlAvg.setDisable(false);
		valueAnzahlSpcKlassen.setDisable(false);
		valueCpkLoLim1.setDisable(false);
		valueCpkLoLim2.setDisable(false);
		unitComboBox.setDisable(false);
		unitOverviewButton.setDisable(false);
		// PLC Auswahl ist nur bei PLC Trigger möglich --> Gefahr von Fehlauswahl
		// plcComboBox.setDisable(false);
		triggerComboBox.setDisable(false);
		plcOverviewButton.setDisable(false);
		triggerOverviewButton.setDisable(false);
	}

	private void disableFileds() {
		btnOK.setDisable(true);
		cbDecpoints.setDisable(true);
		valueStation.setDisable(true);
		valueBezeichnung.setDisable(true);
		cbSetvalueUsed.setDisable(true);
		valueSetvalue.setDisable(true);
		valueAnzahlAvg.setDisable(true);
		valueAnzahlSpcKlassen.setDisable(true);
		valueCpkLoLim1.setDisable(true);
		valueCpkLoLim2.setDisable(true);
		unitComboBox.setDisable(true);
		unitOverviewButton.setDisable(true);
		plcComboBox.setDisable(true);
		triggerComboBox.setDisable(true);
		plcOverviewButton.setDisable(true);
		triggerOverviewButton.setDisable(true);
	}

	private void initValidator() {

		valueSetvalue.setOnKeyPressed((event) -> {

			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueSetvalue);
			spSetvalue.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						disableFileds();
						valueSetvalue.setDisable(false);
						return null;
					}

					float f = Float.parseFloat(text);
					if (f < -10000 || f > 10000) {
						disableFileds();
						valueSetvalue.setDisable(false);
						return new ValidationResult("Should be > -10000 or < 10000", ValidationResult.Type.WARNING);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {

					if (valueSetvalue.getText().contains(",")) {
						enableFileds();
						valueSetvalue.setDisable(false);
						return new ValidationResult("Shoud be a Point", ValidationResult.Type.WARNING);

					}

					else {
						// failed
						disableFileds();
						valueSetvalue.setDisable(false);
						return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
					}
				}
			});
		});

		valueAnzahlAvg.setOnKeyPressed((event) -> {

			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueAnzahlAvg);
			spAnzahlAvg.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						disableFileds();
						valueAnzahlAvg.setDisable(false);
						return null;
					}

					int i = Integer.parseInt(text);
					if (i < 0 || i > 50) {
						disableFileds();
						valueAnzahlAvg.setDisable(false);
						return new ValidationResult("Should be > 0 or < 50", ValidationResult.Type.WARNING);
					}

					if (valueAnzahlAvg.getText().contains(".")) {
						disableFileds();
						valueAnzahlAvg.setDisable(false);
						return new ValidationResult("Should be a non decimalvalue", ValidationResult.Type.ERROR);

					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {
					// failed
					disableFileds();
					valueAnzahlAvg.setDisable(false);
					return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
				}
			});
		});

		valueAnzahlSpcKlassen.setOnKeyPressed((event) -> {

			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueAnzahlSpcKlassen);
			spAnzahlSpcKlassen.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						disableFileds();
						valueAnzahlSpcKlassen.setDisable(false);
						return null;
					}

					int i = Integer.parseInt(text);
					if (i < 1 || i > 200) {
						disableFileds();
						valueAnzahlSpcKlassen.setDisable(false);
						return new ValidationResult("Should be > 1 or <= 200", ValidationResult.Type.WARNING);
					}

					if (valueAnzahlSpcKlassen.getText().contains(".")) {
						disableFileds();
						valueAnzahlSpcKlassen.setDisable(false);
						return new ValidationResult("Should be a non decimalvalue", ValidationResult.Type.ERROR);

					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {
					// failed
					disableFileds();
					valueAnzahlSpcKlassen.setDisable(false);
					return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
				}
			});
		});

		valueCpkLoLim1.setOnKeyPressed((event) -> {

			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueCpkLoLim1);
			spCpkLoLim1.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						disableFileds();
						valueCpkLoLim1.setDisable(false);
						return null;
					}

					float f = Float.parseFloat(text);
					if (f < 0 || f > 1000) {
						disableFileds();
						valueCpkLoLim1.setDisable(false);
						return new ValidationResult("Should be > 0 or <= 1000", ValidationResult.Type.WARNING);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {

					if (valueCpkLoLim1.getText().contains(",")) {
						enableFileds();
						valueCpkLoLim1.setDisable(false);
						return new ValidationResult("Shoud be a Point", ValidationResult.Type.WARNING);

					}

					else {
						// failed
						disableFileds();
						valueCpkLoLim1.setDisable(false);
						return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
					}
				}
			});
		});

		valueCpkLoLim2.setOnKeyPressed((event) -> {

			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueCpkLoLim2);
			spCpkLoLim2.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						disableFileds();
						valueCpkLoLim2.setDisable(false);
						return null;
					}

					float f = Float.parseFloat(text);
					if (f < 0 || f > 1000) {
						disableFileds();
						valueCpkLoLim2.setDisable(false);
						return new ValidationResult("Should be > 0 or < 1000", ValidationResult.Type.WARNING);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {

					if (valueCpkLoLim2.getText().contains(",")) {
						enableFileds();
						valueCpkLoLim2.setDisable(false);
						return new ValidationResult("Shoud be a Point", ValidationResult.Type.WARNING);

					}

					else {
						// failed
						disableFileds();
						valueCpkLoLim2.setDisable(false);
						return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
					}
				}
			});
		});

	}

	public boolean isOkClicked() {
		return okClicked;
	}

	@FXML
	private void handleOk() {
		if (isInputValid()) {

			process.setDecimalPoints(cbDecpoints.getSelectionModel().getSelectedIndex());
			process.setStation(valueStation.getText());
			process.setName(valueBezeichnung.getText());
			process.setSetvalueUsed(cbSetvalueUsed.isSelected());
			process.setSetvalue(DecimalPointFormatter.roundFloat2Float(
					Float.valueOf(valueSetvalue.getText().replace(",", ".")), process.getDecimalPoints()));
			process.setNrAvg(Integer.valueOf((valueAnzahlAvg.getText())));
			process.setNrSpcClass(Integer.valueOf((valueAnzahlSpcKlassen.getText())));
			process.setCpkLoLim1(DecimalPointFormatter
					.roundFloat2Float(Float.valueOf(valueCpkLoLim1.getText().replace(",", ".")), 2));
			valueCpkLoLim1Vis.setText(valueCpkLoLim1.getText());
			process.setCpkLoLim2(DecimalPointFormatter
					.roundFloat2Float(Float.valueOf(valueCpkLoLim2.getText().replace(",", ".")), 2));

			process.setUnitId(unitComboBox.getSelectionModel().getSelectedItem().getId());
			process.setUnit(unitComboBox.getSelectionModel().getSelectedItem());

			if (triggerComboBox.getSelectionModel().getSelectedItem() != null) {
				process.setPlcTriggerId(triggerComboBox.getSelectionModel().getSelectedItem().getId());
				process.setPlcTrigger(triggerComboBox.getSelectionModel().getSelectedItem());
			}

			if (plcComboBox.getSelectionModel().getSelectedItem() != null) {
				process.getPlcTrigger().setPlcId(plcComboBox.getSelectionModel().getSelectedItem().getId());
				process.getPlcTrigger().setPlc(plcComboBox.getSelectionModel().getSelectedItem());
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

		if (valueStation.getText() == null || valueStation.getText().length() == 0
				|| valueStation.getText().length() > 45) {
			errorMessage += "Ungültige Eingabe (Station) !\n";
		}
		if (valueBezeichnung.getText() == null || valueBezeichnung.getText().length() == 0
				|| valueBezeichnung.getText().length() > 45) {
			errorMessage += "Ungültige Eingabe (Name) !\n";
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