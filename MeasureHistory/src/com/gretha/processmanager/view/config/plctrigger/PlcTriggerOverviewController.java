package com.gretha.processmanager.view.config.plctrigger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Optional;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.config.root.IOverview;
import com.gretha.processmanager.view.config.root.OverviewButtonsController;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Plc;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.util.Constants;
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
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PlcTriggerOverviewController implements IOverview {

	private static final Logger logger = Logger.getLogger(PlcTriggerOverviewController.class);

	@FXML
	private OverviewButtonsController overviewButtonsController;
	@FXML
	private TableView<PlcTrigger> dataTable;
	@FXML
	private TableColumn<PlcTrigger, String> idColumn;
	@FXML
	private TableColumn<PlcTrigger, String> dbColumn;
	@FXML
	private TableColumn<PlcTrigger, String> strtadrColumn;
	@FXML
	private TableColumn<PlcTrigger, String> stationColumn;
	@FXML
	private TableColumn<PlcTrigger, String> processColumn;
	@FXML
	private TableColumn<PlcTrigger, Boolean> activatedColumn;
	@FXML
	private TableColumn<PlcTrigger, Plc> plcColumn;
	@FXML
	private TableColumn<PlcTrigger, String> timestampColumn;
	@FXML
	private Label lblUsedForProcess;
	@FXML
	private TextField valueDb;
	@FXML
	private TextField valueStrtAdr;
	@FXML
	private CheckBox cbActivated;
	@FXML
	private TextField valuePlc;
	@FXML
	private Button plcOverviewButton;
	@FXML
	private Label lblFoundDatasets;

	private Main main;
	private Stage dialogStage;
	private PlcTrigger plcTrigger;

	private ObservableList<PlcTrigger> plctriggerList;
	private ChangeListener<String> filterListener;

	public PlcTriggerOverviewController() {
	}

	@FXML
	private void initialize() {

		valueDb.setDisable(true);
		valueStrtAdr.setDisable(true);
		cbActivated.setDisable(true);
		valuePlc.setDisable(true);

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

		plcOverviewButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				main.showPlcOverviewDialog();
				showDetails(plcTrigger);

			}
		});

		idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asString());
		idColumn.setComparator(new StringCustomComparator());
		idColumn.setCellFactory(column -> {
			return new TableCell<PlcTrigger, String>() {
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

		dbColumn.setCellValueFactory(cellData -> cellData.getValue().dbProperty().asString());
		dbColumn.setComparator(new StringCustomComparator());
		dbColumn.setCellFactory(column -> {
			return new TableCell<PlcTrigger, String>() {
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

		strtadrColumn.setCellValueFactory(cellData -> cellData.getValue().strtAdrProperty().asString());
		strtadrColumn.setComparator(new StringCustomComparator());
		strtadrColumn.setCellFactory(column -> {
			return new TableCell<PlcTrigger, String>() {
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

		stationColumn.setCellValueFactory(cellData -> cellData.getValue().processStationProperty());
		stationColumn.setComparator(new StringCustomComparator());
		stationColumn.setCellFactory(column -> {
			return new TableCell<PlcTrigger, String>() {
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

		processColumn.setCellValueFactory(cellData -> cellData.getValue().processNameProperty());
		processColumn.setComparator(new StringCustomComparator());

		plcColumn.setCellValueFactory(new PropertyValueFactory<PlcTrigger, Plc>("plc"));
		plcColumn.setComparator(new Comparator<Plc>() {
			@Override
			public int compare(Plc o1, Plc o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				String i1 = o1.getName();
				String i2 = o2.getName();

				return i1.toLowerCase().compareTo(i2.toLowerCase());
			}

		});
		plcColumn.setCellFactory(column -> {
			return new TableCell<PlcTrigger, Plc>() {
				@Override
				protected void updateItem(Plc item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item.getName());
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		activatedColumn.setCellValueFactory(new PropertyValueFactory<PlcTrigger, Boolean>("activated"));
		activatedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(activatedColumn));

		timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());
		timestampColumn.setComparator(new StringCustomComparator());
		timestampColumn.setCellFactory(column -> {
			return new TableCell<PlcTrigger, String>() {
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

		if (plctriggerList != null)
			plctriggerList.clear();

		plctriggerList = FXCollections.observableArrayList();
		plctriggerList.setAll(Service.getInstance().getPlcTriggers());

		dataTable.setItems(FXCollections.observableArrayList(plctriggerList));

		overviewButtonsController.setRefreshDate();
		overviewButtonsController.getSearchFiled().setPromptText("Suche... (DB, Adresse, Steuerung, Station, Name)");

		if (plctriggerList.size() == 1)
			lblFoundDatasets.setText(plctriggerList.size() + " Datensatz vorhanden");
		else
			lblFoundDatasets.setText(plctriggerList.size() + " Datensätze vorhanden");

		filter();

	}

	private void showDetails(PlcTrigger plcTrigger) {
		this.plcTrigger = plcTrigger;

		if (plcTrigger != null) {

			String plcName = Service.getInstance().getPlc(plcTrigger.getPlcId()).getName();

			if (plcTrigger.getProcess() != null) {
				lblUsedForProcess
						.setText(plcTrigger.getProcess().getStation() + " " + plcTrigger.getProcess().getName());
			} else {
				lblUsedForProcess.setText("Trigger wird von keinem Prozess verwendet");
			}

			valueDb.setText(String.valueOf(plcTrigger.getDb()));
			valueStrtAdr.setText(String.valueOf(plcTrigger.getStrtAdr()));
			cbActivated.setSelected(plcTrigger.isActivated());
			valuePlc.setText(plcName);

		} else {
			lblUsedForProcess.setText("");
			valueDb.setText("");
			valueStrtAdr.setText("");
			cbActivated.setSelected(false);
			valuePlc.setText("");
		}
	}

	private void filter() {

		overviewButtonsController.getSearchFiled().setText("");
		setSearchFeedbacks(-1);

		if (filterListener != null)
			overviewButtonsController.getSearchFiled().textProperty().removeListener(filterListener);

		FilteredList<PlcTrigger> filteredData = new FilteredList<>(plctriggerList, p -> true);

		filterListener = (obs, oldValue, newValue) -> {
			filteredData.setPredicate(plctrigger -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (plctrigger.getProcess() != null) {
					if (plctrigger.getProcess().getName() != null)
						if (plctrigger.getProcess().getName().toLowerCase().contains(lowerCaseFilter)) {
							return true;

						}

					if (plctrigger.getProcess().getStation() != null)
						if (plctrigger.getProcess().getStation().toLowerCase().contains(lowerCaseFilter)) {
							return true;

						}
				}

				if (plctrigger.getPlc() != null) {
					if (plctrigger.getPlc().getName() != null)
						if (plctrigger.getPlc().getName().toLowerCase().contains(lowerCaseFilter)) {
							return true;

						}

					if (plctrigger.getProcess().getStation() != null)
						if (plctrigger.getProcess().getStation().toLowerCase().contains(lowerCaseFilter)) {
							return true;

						}
				}

				if (String.valueOf(plctrigger.getDb()).contains(lowerCaseFilter)) {
					return true;
				}

				if (String.valueOf(plctrigger.getStrtAdr()).contains(lowerCaseFilter)) {
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

		SortedList<PlcTrigger> sortedData = new SortedList<>(filteredData);
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
		PlcTrigger selectedData = dataTable.getSelectionModel().getSelectedItem();
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

				if (Service.getInstance().deletePlcTrigger(selectedData.getId())) {
					setData();
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
		PlcTrigger plctrigger = new PlcTrigger();
		boolean okClicked = showEditDialog(plctrigger);
		if (okClicked) {
			if (Service.getInstance().insertPlcTrigger(plctrigger))
				setData();
		}
	}

	@FXML
	public void handleEdit() {
		PlcTrigger selectedData = dataTable.getSelectionModel().getSelectedItem();
		if (selectedData != null) {
			boolean okClicked = showEditDialog(selectedData);

			if (okClicked) {
				if (Service.getInstance().updatePlcTrigger(selectedData)) {
					showDetails(selectedData);
				}
			}

		} else {

			NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
			alert.showAndWait();
		}
	}

	@FXML
	private void handleClose() {
		this.dialogStage.close();
	}

	public void setDialogStage(Stage dialogStage) {

		this.dialogStage = dialogStage;

	}

	public boolean showEditDialog(PlcTrigger plctrigger) {
		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/config/plctrigger/PlcTriggerEdit.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Bearbeiten: Trigger");
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());

			dialogStage.setScene(scene);

			PlcTriggerEditController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setMain(main);
			controller.setData(plctrigger);

			dialogStage.showAndWait();

			return controller.isOkClicked();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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

		chooser.setInitialFileName("PLC-Trigger-Export" + "-" + dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			writer.write("PLC Trigger Id" + ";" + "DB-Nummer" + ";" + "Startadresse" + ";" + "Aktiv" + ";" + "Steuerung"
					+ ";" + "Station" + ";" + "Prozessname" + ";" + "Zeitstempel" + ";" + "\r\n");

			for (int i = 0; i < dataTable.getItems().size(); i++) {
				if (dataTable.getItems().get(i).getProcess() != null) {
					writer.write(dataTable.getItems().get(i).getId() + ";" + dataTable.getItems().get(i).getDb() + ";"
							+ dataTable.getItems().get(i).getStrtAdr() + ";" + dataTable.getItems().get(i).isActivated()
							+ ";" + dataTable.getItems().get(i).getPlc().getName() + ";"
							+ dataTable.getItems().get(i).getProcess().getStation() + ";"
							+ dataTable.getItems().get(i).getProcess().getName() + ";"
							+ dataTable.getItems().get(i).getTimestamp() + ";" + "\r\n");
				} else {
					writer.write(dataTable.getItems().get(i).getId() + ";" + dataTable.getItems().get(i).getDb() + ";"
							+ dataTable.getItems().get(i).getStrtAdr() + ";" + dataTable.getItems().get(i).isActivated()
							+ ";" + "" + ";" + "" + ";" + dataTable.getItems().get(i).getTimestamp() + ";" + "\r\n");

				}
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