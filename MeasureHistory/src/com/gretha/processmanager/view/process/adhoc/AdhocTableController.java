package com.gretha.processmanager.view.process.adhoc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.filter.FilterSettingsController;
import com.gretha.processmanager.view.process.filter.FilterSettingsController.ESortierung;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.AdhocResult;
import com.gretha.shared.model.Filter;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Result;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.view.alert.FileLoadFailedAlert;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.info.AdhocTableMaxSizeReached;
import com.gretha.shared.view.info.LoadSucessInfo;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdhocTableController implements Initializable {

	private static final Logger logger = Logger.getLogger(AdhocTableController.class);
	private ResourceBundle resources;
	private Stage dialogStage;
	private Main main;
	private FilterSettingsController filterSettingsController;

	@FXML
	private Button btnOpnTableFilter;
	@FXML
	private Button btnResetFilter;
	@FXML
	private Button btnMergeResults;
	@FXML
	private Button btnShowChart;
	@FXML
	private Button btnConfigSave;
	@FXML
	private TableView<AdhocResult> table;
	@FXML
	private Label mergedResultsLabel;

	private Filter filter;

	private ObservableList<List<Result>> resultsList = FXCollections.observableArrayList();
	private ObservableList<Process> processList = FXCollections.observableArrayList();
	private ObservableList<AdhocResult> adhocResultsList;
	private Map<String, Integer> duplicateSerialMap;

	private static final String fieldDelimiter = ";";
	private static final int processColumns = 10;

	// Maximale Anzahl an Prozesse in Adhoc Tabelle --> Hinweis, wird die Anzahl
	// geändert, muss auch das Programm erweitert werden, da dies nicht generisch
	// aufgebaut wurde
	private static final int maxProcessesForAdhoc = 5;

	private MenuItem miFilter = new MenuItem("Filter");
	private MenuItem miFilterReset = new MenuItem("Filter rücksetzen");
	private MenuItem miShowChart = new MenuItem("Diagramm erstellen");
	private MenuItem miMergeProcesses = new MenuItem("Ergebnisse zusammenführen");

	private EventHandler<KeyEvent> tableKeyEvent;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		btnResetFilter.setDisable(true);

		btnResetFilter.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				resetFilter();
				refreshTable();
			}
		});

		tableKeyEvent = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {

				if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
					exportTo();
				}

				if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
					handleFilterSettings();
				}

				if (event.getCode() == KeyCode.F5) {
					refreshTable();
				}
			}
		};

		disableMergeItems(true);
		mergedResultsLabel.setVisible(false);

		miFilterReset.setDisable(btnResetFilter.isDisable());
		miShowChart.setDisable(true);
		btnShowChart.setDisable(true);
		btnConfigSave.setDisable(true);

		initDragAndDrop();
		initContextMenu();

	}

	@FXML
	public void handleFilterSettings() {
		try {

			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/process/filter/FilterSettings.fxml"));
			loader.setResources(resources);
			BorderPane pane = (BorderPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			dialogStage.setResizable(false);

			Scene scene = new Scene(pane);
			scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());
			dialogStage.setScene(scene);

			// Set the settings into the controller.
			filterSettingsController = loader.getController();
			filterSettingsController.setDialogStage(dialogStage);

			if (filter == null)
				filter = new Filter();

			filterSettingsController.setFilterData(filter);

			filterSettingsController.getUebernehmenButton().setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {

					// Für den Fall das der Filter über File geladen wurde muss dieser hier
					// übernommen werden
					filter = filterSettingsController.getFilterData();

					if (adhocResultsList != null)
						setFilteredData();

					setItemsFilterActivated();

				}
			});

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			if (filterSettingsController.isOkClicked()) {

				// Für den Fall das der Filter über File geladen wurde muss dieser hier
				// übernommen werden
				filter = filterSettingsController.getFilterData();

				if (adhocResultsList != null)
					setFilteredData();

				setItemsFilterActivated();

			}

		} catch (

		IOException e) {
			e.printStackTrace();
		}

	}

	private void setFilteredData() {

		for (int i = 0; i < processList.size(); i++) {
			filterSettingsController.setData(processList.get(i), resultsList.get(i));
			filterSettingsController.getFilteredResults(filterSettingsController.isOkClicked());
			resultsList.remove(i);
			resultsList.add(i, filterSettingsController.getProcessResults());
			filterSettingsController.setData(null, null);

		}

		adhocResultsList.clear();

		setAdhocResults();

		table.setItems(adhocResultsList);
		table.getSelectionModel().clearSelection();
		table.refresh();

		disableMergeItems(false);
		mergedResultsLabel.setVisible(false);

	}

	private void setItemsFilterActivated() {
		if (filter.isActivated()) {
			Image image = new Image(getClass().getClassLoader()
					.getResourceAsStream("com/gretha/shared/resource/icons/filter_activ_24.png"));
			btnOpnTableFilter.setGraphic(new ImageView(image));

			btnResetFilter.setDisable(false);
			miFilterReset.setDisable(false);

		} else {
			Image image = new Image(getClass().getClassLoader()
					.getResourceAsStream("com/application/resource/icons/filter_inactiv24.png"));
			btnOpnTableFilter.setGraphic(new ImageView(image));

			btnResetFilter.setDisable(true);
			miFilterReset.setDisable(true);
		}
	}

	private void resetFilter() {

		Image image = new Image(getClass().getClassLoader()
				.getResourceAsStream("com/gretha/shared/resource/icons/filter_inactiv_24.png"));
		btnOpnTableFilter.setGraphic(new ImageView(image));

		filter = null;

		btnResetFilter.setDisable(true);
		miFilterReset.setDisable(true);
	}

	@FXML
	public void handleShowChart() {

		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/process/adhoc/AdhocChart.fxml"));
			loader.setResources(resources);
			BorderPane pane = (BorderPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.setTitle(resources.getString("adhoc_chart"));

			Scene scene = new Scene(pane);
			scene.getStylesheets().add(Constants.STYLESHEET);
			dialogStage.setScene(scene);

			AdhocChartController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setMain(this.main);
			controller.setAdhocTableController(this);
			controller.setData(resultsList, processList);

			dialogStage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String createStageTitle() {

		String stageTitle = "";

		for (Process process : processList) {

			if (processList.indexOf(process) == (processList.size() - 1))
				stageTitle = stageTitle + process.getStation() + " " + process.getName();
			else
				stageTitle = stageTitle + process.getStation() + " " + process.getName() + "; ";

		}

		return stageTitle;

	}

	private void initDragAndDrop() {

		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		table.setOnDragOver(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {

				if (event.getGestureSource() != table && event.getDragboard().hasString())
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

				event.consume();
			}
		});

		table.setOnDragDropped(new EventHandler<DragEvent>() {
			public void handle(DragEvent event) {

				if (event.getGestureSource() != table && event.getDragboard().hasString())
					event.acceptTransferModes(TransferMode.COPY_OR_MOVE);

				int id = Integer.parseInt(event.getDragboard().getString());

				if (resultsList.size() < maxProcessesForAdhoc) {
					initTable(id);

				} else {
					AdhocTableMaxSizeReached info = new AdhocTableMaxSizeReached(dialogStage);
					info.showAndWait();

				}

				event.setDropCompleted(true);
				event.consume();
			}
		});
	}

	private void initTable(int processId) {

		btnOpnTableFilter.setDisable(false);
		miFilter.setDisable(btnOpnTableFilter.isDisable());
		miShowChart.setDisable(false);
		btnShowChart.setDisable(false);
		btnConfigSave.setDisable(false);

		disableMergeItems(false);
		mergedResultsLabel.setVisible(false);

		adhocResultsList = FXCollections.observableArrayList();

		Process process = Service.getInstance().getProcess(processId);
		processList.add(process);

		// Wenn Filter aktiviert und ein neuer Prozess in die Tabelle gezogen, dann
		// sollen gleich die Werte über den aktiven Filter laufen und nur die
		// gefilterten Werte hinzugefügt werden
		if (filter != null) {
			if (filter.isActivated()) {
				filterSettingsController.setData(process, null);
				filterSettingsController.getFilteredResults(filterSettingsController.isOkClicked());
				resultsList.add(filterSettingsController.getProcessResults());
			}

		} else
			resultsList.add(Service.getInstance().getResults(process));

		TableColumn<AdhocResult, Result> colNr = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Nr.]");
		colNr.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colNr.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				Integer i1 = o1.getNr();
				Integer i2 = o2.getNr();

				return i1.compareTo(i2);
			}

		});
		colNr.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {
				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(String.valueOf(item.getNr()));
						setAlignment(Pos.CENTER);
						setStyle(getItemStyle(item));
					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colValue = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Messwert]");
		colValue.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colValue.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				Float i1 = o1.getValue();
				Float i2 = o2.getValue();

				return i1.compareTo(i2);
			}

		});
		colValue.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {

				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(DecimalPointFormatter.roundFloat2String(item.getValue(), process.getDecimalPoints())
								+ " " + process.getUnit().getSign());

						setStyle(getItemStyle(item));
					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colLoLim = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Untergrenze]");
		colLoLim.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colLoLim.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				Float i1 = o1.getLoLim();
				Float i2 = o2.getLoLim();

				return i1.compareTo(i2);
			}

		});
		colLoLim.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {

				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(DecimalPointFormatter.roundFloat2String(item.getLoLim(), process.getDecimalPoints())
								+ " " + process.getUnit().getSign());

						setStyle(getItemStyle(item));
					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colUpLim = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Obergrenze]");
		colUpLim.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colUpLim.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				Float i1 = o1.getUpLim();
				Float i2 = o2.getUpLim();

				return i1.compareTo(i2);
			}

		});
		colUpLim.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {

				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(DecimalPointFormatter.roundFloat2String(item.getUpLim(), process.getDecimalPoints())
								+ " " + process.getUnit().getSign());

						setStyle(getItemStyle(item));
					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colState = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Status]");
		colState.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colState.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				Integer i1 = o1.getState();
				Integer i2 = o2.getState();

				return i1.compareTo(i2);
			}

		});
		colState.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {
				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						if (item.getState() == 0)
							setText(resources.getString("na"));

						else if (item.getState() == 1)
							setText(resources.getString("nok"));

						else if (item.getState() == 2)
							setText(resources.getString("ok"));

						else
							setText(resources.getString("undef") + " -> " + item.getState());

						setStyle(getItemStyle(item));

					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colSerial = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Seriennummer]");
		colSerial.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colSerial.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				String i1 = o1.getSerial();
				String i2 = o2.getSerial();

				return i1.toLowerCase().compareTo(i2.toLowerCase());
			}

		});
		colSerial.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {
				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item.getSerial());
						setAlignment(Pos.CENTER);
						setStyle(getItemStyle(item));
					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colTypNr = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Typennummer]");
		colTypNr.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colTypNr.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				Integer i1 = o1.getTypId();
				Integer i2 = o2.getTypId();

				return i1.compareTo(i2);
			}

		});
		colTypNr.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {
				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(String.valueOf(item.getTypId()));
						setAlignment(Pos.CENTER);
						setStyle(getItemStyle(item));

					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colWtNr = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[WT-Nummer]");
		colWtNr.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colWtNr.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				String i1 = o1.getWt();
				String i2 = o2.getWt();

				return i1.toLowerCase().compareTo(i2.toLowerCase());
			}

		});
		colWtNr.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {
				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item.getWt());
						setAlignment(Pos.CENTER);
						setStyle(getItemStyle(item));

					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colRemark = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Bemerkung]");
		colRemark.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colRemark.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				String i1 = o1.getRemark();
				String i2 = o2.getRemark();

				return i1.toLowerCase().compareTo(i2.toLowerCase());
			}

		});
		colRemark.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {
				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item.getRemark());
						setAlignment(Pos.CENTER);
						setStyle(getItemStyle(item));

					}
				}
			};
		});

		TableColumn<AdhocResult, Result> colTimestamp = new TableColumn<AdhocResult, Result>(
				process.getStation() + " - " + process.getName() + " " + "[Zeitpunkt]");
		colTimestamp.setCellValueFactory(new PropertyValueFactory<AdhocResult, Result>("result" + resultsList.size()));
		colTimestamp.setComparator(new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				if (o1 == null && o2 == null)
					return 0;
				if (o1 == null)
					return -1;
				if (o2 == null)
					return 1;

				String i1 = o1.getTimestamp();
				String i2 = o2.getTimestamp();

				return i1.toLowerCase().compareTo(i2.toLowerCase());
			}

		});
		colTimestamp.setCellFactory(column -> {
			return new TableCell<AdhocResult, Result>() {
				@Override
				protected void updateItem(Result item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(item.getTimestamp());
						setAlignment(Pos.CENTER);
						setStyle(getItemStyle(item));

					}
				}
			};
		});

		setAdhocResults();

		colNr.setVisible(true);
		colValue.setVisible(true);
		colLoLim.setVisible(false);
		colUpLim.setVisible(false);
		colState.setVisible(false);
		colSerial.setVisible(true);
		colTypNr.setVisible(false);
		colWtNr.setVisible(false);
		colRemark.setVisible(false);
		colTimestamp.setVisible(true);

		colNr.setUserData(process);
		colValue.setUserData(process);
		colLoLim.setUserData(process);
		colUpLim.setUserData(process);
		colState.setUserData(process);
		colSerial.setUserData(process);
		colTypNr.setUserData(process);
		colWtNr.setUserData(process);
		colRemark.setUserData(process);
		colTimestamp.setUserData(process);

		table.getColumns().addAll(colNr, colValue, colLoLim, colUpLim, colState, colSerial, colTypNr, colWtNr,
				colRemark, colTimestamp);

		table.setItems(adhocResultsList);
		table.getSelectionModel().clearSelection();
		table.refresh();
		table.setOnKeyPressed(tableKeyEvent);

		dialogStage.setTitle(resources.getString("adhoc_table") + " " + "[" + createStageTitle() + "]");

	}

	private void clearTable() {

		processList.clear();
		resultsList.clear();

		table.getItems().clear();
		table.getColumns().clear();

		miShowChart.setDisable(true);

		btnShowChart.setDisable(true);
		btnConfigSave.setDisable(true);

		dialogStage
				.setTitle(resources.getString("adhoc_table") + " " + "(Prozesse via Drag and Drop in Tabelle ziehen)");

	}

	private void refreshTable() {

		ObservableList<Process> processListTemp = FXCollections.observableArrayList(processList);

		clearTable();

		for (Process process : processListTemp) {
			initTable(process.getId());
		}
	}

	private String getItemStyle(Result item) {
		if (item.getState() == 0)
			return "-fx-background-color:lightgrey";

		else if (item.getState() == 1)
			return "-fx-background-color:lightcoral";

		else if (item.getState() == 2)
			return "-fx-background-color:lightgreen";

		else
			return "-fx-background-color:yellow";
	}

	private void setAdhocResults() {

		if (resultsList.size() == 1) {
			for (int i = 0; i < resultsList.get(0).size(); i++) {

				AdhocResult ad = new AdhocResult();

				if (i < resultsList.get(0).size())
					ad.setResult1(resultsList.get(0).get(i));

				adhocResultsList.add(ad);
			}
		}

		if (resultsList.size() == 2) {
			for (int i = 0; i < resultsList.get(0).size() || i < resultsList.get(1).size(); i++) {

				AdhocResult ad = new AdhocResult();

				if (i < resultsList.get(0).size())
					ad.setResult1(resultsList.get(0).get(i));

				if (i < resultsList.get(1).size())
					ad.setResult2(resultsList.get(1).get(i));

				adhocResultsList.add(ad);

			}
		}

		if (resultsList.size() == 3) {
			for (int i = 0; i < resultsList.get(0).size() || i < resultsList.get(1).size()
					|| i < resultsList.get(2).size(); i++) {

				AdhocResult ad = new AdhocResult();

				if (i < resultsList.get(0).size())
					ad.setResult1(resultsList.get(0).get(i));

				if (i < resultsList.get(1).size())
					ad.setResult2(resultsList.get(1).get(i));

				if (i < resultsList.get(2).size())
					ad.setResult3(resultsList.get(2).get(i));

				adhocResultsList.add(ad);

			}
		}

		if (resultsList.size() == 4) {
			for (int i = 0; i < resultsList.get(0).size() || i < resultsList.get(1).size()
					|| i < resultsList.get(2).size() || i < resultsList.get(3).size(); i++) {

				AdhocResult ad = new AdhocResult();

				if (i < resultsList.get(0).size())
					ad.setResult1(resultsList.get(0).get(i));

				if (i < resultsList.get(1).size())
					ad.setResult2(resultsList.get(1).get(i));

				if (i < resultsList.get(2).size())
					ad.setResult3(resultsList.get(2).get(i));

				if (i < resultsList.get(3).size())
					ad.setResult4(resultsList.get(3).get(i));

				adhocResultsList.add(ad);

			}
		}

		if (resultsList.size() == 5) {
			for (int i = 0; i < resultsList.get(0).size() || i < resultsList.get(1).size()
					|| i < resultsList.get(2).size() || i < resultsList.get(3).size()
					|| i < resultsList.get(4).size(); i++) {

				AdhocResult ad = new AdhocResult();

				if (i < resultsList.get(0).size())
					ad.setResult1(resultsList.get(0).get(i));

				if (i < resultsList.get(1).size())
					ad.setResult2(resultsList.get(1).get(i));

				if (i < resultsList.get(2).size())
					ad.setResult3(resultsList.get(2).get(i));

				if (i < resultsList.get(3).size())
					ad.setResult4(resultsList.get(3).get(i));

				if (i < resultsList.get(4).size())
					ad.setResult5(resultsList.get(4).get(i));

				adhocResultsList.add(ad);

			}
		}
	}

	private void initContextMenu() {

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem miRefresh = new MenuItem("Refresh (Set to default)");
		miRefresh.setAccelerator(new KeyCodeCombination(KeyCode.F5, KeyCombination.SHORTCUT_ANY));
		miRefresh.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				refreshTable();

			}
		});

		miShowChart.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				handleShowChart();

			}
		});

		miFilter.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		miFilter.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				handleFilterSettings();

			}
		});

		miFilterReset.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				resetFilter();
				refreshTable();

			}
		});

		MenuItem miColumnsSetVisible = new MenuItem("Alle einblenden");
		miColumnsSetVisible.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				for (TableColumn<AdhocResult, ?> column : table.getColumns()) {
					column.setVisible(true);

				}

			}
		});

		MenuItem miColumnsSetInvisible = new MenuItem("Alle ausblenden");
		miColumnsSetInvisible.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				for (TableColumn<AdhocResult, ?> column : table.getColumns()) {
					column.setVisible(false);

				}

			}
		});

		Menu mColumns = new Menu("Spalten");
		mColumns.getItems().addAll(miColumnsSetVisible, miColumnsSetInvisible);

		MenuItem miExport = new MenuItem("Exportieren");
		miExport.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		miExport.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				exportTo();
			}
		});

		MenuItem miConfigSave = new MenuItem("Speichern");
		miConfigSave.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				try {
					handleConfigSave();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		MenuItem miConfigLoad = new MenuItem("Laden");
		miConfigLoad.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				try {
					handleConfigLoad();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		MenuItem miClearTable = new MenuItem("Tabelle leeren");
		miClearTable.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				clearTable();

				table.setOnKeyPressed(null);

				disableMergeItems(true);
				mergedResultsLabel.setVisible(false);

			}
		});

		miMergeProcesses.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				mergeResults();

			}
		});

		Menu mConfig = new Menu("Konfiguration");
		mConfig.getItems().addAll(miConfigSave, miConfigLoad);

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miRefresh, new SeparatorMenuItem(), miMergeProcesses, miShowChart, miFilter,
				miFilterReset, new SeparatorMenuItem(), mConfig, mColumns, miClearTable, new SeparatorMenuItem(),
				miExport);

		// When user right-click
		table.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {

				contextMenu.hide();

				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {

					contextMenu.show(table, mouseEvent.getScreenX(), mouseEvent.getScreenY());

				}
			}
		});

	}

	@FXML
	private void mergeResults() {

		duplicateSerialMap = new HashMap<>();

		int max = 0;
		int cnt = 0;

		for (List<Result> resultList : resultsList) {

			Collections.sort(resultList, new ResultSort());

		}

		removeDuplicateSerials();
		getMultipleSerials();
		removeSingleSerials();

		adhocResultsList.clear();

		setAdhocResults();

		table.setItems(adhocResultsList);
		table.getSelectionModel().clearSelection();
		table.refresh();

		// Größte Ergebnisliste ermitteln (sollte im Regelfall nach dem Mergen immer
		// gleich sein)
		for (List<Result> resultList : resultsList) {

			if (cnt == 0)
				max = resultList.size();

			if (cnt > 0) {
				if (resultList.size() > max)
					max = resultList.size();
			}

			cnt++;
		}

		if (max == 1)
			mergedResultsLabel.setText(max + " Ergebnis zusammengeführt");
		else
			mergedResultsLabel.setText(max + " Ergebnisse zusammengeführt");

		disableMergeItems(true);
		mergedResultsLabel.setVisible(true);

	}

	private void getMultipleSerials() {

		for (List<Result> resultList : resultsList) {

			for (Result result : resultList) {

				if (!duplicateSerialMap.containsKey(result.getSerial())) {
					duplicateSerialMap.put(result.getSerial(), 1);

				}

				else {
					int value = duplicateSerialMap.get(result.getSerial());
					duplicateSerialMap.put(result.getSerial(), value + 1);

				}
			}
		}
	}

	private void removeSingleSerials() {

		for (List<Result> resultList : resultsList) {

			Iterator<Result> i = resultList.iterator();
			while (i.hasNext()) {

				Result result = i.next();

				for (Map.Entry<String, Integer> entry : duplicateSerialMap.entrySet()) {

					if (result.getSerial().equals(entry.getKey())) {

						if (entry.getValue() != processList.size())
							i.remove();
					}
				}
			}

			for (int j = 0; j < resultList.size(); j++) {
				resultList.get(j).setNr(j + 1);

			}
		}
	}

	private void removeDuplicateSerials() {

		for (List<Result> resultList : resultsList) {

			List<Result> resultListTemp = new ArrayList<>();

			for (Result result : resultList) {

				boolean serialAlreadyExists = false;

				if (resultListTemp.size() > 0)
					for (int i = 0; i < resultListTemp.size(); i++) {

						if (result.getSerial().equals(resultListTemp.get(i).getSerial()))
							serialAlreadyExists = true;
					}

				if (!serialAlreadyExists)
					resultListTemp.add(result);
			}

			resultList.clear();
			resultList.addAll(resultListTemp);

		}
	}

	private void disableMergeItems(boolean disable) {

		btnMergeResults.setDisable(disable);
		miMergeProcesses.setDisable(disable);
	}

	private void exportTo() {

		File file = null;

		DateFormat tf = new SimpleDateFormat("HH-mm-ss");
		String timeString = tf.format(Calendar.getInstance().getTime());

		DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		String dateString = df.format(Calendar.getInstance().getTime());

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Exportieren nach");
		FileChooser.ExtensionFilter extFilterCsv = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
		chooser.getExtensionFilters().addAll(extFilterCsv);

		chooser.setInitialFileName("Ad-hoc-Tabelle-Ergebnisse-Export-" + dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		String defaultText = "";
		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			for (int i = 0; i < processColumns; i++) {
				defaultText = defaultText + fieldDelimiter;

			}

			for (TableColumn<AdhocResult, ?> column : table.getColumns()) {
				writer.write(column.getText() + ";");

			}

			for (AdhocResult result : table.getItems()) {

				String text1 = "", text2 = "";

				if (result.getResult1() != null) {

					text1 = result.getResult1().getNr() + ";" + result.getResult1().getValue() + ";"
							+ result.getResult1().getLoLim() + ";" + result.getResult1().getUpLim() + ";";

					text2 = result.getResult1().getState() + ";" + result.getResult1().getSerial() + ";"
							+ result.getResult1().getTypId() + ";" + result.getResult1().getWt() + ";"
							+ result.getResult1().getRemark() + ";" + result.getResult1().getTimestamp() + ";";

					writer.write("\n" + text1.replace(".", ",") + text2);

				} else {
					writer.write("\n" + defaultText);
				}

				if (result.getResult2() != null) {

					text1 = result.getResult2().getNr() + ";" + result.getResult2().getValue() + ";"
							+ result.getResult2().getLoLim() + ";" + result.getResult2().getUpLim() + ";";

					text2 = result.getResult2().getState() + ";" + result.getResult2().getSerial() + ";"
							+ result.getResult2().getTypId() + ";" + result.getResult2().getWt() + ";"
							+ result.getResult2().getRemark() + ";" + result.getResult2().getTimestamp() + ";";

					writer.write(text1.replace(".", ",") + text2);

				} else {
					writer.write(defaultText);
				}

				if (result.getResult3() != null) {

					text1 = result.getResult3().getNr() + ";" + result.getResult3().getValue() + ";"
							+ result.getResult3().getLoLim() + ";" + result.getResult3().getUpLim() + ";";

					text2 = result.getResult3().getState() + ";" + result.getResult3().getSerial() + ";"
							+ result.getResult3().getTypId() + ";" + result.getResult3().getWt() + ";"
							+ result.getResult3().getRemark() + ";" + result.getResult3().getTimestamp() + ";";

					writer.write(text1.replace(".", ",") + text2);

				} else {
					writer.write(defaultText);
				}

				if (result.getResult4() != null) {

					text1 = result.getResult4().getNr() + ";" + result.getResult4().getValue() + ";"
							+ result.getResult4().getLoLim() + ";" + result.getResult4().getUpLim() + ";";

					text2 = result.getResult4().getState() + ";" + result.getResult4().getSerial() + ";"
							+ result.getResult4().getTypId() + ";" + result.getResult4().getWt() + ";"
							+ result.getResult4().getRemark() + ";" + result.getResult4().getTimestamp() + ";";

					writer.write(text1.replace(".", ",") + text2);

				} else {
					writer.write(defaultText);
				}

				if (result.getResult5() != null) {

					text1 = result.getResult5().getNr() + ";" + result.getResult5().getValue() + ";"
							+ result.getResult5().getLoLim() + ";" + result.getResult5().getUpLim() + ";";

					text2 = result.getResult5().getState() + ";" + result.getResult5().getSerial() + ";"
							+ result.getResult5().getTypId() + ";" + result.getResult5().getWt() + ";"
							+ result.getResult5().getRemark() + ";" + result.getResult5().getTimestamp() + ";";

					writer.write(text1.replace(".", ",") + text2);

				} else {
					writer.write(defaultText);
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

	@FXML
	public void handleConfigSave() {

		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("adhocTable files", "*.adhocTable"));

		File file = chooser.showSaveDialog(dialogStage);
		if (file != null) {

			Writer writer = null;

			try {
				writer = new BufferedWriter(new FileWriter(file));

				for (Process process : processList) {
					if (processList.indexOf(process) < (processList.size() - 1))
						writer.write(String.valueOf(process.getId()) + fieldDelimiter);
					else
						writer.write(String.valueOf(process.getId()));
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

	@FXML
	public void handleConfigLoad() throws FileNotFoundException {

		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("adhocTable files", "*.adhocTable"));

		File file = chooser.showOpenDialog(dialogStage);

		if (file != null) {

			BufferedReader bufferReader;
			bufferReader = new BufferedReader(new FileReader(file));
			String line;
			String[] fields = null;

			try {

				while ((line = bufferReader.readLine()) != null) {
					fields = line.split(fieldDelimiter, -1);
				}

				bufferReader.close();

				clearTable();

				if (fields != null)
					for (String string : fields) {
						try {
							initTable(Integer.parseInt(string));

						} catch (Exception e) {
							e.printStackTrace();

							logger.error(e);

							FileLoadFailedAlert alert = new FileLoadFailedAlert(dialogStage, file, e);
							alert.showAndWait();

							return;

						}
					}

				LoadSucessInfo info = new LoadSucessInfo(dialogStage, file);
				info.showAndWait();

			} catch (IOException e) {
				e.printStackTrace();

				FileLoadFailedAlert alert = new FileLoadFailedAlert(dialogStage, file, e);
				alert.showAndWait();

			}
		}
	}

	public void setData() {

	}

	public void setMain(Main main) {
		this.main = main;

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	public ObservableList<Process> getProcessList() {
		return processList;
	}

	public ObservableList<List<Result>> getResultsList() {
		return resultsList;
	}

	class ResultSort implements Comparator<Result> {
		@Override
		public int compare(Result a1, Result a2) {

			if (a1.getTimestamp() == null || a2.getTimestamp() == null)
				return a1.getNr() - a2.getNr();

			if (filter != null) {

				if (filter.getSortierung() == ESortierung.ASC.ordinal())
					return a1.getTimestamp().toLowerCase().compareTo(a2.getTimestamp().toLowerCase()) * 1;

				if (filter.getSortierung() == ESortierung.DESC.ordinal())
					return a1.getTimestamp().toLowerCase().compareTo(a2.getTimestamp().toLowerCase()) * -1;
			}

			return a1.getTimestamp().toLowerCase().compareTo(a2.getTimestamp().toLowerCase()) * -1;
		}
	}

}
