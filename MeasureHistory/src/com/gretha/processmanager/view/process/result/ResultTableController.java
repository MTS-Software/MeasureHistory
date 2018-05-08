package com.gretha.processmanager.view.process.result;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Result;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.LoginLogout;
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
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
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

public class ResultTableController implements Initializable {

	private static final Logger logger = Logger.getLogger(ResultTableController.class);
	private ResourceBundle resources;
	private Stage dialogStage;
	private RootPaneController rootPaneController;

	@FXML
	private MenuItem btnBeenden;
	@FXML
	private MenuItem btnOpenConfig;
	@FXML
	private MenuItem btnOpenInfo;
	@FXML
	private TableView<Result> tblResult;
	@FXML
	private TableColumn<Result, Float> colValue;
	@FXML
	private TableColumn<Result, Float> colLoLim;
	@FXML
	private TableColumn<Result, Float> colUpLim;
	@FXML
	private TableColumn<Result, Integer> colState;
	@FXML
	private TableColumn<Result, String> colSerial;
	@FXML
	private TableColumn<Result, String> colWtNr;
	@FXML
	private TableColumn<Result, String> colRemark;
	@FXML
	private TableColumn<Result, String> colTimestamp;
	@FXML
	private TableColumn<Result, Integer> colTypNr;
	@FXML
	private TableColumn<Result, Integer> colNr;
	@FXML
	private Button btnOpnTableFilter;

	private ObservableList<Result> resultList;
	private ChangeListener<String> filterListener;

