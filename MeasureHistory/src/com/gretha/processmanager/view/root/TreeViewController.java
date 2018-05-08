package com.gretha.processmanager.view.root;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.adhoc.AdhocTableController;
import com.gretha.processmanager.view.process.overview.OverviewController;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Process;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DurationDateAndTime;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * 
 * @author Markus Thaler
 */
public class TreeViewController implements Initializable {

	private static final Logger logger = Logger.getLogger(TreeViewController.class);

	// Reference to the main application
	private Main main;
	private ResourceBundle resources;
	private Stage dialogStage;

	@FXML
	private BorderPane dataPane;
	@FXML
	private AnchorPane treePane;

	@FXML
	private TreeView<Object> treeView;

	private List<OverviewController> overviewControllerList;

	private Set<String> stationsSet = new TreeSet<>();
	private ObservableList<Process> processList = FXCollections.observableArrayList();
	private ObservableList<Process> processPerStation = FXCollections.observableArrayList();

	private ChangeListener<String> filterListener;
	private ChangeListener<TreeItem<Object>> treeViewListener;

	private long beginTime;
	private long endTime;
	private long duration;
	private String dbQueryLoggerText;
	private int cntProcess;
	private Thread refreshVisuThread;

	public TreeViewController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		createTree();
		showHomeScreen();

	}

	public void refreshTreeView(Process process, int state) {

		// state = 0 --> neuer Prozess wurde angelegt
		// state = 1 --> bestehender Prozess wurde bearbeitet
		// state = 2 --> bestehender Prozess wurde gelöscht

		if (state == 0)
			createTree();

		else if (state == 1) {
			for (int i = 0; i < treeView.getRoot().getChildren().size(); i++) {

				for (int j = 0; j < treeView.getRoot().getChildren().get(i).getChildren().size(); j++) {
					Process treeProcess = (Process) (treeView.getRoot().getChildren().get(i).getChildren().get(j)
							.getValue());

					if (treeProcess.getId() == process.getId()) {
						if (!treeProcess.getStation().equals(process.getStation())) {
							createTree();

						} else if (!treeProcess.getName().equals(process.getName())) {
							treeProcess.setName(process.getName());
							treeView.refresh();
						}
					}
				}
			}
		}

		else if (state == 2)
			createTree();

		else
			createTree();

		filter();

	}

	private void createTree() {

		if (processList != null)
			processList.clear();

		if (stationsSet != null)
			stationsSet.clear();

		if (processPerStation != null)
			processPerStation.clear();

		try {
			// Alle Prozess aus der DB holen
			for (Process process : Service.getInstance().getProcesses()) {
				processList.add(process);
				stationsSet.add(process.getStation());
			}

			cntProcess = processList.size();

		} catch (NullPointerException e) {
			e.printStackTrace();
		}

		// Item [Prozesse] erstellen
		TreeItem<Object> itemProcessRoot = new TreeItem<>("Prozesse");
		itemProcessRoot.setExpanded(true);

		// Item [Stationsbezeichnung] erstellen
		for (String station : stationsSet) {
			TreeItem<Object> itemStation = new TreeItem<Object>(station);
			itemStation.setExpanded(false);

			// Item [Prozessbezeichnung] erstellen
			for (Process process : processList) {
				if (process.getStation().equals(station)) {
					TreeItem<Object> itemProcess = new TreeItem<Object>(process);
					itemProcess.setGraphic(new ImageView(new Image("com/gretha/shared/resource/icons/process16.png")));
					itemProcess.setExpanded(false);
					itemStation.getChildren().add(itemProcess);
				}

			}

			itemProcessRoot.getChildren().add(itemStation);
		}

		treeView.setRoot(itemProcessRoot);

		if (treeViewListener != null)
			treeView.getSelectionModel().selectedItemProperty().removeListener(treeViewListener);

		treeViewListener = (obs, oldValue, newValue) -> {

			TreeItem<Object> selectedItem = treeView.getSelectionModel().getSelectedItem();

			if (selectedItem == null) {
				showHomeScreen();
				return;
			}

			if (selectedItem.getParent() == null) {
				showOverviewProcessScreen(processList);
				return;
			}

			processPerStation.clear();

			// Alle vorhandenen Prozesse pro Station ermitteln oder nur den
			// eigentlichen Prozess
			for (int i = 0; i < processList.size(); i++) {

				if (treeView.getSelectionModel().getSelectedItem().getValue().toString()
						.equalsIgnoreCase(processList.get(i).getStation())) {
					processPerStation.add(processList.get(i));

				}

				else if (treeView.getSelectionModel().getSelectedItem().getParent().getValue().toString()
						.equalsIgnoreCase(processList.get(i).getStation())
						&& treeView.getSelectionModel().getSelectedItem().getValue().toString()
								.equalsIgnoreCase(processList.get(i).getName())) {
					processPerStation.add(processList.get(i));

				}

			}

			// Prozessübersicht im datenPane anzeigen (nur der aktuelle
			// ausgewähle Prozess)
			if (processPerStation.size() > 0) {
				showOverviewProcessScreen(processPerStation);
			}

		};

		treeView.getSelectionModel().selectedItemProperty().addListener(treeViewListener);

		treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY)
					if (event.getClickCount() == 2) {

						showProcessRootPaneDialog(0);
					}
			}
		});

		treeView.setOnDragDetected(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				Dragboard db = treeView.startDragAndDrop(TransferMode.ANY);

				ClipboardContent content = new ClipboardContent();

				Process process = (Process) treeView.getSelectionModel().getSelectedItem().getValue();
				content.putString(String.valueOf(process.getId()));

				db.setContent(content);

				event.consume();

			}
		});

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem miExpandCollapse = new MenuItem("Erweitern");
		miExpandCollapse.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (!treeView.getSelectionModel().getSelectedItem().isExpanded()
						&& !treeView.getSelectionModel().getSelectedItem().isLeaf()) {
					treeView.getSelectionModel().getSelectedItem().setExpanded(true);
				} else {
					treeView.getSelectionModel().getSelectedItem().setExpanded(false);
				}
			}
		});

		Menu mConfig = new Menu("Konfiguration");

		MenuItem miConfigProcesses = new MenuItem("Prozesse");
		miConfigProcesses.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				main.showProcessOverviewDialog();
			}
		});

		MenuItem miConfigUnits = new MenuItem("Einheiten");
		miConfigUnits.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				main.showUnitOverviewDialog();
			}
		});

		MenuItem miConfigPlcs = new MenuItem("Steuerungen");
		miConfigPlcs.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				main.showPlcOverviewDialog();
			}
		});

		MenuItem miConfigPlcTriggers = new MenuItem("Trigger");
		miConfigPlcTriggers.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				main.showPlcTriggerOverviewDialog();
			}
		});

		MenuItem miShiftSettings = new MenuItem("Schichtzeiten");
		miShiftSettings.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				main.showShiftSettingsOverviewDialog();
			}
		});

		mConfig.getItems().addAll(miConfigProcesses, miConfigUnits, miConfigPlcs, miConfigPlcTriggers, miShiftSettings);

		Menu mOpen = new Menu("Öffnen");
		Menu mNew = new Menu("Neu");

		MenuItem mNewAdhoc = new MenuItem("Ad-hoc Tabelle");
		mNewAdhoc.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showAdHocTable();
			}
		});

		MenuItem mOpenProcessResultTable = new MenuItem("Messwerttabelle");
		mOpenProcessResultTable.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showProcessRootPaneDialog(0);
			}
		});

		MenuItem mOpenProcessResultChart = new MenuItem("Messwertdiagramm");
		mOpenProcessResultChart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showProcessRootPaneDialog(1);
			}
		});

		MenuItem mOpenProcessStatisticChart = new MenuItem("Statistik");
		mOpenProcessStatisticChart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showProcessRootPaneDialog(2);
			}
		});

		MenuItem mOpenProcessSPCChart = new MenuItem("Prozessanalyse");
		mOpenProcessSPCChart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showProcessRootPaneDialog(3);
			}
		});

		MenuItem mOpenProcessTimeChart = new MenuItem("Zeitdiagramm");
		mOpenProcessTimeChart.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showProcessRootPaneDialog(4);
			}
		});

		mNew.getItems().addAll(mNewAdhoc);

		mOpen.getItems().addAll(mOpenProcessResultTable, mOpenProcessResultChart, mOpenProcessStatisticChart,
				mOpenProcessSPCChart, mOpenProcessTimeChart);

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miExpandCollapse, new SeparatorMenuItem(), mNew, mOpen, new SeparatorMenuItem(),
				mConfig);

		// When user right-click
		treeView.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {

				contextMenu.hide();

				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {

					if (treeView.getSelectionModel().getSelectedItem() != null) {

						if (!treeView.getSelectionModel().getSelectedItem().isLeaf())
							mOpen.setDisable(true);
						else
							mOpen.setDisable(false);

						if (treeView.getSelectionModel().getSelectedItem().isExpanded()
								&& !treeView.getSelectionModel().getSelectedItem().isLeaf()) {

							miExpandCollapse.setText("Reduzieren");
							miExpandCollapse.setDisable(false);
						}

						else if (!treeView.getSelectionModel().getSelectedItem().isExpanded()
								&& !treeView.getSelectionModel().getSelectedItem().isLeaf()) {

							miExpandCollapse.setText("Erweitern");
							miExpandCollapse.setDisable(false);
						}

						else {
							miExpandCollapse.setText("Reduzieren");
							miExpandCollapse.setDisable(true);
						}

						contextMenu.show(treeView, mouseEvent.getScreenX(), mouseEvent.getScreenY());

					}
				}
			}
		});
	}

	private void filter() {

		main.getLayoutController().getSearchFiled().setText("");
		setSearchFeebacks(-1, true);

		if (filterListener != null)
			main.getLayoutController().getSearchFiled().textProperty().removeListener(filterListener);

		FilteredList<Process> filteredData = new FilteredList<>(processList, p -> true);
		SortedList<Process> sortedData = new SortedList<>(filteredData);

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

				if (process.getStation() != null && process.getName() != null)

					if (lowerCaseFilter.toLowerCase().contains(process.getStation().toLowerCase())) {

						String text = lowerCaseFilter.replace(process.getStation().toLowerCase(), "");
						text = text.replaceFirst(" ", "");

						if (process.getName().toLowerCase().contains(text)) {
							return true;
						}

					}

				return false;
			});

			if (newValue == null || newValue.isEmpty())
				setSearchFeebacks(-1, true);
			else
				setSearchFeebacks(filteredData.size(), false);
		};

		main.getLayoutController().getSearchFiled().textProperty().addListener(filterListener);

		main.getLayoutController().getSearchFiled().setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER && filteredData.size() > 0) {

					if (!main.getLayoutController().getSearchFiled().getText().isEmpty()) {

						treeView.getSelectionModel().clearSelection();
						showOverviewProcessScreen(sortedData);

					}

				}
			}

		});
	}

	private void setSearchFeebacks(int foundDatasets, boolean setToDefault) {

		if (!setToDefault) {

			if (foundDatasets == 1) {
				main.getLayoutController().getFoundValueLabel().setText(foundDatasets + " Übereinstimmung");
				main.getLayoutController().getSearchFiled().setId("textfield-search-default");
			}
			if (foundDatasets > 1) {
				main.getLayoutController().getFoundValueLabel().setText(foundDatasets + " Übereinstimmungen");
				main.getLayoutController().getSearchFiled().setId("textfield-search-default");
			}
			if (foundDatasets <= 0) {
				main.getLayoutController().getFoundValueLabel().setText("Keine Übereinstimmungen\ngefunden");
				main.getLayoutController().getSearchFiled().setId("textfield-search-no-match");
			}
		} else {
			main.getLayoutController().getFoundValueLabel().setText("");
			main.getLayoutController().getSearchFiled().setId("textfield-search-default");
		}

	}

	public void showProcessRootPaneDialog(int tab) {
		for (int i = 0; i < processPerStation.size(); i++) {
			if (treeView.getSelectionModel().getSelectedItem().getValue().toString()
					.equalsIgnoreCase(processPerStation.get(i).getName()))
				main.showProcessRootPaneDialog(processPerStation.get(i), tab);
		}
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMain(Main main) {
		this.main = main;

	}

	public void setData() {
		filter();
	}

	public void showHomeScreen() {

		try {

			// Load overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/root/HomeScreen.fxml"));
			loader.setResources(resources);
			BorderPane pane = (BorderPane) loader.load();

			dataPane.setCenter(pane);

			// Give the controller access to the main app.
			HomeScreenController controller = loader.getController();
			controller.setMain(main);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void showAdHocTable() {

		try {

			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/process/adhoc/AdhocTable.fxml"));
			loader.setResources(resources);
			BorderPane pane = (BorderPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			// Scene scene = new Scene(new Group());
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.setTitle(
					resources.getString("adhoc_table") + " " + "(Prozesse via Drag and Drop in Tabelle ziehen)");
			dialogStage.initOwner(this.dialogStage);

			Scene scene = new Scene(pane);
			scene.getStylesheets().add(Constants.STYLESHEET);
			dialogStage.setScene(scene);

			AdhocTableController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setMain(this.main);
			controller.setData();

			dialogStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void showOverviewProcessScreen(List<Process> processList) {

		beginTime = System.currentTimeMillis();

		dbQueryLoggerText = "Prozess(e) laden...";

		// startStopRefreshVisuThread(true);

		ScrollPane sp;

		overviewControllerList = new ArrayList<>();

		FlowPane flow = new FlowPane();
		flow.setPadding(new Insets(10, 10, 10, 10));
		flow.setVgap(10);
		flow.setHgap(10);
		flow.setPrefWrapLength(50);

		try {

			for (Process process : processList) {

				// Load overview.
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/process/overview/Overview.fxml"));
				loader.setResources(resources);

				BorderPane pane = (BorderPane) loader.load();
				pane.setUserData(process);

				// Give the controller access to the main app.
				OverviewController controller = loader.getController();
				controller.setMain(main);
				controller.setData(process);

				overviewControllerList.add(controller);

				flow.getChildren().add(pane);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		sp = new ScrollPane();
		sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		sp.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
			@Override
			public void changed(ObservableValue<? extends Bounds> ov, Bounds oldBounds, Bounds bounds) {
				flow.setPrefWidth(bounds.getWidth());
				flow.setPrefHeight(bounds.getHeight());
				flow.setAlignment(Pos.CENTER);
			}
		});

		sp.setContent(flow);

		dataPane.setCenter(sp);

		/*
		 * Refresh Items (Button, Menüeintrag) freigeben; Es kann darf nur aktualisiert
		 * werden, wenn mind. ein Prozess in der Übersicht vorhanden ist
		 * 
		 */
		main.getLayoutController().setResfreshItems(true);
		main.getLayoutController().setRefreshDate();

		endTime = System.currentTimeMillis();
		duration = endTime - beginTime;

		// startStopRefreshVisuThread(false);

		dbQueryLoggerText = String.format("%d von %d Prozess(e) sichtbar (Executing time: %s)", processList.size(),
				cntProcess, DurationDateAndTime.getTimestampFromMilliseconds(duration));
	}

	class refreshVisuThread implements Runnable {

		@Override
		public void run() {

			logger.info(getClass().getSimpleName() + "; " + Thread.currentThread().getName() + "; Status: "
					+ Thread.currentThread().getState() + ";");

			while (!Thread.currentThread().isInterrupted()) {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						dbQueryLoggerText = "Prozess(e) laden...";
					}
				});

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {

					logger.error(e);

					e.printStackTrace();

					Thread.currentThread().interrupt();
				}
			}

		}

	}

	public void startStopRefreshVisuThread(boolean start) {

		if (start) {

			refreshVisuThread = new Thread(new refreshVisuThread());
			refreshVisuThread.setDaemon(true);
			refreshVisuThread.start();

		} else {

			if (!refreshVisuThread.isInterrupted())
				refreshVisuThread.interrupt();

		}
	}

	public String getDbQueryLoggerText() {
		return dbQueryLoggerText;
	}

	public List<OverviewController> getOverviewControllerList() {
		return overviewControllerList;
	}

}
