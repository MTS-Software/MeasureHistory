package com.gretha.processmanager.view.process.settings;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Plc;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Unit;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.LoginLogout;
import com.gretha.shared.util.textvalidator.TextInputValidatorPane;
import com.gretha.shared.util.textvalidator.ValidationResult;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class SettingsController implements Initializable {

	private ResourceBundle resources;
	private static final Logger logger = Logger.getLogger(SettingsController.class);

	@FXML
	private URL location;
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
	private Button btnAbbrechen;
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

	private RootPaneController rootPaneController;

	private Stage dialogStage;
	private Main main;
	private boolean okClicked = false;
	private boolean textValueStationChanged = false;
	private boolean textValueBezeichnungChanged = false;
	private String textValueStation = "";
	private String textValueBezeichnung = "";

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resources = resources;

		if (LoginLogout.isUserLevelOK(LoginLogout.EUserLevels.ADMINISTRATOR)) {
			// PLC Auswahl ist nur bei PLC Trigger möglich --> Gefahr von Fehlauswahl
			plcComboBox.setDisable(true);
			triggerComboBox.setDisable(false);

			plcOverviewButton.setDisable(false);
			triggerOverviewButton.setDisable(false);
		} else {
			plcComboBox.setDisable(true);
			triggerComboBox.setDisable(true);

			plcOverviewButton.setDisable(true);
			triggerOverviewButton.setDisable(true);
		}

		unitComboBox.setDisable(false);
		unitOverviewButton.setDisable(false);

		btnOK.setDisable(true);

		// Fix Disablen da dies nur zum Visualisieren benötigt wird
		valueCpkLoLim1Vis.setDisable(true);

		cbDecpoints.setOnAction((event) -> {
			btnOK.setDisable(false);
		});

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

		cbSetvalueUsed.setOnAction((event) -> {
			if (cbSetvalueUsed.isSelected())
				valueSetvalue.setDisable(false);
			else
				valueSetvalue.setDisable(true);

			btnOK.setDisable(false);
		});

		valueStation.setOnKeyReleased((event) -> {
			if (!valueStation.getText().equals(textValueStation)) {
				btnOK.setDisable(false);
				textValueStationChanged = true;
			} else
				textValueStationChanged = false;
		});

		valueBezeichnung.setOnKeyReleased((event) -> {
			if (!valueBezeichnung.getText().equals(textValueBezeichnung)) {
				btnOK.setDisable(false);
				textValueBezeichnungChanged = true;
			} else
				textValueBezeichnungChanged = false;
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

	private void enableFileds() {
		btnOK.setDisable(false);
		cbDecpoints.setDisable(false);
		cbSetvalueUsed.setDisable(false);
		valueSetvalue.setDisable(false);
		valueAnzahlAvg.setDisable(false);
		valueAnzahlSpcKlassen.setDisable(false);
		valueCpkLoLim1.setDisable(false);
		valueCpkLoLim2.setDisable(false);
		unitComboBox.setDisable(false);
		unitOverviewButton.setDisable(false);

		if (LoginLogout.isUserLevelOK(LoginLogout.EUserLevels.ADMINISTRATOR)) {
			// PLC Auswahl ist nur bei PLC Trigger möglich --> Gefahr von Fehlauswahl
			plcComboBox.setDisable(true);
			triggerComboBox.setDisable(false);

			plcOverviewButton.setDisable(false);
			triggerOverviewButton.setDisable(false);
		} else {
			plcComboBox.setDisable(true);
			triggerComboBox.setDisable(true);

			plcOverviewButton.setDisable(true);
			triggerOverviewButton.setDisable(true);
		}
	}

	private void disableFileds() {
		btnOK.setDisable(true);
		cbDecpoints.setDisable(true);
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

	private void getActualSettings() {
		// Auswahlfeld Dezimalstellen/Kommastellen initialisieren
		ObservableList<Integer> decPoints = FXCollections.observableArrayList(0, 1, 2, 3, 4);
		cbDecpoints.setItems(decPoints);
		// Einstellungen auslesen
		cbDecpoints.getSelectionModel().select(rootPaneController.getProcess().getDecimalPoints());
		valueStation.setText(rootPaneController.getProcess().getStation());
		textValueStation = valueStation.getText();
		valueBezeichnung.setText(rootPaneController.getProcess().getName());
		textValueBezeichnung = valueBezeichnung.getText();
		cbSetvalueUsed.setSelected(rootPaneController.getProcess().isSetvalueUsed());
		valueSetvalue.setText(DecimalPointFormatter.roundFloat2String(rootPaneController.getProcess().getSetvalue(),
				rootPaneController.getProcess().getDecimalPoints()));
		lblUnit.setText("[" + rootPaneController.getProcess().getUnit().getSign() + "]");
		valueAnzahlAvg.setText(String.valueOf(rootPaneController.getProcess().getNrAvg()));
		valueAnzahlSpcKlassen.setText(String.valueOf(rootPaneController.getProcess().getNrSpcClass()));
		valueCpkLoLim1
				.setText(DecimalPointFormatter.roundFloat2String(rootPaneController.getProcess().getCpkLoLim1(), 2));
		valueCpkLoLim1Vis.setText(valueCpkLoLim1.getText());
		valueCpkLoLim2
				.setText(DecimalPointFormatter.roundFloat2String(rootPaneController.getProcess().getCpkLoLim2(), 2));

		if (cbSetvalueUsed.isSelected())
			valueSetvalue.setDisable(false);
		else
			valueSetvalue.setDisable(true);

		setUnitComboBox();
		setPlcComboBox();
		setPlcTriggerComboBox();
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
			if (unit.getId() == rootPaneController.getProcess().getUnitId()) {
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
			if (rootPaneController.getProcess().getPlcTrigger() != null)
				if (plc.getId() == rootPaneController.getProcess().getPlcTrigger().getPlcId()) {
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
			if (plcTrigger.getId() == rootPaneController.getProcess().getPlcTriggerId()) {

				// aktuellen verwendeten Trigger in Combo zur Verfügung stellen
				triggerComboBox.getItems().add(1, plcTrigger);

				// aktuellen verwendeten Trigger in Combo auswählen
				triggerComboBox.getSelectionModel().select(plcTrigger);

				break;
			}
		}

	}

	private boolean setActualSettings() {

		rootPaneController.getProcess().setDecimalPoints(cbDecpoints.getSelectionModel().getSelectedIndex());
		rootPaneController.getProcess().setStation(valueStation.getText());
		rootPaneController.getProcess().setName(valueBezeichnung.getText());
		rootPaneController.getProcess().setSetvalueUsed(cbSetvalueUsed.isSelected());
		rootPaneController.getProcess().setSetvalue(
				DecimalPointFormatter.roundFloat2Float(Float.valueOf(valueSetvalue.getText().replace(",", ".")),
						rootPaneController.getProcess().getDecimalPoints()));
		rootPaneController.getProcess().setNrAvg(Integer.valueOf((valueAnzahlAvg.getText())));
		rootPaneController.getProcess().setNrSpcClass(Integer.valueOf((valueAnzahlSpcKlassen.getText())));
		rootPaneController.getProcess().setCpkLoLim1(
				DecimalPointFormatter.roundFloat2Float(Float.valueOf(valueCpkLoLim1.getText().replace(",", ".")), 2));
		valueCpkLoLim1Vis.setText(valueCpkLoLim1.getText());
		rootPaneController.getProcess().setCpkLoLim2(
				DecimalPointFormatter.roundFloat2Float(Float.valueOf(valueCpkLoLim2.getText().replace(",", ".")), 2));

		rootPaneController.getProcess().setUnitId(unitComboBox.getSelectionModel().getSelectedItem().getId());
		rootPaneController.getProcess().setUnit(unitComboBox.getSelectionModel().getSelectedItem());

		if (triggerComboBox.getSelectionModel().getSelectedItem() != null) {
			rootPaneController.getProcess()
					.setPlcTriggerId(triggerComboBox.getSelectionModel().getSelectedItem().getId());
			rootPaneController.getProcess().setPlcTrigger(triggerComboBox.getSelectionModel().getSelectedItem());
		}

		if (plcComboBox.getSelectionModel().getSelectedItem() != null) {
			rootPaneController.getProcess().getPlcTrigger()
					.setPlcId(plcComboBox.getSelectionModel().getSelectedItem().getId());
			rootPaneController.getProcess().getPlcTrigger().setPlc(plcComboBox.getSelectionModel().getSelectedItem());
		}

		Service.getInstance().updateProcess(rootPaneController.getProcess());

		if (!Service.getInstance().isErrorStatus())
			if (rootPaneController.getProcess().getPlcTrigger() != null)
				Service.getInstance().updatePlcTrigger(rootPaneController.getProcess().getPlcTrigger());

		if (Service.getInstance().isErrorStatus())
			return false;
		else
			return true;
	}

	@FXML
	private void handleOK() {
		if (setActualSettings()) {

			okClicked = true;

			if (textValueStationChanged || textValueBezeichnungChanged)
				main.refreshTreeView(rootPaneController.getProcess(), -1);

			this.dialogStage.close();
		}
	}

	@FXML
	private void handleClose() {
		okClicked = false;
		this.dialogStage.close();
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setData(Process process) {
		this.dialogStage
				.setTitle(process.getStation() + ": " + process.getName() + " - " + resources.getString("settings"));

		getActualSettings();
	}

	public void setMain(Main main) {
		this.main = main;
	}

	public RootPaneController getRootPaneController() {
		return rootPaneController;
	}

	public void setRootPaneController(RootPaneController rootPaneController) {
		this.rootPaneController = rootPaneController;
	}

	public boolean isOkClicked() {
		return okClicked;
	}
}
