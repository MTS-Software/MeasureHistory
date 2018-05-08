package com.gretha.processmanager.view.config.settings.shift;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.config.root.IOverview;
import com.gretha.processmanager.view.config.root.OverviewButtonsController;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Settings;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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

public class ShiftOverviewController implements IOverview {

	private static final Logger logger = Logger.getLogger(ShiftOverviewController.class);

	@FXML
	private OverviewButtonsController overviewButtonsController;
	@FXML
	private TableView<Settings> dataTable;
	@FXML
	private TableColumn<Settings, String> idColumn;
	@FXML
	private TableColumn<Settings, String> shift1StartTimeColumn;
	@FXML
	private TableColumn<Settings, String> shift1EndTimeColumn;
	@FXML
	private TableColumn<Settings, String> shift2StartTimeColumn;
	@FXML
	private TableColumn<Settings, String> shift2EndTimeColumn;
	@FXML
	private TableColumn<Settings, String> shift3StartTimeColumn;
	@FXML
	private TableColumn<Settings, String> shift3EndTimeColumn;
	@FXML
	private TableColumn<Settings, String> timestampColumn;
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
	@FXML
	private Label lblFoundDatasets;

	private Main main;
	private Stage dialogStage;

	private ObservableList<Settings> settingsList;
	private ChangeListener<String> filterListener;

	public ShiftOverviewController() {
	}

