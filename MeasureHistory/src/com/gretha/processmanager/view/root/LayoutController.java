package com.gretha.processmanager.view.root;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.licensemanager.LicenseManager;
import com.gretha.processmanager.Main;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Result;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.DurationDateAndTime;
import com.gretha.shared.util.LoginLogout;
import com.gretha.shared.util.ProgramChecker;
import com.gretha.shared.util.StringCustomComparator;
import com.gretha.shared.util.SystemTrayIcon;
import com.gretha.shared.util.WebBrowser;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * 
 * @author Markus Thaler
 */
public class LayoutController implements Initializable {

	private ResourceBundle resources;
	private static final Logger logger = Logger.getLogger(LayoutController.class);
	private Main main;
	private boolean autoRefresh;

	@FXML
	private CheckMenuItem cbAutoRefresh;
	@FXML
	private MenuItem miLogin;
	@FXML
	private MenuItem miLogout;
	@FXML
	private MenuItem miLicenceGenerator;
	@FXML
	private Menu mBearbeiten;
	@FXML
	private MenuItem miRefresh;
	@FXML
	private Label labelLeftStatus;
	@FXML
	private Label labelRightStatus;
	@FXML
	private Label labelCenterStatus;
	@FXML
	private Accordion resultAccordion;
	@FXML
	private TitledPane resultTitledPane;
	@FXML
	private Label lblRefreshDate;
	@FXML
	private Label lblRefreshText;
	@FXML
	private Button btnRefresh;
	@FXML
	private TextField searchField;
	@FXML
	private Button clearButton;
	@FXML
	private Label lblFoundValue;
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
	private TableColumn<Result, String> colRemark;
	@FXML
	private TableColumn<Result, String> colTimestamp;
	@FXML
	private TableColumn<Result, Integer> colTypNr;
	@FXML
	private TableColumn<Result, Integer> colNr;
	@FXML
	private TableColumn<Result, String> colStation;
	@FXML
	private TableColumn<Result, String> colName;
	@FXML
	private BorderPane bpTreeView;
	@FXML
	private SplitPane spData;
	@FXML
	private Label lblQueryExecTime;

	private Thread statusUpdateThread;
	private ObservableList<Result> resultList = FXCollections.observableArrayList();
	private String tableViewQueryTime;

	private RadioMenuItem rmiTableViewShowNok = new RadioMenuItem("Letzten 'nicht in Ordnung' Ergebnisse");
	private RadioMenuItem rmiTableViewShowOk = new RadioMenuItem("Letzten 'in Ordnung' Ergebnisse");
	private RadioMenuItem rmiTableViewShowUnbewertet = new RadioMenuItem("Letzten 'unbewertet' Ergebnisse");
	private RadioMenuItem rmiTableViewShowAll = new RadioMenuItem("Letzten Ergebnisse");
	private int nrLastResultShown = Constants.DEFAULT_LAST_RESULT_TABLE_VIEW_SHOWN;
	private double dividerPositions = 0.70;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		labelCenterStatus.setText(resources.getString("companyname"));

		searchField.setPromptText("Suche... (Station, Name)");

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

		setResfreshItems(false);
		initContexMenuTableView();

		// Defaulteinstellung für TableView nur NIO darstellen
		rmiTableViewShowNok.setSelected(true);

		// Autorefresh automatisch deaktivieren
		cbAutoRefresh.setSelected(false);
		autoRefresh = cbAutoRefresh.isSelected();

		cbAutoRefresh.setOnAction((event) -> {
			if (cbAutoRefresh.isSelected()) {
				autoRefresh = true;
				main.startStopAutoRefreshThread(autoRefresh);
			} else {
				autoRefresh = false;
				main.startStopAutoRefreshThread(autoRefresh);
			}

		});

		statusUpdateThread = new Thread(new StatusUpdateDataThread());
		statusUpdateThread.setDaemon(true);
		statusUpdateThread.start();

		setTableView();

		// Wenn keine gültige Lizenz vorhanden bzw. gefunden dann darf auch
		// nichts bearbeitet werden
		if (!LicenseManager.licenseValid) {
			searchField.setVisible(false);
			clearButton.setVisible(false);
			lblFoundValue.setVisible(false);
			resultAccordion.setVisible(false);
			mBearbeiten.setDisable(true);
		}

		searchField.setOnAction((event) -> {

		});

