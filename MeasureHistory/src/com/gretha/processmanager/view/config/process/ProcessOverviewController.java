package com.gretha.processmanager.view.config.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.config.root.IOverview;
import com.gretha.processmanager.view.config.root.OverviewButtonsController;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Process;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.StringCustomComparator;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.alert.NoSelectionAlert;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProcessOverviewController implements IOverview {

	private static final Logger logger = Logger.getLogger(ProcessOverviewController.class);

	@FXML
	private OverviewButtonsController overviewButtonsController;
	@FXML
	private TableView<Process> dataTable;
	@FXML
	private TableColumn<Process, String> idColumn;
	@FXML
	private TableColumn<Process, String> stationColumn;
	@FXML
	private TableColumn<Process, String> nameColumn;
	@FXML
	private TableColumn<Process, String> timestampColumn;
	@FXML
	private TextField valueDecpoints;
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
	private Label lblUnit;
	@FXML
	private TextField valueUnit;
	@FXML
	private TextField valuePlc;
	@FXML
	private TextArea valueTrigger;
	@FXML
	private Button unitOverviewButton;
	@FXML
	private Button plcOverviewButton;
	@FXML
	private Button triggerOverviewButton;
	@FXML
	private Label lblFoundDatasets;

	private Main main;
	private Stage dialogStage;
	private Process process;

	private ObservableList<Process> processList;
	private ChangeListener<String> filterListener;

	public ProcessOverviewController() {

	}

	@FXML
	private void initialize() {

		valueDecpoints.setDisable(true);
		valueStation.setDisable(true);
		valueBezeichnung.setDisable(true);
		cbSetvalueUsed.setDisable(true);
		valueSetvalue.setDisable(true);
		valueAnzahlAvg.setDisable(true);
		valueAnzahlSpcKlassen.setDisable(true);
		valueCpkLoLim1.setDisable(true);
		valueCpkLoLim2.setDisable(true);

		valueUnit.setDisable(true);
		valuePlc.setDisable(true);
		valueTrigger.setDisable(true);

		unitOverviewButton.setDisable(false);
		plcOverviewButton.setDisable(false);
		triggerOverviewButton.setDisable(false);

		overviewButtonsController.getNewButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				handleNew();
			}
		});
		overviewButtonsController.getEditButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				handleEdit();
			}
		});
		overviewButtonsController.getDeleteButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				handleDelete();
			}
		});
		overviewButtonsController.getRefreshButton().setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				logger.info("Refresh über Button");
				setData();
			}
		});

		unitOverviewButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				main.showUnitOverviewDialog();
				showDetails(process);

			}
		});

		plcOverviewButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				main.showPlcOverviewDialog();
				showDetails(process);

			}
		});

		triggerOverviewButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				main.showPlcTriggerOverviewDialog();
				showDetails(process);

			}
		});

		idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asString());
		idColumn.setComparator(new StringCustomComparator());
		idColumn.setCellFactory(column -> {
			return new TableCell<Process, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(String.valueOf(item));
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		stationColumn.setCellValueFactory(cellData -> cellData.getValue().stationProperty());
		stationColumn.setComparator(new StringCustomComparator());
		stationColumn.setCellFactory(column -> {
			return new TableCell<Process, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(String.valueOf(item));
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());
		timestampColumn.setComparator(new StringCustomComparator());
		timestampColumn.setCellFactory(column -> {
			return new TableCell<Process, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(String.valueOf(item));
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
		nameColumn.setComparator(new StringCustomComparator());

		showDetails(null);

		dataTable.getSelectionModel().selectedItemProperty()
				.addListener((observable, oldValue, newValue) -> showDetails(newValue));

		dataTable.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY)
					if (event.getClickCount() == 2) {
						handleEdit();
					}
			}
		});

		dataTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event))
					exportTo();

				if (event.getCode() == KeyCode.DELETE)
					handleDelete();

				if (event.getCode() == KeyCode.ENTER)
					handleEdit();
			}
		});

		initContexMenuDataTable();
	}

	@FXML
	private void handleRefresh(KeyEvent e) {
		if (e.getCode() == KeyCode.F5) {
			logger.info("Refresh mit F5");
			setData();
		}
	}

	public void setMain(Main main) {
		this.main = main;
	}

	public void setData() {

		if (processList != null)
			processList.clear();

		processList = FXCollections.observableArrayList();
		processList.setAll(Service.getInstance().getProcesses());

		dataTable.setItems(FXCollections.observableArrayList(processList));

		overviewButtonsController.setRefreshDate();
		overviewButtonsController.getSearchFiled().setPromptText("Suche... (Station, Name)");

		if (processList.size() == 1)
			lblFoundDatasets.setText(processList.size() + " Datensatz vorhanden");
		else
			lblFoundDatasets.setText(processList.size() + " Datensätze vorhanden");

		filter();

	}

	private void showDetails(Process process) {
		this.process = process;

		if (process != null) {

			String plcTriggerName = null;
			String plcName = null;

			if (process.getPlcTrigger() != null) {
				if (process.getPlcTrigger().getPlc() != null)
					plcName = Service.getInstance().getPlcTrigger(process.getPlcTriggerId()).getPlc().getName();
			}

			String unitName = Service.getInstance().getProcess(process.getId()).getUnit().getSign();

			if (process.getPlcTriggerId() != 0) {
				plcTriggerName = Service.getInstance().getPlcTrigger(process.getPlcTriggerId()).getPlcTriggerInfo();
			}

			valueDecpoints.setText(String.valueOf(process.getDecimalPoints()));
			valueStation.setText(process.getStation());
			valueBezeichnung.setText(process.getName());
			cbSetvalueUsed.setSelected(process.isSetvalueUsed());
			valueSetvalue.setText(
					DecimalPointFormatter.roundFloat2String(process.getSetvalue(), process.getDecimalPoints()));
			lblUnit.setText("[" + unitName + "]");
			valueAnzahlAvg.setText(String.valueOf(process.getNrAvg()));
			valueAnzahlSpcKlassen.setText(String.valueOf(process.getNrSpcClass()));
			valueCpkLoLim1.setText(DecimalPointFormatter.roundFloat2String(process.getCpkLoLim1(), 2));
			valueCpkLoLim1Vis.setText(valueCpkLoLim1.getText());
			valueCpkLoLim2.setText(DecimalPointFormatter.roundFloat2String(process.getCpkLoLim2(), 2));
			valueUnit.setText(unitName);
			valuePlc.setText(plcName);
			valueTrigger.setText(plcTriggerName);

		} else {

			valueDecpoints.setText("");
			valueStation.setText("");
			valueBezeichnung.setText("");
			cbSetvalueUsed.setSelected(false);
			valueSetvalue.setText("");
			lblUnit.setText("[Einheit]");
			valueAnzahlAvg.setText("");
			valueAnzahlSpcKlassen.setText("");
			valueCpkLoLim1.setText("");
			valueCpkLoLim1Vis.setText("");
			valueCpkLoLim2.setText("");
			valueUnit.setText("");
			valuePlc.setText("");
			valueTrigger.setText("");
		}
	}

	private void filter() {

		overviewButtonsController.getSearchFiled().setText("");
		setSearchFeedbacks(-1);

		if (filterListener != null)
			overviewButtonsController.getSearchFiled().textProperty().removeListener(filterListener);

		FilteredList<Process> filteredData = new FilteredList<>(processList, p -> true);

		filterListener = (obs, oldValue, newValue) -> {
			filteredData.setPredicate(process -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (process.getName() != null)
					if (process.getName().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (process.getStation() != null)
					if (process.getStation().toLowerCase().contains(lowerCaseFilter)) {
						return true;
					}

				return false;
			});

			if (newValue == null || newValue.isEmpty())
				setSearchFeedbacks(-1);
			else
				setSearchFeedbacks(filteredData.size());
		};

		overviewButtonsController.getSearchFiled().textProperty().addListener(filterListener);

		SortedList<Process> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(dataTable.comparatorProperty());
		dataTable.setItems(sortedData);
	}

	private void setSearchFeedbacks(int foundDatasets) {

		if (foundDatasets == 1) {
			overviewButtonsController.getfoundValuesLabel().setText(foundDatasets + " Übereinstimmung");
			overviewButtonsController.getSearchFiled().setId("textfield-search-default");
		}
		if (foundDatasets > 1) {
			overviewButtonsController.getfoundValuesLabel().setText(foundDatasets + " Übereinstimmungen");
			overviewButtonsController.getSearchFiled().setId("textfield-search-default");
		}
		if (foundDatasets == 0) {
			overviewButtonsController.getfoundValuesLabel().setText("Keine Übereinstimmungen\ngefunden");
			overviewButtonsController.getSearchFiled().setId("textfield-search-no-match");
		}
		if (foundDatasets < 0) {
			overviewButtonsController.getfoundValuesLabel().setText("");
			overviewButtonsController.getSearchFiled().setId("textfield-search-default");
		}
	}

	private void initContexMenuDataTable() {

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();
		SeparatorMenuItem separator = new SeparatorMenuItem();

		MenuItem miNew = new MenuItem("Neu");
		miNew.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				dataTable.getSelectionModel().clearSelection();
				handleNew();
			}
		});

		MenuItem miEdit = new MenuItem("Bearbeiten");
		miEdit.setAccelerator(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_ANY));
		miEdit.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				handleEdit();
			}
		});

		MenuItem miDelete = new MenuItem("Löschen");
		miDelete.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHORTCUT_ANY));
		miDelete.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				handleDelete();
			}
		});

		MenuItem miExport = new MenuItem("Exportieren");
		miExport.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		miExport.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				exportTo();
			}
		});

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miNew, miEdit, miDelete, separator, miExport);

		// When user right-click on Circle
		dataTable.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				contextMenu.hide();
				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {
					contextMenu.show(dataTable, mouseEvent.getScreenX(), mouseEvent.getScreenY());
				}
			}
		});

	}

	@FXML
	public void handleDelete() {
		Process selectedData = dataTable.getSelectionModel().getSelectedItem();
		if (selectedData != null) {

			logger.info("Abfrage löschen öffnen");

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Löschen");
			alert.setHeaderText("Wollen Sie wirklich löschen?");
			alert.setContentText("Hinweis:\nVorgang kann nicht rückgängig gemacht werden!\n\n");

			DialogPane dialogPane = alert.getDialogPane();
			dialogPane.getStylesheets().addAll(getClass().getResource(Constants.STYLESHEET).toExternalForm());
			Optional<ButtonType> result = alert.showAndWait();

			if (result.get() == ButtonType.OK) {

				logger.info("Abfrage löschen mit OK bestätigt");

				if (Service.getInstance().deleteProcess(selectedData.getId())) {
					setData();
					main.refreshTreeView(selectedData, 2);

				}

			} else {

				logger.info("Abfrage löschen abgebrochen");
			}

		} else {

			NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
			alert.showAndWait();
		}
	}

	@FXML
	public void handleNew() {
		Process newProcess = new Process();
		boolean okClicked = showEditDialog(newProcess);
		if (okClicked) {
			if (Service.getInstance().insertProcess(newProcess)) {
				setData();
				main.refreshTreeView(newProcess, 0);
			}

		}
	}

	@FXML
	public void handleEdit() {
		Process selectedData = dataTable.getSelectionModel().getSelectedItem();
		if (selectedData != null) {
			boolean okClicked = showEditDialog(selectedData);

			if (okClicked) {
				if (Service.getInstance().updateProcess(selectedData)) {
					if (selectedData.getPlcTrigger() != null)
						Service.getInstance().updatePlcTrigger(selectedData.getPlcTrigger());

					showDetails(selectedData);
					main.refreshTreeView(selectedData, 1);
				}
			}

		} else {

			NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
			alert.showAndWait();
		}
	}

	public void setDialogStage(Stage dialogStage) {

		this.dialogStage = dialogStage;

	}

	public boolean showEditDialog(Process process) {
		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/config/process/ProcessEdit.fxml"));
			// loader.setResources(resources);
			AnchorPane pane = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Bearbeiten: Prozess");
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(Constants.STYLESHEET);

			dialogStage.setScene(scene);

			ProcessEditController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setMain(main);
			controller.setData(process);

			dialogStage.showAndWait();

			return controller.isOkClicked();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@FXML
	private void handleClose() {
		this.dialogStage.close();
	}

	public void exportTo() {

		File file = null;

		DateFormat tf = new SimpleDateFormat("HH-mm-ss");
		String timeString = tf.format(Calendar.getInstance().getTime());

		DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		String dateString = df.format(Calendar.getInstance().getTime());

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Exportieren nach");
		FileChooser.ExtensionFilter extFilterCsv = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
		chooser.getExtensionFilters().addAll(extFilterCsv);

		chooser.setInitialFileName("Prozesse-Export" + "-" + dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			writer.write("Prozess Id" + ";" + "Station" + ";" + "Name" + ";" + "Sollwert in Verwendung" + ";"
					+ "Sollwert" + ";" + "Dezimalstellen" + ";" + "Anzahl Werte für Mittelwertbildung" + ";"
					+ "Anzahl SPC Klassen" + ";" + "CPK Untergrenze 1" + ";" + "CPK Untergrenze 2" + ";" + "Einheit Id"
					+ ";" + "PLC Trigger Id" + ";" + "Zeitstempel" + ";" + "\r\n");

			for (int i = 0; i < dataTable.getItems().size(); i++) {
				writer.write(dataTable.getItems().get(i).getId() + ";" + dataTable.getItems().get(i).getStation() + ";"
						+ dataTable.getItems().get(i).getName() + ";" + dataTable.getItems().get(i).isSetvalueUsed()
						+ ";" + String.valueOf(dataTable.getItems().get(i).getSetvalue()).replace(".", ",") + ";"
						+ dataTable.getItems().get(i).getDecimalPoints() + ";" + dataTable.getItems().get(i).getNrAvg()
						+ ";" + dataTable.getItems().get(i).getNrSpcClass() + ";"
						+ String.valueOf(dataTable.getItems().get(i).getCpkLoLim1()).replace(".", ",") + ";"
						+ String.valueOf(dataTable.getItems().get(i).getCpkLoLim2()).replace(".", ",") + ";"
						+ dataTable.getItems().get(i).getUnitId() + ";" + dataTable.getItems().get(i).getPlcTriggerId()
						+ ";" + dataTable.getItems().get(i).getTimestamp() + ";" + "\r\n");
			}

			writer.flush();
			writer.close();

			SaveSucessInfo info = new SaveSucessInfo(dialogStage, file);
			info.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

			logger.error(e);

			FileSaveFailedAlert alert = new FileSaveFailedAlert(dialogStage, file, e);
			alert.showAndWait();

		}
	}
}