	public ResultTableController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		initTable();
		initContextMenu();

	}

	public void initTable() {

		colNr.setCellValueFactory(new PropertyValueFactory<Result, Integer>("nr"));
		colNr.setCellFactory(column -> {
			return new TableCell<Result, Integer>() {
				@Override
				protected void updateItem(Integer item, boolean empty) {
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

		colValue.setCellValueFactory(new PropertyValueFactory<Result, Float>("value"));
		colValue.setCellFactory(column -> {
			return new TableCell<Result, Float>() {

				@Override
				protected void updateItem(Float item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(DecimalPointFormatter.roundFloat2String(item,
								rootPaneController.getProcess().getDecimalPoints()) + " "
								+ rootPaneController.getProcess().getUnit().getSign());
					}
				}
			};
		});

		colLoLim.setCellValueFactory(new PropertyValueFactory<Result, Float>("loLim"));
		colLoLim.setCellFactory(column -> {
			return new TableCell<Result, Float>() {

				@Override
				protected void updateItem(Float item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(DecimalPointFormatter.roundFloat2String(item,
								rootPaneController.getProcess().getDecimalPoints()) + " "
								+ rootPaneController.getProcess().getUnit().getSign());
					}
				}
			};
		});

		colUpLim.setCellValueFactory(new PropertyValueFactory<Result, Float>("upLim"));
		colUpLim.setCellFactory(column -> {
			return new TableCell<Result, Float>() {
				@Override
				protected void updateItem(Float item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(DecimalPointFormatter.roundFloat2String(item,
								rootPaneController.getProcess().getDecimalPoints()) + " "
								+ rootPaneController.getProcess().getUnit().getSign());
					}
				}
			};
		});

		colState.setCellValueFactory(new PropertyValueFactory<Result, Integer>("state"));
		colState.setCellFactory(column -> {
			return new TableCell<Result, Integer>() {
				@Override
				protected void updateItem(Integer item, boolean empty) {
					super.updateItem(item, empty);

					TableRow<?> currentRow = getTableRow();

					if (item == null || empty) {
						setText(null);
						setStyle("");
						currentRow.setStyle(null);
					} else {

						if (item == 0) {
							setText(resources.getString("na"));
							currentRow.setStyle("-fx-background-color:lightgrey");
						}
						if (item == 1) {
							setText(resources.getString("nok"));
							currentRow.setStyle("-fx-background-color:lightcoral");
						}
						if (item == 2) {
							setText(resources.getString("ok"));
							currentRow.setStyle("-fx-background-color:lightgreen");
						}

						if (item < 0 || item > 2) {
							setText(resources.getString("undef") + " -> " + item);
							currentRow.setStyle("-fx-background-color:yellow");
						}

					}
				}
			};
		});

		colSerial.setCellValueFactory(cellData -> cellData.getValue().serialProperty());
		colSerial.setComparator(new StringCustomComparator());
		colSerial.setCellFactory(column -> {
			return new TableCell<Result, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item);
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		colTypNr.setCellValueFactory(new PropertyValueFactory<Result, Integer>("typId"));
		colTypNr.setCellFactory(column -> {
			return new TableCell<Result, Integer>() {
				@Override
				protected void updateItem(Integer item, boolean empty) {
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

		colWtNr.setCellValueFactory(cellData -> cellData.getValue().wtProperty());
		colWtNr.setComparator(new StringCustomComparator());
		colWtNr.setCellFactory(column -> {
			return new TableCell<Result, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item);
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		colRemark.setCellValueFactory(cellData -> cellData.getValue().remarkProperty());
		colRemark.setComparator(new StringCustomComparator());
		colRemark.setCellFactory(column -> {
			return new TableCell<Result, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item);
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		colTimestamp.setCellValueFactory(new PropertyValueFactory<Result, String>("timestamp"));
		colTimestamp.setComparator(new StringCustomComparator());
		colTimestamp.setCellFactory(column -> {
			return new TableCell<Result, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item);
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		// Auto resize columns
		tblResult.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tblResult.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		tblResult.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event))
					exportTo();

				if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event))
					rootPaneController.getRootPaneBarController().handleFilterSettings();

				if (event.getCode() == KeyCode.DELETE)
					delteResult();

				if (event.getCode() == KeyCode.ENTER)
					showResultTableEdit();

			}
		});

		tblResult.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getClickCount() == 2) {
					showResultTableEdit();
				}
			}
		});

	}

	private void initContextMenu() {

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem miBearbeiten = new MenuItem("Bearbeiten");
		miBearbeiten.setAccelerator(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_ANY));
		miBearbeiten.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				showResultTableEdit();
			}
		});

		MenuItem miLöschen = new MenuItem("Löschen");
		miLöschen.setAccelerator(new KeyCodeCombination(KeyCode.DELETE, KeyCombination.SHORTCUT_ANY));
		miLöschen.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				delteResult();
			}
		});

		MenuItem miFilter = new MenuItem("Filter");
		miFilter.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		miFilter.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				rootPaneController.getRootPaneBarController().handleFilterSettings();
			}
		});

		MenuItem miFilterReset = new MenuItem("Filter rücksetzen");
		miFilterReset.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				rootPaneController.getRootPaneBarController().handleFilterReset();
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
		contextMenu.getItems().addAll(miBearbeiten, miLöschen, miFilter, miFilterReset, new SeparatorMenuItem(),
				miExport);

		// When user right-click on Circle
		tblResult.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {

				contextMenu.hide();

				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {

					if (rootPaneController.getRootPaneBarController().getFilter() != null) {
						if (rootPaneController.getRootPaneBarController().getFilter().isActivated())
							miFilterReset.setDisable(false);
					} else
						miFilterReset.setDisable(true);

					contextMenu.show(tblResult, mouseEvent.getScreenX(), mouseEvent.getScreenY());

				}
			}
		});

	}

	public void delteResult() {

		Result selectedData = tblResult.getSelectionModel().getSelectedItem();

		if (selectedData != null) {

			if (LoginLogout.login(LoginLogout.EUserLevels.USER)) {

				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Löschen");
				alert.setHeaderText("Wollen Sie wirklich löschen?");
				alert.setContentText("Hinweis:\nVorgang kann nicht rückgängig gemacht werden!\n\n");

				DialogPane dialogPane = alert.getDialogPane();
				dialogPane.getStylesheets().addAll(Constants.STYLESHEET);
				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() == ButtonType.OK) {

					ObservableList<Result> selectedItems = tblResult.getSelectionModel().getSelectedItems();
					for (Result item : selectedItems) {
						Service.getInstance().deleteResult(item);

					}
					// Abfrage ob Upate erfolgreich und Übergabe an Returnwert
					if (!Service.getInstance().isErrorStatus()) {
						resultList.removeAll(selectedItems);
						tblResult.setItems(FXCollections.observableArrayList(resultList));
						tblResult.getSelectionModel().clearSelection();
						tblResult.refresh();
						filter();
					}
				}
			}
		}

		else {

			NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
			alert.showAndWait();

		}
	}

	private void showResultTableEdit() {

		Result selectedData = tblResult.getSelectionModel().getSelectedItem();

		if (selectedData != null) {

			if (LoginLogout.login(LoginLogout.EUserLevels.USER)) {

				try {
					// Load the fxml file and create a new stage for the popup
					// dialog.
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(Main.class.getResource("view/process/result/ResultTableEdit.fxml"));
					loader.setResources(resources);
					AnchorPane pane = (AnchorPane) loader.load();

					// Create the dialog Stage.
					Stage dialogStage = new Stage();
					dialogStage.getIcons().addAll(this.dialogStage.getIcons());
					dialogStage.setTitle(rootPaneController.getProcess().getStation() + ": "
							+ rootPaneController.getProcess().getName() + " - " + resources.getString("edit_result")
							+ " - Nr.: " + selectedData.getNr());
					dialogStage.setResizable(false);
					dialogStage.initModality(Modality.WINDOW_MODAL);
					dialogStage.initOwner(this.dialogStage);

					Scene scene = new Scene(pane);
					scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());
					dialogStage.setScene(scene);

					ResultTableEditController controller = loader.getController();
					controller.setRootPaneController(rootPaneController);
					controller.setDialogStage(dialogStage);
					controller.setData(selectedData);

					// Show the dialog and wait until the user closes it
					dialogStage.showAndWait();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		else {

			NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
			alert.showAndWait();
		}
	}

	public void setData() {

		this.dialogStage.setTitle(
				rootPaneController.getProcess().getStation() + ": " + rootPaneController.getProcess().getName());

		if (resultList != null)
			resultList.clear();

		resultList = FXCollections.observableArrayList();
		resultList.setAll(rootPaneController.getResults());

		tblResult.setItems(FXCollections.observableArrayList(resultList));

		filter();
		initTable();

	}

	private void filter() {

		rootPaneController.getRootPaneBarController().getSearchFiled().setText("");
		setSearchFeedbacks(-1);

		if (filterListener != null)
			rootPaneController.getRootPaneBarController().getSearchFiled().textProperty()
					.removeListener(filterListener);

		FilteredList<Result> filteredData = new FilteredList<>(resultList, p -> true);

		filterListener = (obs, oldValue, newValue) -> {
			filteredData.setPredicate(result -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (result.getRemark() != null)
					if (result.getRemark().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (result.getSerial() != null)
					if (result.getSerial().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				if (String.valueOf(result.getValue()).contains(lowerCaseFilter)) {
					return true;
				}

				if (result.getTimestamp() != null)
					if (result.getTimestamp().toLowerCase().contains(lowerCaseFilter)) {
						return true;

					}

				return false;
			});

			if (newValue == null || newValue.isEmpty())
				setSearchFeedbacks(-1);
			else
				setSearchFeedbacks(filteredData.size());
		};

		rootPaneController.getRootPaneBarController().getSearchFiled().textProperty().addListener(filterListener);

		SortedList<Result> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(tblResult.comparatorProperty());
		tblResult.setItems(sortedData);
	}

	private void setSearchFeedbacks(int foundDatasets) {

		if (foundDatasets == 1) {
			rootPaneController.getRootPaneBarController().getFoundValuesLabel()
					.setText(foundDatasets + " Übereinstimmung");
			rootPaneController.getRootPaneBarController().getSearchFiled().setId("textfield-search-default");
		}
		if (foundDatasets > 1) {
			rootPaneController.getRootPaneBarController().getFoundValuesLabel()
					.setText(foundDatasets + " Übereinstimmungen");
			rootPaneController.getRootPaneBarController().getSearchFiled().setId("textfield-search-default");
		}
		if (foundDatasets == 0) {
			rootPaneController.getRootPaneBarController().getFoundValuesLabel()
					.setText("Keine Übereinstimmungen\ngefunden");
			rootPaneController.getRootPaneBarController().getSearchFiled().setId("textfield-search-no-match");
		}
		if (foundDatasets < 0) {
			rootPaneController.getRootPaneBarController().getFoundValuesLabel().setText("");
			rootPaneController.getRootPaneBarController().getSearchFiled().setId("textfield-search-default");
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

		chooser.setInitialFileName(
				rootPaneController.getProcess().getStation() + "-" + rootPaneController.getProcess().getName()
						+ "-Ergebnisse-Export-" + dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			writer.write(
					colNr.getText() + ";" + colValue.getText() + ";" + colLoLim.getText() + ";" + colUpLim.getText()
							+ ";" + colState.getText() + ";" + colSerial.getText() + ";" + colTypNr.getText() + ";"
							+ colWtNr.getText() + ";" + colRemark.getText() + ";" + colTimestamp.getText() + "\n");

			for (Result result : tblResult.getItems()) {

				String text1 = result.getNr() + ";" + result.getValue() + ";" + result.getLoLim() + ";"
						+ result.getUpLim() + ";";
				String text2 = result.getState() + ";" + result.getSerial() + ";" + result.getTypId() + ";"
						+ result.getWt() + ";" + result.getRemark() + ";" + result.getTimestamp() + "\n";

				writer.write(text1.replace(".", ",") + text2);
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

	public void setMain(Main main) {

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	public void setRootPaneController(RootPaneController rootPaneController) {
		this.rootPaneController = rootPaneController;
	}
}
