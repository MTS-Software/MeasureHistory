package com.gretha.processmanager.view.config.plc;

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
import com.gretha.shared.model.Plc;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.StringCustomComparator;
import com.gretha.shared.util.WebBrowser;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
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

public class PlcOverviewController implements IOverview {

	private static final Logger logger = Logger.getLogger(PlcOverviewController.class);

	@FXML
	private OverviewButtonsController overviewButtonsController;
	@FXML
	private TableView<Plc> dataTable;
	@FXML
	private TableColumn<Plc, String> idColumn;
	@FXML
	private TableColumn<Plc, String> nameColumn;
	@FXML
	private TableColumn<Plc, String> ipColumn;
	@FXML
	private TableColumn<Plc, String> timestampColumn;
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
	@FXML
	private Label lblFoundDatasets;

	private Main main;
	private Stage dialogStage;

	private ObservableList<Plc> plcList;
	private ChangeListener<String> filterListener;

	public PlcOverviewController() {
	}

	@FXML
	private void initialize() {

		cbType.setItems(Constants.PLC_TYPES);

		valueName.setDisable(true);
		valueIp.setDisable(true);
		valueRack.setDisable(true);
		valueSlot.setDisable(true);
		cbType.setDisable(true);
		valueTimeout.setDisable(true);

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
			return new TableCell<Plc, String>() {
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
		nameColumn.setCellFactory(column -> {
			return new TableCell<Plc, String>() {
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

		ipColumn.setCellValueFactory(cellData -> cellData.getValue().ipProperty());
		ipColumn.setComparator(new StringCustomComparator());
		ipColumn.setCellFactory(column -> {
			return new TableCell<Plc, String>() {
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
			return new TableCell<Plc, String>() {
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

		if (plcList != null)
			plcList.clear();

		plcList = FXCollections.observableArrayList();
		plcList.setAll(Service.getInstance().getPlcs());

		dataTable.setItems(FXCollections.observableArrayList(plcList));

		overviewButtonsController.setRefreshDate();
		overviewButtonsController.getSearchFiled().setPromptText("Suche... (Name, IP-Adresse)");

		if (plcList.size() == 1)
			lblFoundDatasets.setText(plcList.size() + " Datensatz vorhanden");
		else
			lblFoundDatasets.setText(plcList.size() + " Datensätze vorhanden");

		initFilter();

	}

	private void showDetails(Plc plc) {
		if (plc != null) {
			valueName.setText(plc.getName());
			valueIp.setText(plc.getIp());
			valueRack.setText(String.valueOf(plc.getRack()));
			valueSlot.setText(String.valueOf(plc.getSlot()));
			cbType.getSelectionModel().select(plc.getType());
			valueTimeout.setText(String.valueOf(plc.getTimeout()));

		} else {
			valueName.setText("");
			valueIp.setText("");
			valueRack.setText("");
			valueSlot.setText("");
			cbType.getSelectionModel().select("");
			valueTimeout.setText("");
		}
	}

	private void initFilter() {

		overviewButtonsController.getSearchFiled().setText("");
		setSearchFeedbacks(-1);

		if (filterListener != null)
			overviewButtonsController.getSearchFiled().textProperty().removeListener(filterListener);

		FilteredList<Plc> filteredData = new FilteredList<>(plcList, p -> true);

		filterListener = (obs, oldValue, newValue) -> {
			filteredData.setPredicate(plc -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;

				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (plc.getName() != null)
					if (plc.getName().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (plc.getIp() != null)
					if (plc.getIp().toLowerCase().contains(lowerCaseFilter)) {
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

		SortedList<Plc> sortedData = new SortedList<>(filteredData);
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

		Menu mPlcInfo = new Menu("Webinterface");

		MenuItem miShowPlcInfoinJavaBrowser = new MenuItem("Öffnen mit Javabrowser");
		miShowPlcInfoinJavaBrowser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Plc selectedData = dataTable.getSelectionModel().getSelectedItem();
				if (selectedData != null) {

					WebBrowser.openURLinJavaBrowser("http://" + selectedData.getIp() + "/");

				} else {

					NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
					alert.showAndWait();
				}
			}
		});

		MenuItem miShowPlcInfoinStandardBrowser = new MenuItem("Öffnen mit Standardbrowser");
		miShowPlcInfoinStandardBrowser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Plc selectedData = dataTable.getSelectionModel().getSelectedItem();
				if (selectedData != null) {

					WebBrowser.openURLinStandardBrowser(selectedData.getIp());

				} else {

					NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
					alert.showAndWait();
				}
			}
		});

		mPlcInfo.getItems().addAll(miShowPlcInfoinStandardBrowser, miShowPlcInfoinJavaBrowser);

		MenuItem miExport = new MenuItem("Exportieren");
		miExport.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		miExport.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				exportTo();
			}
		});

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miNew, miEdit, miDelete, separator, mPlcInfo, miExport);

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
		Plc selectedData = dataTable.getSelectionModel().getSelectedItem();
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

				if (Service.getInstance().deletePlc(selectedData.getId())) {
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
		Plc plc = new Plc();
		boolean okClicked = showEditDialog(plc);
		if (okClicked) {
			if (Service.getInstance().insertPlc(plc))
				setData();
		}
	}

	@FXML
	public void handleEdit() {
		Plc selectedData = dataTable.getSelectionModel().getSelectedItem();
		if (selectedData != null) {
			boolean okClicked = showEditDialog(selectedData);

			if (okClicked) {
				if (Service.getInstance().updatePlc(selectedData)) {
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

	public boolean showEditDialog(Plc plc) {
		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/config/plc/PlcEdit.fxml"));
			AnchorPane page = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Bearbeiten: Einheit");
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(page);
			scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());

			dialogStage.setScene(scene);

			PlcEditController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setData(plc);

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

		chooser.setInitialFileName("PLC-Export" + "-" + dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			writer.write("PLC Id" + ";" + "Name" + ";" + "IP-Adresse" + ";" + "Rack" + ";" + "Slot" + ";" + "Type" + ";"
					+ "Timeout" + ";" + "Zeitstempel" + ";" + "\r\n");

			for (int i = 0; i < dataTable.getItems().size(); i++) {
				writer.write(dataTable.getItems().get(i).getId() + ";" + dataTable.getItems().get(i).getName() + ";"
						+ dataTable.getItems().get(i).getIp() + ";" + dataTable.getItems().get(i).getRack() + ";"
						+ dataTable.getItems().get(i).getSlot() + ";" + dataTable.getItems().get(i).getType() + ";"
						+ dataTable.getItems().get(i).getTimeout() + ";" + dataTable.getItems().get(i).getTimestamp()
						+ ";" + "\r\n");
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