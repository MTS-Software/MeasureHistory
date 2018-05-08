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
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.charts.SPCChartController;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.SPCClass;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 
 * @author Michael Grebesits
 */
public class SPCClassDataController implements Initializable {

	private static final Logger logger = Logger.getLogger(SPCClassDataController.class);
	private Stage dialogStage;
	private Main main;

	@FXML
	private TextArea loggerinfo;
	@FXML
	private TableView<SPCClass> dataTable;
	@FXML
	private TableColumn<SPCClass, Integer> idColumn;
	@FXML
	private TableColumn<SPCClass, Float> minColumn;
	@FXML
	private TableColumn<SPCClass, Float> maxColumn;
	@FXML
	private TableColumn<SPCClass, Integer> stueckAbsolutColumn;
	@FXML
	private TableColumn<SPCClass, Float> stueckRelativColumn;
	@FXML
	private TextField searchField;
	@FXML
	private Button clearButton;
	@FXML
	private Label anzahlKlassenLabel;
	@FXML
	private Label foundValuesLabel;

	private Process process;
	private SPCChartController spcChartController;

	private ChangeListener<String> filterListener;
	private ObservableList<SPCClass> spcClassChartDataList;

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public SPCClassDataController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		idColumn.setCellValueFactory(new PropertyValueFactory<SPCClass, Integer>("id"));
		idColumn.setCellFactory(column -> {
			return new TableCell<SPCClass, Integer>() {
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

		minColumn.setCellValueFactory(new PropertyValueFactory<SPCClass, Float>("min"));
		minColumn.setCellFactory(column -> {
			return new TableCell<SPCClass, Float>() {
				@Override
				protected void updateItem(Float item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(DecimalPointFormatter.roundFloat2String(item, process.getDecimalPoints()));
						setAlignment(Pos.CENTER);
					}
				}
			};
		});

		maxColumn.setCellValueFactory(new PropertyValueFactory<SPCClass, Float>("max"));
		maxColumn.setCellFactory(column -> {
			return new TableCell<SPCClass, Float>() {
				@Override
				protected void updateItem(Float item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(DecimalPointFormatter.roundFloat2String(item, process.getDecimalPoints()));
						setAlignment(Pos.CENTER);
					}
				}
			};
		});

		stueckAbsolutColumn.setCellValueFactory(new PropertyValueFactory<SPCClass, Integer>("stueckAbsolut"));
		stueckAbsolutColumn.setCellFactory(column -> {
			return new TableCell<SPCClass, Integer>() {
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

		stueckRelativColumn.setCellValueFactory(new PropertyValueFactory<SPCClass, Float>("stueckRelativ"));
		stueckRelativColumn.setCellFactory(column -> {
			return new TableCell<SPCClass, Float>() {
				@Override
				protected void updateItem(Float item, boolean empty) {
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

		dataTable.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event))
					exportTo();
			}
		});

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem miExport = new MenuItem("Exportieren");
		miExport.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		miExport.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				exportTo();
			}
		});

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miExport);

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

	private void filter() {

		searchField.setPromptText("Suche... (Klasse, absolute / relative Häufigkeit)");
		setSearchFeedbacks(-1);

		clearButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				searchField.setText("");
			}
		});

		// Wenn das Searchfild leer ist, wird der LöschenButton Disabled
		BooleanBinding bb = new BooleanBinding() {
			{
				super.bind(searchField.textProperty());
			}

			@Override
			protected boolean computeValue() {
				return (searchField.getText().isEmpty());
			}
		};

		clearButton.disableProperty().bind(bb);

		searchField.setText("");

		if (filterListener != null)
			searchField.textProperty().removeListener(filterListener);

		FilteredList<SPCClass> filteredData = new FilteredList<>(spcClassChartDataList, p -> true);

		filterListener = (obs, oldValue, newValue) -> {
			filteredData.setPredicate(spcClassChartData -> {

				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (String.valueOf(spcClassChartData.getId()).contains(lowerCaseFilter)) {
					return true;
				}

				if (String.valueOf(spcClassChartData.getStueckAbsolut()).contains(lowerCaseFilter)) {
					return true;
				}

				if (String.valueOf(spcClassChartData.getStueckRelativ()).contains(lowerCaseFilter)) {
					return true;
				}

				return false;
			});

			if (newValue == null || newValue.isEmpty())
				setSearchFeedbacks(-1);
			else
				setSearchFeedbacks(filteredData.size());
		};

		searchField.textProperty().addListener(filterListener);

		SortedList<SPCClass> sortedData = new SortedList<>(filteredData);
		sortedData.comparatorProperty().bind(dataTable.comparatorProperty());
		dataTable.setItems(sortedData);
	}

	private void setSearchFeedbacks(int foundDatasets) {

		if (foundDatasets == 1) {
			foundValuesLabel.setText(foundDatasets + " Übereinstimmung");
			searchField.setId("textfield-search-default");
		}
		if (foundDatasets > 1) {
			foundValuesLabel.setText(foundDatasets + " Übereinstimmungen");
			searchField.setId("textfield-search-default");
		}
		if (foundDatasets == 0) {
			foundValuesLabel.setText("Keine Übereinstimmungen\ngefunden");
			searchField.setId("textfield-search-no-match");
		}
		if (foundDatasets < 0) {
			foundValuesLabel.setText("");
			searchField.setId("textfield-search-default");
		}
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMain(Main main) {
		this.main = main;

	}

	@FXML
	private void handleClose() {
		this.dialogStage.close();
	}

	@FXML
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

		chooser.setInitialFileName(process.getStation() + "-" + process.getName() + "-SPC-Klassen-Export" + "-"
				+ dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));

			writer.write(idColumn.getText() + ";" + minColumn.getText() + ";" + maxColumn.getText() + ";"
					+ stueckAbsolutColumn.getText() + ";" + stueckRelativColumn.getText() + ";" + "\r\n");
			for (int i = 0; i < dataTable.getItems().size(); i++) {

				String text1 = dataTable.getItems().get(i).getId() + ";" + dataTable.getItems().get(i).getMin() + ";"
						+ dataTable.getItems().get(i).getMax() + ";" + dataTable.getItems().get(i).getStueckAbsolut()
						+ ";" + dataTable.getItems().get(i).getStueckRelativ() + ";" + "\r\n";

				writer.write(text1.replace(".", ","));

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

	public void setSPCClassChartData(ObservableList<SPCClass> spcClassChartDataList) {
		this.spcClassChartDataList = spcClassChartDataList;
		dataTable.setItems(FXCollections.observableArrayList(spcClassChartDataList));
		anzahlKlassenLabel.setText("Anzahl an Klassen: " + spcClassChartDataList.size());
		filter();
	}

	public void setData(Process process) {
		this.process = process;
	}

	public void setParentController(SPCChartController spcChartController) {
		this.spcChartController = spcChartController;
	}
}
