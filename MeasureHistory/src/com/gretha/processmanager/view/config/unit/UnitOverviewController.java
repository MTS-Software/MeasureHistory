package com.gretha.processmanager.view.config.unit;

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
import com.gretha.shared.model.Unit;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
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

public class UnitOverviewController implements IOverview {

	private static final Logger logger = Logger.getLogger(UnitOverviewController.class);

	@FXML
	private OverviewButtonsController overviewButtonsController;
	@FXML
	private TableView<Unit> dataTable;
	@FXML
	private TableColumn<Unit, String> idColumn;
	@FXML
	private TableColumn<Unit, String> signColumn;
	@FXML
	private TableColumn<Unit, String> timestampColumn;
	@FXML
	private TextField valueSign;
	@FXML
	private Label lblFoundDatasets;

	private Main main;
	private Stage dialogStage;

	private ObservableList<Unit> unitList;
	private ChangeListener<String> filterListener;

	public UnitOverviewController() {
	}

	@FXML
	private void initialize() {

		valueSign.setDisable(true);

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
			return new TableCell<Unit, String>() {
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

		signColumn.setCellValueFactory(cellData -> cellData.getValue().signProperty());
		signColumn.setComparator(new StringCustomComparator());

		timestampColumn.setCellValueFactory(cellData -> cellData.getValue().timestampProperty());
		timestampColumn.setComparator(new StringCustomComparator());
		timestampColumn.setCellFactory(column -> {
			return new TableCell<Unit, String>() {
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

		if (unitList != null)
			unitList.clear();

		unitList = FXCollections.observableArrayList();
		unitList.setAll(Service.getInstance().getUnits());

		dataTable.setItems(FXCollections.observableArrayList(unitList));

		overviewButtonsController.setRefreshDate();
		overviewButtonsController.getSearchFiled().setPromptText("Suche... (Zeichen)");

		if (unitList.size() == 1)
			lblFoundDatasets.setText(unitList.size() + " Datensatz vorhanden");
		else
			lblFoundDatasets.setText(unitList.size() + " Datensätze vorhanden");

		filter();

	}

	private void showDetails(Unit unit) {
		if (unit != null) {
			valueSign.setText(unit.getSign());

		} else {
			valueSign.setText("");
		}
	}

	private void filter() {

		overviewButtonsController.getSearchFiled().setText("");
		setSearchFeedbacks(-1);

		if (filterListener != null)
			overviewButtonsController.getSearchFiled().textProperty().removeListener(filterListener);

		FilteredList<Unit> filteredData = new FilteredList<>(unitList, p -> true);

		filterListener = (obs, oldValue, newValue) -> {
			filteredData.setPredicate(unit -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (unit.getSign() != null)
					if (unit.getSign().toLowerCase().contains(lowerCaseFilter)) {
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

		SortedList<Unit> sortedData = new SortedList<>(filteredData);
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
		Unit selectedData = dataTable.getSelectionModel().getSelectedItem();
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

				if (Service.getInstance().deleteUnit(selectedData.getId())) {
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
		Unit unit = new Unit();
		boolean okClicked = showEditDialog(unit);
		if (okClicked) {
			if (Service.getInstance().insertUnit(unit))
				setData();
		}
	}

	@FXML
	public void handleEdit() {
		Unit selectedData = dataTable.getSelectionModel().getSelectedItem();
		if (selectedData != null) {
			boolean okClicked = showEditDialog(selectedData);

			if (okClicked) {
				if (Service.getInstance().updateUnit(selectedData)) {
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

	public boolean showEditDialog(Unit unit) {
		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/config/unit/UnitEdit.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Bearbeiten: Einheit");
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());

			dialogStage.setScene(scene);

			UnitEditController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setData(unit);

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

		chooser.setInitialFileName("Unit-Export" + "-" + dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			writer.write("Unit Id" + ";" + "Zeichen" + ";" + "Zeitstempel" + ";" + "\r\n");

			for (int i = 0; i < dataTable.getItems().size(); i++) {
				writer.write(dataTable.getItems().get(i).getId() + ";" + dataTable.getItems().get(i).getSign() + ";"
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