		tblResult.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event))
					exportTo();
			}
		});

		resultTitledPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {

				if (!resultTitledPane.isExpanded()) {
					dividerPositions = spData.getDividers().get(0).getPosition();
					spData.setDividerPositions(1);
				} else {
					spData.setDividerPositions(dividerPositions);
				}
			}

		});

		initResultView();

	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param main
	 */
	public void setMain(Main main) {
		this.main = main;
	}

	public void setResfreshItems(boolean enable) {
		if (enable) {
			btnRefresh.setVisible(true);
			btnRefresh.setDisable(false);

			lblRefreshDate.setVisible(true);
			lblRefreshDate.setDisable(false);

			lblRefreshText.setVisible(true);
			lblRefreshText.setDisable(false);

			miRefresh.setVisible(true);
			miRefresh.setDisable(false);

		} else {
			btnRefresh.setVisible(false);
			btnRefresh.setDisable(true);

			lblRefreshDate.setVisible(false);
			lblRefreshDate.setDisable(true);

			lblRefreshText.setVisible(false);
			lblRefreshText.setDisable(true);

			miRefresh.setVisible(true);
			miRefresh.setDisable(true);
		}

	}

	public void resetAutoRefreh() {
		autoRefresh = false;
		cbAutoRefresh.setSelected(false);
	}

	@FXML
	private void handleProcesses() {

		main.showProcessOverviewDialog();

	}

	@FXML
	private void handleUnits() {

		main.showUnitOverviewDialog();

	}

	@FXML
	private void handlePLCs() {

		main.showPlcOverviewDialog();

	}

	@FXML
	private void handlePLCTriggers() {

		main.showPlcTriggerOverviewDialog();

	}

	@FXML
	private void handleSettings() {
		if (LoginLogout.login(LoginLogout.EUserLevels.ADMINISTRATOR)) {
			main.showSettingsDialog();
		}
	}

	@FXML
	private void handleShiftSettings() {
		if (LoginLogout.login(LoginLogout.EUserLevels.USER)) {
			main.showShiftSettingsOverviewDialog();
		}
	}

	@FXML
	private void handleLogin() {
		LoginLogout.login(LoginLogout.EUserLevels.ALWAYS_LOGIN);

	}

	@FXML
	private void handleLogout() {
		LoginLogout.logout();

	}

	public void setTableView() {

		getDataTableView();
		updateResultView();

	}

	public void getDataTableView() {

		// nur Daten rückladen, wenn das TitledPane auch offen ist
		if (resultTitledPane.isExpanded()) {

			long beginTime;
			long endTime;
			long duration;

			beginTime = System.currentTimeMillis();

			if (rmiTableViewShowNok.isSelected()) {
				resultList = FXCollections
						.observableArrayList(Service.getInstance().getLastXResults(nrLastResultShown, 1));
			} else if (rmiTableViewShowOk.isSelected()) {
				resultList = FXCollections
						.observableArrayList(Service.getInstance().getLastXResults(nrLastResultShown, 2));
			} else if (rmiTableViewShowUnbewertet.isSelected()) {
				resultList = FXCollections
						.observableArrayList(Service.getInstance().getLastXResults(nrLastResultShown, 0));
			} else {
				resultList = FXCollections
						.observableArrayList(Service.getInstance().getLastXResults(nrLastResultShown, -1));
			}

			endTime = System.currentTimeMillis();
			duration = endTime - beginTime;

			tableViewQueryTime = "Daten zuletzt aktualisiert am " + getRefreshDate() + " -> " + "QUERY executing time: "
					+ DurationDateAndTime.getTimestampFromMilliseconds(duration);
		}

	}

	public void updateResultView() {

		if (rmiTableViewShowNok.isSelected())
			resultTitledPane.setText("Letzten 'nicht in Ordnung' Ergebnisse");

		else if (rmiTableViewShowOk.isSelected())
			resultTitledPane.setText("Letzten 'in Ordnung' Ergebnisse");

		else if (rmiTableViewShowUnbewertet.isSelected())
			resultTitledPane.setText("Letzten 'unbewertet' Ergebnisse");

		else
			resultTitledPane.setText("Letzten Ergebnisse");

		lblQueryExecTime.setText("(" + tableViewQueryTime + ")");

		tblResult.setItems(FXCollections.observableArrayList(resultList));

	}

	private boolean getNrLastResultTableViewFromUser() {

		TextInputDialog dialog = new TextInputDialog(String.valueOf(nrLastResultShown));
		dialog.getDialogPane().getScene().getStylesheets().add(Constants.STYLESHEET);
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream(Main.APP_ICON)));
		dialog.setTitle("Anzahl ändern");
		dialog.setHeaderText("Eingabe für Anzahl der letzen Ergebnisse");
		dialog.setContentText("Anzahl (Limit: 0 - " + Constants.LIMIT_LAST_RESULT_TABLE_VIEW_SHOWN + "):");

		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {
			if (Integer.valueOf(result.get()) < 0)
				nrLastResultShown = 0;
			else if (Integer.valueOf(result.get()) > Constants.LIMIT_LAST_RESULT_TABLE_VIEW_SHOWN)
				nrLastResultShown = Constants.LIMIT_LAST_RESULT_TABLE_VIEW_SHOWN;
			else
				nrLastResultShown = Integer.valueOf(result.get());
		}

		return result.isPresent();

	}

	private void initResultView() {

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

		colStation.setCellValueFactory(new PropertyValueFactory<Result, String>("processStation"));
		colStation.setCellFactory(column -> {
			return new TableCell<Result, String>() {

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

		colName.setCellValueFactory(new PropertyValueFactory<Result, String>("processName"));
		colName.setCellFactory(column -> {
			return new TableCell<Result, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(String.valueOf(item));

					}
				}
			};
		});

		colValue.setCellValueFactory(new PropertyValueFactory<Result, Float>("value"));
		colValue.setCellFactory(column ->

		{
			return new TableCell<Result, Float>() {

				@Override
				protected void updateItem(Float item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						int index = getTableRow().getIndex();
						Result result = null;
						if (index >= 0) {
							result = getTableView().getItems().get(index);
						}
						setText(DecimalPointFormatter.roundFloat2String(item, result.getProcess().getDecimalPoints())
								+ " " + result.getProcess().getUnit().getSign());
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
						int index = getTableRow().getIndex();
						Result result = null;
						if (index >= 0) {
							result = getTableView().getItems().get(index);
						}
						setText(DecimalPointFormatter.roundFloat2String(item, result.getProcess().getDecimalPoints())
								+ " " + result.getProcess().getUnit().getSign());
					}
				}
			};
		});

		colUpLim.setCellValueFactory(new PropertyValueFactory<Result, Float>("upLim"));
		colUpLim.setCellFactory(column ->

		{
			return new TableCell<Result, Float>() {
				@Override
				protected void updateItem(Float item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						int index = getTableRow().getIndex();
						Result result = null;
						if (index >= 0) {
							result = getTableView().getItems().get(index);
						}
						setText(DecimalPointFormatter.roundFloat2String(item, result.getProcess().getDecimalPoints())
								+ " " + result.getProcess().getUnit().getSign());
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
		tblResult.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		tblResult.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		tblResult.setItems(FXCollections.observableArrayList(resultList));
		tblResult.getSelectionModel().clearSelection();
		tblResult.refresh();

	}

	private void initContexMenuTableView() {

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem miRefreshTableView = new MenuItem("Refresh (Set to default)");
		miRefreshTableView.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				rmiTableViewShowNok.setSelected(true);
				setTableView();
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

		MenuItem miNrLastResult = new MenuItem("Anzahl der letzten Ergebnisse");
		miNrLastResult.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (getNrLastResultTableViewFromUser())
					setTableView();
			}
		});

		Menu mTableViewAnsicht = new Menu("Ansicht");

		rmiTableViewShowNok.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				setTableView();
			}
		});

		rmiTableViewShowOk.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setTableView();
			}
		});

		rmiTableViewShowUnbewertet.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setTableView();
			}
		});

		rmiTableViewShowAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setTableView();
			}
		});

		ToggleGroup group = new ToggleGroup();
		rmiTableViewShowNok.setToggleGroup(group);
		rmiTableViewShowOk.setToggleGroup(group);
		rmiTableViewShowUnbewertet.setToggleGroup(group);
		rmiTableViewShowAll.setToggleGroup(group);

		mTableViewAnsicht.getItems().addAll(rmiTableViewShowNok, rmiTableViewShowOk, rmiTableViewShowUnbewertet,
				rmiTableViewShowAll);

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miRefreshTableView, new SeparatorMenuItem(), mTableViewAnsicht, miNrLastResult,
				new SeparatorMenuItem(), miExport);

		// When user right-click
		tblResult.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				contextMenu.hide();
				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {

					contextMenu.show(tblResult, mouseEvent.getScreenX(), mouseEvent.getScreenY());
				}
			}
		});
	}

	@FXML
	private void handleAbout() {

		StringBuilder sb = new StringBuilder();

		sb.append(Main.VERSION + "\n");
		sb.append(Main.BUILD + "\n");
		sb.append(Main.DATE.substring(0, 26) + " $" + "\n");
		sb.append("JDK: " + Main.JDK);

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(resources.getString("about"));
		alert.setHeaderText(resources.getString("appname1") + "\n" + sb.toString().replace("$", ""));

		alert.setContentText(
				"Entwicklung:\n" + resources.getString("programer1") + "\n" + resources.getString("programer2"));

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_ABOUT));

		alert.showAndWait();
	}

	@FXML
	private void handleLicenseInfo() {
		LicenseManager.showLicenseInformation();
	}

	@FXML
	private void handleLicenseGenerator() {
		if (LoginLogout.login(LoginLogout.EUserLevels.ADMINISTRATOR)) {
			LicenseManager.startLicenseGenerator();
		}

	}

	@FXML
	private void handleExit() {
		logger.info("Programm beenden");
		ProgramChecker.closeSocket();
		Platform.exit();
		System.exit(0);
	}

	@FXML
	private void handleMinimize() {
		if (!Main.messageInSystrayShown) {
			SystemTrayIcon.showInfoMessage("Programm wurde minimiert");
			Main.messageInSystrayShown = true;
		}

		logger.info("Programm minimiert");
		main.getPrimaryStage().close();
	}

	@FXML
	private void handleRestart() {
		logger.info("Programm Neustart");
		main.restart();
	}

	@FXML
	private void handleRefresh(KeyEvent e) {
		if (e.getCode() == KeyCode.F5) {
			main.updateOverviewController();
			setTableView();
		}
	}

	@FXML
	private void handleMenuRefresh() {
		main.updateOverviewController();
		setTableView();
	}

	@FXML
	private void handleButtonRefresh() {
		main.updateOverviewController();
		setTableView();
	}

	@FXML
	private void openJavaWebbrowser() {
		if (LoginLogout.login(LoginLogout.EUserLevels.USER))
			WebBrowser.openURLinJavaBrowser("");

	}

	public void setRefreshDate() {
		lblRefreshDate.setText(getRefreshDate());

	}

	private String getRefreshDate() {
		DateFormat df = new SimpleDateFormat("EEEE, dd. MMMM yyyy HH:mm:ss");
		Date date = Calendar.getInstance().getTime();
		return df.format(date);
	}

	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	class StatusUpdateDataThread implements Runnable {

		DateFormat df = new SimpleDateFormat("EEEE, dd. MMMM yyyy HH:mm:ss");

		@Override
		public void run() {

			logger.info(getClass().getSimpleName() + "; " + Thread.currentThread().getName() + "; Status: "
					+ Thread.currentThread().getState() + ";");

			while (!Thread.currentThread().isInterrupted()) {

				Date date = Calendar.getInstance().getTime();

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						String formatDate = df.format(date);
						labelRightStatus.setText(formatDate);

						labelLeftStatus.setText("UserName: " + LoginLogout.getUserNameString() + " / " + "UserLevel: "
								+ LoginLogout.getUserLevelInteger());

						if (main.getTreeViewController() != null)
							labelCenterStatus.setText(main.getTreeViewController().getDbQueryLoggerText());

						if (LoginLogout.getUserNameString() == LoginLogout.EUserLevels.GUEST.getUserName())
							miLogout.setDisable(true);
						else
							miLogout.setDisable(false);

						if (LoginLogout.getUserNameString() != LoginLogout.EUserLevels.ADMINISTRATOR.getUserName())
							miLicenceGenerator.setDisable(true);
						else
							miLicenceGenerator.setDisable(false);

					}
				});

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}

			}

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
				resultTitledPane.getText().replaceAll("'", "") + "-" + dateString + "-" + timeString + ".csv");

		file = chooser.showSaveDialog(main.getPrimaryStage());

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterCsv)
				exportToCsv(file);
	}

	private void exportToCsv(File file) {

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			writer.write(colNr.getText() + ";" + colStation.getText() + ";" + colName.getText() + ";"
					+ colValue.getText() + ";" + colLoLim.getText() + ";" + colUpLim.getText() + ";"
					+ colState.getText() + ";" + colSerial.getText() + ";" + colTypNr.getText() + ";" + "WT-Nummer"
					+ ";" + colRemark.getText() + ";" + colTimestamp.getText() + "\n");

			for (Result result : tblResult.getItems()) {

				String text1 = result.getNr() + ";" + result.getProcess().getStation() + ";"
						+ result.getProcess().getName() + ";";

				String text2 = result.getValue() + ";" + result.getLoLim() + ";" + result.getUpLim() + ";";

				String text3 = result.getState() + ";" + result.getSerial() + ";" + result.getTypId() + ";"
						+ result.getWt() + ";" + result.getRemark() + ";" + result.getTimestamp() + "\n";

				writer.write(text1 + text2.replace(".", ",") + text3);
			}

			writer.flush();
			writer.close();

			SaveSucessInfo info = new SaveSucessInfo(main.getPrimaryStage(), file);
			info.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

			logger.error(e);

			FileSaveFailedAlert alert = new FileSaveFailedAlert(main.getPrimaryStage(), file, e);
			alert.showAndWait();

		}
	}

	public Button getClearButton() {
		return clearButton;
	}

	public TextField getSearchFiled() {
		return searchField;
	}

	public Label getFoundValueLabel() {
		return lblFoundValue;
	}

	public BorderPane getTreeViewBorderPane() {
		return bpTreeView;
	}
}