package com.gretha.processmanager.view.process.result;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Result;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.textvalidator.TextInputValidatorPane;
import com.gretha.shared.util.textvalidator.ValidationResult;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ResultTableEditController implements Initializable {

	private ResourceBundle resources;
	private static final Logger logger = Logger.getLogger(ResultTableEditController.class);
	private RootPaneController rootPaneController;
	private Result result;
	private Stage dialogStage;
	private boolean okClicked = false;

	@FXML
	private URL location;
	@FXML
	private StackPane spNr;
	@FXML
	private TextField valueNr;
	@FXML
	private StackPane spValue;
	@FXML
	private TextField valueValue;
	@FXML
	private Label lblUnit;
	@FXML
	private Label lblUnit1;
	@FXML
	private Label lblUnit2;
	@FXML
	private StackPane spLoLim;
	@FXML
	private TextField valueLoLim;
	@FXML
	private StackPane spUpLim;
	@FXML
	private TextField valueUpLim;
	@FXML
	private StackPane spState;
	@FXML
	private ComboBox<EState> cbState;
	@FXML
	private StackPane spSerial;
	@FXML
	private TextField valueSerial;
	@FXML
	private StackPane spTypId;
	@FXML
	private TextField valueTypId;
	@FXML
	private StackPane spWt;
	@FXML
	private TextField valueWt;
	@FXML
	private StackPane spRemark;
	@FXML
	private TextField valueRemark;
	@FXML
	private TextField valueTimestamp;
	@FXML
	private Button btnOK;
	@FXML
	private Button btnAbbrechen;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resources = resources;

		btnOK.setDisable(false);

		// Fix Disablen da dies nur zum Visualisieren benötigt wird
		valueNr.setDisable(true);
		valueTimestamp.setDisable(true);

		cbState.setItems(FXCollections.observableArrayList(EState.values()));

		initValidator();
	}

	private void initValidator() {

		valueValue.setOnKeyPressed((event) -> {
			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueValue);
			spValue.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						disableFileds();
						valueValue.setDisable(false);
						return null;
					}

					float f = Float.parseFloat(text);
					if (f < -10000 || f > 10000) {
						disableFileds();
						valueValue.setDisable(false);
						return new ValidationResult("Should be > -10000 or < 10000", ValidationResult.Type.WARNING);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {

					if (valueValue.getText().contains(",")) {
						enableFileds();
						valueValue.setDisable(false);
						return new ValidationResult("Shoud be a Point", ValidationResult.Type.WARNING);

					}

					else {
						// failed
						disableFileds();
						valueValue.setDisable(false);
						return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
					}
				}
			});
		});

		valueLoLim.setOnKeyPressed((event) -> {
			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueLoLim);
			spLoLim.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						disableFileds();
						valueLoLim.setDisable(false);
						return null;
					}

					float f = Float.parseFloat(text);
					if (f < -10000 || f > 10000) {
						disableFileds();
						valueLoLim.setDisable(false);
						return new ValidationResult("Should be > -10000 or < 10000", ValidationResult.Type.WARNING);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {

					if (valueLoLim.getText().contains(",")) {
						enableFileds();
						valueLoLim.setDisable(false);
						return new ValidationResult("Shoud be a Point", ValidationResult.Type.WARNING);

					}

					else {
						// failed
						disableFileds();
						valueLoLim.setDisable(false);
						return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
					}
				}
			});
		});

		valueUpLim.setOnKeyPressed((event) -> {
			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueUpLim);
			spUpLim.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						disableFileds();
						valueUpLim.setDisable(false);
						return null;
					}

					float f = Float.parseFloat(text);
					if (f < -10000 || f > 10000) {
						disableFileds();
						valueUpLim.setDisable(false);
						return new ValidationResult("Should be > -10000 or < 10000", ValidationResult.Type.WARNING);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {

					if (valueUpLim.getText().contains(",")) {
						enableFileds();
						valueUpLim.setDisable(false);
						return new ValidationResult("Shoud be a Point", ValidationResult.Type.WARNING);

					}

					else {
						// failed
						disableFileds();
						valueUpLim.setDisable(false);
						return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
					}
				}
			});
		});

		cbState.setOnAction((event) -> {
			if (cbState.getSelectionModel().isSelected(EState.UNDEF.nr)) {
				cbState.setStyle("-fx-background-color: transparent, #bababa, transparent, yellow");
			} else if (cbState.getSelectionModel().isSelected(EState.OK.nr)) {
				cbState.setStyle("-fx-background-color: transparent, #bababa, transparent, lightgreen");
			} else if (cbState.getSelectionModel().isSelected(EState.NOK.nr)) {
				cbState.setStyle("-fx-background-color: transparent, #bababa, transparent, lightcoral");
			} else if (cbState.getSelectionModel().isSelected(EState.UNBEWERTET.nr)) {
				cbState.setStyle("-fx-background-color: transparent, #bababa, transparent, lightgrey");
			}
		});

		valueSerial.setOnKeyPressed((event) -> {
			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueSerial);
			spSerial.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text.length() > 20) {
						disableFileds();
						valueSerial.setDisable(false);
						return new ValidationResult("String lenght to long", ValidationResult.Type.ERROR);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {
					// failed
					disableFileds();
					valueSerial.setDisable(false);
					return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
				}
			});
		});

		valueWt.setOnKeyPressed((event) -> {
			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueWt);
			spWt.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text.length() > 20) {
						disableFileds();
						valueWt.setDisable(false);
						return new ValidationResult("String lenght to long", ValidationResult.Type.ERROR);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {
					// failed
					disableFileds();
					valueWt.setDisable(false);
					return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
				}
			});
		});

		valueRemark.setOnKeyPressed((event) -> {
			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueRemark);
			spRemark.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text.length() > 50) {
						disableFileds();
						valueRemark.setDisable(false);
						return new ValidationResult("String lenght to long", ValidationResult.Type.ERROR);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {
					// failed
					disableFileds();
					valueRemark.setDisable(false);
					return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
				}
			});
		});

		valueTypId.setOnKeyPressed((event) -> {
			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueTypId);
			spTypId.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					int i = Integer.parseInt(text);
					if (i < -1000 || i > 1000) {
						disableFileds();
						valueTypId.setDisable(false);
						return new ValidationResult("Should be > -1000 or < 1000", ValidationResult.Type.WARNING);
					}

					else {
						enableFileds();
						return null;
					}

				} catch (Exception e) {
					// failed
					disableFileds();
					valueTypId.setDisable(false);
					return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
				}
			});
		});

	}

	private void enableFileds() {
		btnOK.setDisable(false);
		// valueNr.setDisable(false);
		valueValue.setDisable(false);
		valueLoLim.setDisable(false);
		valueUpLim.setDisable(false);
		cbState.setDisable(false);
		valueSerial.setDisable(false);
		valueTypId.setDisable(false);
		valueWt.setDisable(false);
		valueRemark.setDisable(false);
		// valueTimestamp.setDisable(false);
	}

	private void disableFileds() {
		btnOK.setDisable(true);
		valueNr.setDisable(true);
		valueValue.setDisable(true);
		valueLoLim.setDisable(true);
		valueUpLim.setDisable(true);
		cbState.setDisable(true);
		valueSerial.setDisable(true);
		valueTypId.setDisable(true);
		valueWt.setDisable(true);
		valueRemark.setDisable(true);
		valueTimestamp.setDisable(true);
	}

	private void getActualResult() {
		// Ergebnisse auslesen
		valueNr.setText(String.valueOf(result.getNr()));
		lblUnit.setText("[" + rootPaneController.getProcess().getUnit().getSign() + "]");
		lblUnit1.setText(lblUnit.getText());
		lblUnit2.setText(lblUnit.getText());
		valueValue.setText(DecimalPointFormatter.roundFloat2String(result.getValue(),
				rootPaneController.getProcess().getDecimalPoints()));
		valueLoLim.setText(DecimalPointFormatter.roundFloat2String(result.getLoLim(),
				rootPaneController.getProcess().getDecimalPoints()));
		valueUpLim.setText(DecimalPointFormatter.roundFloat2String(result.getUpLim(),
				rootPaneController.getProcess().getDecimalPoints()));

		if (result.getState() == EState.UNBEWERTET.nr) {
			cbState.getSelectionModel().select(EState.UNBEWERTET);
			cbState.setStyle("-fx-background-color: transparent, #bababa, transparent, lightgrey");

		} else if (result.getState() == EState.OK.nr) {
			cbState.getSelectionModel().select(EState.OK);
			cbState.setStyle("-fx-background-color: transparent, #bababa, transparent, lightgreen");

		} else if (result.getState() == EState.NOK.nr) {
			cbState.getSelectionModel().select(EState.NOK);
			cbState.setStyle("-fx-background-color: transparent, #bababa, transparent, lightcoral");

		} else {
			cbState.getSelectionModel().select(EState.UNDEF);
			cbState.setStyle("-fx-background-color: transparent, #bababa, transparent, yellow");
		}

		valueSerial.setText(result.getSerial());
		valueTypId.setText(String.valueOf(result.getTypId()));
		valueWt.setText(result.getWt());
		valueRemark.setText(result.getRemark());
		valueTimestamp.setText(result.getTimestamp());

	}

	private boolean setActualResult() {
		// Ergebnisse übernehmen
		result.setNr(Integer.valueOf(valueNr.getText()));
		result.setValue(DecimalPointFormatter.roundFloat2Float(Float.valueOf(valueValue.getText().replace(",", ".")),
				rootPaneController.getProcess().getDecimalPoints()));
		result.setLoLim(DecimalPointFormatter.roundFloat2Float(Float.valueOf(valueLoLim.getText().replace(",", ".")),
				rootPaneController.getProcess().getDecimalPoints()));
		result.setUpLim(DecimalPointFormatter.roundFloat2Float(Float.valueOf(valueUpLim.getText().replace(",", ".")),
				rootPaneController.getProcess().getDecimalPoints()));

		if (cbState.getSelectionModel().isSelected(EState.UNBEWERTET.nr)) {
			result.setState(EState.UNBEWERTET.nr);

		} else if (cbState.getSelectionModel().isSelected(EState.OK.nr)) {
			result.setState(EState.OK.nr);

		} else if (cbState.getSelectionModel().isSelected(EState.NOK.nr)) {
			result.setState(EState.NOK.nr);

		} else if (cbState.getSelectionModel().isSelected(EState.UNDEF.nr)) {
			result.setState(EState.UNDEF.nr);
		}

		result.setSerial(valueSerial.getText());
		result.setTypId(Integer.valueOf(valueTypId.getText()));
		result.setWt(valueWt.getText());
		result.setRemark(valueRemark.getText());
		result.setTimestamp(valueTimestamp.getText());

		// In DB schreiben
		Service.getInstance().updateResult(result);
		// Abfrage ob Upate erfolgreich und Übergabe an Returnwert
		if (Service.getInstance().isErrorStatus())
			return false;
		else
			return true;
	}

	@FXML
	private void handleOK() {
		if (setActualResult()) {
			okClicked = true;
			rootPaneController.setControllerData();
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

	public void setData(Result result) {
		this.result = result;
		getActualResult();
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

	public enum EState {

		UNBEWERTET("unbewertet", 0), NOK("nicht in Ordnung", 1), OK("in Ordnung", 2), UNDEF("undefiniert", 3);

		private String label;
		private Integer nr;

		EState(String label, Integer nr) {
			this.label = label;
			this.nr = nr;
		}

		public Integer getNr() {
			return nr;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