	@FXML
	private void initialize() {

		valueShift1StartTime.setDisable(true);
		valueShift1EndTime.setDisable(true);
		valueShift2StartTime.setDisable(true);
		valueShift2EndTime.setDisable(true);
		valueShift3StartTime.setDisable(true);
		valueShift3EndTime.setDisable(true);

		overviewButtonsController.getDeleteButton().setDisable(true);
		overviewButtonsController.getNewButton().setDisable(true);

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

		idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty().asString());
		idColumn.setComparator(new StringCustomComparator());
		idColumn.setCellFactory(column -> {
			return new TableCell<Settings, String>() {
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

		shift1StartTimeColumn.setCellValueFactory(cellData -> cellData.getValue().shift1StartTimeProperty());
		shift1StartTimeColumn.setComparator(new StringCustomComparator());
		shift1StartTimeColumn.setCellFactory(column -> {
			return new TableCell<Settings, String>() {
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

		shift1EndTimeColumn.setCellValueFactory(cellData -> cellData.getValue().shift1EndTimeProperty());
		shift1EndTimeColumn.setComparator(new StringCustomComparator());
		shift1EndTimeColumn.setCellFactory(column -> {
			return new TableCell<Settings, String>() {
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

		shift2StartTimeColumn.setCellValueFactory(cellData -> cellData.getValue().shift2StartTimeProperty());
		shift2StartTimeColumn.setComparator(new StringCustomComparator());
		shift2StartTimeColumn.setCellFactory(column -> {
			return new TableCell<Settings, String>() {
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

		shift2EndTimeColumn.setCellValueFactory(cellData -> cellData.getValue().shift2EndTimeProperty());
		shift2EndTimeColumn.setComparator(new StringCustomComparator());
		shift2EndTimeColumn.setCellFactory(column -> {
			return new TableCell<Settings, String>() {
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

		shift3StartTimeColumn.setCellValueFactory(cellData -> cellData.getValue().shift3StartTimeProperty());
		shift3StartTimeColumn.setComparator(new StringCustomComparator());
		shift3StartTimeColumn.setCellFactory(column -> {
			return new TableCell<Settings, String>() {
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

		shift3EndTimeColumn.setCellValueFactory(cellData -> cellData.getValue().shift3EndTimeProperty());
		shift3EndTimeColumn.setComparator(new StringCustomComparator());
		shift3EndTimeColumn.setCellFactory(column -> {
			return new TableCell<Settings, String>() {
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
			return new TableCell<Settings, String>() {
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

		if (settingsList != null)
			settingsList.clear();

		settingsList = FXCollections.observableArrayList();
		settingsList.setAll(Service.getInstance().getSettings());

		lblFoundDatasets.setText(String.format("%02d Datensätze vorhanden", settingsList.size()));

		overviewButtonsController.setRefreshDate();
		overviewButtonsController.getSearchFiled().setPromptText("Suche... (Zeiten)");

		if (settingsList.size() == 1)
			lblFoundDatasets.setText(settingsList.size() + " Datensatz vorhanden");
		else
			lblFoundDatasets.setText(settingsList.size() + " Datensätze vorhanden");

		filter();

	}

	private void showDetails(Settings settings) {
		if (settings != null) {
			valueShift1StartTime.setText(settings.getShift1StartTime());
			valueShift1EndTime.setText(settings.getShift1EndTime());
			valueShift2StartTime.setText(settings.getShift2StartTime());
			valueShift2EndTime.setText(settings.getShift2EndTime());
			valueShift3StartTime.setText(settings.getShift3StartTime());
			valueShift3EndTime.setText(settings.getShift3EndTime());

		} else {
			valueShift1StartTime.setText("");
			valueShift1EndTime.setText("");
			valueShift2StartTime.setText("");
			valueShift2EndTime.setText("");
			valueShift3StartTime.setText("");
			valueShift3EndTime.setText("");
		}
	}

	private void filter() {

		overviewButtonsController.getSearchFiled().setText("");
		setSearchFeedbacks(-1);

		if (filterListener != null)
			overviewButtonsController.getSearchFiled().textProperty().removeListener(filterListener);

		FilteredList<Settings> filteredData = new FilteredList<>(settingsList, p -> true);

		filterListener = (obs, oldValue, newValue) -> {
			filteredData.setPredicate(settings -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (settings.getShift1StartTime() != null)
					if (settings.getShift1StartTime().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (settings.getShift1EndTime() != null)
					if (settings.getShift1EndTime().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (settings.getShift2StartTime() != null)
					if (settings.getShift2StartTime().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (settings.getShift2EndTime() != null)
					if (settings.getShift2EndTime().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (settings.getShift3StartTime() != null)
					if (settings.getShift3StartTime().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (settings.getShift3EndTime() != null)
					if (settings.getShift3EndTime().toLowerCase().contains(lowerCaseFilter)) {
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

		SortedList<Settings> sortedData = new SortedList<>(filteredData);
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
		miNew.setDisable(true);
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
		miDelete.setDisable(true);
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

		// When user right-click
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

	}

	@FXML
	public void handleNew() {

	}

	@FXML
	public void handleEdit() {
		Settings selectedData = dataTable.getSelectionModel().getSelectedItem();
		if (selectedData != null) {
			boolean okClicked = showEditDialog(selectedData);

			if (okClicked) {
				if (Service.getInstance().updateSettings(selectedData)) {
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

	public boolean showEditDialog(Settings settings) {
		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/config/settings/shift/ShiftEdit.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Bearbeiten: Schichtzeiten");
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());

			dialogStage.setScene(scene);

			ShiftEditController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setData(settings);

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

		chooser.setInitialFileName("Shiftsettings-Export" + "-" + dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			writer.write("Settings Id" + ";" + "Schicht 1 Startzeit" + ";" + "Schicht 1 Endzeit" + ";"
					+ "Schicht 2 Startzeit" + ";" + "Schicht 2 Endzeit" + ";" + "Schicht 3 Startzeit" + ";"
					+ "Schicht 3 Endzeit" + ";" + "Zeitstempel" + ";" + "\r\n");

			for (int i = 0; i < dataTable.getItems().size(); i++) {
				writer.write(
						dataTable.getItems().get(i).getId() + ";" + dataTable.getItems().get(i).getShift1StartTime()
								+ ";" + dataTable.getItems().get(i).getShift1EndTime() + ";"
								+ dataTable.getItems().get(i).getShift2StartTime() + ";"
								+ dataTable.getItems().get(i).getShift2EndTime() + ";"
								+ dataTable.getItems().get(i).getShift3StartTime() + ";"
								+ dataTable.getItems().get(i).getShift3EndTime() + ";"
								+ dataTable.getItems().get(i).getTimestamp() + ";" + "\r\n");
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