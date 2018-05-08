package com.gretha.processmanager.view.process.root;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.charts.ResultChartController;
import com.gretha.processmanager.view.process.charts.SPCChartController;
import com.gretha.processmanager.view.process.charts.StatisticChartController;
import com.gretha.processmanager.view.process.charts.TimeChartController;
import com.gretha.processmanager.view.process.result.ResultDataController;
import com.gretha.processmanager.view.process.result.ResultTableController;
import com.gretha.processmanager.view.process.result.SPCDataController;
import com.gretha.processmanager.view.process.result.TimeDataController;
import com.gretha.processmanager.view.process.settings.SettingsController;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Result;
import com.gretha.shared.model.ResultData;
import com.gretha.shared.model.TimeData;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DurationDateAndTime;
import com.gretha.shared.util.LoginLogout;

import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class RootPaneController implements Initializable {

	private static final Logger logger = Logger.getLogger(RootPaneController.class);
	private ResourceBundle resources;

	private Stage dialogStage;

	private Process process;
	private List<Result> results;
	private ResultData resultData;
	private TimeData timeData;
	private Main main;

	@FXML
	private HBox southPane;
	@FXML
	private MenuItem miRefresh;
	@FXML
	private TabPane tabPane;
	@FXML
	private Tab tabResultTable;
	@FXML
	private Tab tabResultChart;
	@FXML
	private Tab tabStatisticChart;
	@FXML
	private Tab tabSPCChart;
	@FXML
	private Tab tabTimeChart;
	@FXML
	private Label labelLeftStatus;
	@FXML
	private Label labelRightStatus;
	@FXML
	private Label labelCenterStatus;

	@FXML
	private RootPaneBarController rootPaneBarController;
	@FXML
	private ResultTableController resultTableController;
	@FXML
	private ResultChartController resultChartController;
	@FXML
	private StatisticChartController statisticChartController;
	@FXML
	private SPCChartController spcChartController;
	@FXML
	private ResultDataController resultDataController;
	@FXML
	private SPCDataController spcDataController;
	@FXML
	private TimeDataController timeDataController;
	@FXML
	private TimeChartController timeChartController;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		initController();
		initUI();

	}

	private void initController() {

		this.resultDataController.setRootPaneController(this);
		this.spcDataController.setRootPaneController(this);
		this.timeDataController.setRootPaneController(this);

		this.resultTableController.setRootPaneController(this);
		this.resultChartController.setRootPaneController(this);
		this.statisticChartController.setRootPaneController(this);
		this.spcChartController.setRootPaneController(this);
		this.timeChartController.setRootPaneController(this);
		this.rootPaneBarController.setRootPaneController(this);

	}

	private void initUI() {

		labelLeftStatus.setText("");
		labelCenterStatus.setText("");
		labelRightStatus.setText("");

		rootPaneBarController.getBtnRefresh().setOnAction((event) -> {
			setData(process);

		});

		rootPaneBarController.getSearchFiled().setPromptText("Suche... (Messwert, Seriennr., Bemerkung, Zeit)");

		tabPane.getSelectionModel().selectedItemProperty()
				.addListener((ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) -> {

					southPane.getChildren().clear();

					if (newValue == tabResultTable) {
						southPane.getChildren().add(resultDataController.getPane());
						rootPaneBarController.getSearchFiled().setText("");
						rootPaneBarController.getSearchFiled()
								.setPromptText("Suche... (Messwert, Seriennr., Bemerkung, Zeit)");
						rootPaneBarController.getSearchFiled().setDisable(false);

					} else if (newValue == tabResultChart) {
						southPane.getChildren().add(resultDataController.getPane());
						rootPaneBarController.getSearchFiled().setText("");
						rootPaneBarController.getSearchFiled().setPromptText("Suche...");
						rootPaneBarController.getSearchFiled().setDisable(true);

					} else if (newValue == tabStatisticChart) {
						southPane.getChildren().add(resultDataController.getPane());
						rootPaneBarController.getSearchFiled().setText("");
						rootPaneBarController.getSearchFiled().setPromptText("Suche...");
						rootPaneBarController.getSearchFiled().setDisable(true);

					} else if (newValue == tabSPCChart) {
						southPane.getChildren().add(spcDataController.getPane());
						rootPaneBarController.getSearchFiled().setText("");
						rootPaneBarController.getSearchFiled().setPromptText("Suche...");
						rootPaneBarController.getSearchFiled().setDisable(true);

					} else if (newValue == tabTimeChart) {
						southPane.getChildren().add(timeDataController.getPane());
						rootPaneBarController.getSearchFiled().setText("");
						rootPaneBarController.getSearchFiled().setPromptText("Suche...");
						rootPaneBarController.getSearchFiled().setDisable(true);

					} else {
						rootPaneBarController.getSearchFiled().setText("");
						rootPaneBarController.getSearchFiled().setPromptText("Suche...");
						rootPaneBarController.getSearchFiled().setDisable(true);
					}
				});

	}

	public void setMain(Main main) {
		this.main = main;
	}

	public void setDialogStage(Stage dialogStage) {

		this.dialogStage = dialogStage;

		this.resultTableController.setDialogStage(dialogStage);
		this.resultChartController.setDialogStage(dialogStage);
		this.resultDataController.setDialogStage(dialogStage);
		this.statisticChartController.setDialogStage(dialogStage);
		this.spcChartController.setDialogStage(dialogStage);
		this.spcDataController.setDialogStage(dialogStage);
		this.rootPaneBarController.setDialogStage(dialogStage);
		this.timeChartController.setDialogStage(dialogStage);
		this.timeDataController.setDialogStage(dialogStage);
	}

	public void setTab(int tab) {

		tabPane.getSelectionModel().select(tab);

		southPane.getChildren().clear();
		if (tabPane.getSelectionModel().getSelectedIndex() == 0)
			southPane.getChildren().add(resultDataController.getPane());
		if (tabPane.getSelectionModel().getSelectedIndex() == 1)
			southPane.getChildren().add(resultDataController.getPane());
		if (tabPane.getSelectionModel().getSelectedIndex() == 2)
			southPane.getChildren().add(resultDataController.getPane());
		if (tabPane.getSelectionModel().getSelectedIndex() == 3)
			southPane.getChildren().add(spcDataController.getPane());
		if (tabPane.getSelectionModel().getSelectedIndex() == 4)
			southPane.getChildren().add(timeDataController.getPane());

		dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				dialogStage.close();
			}
		});
	}

	public void setControllerData() {

		this.resultDataController.setData();
		this.spcDataController.setData();
		this.timeDataController.setData();

		this.rootPaneBarController.setData();
		this.resultTableController.setData();
		this.resultChartController.setData();
		this.statisticChartController.setData();
		this.spcChartController.setData();
		this.timeChartController.setData();

	}

	public void setData(Process process) {

		long beginTime = System.currentTimeMillis();

		labelCenterStatus.setText("Ergebnisse laden...");

		this.process = Service.getInstance().getProcess(process.getId());

		// Wenn Filter aktiviert und aktualisieren gedrückt (über F5, Menü, etc.)
		// sollen gleich die Werte über den aktiven Filter laufen und nur die
		// gefilterten Werte hinzugefügt werden
		if (rootPaneBarController.getFilter() != null) {
			if (rootPaneBarController.getFilter().isActivated()) {
				rootPaneBarController.getFilterSettingsController().setData(process, null);
				rootPaneBarController.getFilterSettingsController()
						.getFilteredResults(rootPaneBarController.getFilterSettingsController().isOkClicked());
				this.results.clear();
				this.results.addAll(rootPaneBarController.getFilterSettingsController().getProcessResults());
			}

		} else
			this.results = Service.getInstance().getResults(process);

		setControllerData();

		long endTime = System.currentTimeMillis();
		long duration = endTime - beginTime;

		if (rootPaneBarController.getFilter() != null) {
			if (rootPaneBarController.getFilter().isActivated()) {
				labelCenterStatus
						.setText(String.format("%d Ergebnisse sichtbar (Executing time: %s - Ansicht gefiltert)",
								results.size(), DurationDateAndTime.getTimestampFromMilliseconds(duration)));
			}
		}

		else
			labelCenterStatus.setText(String.format("%d Ergebnisse geladen (Executing time: %s)", results.size(),
					DurationDateAndTime.getTimestampFromMilliseconds(duration)));
	}

	@FXML
	private void handleClose() {
		this.dialogStage.close();

	}

	@FXML
	private void handleSettings() {

		if (LoginLogout.login(LoginLogout.EUserLevels.USER)) {

			try {

				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/process/settings/Settings.fxml"));
				loader.setResources(resources);
				AnchorPane pane = (AnchorPane) loader.load();

				// Create the dialog Stage
				Stage dialogStage = new Stage();
				dialogStage.getIcons().addAll(new Image(Constants.ICON_SETTINGS));
				dialogStage.initModality(Modality.WINDOW_MODAL);
				dialogStage.initOwner(this.dialogStage);

				Scene scene = new Scene(pane);
				scene.getStylesheets().add(Constants.STYLESHEET);
				dialogStage.setScene(scene);

				// Set the settings into the controller.
				SettingsController controller = loader.getController();
				controller.setDialogStage(dialogStage);
				controller.setRootPaneController(this);
				controller.setMain(main);
				controller.setData(process);

				// Show the dialog and wait until the user closes it
				dialogStage.showAndWait();

				if (controller.isOkClicked()) {
					setControllerData();

					dialogStage.setTitle(
							process.getStation() + ": " + process.getName() + " - " + resources.getString("settings"));

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	@FXML
	private void handleFAQsSPCInfo() {

		StringBuilder sb = new StringBuilder();

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("FAQ");
		alert.setHeaderText("");

		sb.append(
				"Prozessfähigkeit:\nEin Prozess ist fähig, wenn er in der Lage ist, die Toleranzen auf ein vorgegebenes Maß einzuhalten.");
		sb.append("\nTypische Messwerte: Cp, Cpk");
		sb.append(
				"\n\nDer Cpk-Wert wird aus dem Mittelwert (Xquer), der dazugehörigen Standardabweichung/Streuung (Sigma) und der oberen (OSG) beziehungsweise unteren (USG) Spezifikationsgrenze definiert. Um die Prozessfähigkeiten zu ermitteln, müssen immer Toleranzen gegeben sein.");
		sb.append(
				"\n\nWährend der Cp-Wert nur das Verhältnis der vorgegebenen Toleranz zur Prozessstreuung angibt, beinhaltet der Cpk-Wert auch die Lage des Mittelwertes zur vorgegeben Toleranzmitte. Daher ist der Cpk-Wert stets kleiner als der Cp-Wert.");
		sb.append(
				"\n\nZielwerte: Vielfach wird ein Cp-Wert von 2.00 (die Breite des Toleranzbereichs entspricht einer Streubreite von ±6 Standardabweichungen, daher Six Sigma) kombiniert mit einem Cpk-Wert von 1.5 (der Abstand der nächstgelegenen Toleranzgrenze vom Prozessmittelwert beträgt mindestens 4.5 Standardabweichungen) als wünschenswertes Ziel definiert.");
		sb.append(
				"\n\nProzessbeherrschung:\nEin Prozess ist beherrscht (stabil), wenn er lediglich zufällig streut. Es gibt mathematische und empirische Regeln über das, was hier als 'zufällig' bezeichnet wird.");
		sb.append("\nBeobachtungsgrößen: Mittelwert (Xquer) und Standardabweichung/Streuung (Sigma).");
		sb.append("\n\nDie Prozessbeherrschung wird nicht in Bezug auf die Toleranzen gesetzt!");

		alert.setContentText(sb.toString());

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setGraphic(new ImageView(Constants.ICON_SPC_INFO));

		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_HELP));

		alert.showAndWait();
	}

	@FXML
	private void handeFAQsChartZoom() {

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("FAQ");
		alert.setHeaderText("Wie kann im Diagramm gezoomt werden?");

		alert.setContentText("Zoomen (Achse oder Diagramm):\n" + "- mit Strg + Mausrad\n"
				+ "- mit Strg + linke oder rechte Mausstaste Bereich markieren\n\n" + "Rückgängig:\n"
				+ "- mit Rechtsklick (Kontextmenü)" + "\n\n");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_HELP));

		alert.showAndWait();
	}

	@FXML
	private void handeFAQsChartMove() {

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("FAQ");
		alert.setHeaderText("Wie kann der Inhalt im Diagramm verschoben werden?");

		alert.setContentText("Verschieben (Achse oder Diagramm):\n" + "- mit der linken Maustaste ziehen\n\n"
				+ "Rückgängig:\n" + "- mit Rechtsklick (Kontextmenü)" + "\n\n");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_HELP));

		alert.showAndWait();
	}

	@FXML
	private void handeFAQsChartLineVisibility() {

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("FAQ");
		alert.setHeaderText("Wie können Linien im Diagramm ein- bzw. ausgeblendet werden werden?");

		alert.setContentText("Linie(n) ein- bzw. ausblenden:\n" + "- mit der rechten Maustaste (Kontextmenü)\n"
				+ "- mit der linken Maustaste auf den Legendeneintag" + "\n\n");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_HELP));

		alert.showAndWait();
	}

	@FXML
	private void handleRefresh(KeyEvent e) {
		if (e.getCode() == KeyCode.F5)
			setData(process);
	}

	@FXML
	private void handleMenuRefresh() {
		setData(process);
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;

		if (rootPaneBarController.getFilter().isActivated())
			labelCenterStatus.setText(rootPaneBarController.getFilterSettingsController().getDbQueryLoggerText());
	}

	public Process getProcess() {
		return process;
	}

	public ResultData getResultData() {
		return resultData;
	}

	public RootPaneBarController getRootPaneBarController() {
		return rootPaneBarController;
	}

	public void setResultData(ResultData resultData) {
		this.resultData = resultData;
	}

	public TimeData getTimeData() {
		return timeData;
	}

	public void setTimeData(TimeData timeData) {
		this.timeData = timeData;
	}
}