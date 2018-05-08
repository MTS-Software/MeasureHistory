package com.gretha.processmanager;

import java.awt.SystemTray;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.gretha.licensemanager.LicenseManager;
import com.gretha.processmanager.view.config.plc.PlcOverviewController;
import com.gretha.processmanager.view.config.plctrigger.PlcTriggerOverviewController;
import com.gretha.processmanager.view.config.process.ProcessOverviewController;
import com.gretha.processmanager.view.config.settings.shift.ShiftOverviewController;
import com.gretha.processmanager.view.config.unit.UnitOverviewController;
import com.gretha.processmanager.view.process.overview.OverviewController;
import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.processmanager.view.root.LayoutController;
import com.gretha.processmanager.view.root.SettingsController;
import com.gretha.processmanager.view.root.SettingsController.EStartWindow;
import com.gretha.processmanager.view.root.TreeViewController;
import com.gretha.shared.db.util.EDatabase;
import com.gretha.shared.model.Process;
import com.gretha.shared.util.ApplicationProperties;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.LoginLogout;
import com.gretha.shared.util.LoginLogout.EUserLevels;
import com.gretha.shared.util.ProgramChecker;
import com.gretha.shared.util.ProgramRestart;
import com.gretha.shared.util.SystemTrayIcon;
import com.gretha.shared.view.alert.ExceptionAlert;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class Main extends Application {

	// Revisionhistory
	// Muss manuell verändert werden
	public final static String VERSION = "$Version: 1 $";
	// Wird automatisch von SVN beschrieben
	public final static String BUILD = "$Rev: 1657 $";
	public final static String DATE = "$Date: 2017-10-15 20:47:37 +0200 (So, 15 Okt 2017) $";

	// Java Entwicklungsversion
	public final static String JDK = "1.8.0_141";

	private static final Logger logger = Logger.getLogger(Main.class);
	private ResourceBundle resources = ResourceBundle.getBundle("language");

	public final static String APP_ICON = Constants.APP_ICON_PROCESSMANAGER;

	// Port für ProgrammChecker
	public final static int PORT = 9999;

	// Für Splashscreen
	public static final String SPLASH_IMAGE = Constants.SPLASHSCREEN_IMAGE_PROCESSMANAGER;

	// Für Systray --> Wolke im SysTray nur einmalig ausgeben
	public static boolean messageInSystrayShown = false;

	private Pane splashLayout;
	private ProgressBar loadProgress;
	private Label progressText;
	private Label appInfo;
	private Label developerInfo;

	private static int threadSplashSleepTime = Constants.THREAD_SPLASH_SLEEP_TIME;
	private static double fadeTransitionsTime = Constants.FADE_TRANSITIONS_TIME;
	private static boolean showSplashScreen = Constants.SHOW_SPLASH_SCREEN;

	private Stage primaryStage;
	private Stage dialogStage;
	private BorderPane rootLayout;
	private Scene scene;
	private LayoutController layoutController;
	private TreeViewController treeViewController;

	private Thread autoRefreshVisuThread;
	private Thread autoRefreshDataThread;

	public static void main(String[] args) {

		if (args.length == 1) {
			LoginLogout.actUserLevel = EUserLevels.ADMINISTRATOR;

			LicenseManager.licenseValid = true;

			threadSplashSleepTime = 0;
			fadeTransitionsTime = 0.0;
			showSplashScreen = false;
		}

		launch(args);

	}

	public void restart() {
		if (ProgramRestart.showRestartDialog())
			if (ProgramChecker.closeSocket())
				try {
					ProgramRestart.restartApplicationWithoutDialog();

				} catch (IOException e) {
					e.printStackTrace();

					ExceptionAlert alert = new ExceptionAlert(primaryStage, e);
					alert.showAndWait();
				}
	}

	@Override
	public void start(Stage initStage) {

		primaryStage = new Stage();

		PropertyConfigurator.configure(
				getClass().getClassLoader().getResource("log4j" + resources.getString("appname1") + ".properties"));

		logger.info("Programm starten");

		ProgramChecker.checkIfRunning(PORT);

		if (LicenseManager.licenseValid == false) {
			logger.info("Kontrolle ob Lizenz gültig" + " (" + resources.getString("appname1") + "Data" + File.separator
					+ Constants.LICENSE_FILENAME + ")");
			checkLicense();
		}

		final Task<Integer> modulTask = new Task<Integer>() {
			@Override
			protected Integer call() throws InterruptedException {

				int actProgress = 1;
				int maxProgress = 2;

				updateProgress(0, maxProgress);
				updateMessage("Programm wird gestartet. . .");
				Thread.sleep(threadSplashSleepTime * 2);

				if (actProgress == 1) {
					updateProgress(actProgress, maxProgress);
					updateMessage(
							actProgress + " von " + maxProgress + ": " + "Initialisiere Programmeinstellungen. . .");

					ApplicationProperties.configure("application.properties", resources.getString("appname1") + "Data",
							"application.properties");
					ApplicationProperties.getInstance().setup();

					Thread.sleep(threadSplashSleepTime);
					actProgress++;
				}

				if (actProgress == 2) {
					updateProgress(actProgress, maxProgress);
					updateMessage(actProgress + " von " + maxProgress + ": "
							+ "Initialisiere Visualisierung, Datenbank, Schnittstellen. . .");

					String dbVendor = null;

					if (SystemTray.isSupported()) {
						Platform.setImplicitExit(false);
						SystemTrayIcon.setTrayIcon(primaryStage, resources.getString("appname1"),
								getClass().getClassLoader().getResource(Main.APP_ICON));
					}

					if (ApplicationProperties.getInstance().getProperty("db_vendor")
							.equalsIgnoreCase(EDatabase.MYSQL.getVendor()))
						dbVendor = EDatabase.MYSQL.toString();
					if (ApplicationProperties.getInstance().getProperty("db_vendor")
							.equalsIgnoreCase(EDatabase.SQLSERVER.getVendor()))
						dbVendor = EDatabase.SQLSERVER.toString();

					primaryStage.setTitle(resources.getString("appname1") + " @" + dbVendor + " on "
							+ ApplicationProperties.getInstance().getProperty("db_host"));

					primaryStage.setMaximized(ApplicationProperties.getInstance().getProperty("start_window")
							.equalsIgnoreCase(EStartWindow.MAXIMIZED.getNr()));
					primaryStage.getIcons()
							.add(new Image(getClass().getClassLoader().getResourceAsStream(Main.APP_ICON)));
					primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
						@Override
						public void handle(WindowEvent event) {

							if (ApplicationProperties.getInstance().getProperty("minimize_on_close")
									.equalsIgnoreCase("true")) {
								logger.info("Programm minimiert");
								if (!messageInSystrayShown) {
									SystemTrayIcon.showInfoMessage("Programm wurde minimiert");
									// Meldung nur einmal anzeigen
									messageInSystrayShown = true;
								}

							} else {
								logger.info("Programm beenden");
								ProgramChecker.closeSocket();
								Platform.exit();
								System.exit(0);
							}
						}
					});

					Thread.sleep(threadSplashSleepTime);
					actProgress++;
				}

				return actProgress;
			}
		};

		showSplash(initStage, modulTask, () -> showMainStage());
		new Thread(modulTask).start();

	}

	private void showMainStage() {
		initRootLayout();
	}

	public void initRootLayout() {
		try {

			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setResources(resources);
			loader.setLocation(Main.class.getResource("view/root/Layout.fxml"));
			rootLayout = (BorderPane) loader.load();

			// Show the scene containing the root layout.
			scene = new Scene(rootLayout);
			scene.getStylesheets().add(Constants.STYLESHEET);

			primaryStage.setWidth(Constants.DEFAULT_ROOT_LAYOUT_WIDTH);
			primaryStage.setHeight(Constants.DEFAULT_PROCESS_ROOT_PANE_HEIGTH);
			primaryStage.setScene(scene);

			// Give the controller access to the main app.
			layoutController = loader.getController();
			layoutController.setMain(this);

			// Wenn keine gültige Lizenz vorhanden bzw. gefunden dann darf auch
			// nichts bearbeitet werden
			if (LicenseManager.licenseValid) {
				initTreeView();
			}

			if (ApplicationProperties.getInstance().getProperty("start_window")
					.equalsIgnoreCase(EStartWindow.MINIMIZED.getNr())) {
				logger.info("Programm wurde minimiert gestartet");
				SystemTrayIcon.showInfoMessage("Programm wurde minimiert gestartet");
			} else {
				primaryStage.show();
			}

		} catch (IOException e) {
			e.printStackTrace();

			logger.error(e);

		}
	}

	public void startStopAutoRefreshThread(boolean start) {

		if (start) {

			autoRefreshDataThread = new Thread(new AutoRefreshDataThread());
			autoRefreshDataThread.start();

			autoRefreshVisuThread = new Thread(new AutoRefreshVisuThread());
			autoRefreshVisuThread.setDaemon(true);
			autoRefreshVisuThread.start();

		} else {

			if (!autoRefreshVisuThread.isInterrupted())
				autoRefreshVisuThread.interrupt();

			if (!autoRefreshDataThread.isInterrupted())
				autoRefreshDataThread.interrupt();

		}
	}

	public void updateOverviewController() {

		if (treeViewController.getOverviewControllerList() != null) {

			for (OverviewController controller : treeViewController.getOverviewControllerList()) {
				controller.getDataFromDatabase();
				controller.updateComponents();
			}

			layoutController.setRefreshDate();

		}

	}

	public boolean showSettingsDialog() {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/root/Settings.fxml"));
			loader.setResources(resources);
			AnchorPane pane = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().addAll(new Image(Constants.ICON_SETTINGS));
			dialogStage.setTitle(resources.getString("appname1") + " - " + resources.getString("settings"));
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(Constants.STYLESHEET);
			dialogStage.setScene(scene);

			// Set the settings into the controller.
			SettingsController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setData();

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			return controller.isOkClicked();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	public void refreshTreeView(Process process, int state) {

		layoutController.setResfreshItems(false);
		layoutController.resetAutoRefreh();

		if (layoutController.isAutoRefresh()) {
			startStopAutoRefreshThread(false);
		}

		treeViewController.refreshTreeView(process, state);

	}

	private void initTreeView() {

		try {

			// Load overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/root/TreeView.fxml"));
			loader.setResources(resources);
			AnchorPane pane = (AnchorPane) loader.load();

			layoutController.getTreeViewBorderPane().setCenter(pane);

			// Give the controller access to the main app.
			treeViewController = loader.getController();
			treeViewController.setDialogStage(primaryStage);
			treeViewController.setMain(this);
			treeViewController.setData();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean showProcessRootPaneDialog(Process process, int tab) {

		try {

			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/process/root/RootPane.fxml"));
			loader.setResources(resources);
			BorderPane pane = (BorderPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().addAll(primaryStage.getIcons());
			dialogStage.setTitle(resources.getString("appname1"));
			dialogStage.setWidth(Constants.DEFAULT_PROCESS_ROOT_PANE_WIDTH);
			dialogStage.setHeight(Constants.DEFAULT_PROCESS_ROOT_PANE_HEIGTH);

			Scene scene = new Scene(pane);
			scene.getStylesheets().add(Constants.STYLESHEET);
			dialogStage.setScene(scene);

			RootPaneController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setMain(this);
			controller.setData(process);
			controller.setTab(tab);

			dialogStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					dialogStage.close();
				}
			});

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}

	public boolean showUnitOverviewDialog() {

		if (LoginLogout.login(LoginLogout.EUserLevels.USER)) {

			try {

				// Load the fxml file and create a new stage for the popup
				// dialog.
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/config/unit/UnitOverview.fxml"));
				loader.setResources(resources);
				AnchorPane pane = (AnchorPane) loader.load();

				// Create the dialog Stage.
				Stage dialogStage = new Stage();
				dialogStage.getIcons().addAll(primaryStage.getIcons());
				dialogStage.setTitle("Übersicht: Einheiten");
				dialogStage.initOwner(this.dialogStage);
				dialogStage.initModality(Modality.WINDOW_MODAL);

				Scene scene = new Scene(pane);
				scene.getStylesheets().add(Constants.STYLESHEET);
				dialogStage.setScene(scene);

				UnitOverviewController controller = loader.getController();
				controller.setDialogStage(dialogStage);
				controller.setMain(this);
				controller.setData();

				// Show the dialog and wait until the user closes it
				dialogStage.showAndWait();

				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}

	}

	public boolean showPlcOverviewDialog() {

		if (LoginLogout.login(LoginLogout.EUserLevels.ADMINISTRATOR)) {

			try {

				// Load the fxml file and create a new stage for the popup
				// dialog.
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/config/plc/PlcOverview.fxml"));
				loader.setResources(resources);
				AnchorPane pane = (AnchorPane) loader.load();

				// Create the dialog Stage.
				Stage dialogStage = new Stage();
				dialogStage.getIcons().addAll(primaryStage.getIcons());
				dialogStage.setTitle("Übersicht: Steuerungen");
				dialogStage.initOwner(this.dialogStage);
				dialogStage.initModality(Modality.WINDOW_MODAL);

				Scene scene = new Scene(pane);
				scene.getStylesheets().add(Constants.STYLESHEET);
				dialogStage.setScene(scene);

				PlcOverviewController controller = loader.getController();
				controller.setDialogStage(dialogStage);
				controller.setMain(this);
				controller.setData();

				// Show the dialog and wait until the user closes it
				dialogStage.showAndWait();

				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}

	}

	public boolean showPlcTriggerOverviewDialog() {

		if (LoginLogout.login(LoginLogout.EUserLevels.ADMINISTRATOR)) {

			try {

				// Load the fxml file and create a new stage for the popup
				// dialog.
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/config/plctrigger/PlcTriggerOverview.fxml"));
				loader.setResources(resources);
				AnchorPane pane = (AnchorPane) loader.load();

				// Create the dialog Stage.
				Stage dialogStage = new Stage();
				dialogStage.getIcons().addAll(primaryStage.getIcons());
				dialogStage.setTitle("Übersicht: Trigger");
				dialogStage.initOwner(this.dialogStage);
				dialogStage.initModality(Modality.WINDOW_MODAL);

				Scene scene = new Scene(pane);
				scene.getStylesheets().add(Constants.STYLESHEET);
				dialogStage.setScene(scene);

				PlcTriggerOverviewController controller = loader.getController();
				controller.setDialogStage(dialogStage);
				controller.setMain(this);
				controller.setData();

				// Show the dialog and wait until the user closes it
				dialogStage.showAndWait();

				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}

	}

	public boolean showProcessOverviewDialog() {

		if (LoginLogout.login(LoginLogout.EUserLevels.ADMINISTRATOR)) {

			try {

				// Load the fxml file and create a new stage for the popup
				// dialog.
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/config/process/ProcessOverview.fxml"));
				loader.setResources(resources);
				AnchorPane pane = (AnchorPane) loader.load();

				// Create the dialog Stage.
				Stage dialogStage = new Stage();
				dialogStage.getIcons().addAll(primaryStage.getIcons());
				dialogStage.setTitle("Übersicht: Prozesse");
				dialogStage.initOwner(this.dialogStage);
				dialogStage.initModality(Modality.WINDOW_MODAL);

				Scene scene = new Scene(pane);
				scene.getStylesheets().add(Constants.STYLESHEET);
				dialogStage.setScene(scene);

				ProcessOverviewController controller = loader.getController();
				controller.setDialogStage(dialogStage);
				controller.setMain(this);
				controller.setData();

				// Show the dialog and wait until the user closes it
				dialogStage.showAndWait();

				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else
			return false;

	}

	public boolean showShiftSettingsOverviewDialog() {

		if (LoginLogout.login(LoginLogout.EUserLevels.ADMINISTRATOR)) {

			try {

				// Load the fxml file and create a new stage for the popup
				// dialog.
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(Main.class.getResource("view/config/settings/shift/ShiftOverview.fxml"));
				loader.setResources(resources);
				AnchorPane pane = (AnchorPane) loader.load();

				// Create the dialog Stage.
				Stage dialogStage = new Stage();
				dialogStage.getIcons().addAll(primaryStage.getIcons());
				dialogStage.setTitle("Übersicht: Schichtzeiten");
				dialogStage.initOwner(this.dialogStage);
				dialogStage.initModality(Modality.WINDOW_MODAL);

				Scene scene = new Scene(pane);
				scene.getStylesheets().add(Constants.STYLESHEET);
				dialogStage.setScene(scene);

				ShiftOverviewController controller = loader.getController();
				controller.setDialogStage(dialogStage);
				controller.setMain(this);
				controller.setData();

				// Show the dialog and wait until the user closes it
				dialogStage.showAndWait();

				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else
			return false;

	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public LayoutController getLayoutController() {
		return layoutController;
	}

	public TreeViewController getTreeViewController() {
		return treeViewController;
	}

	class AutoRefreshVisuThread implements Runnable {

		@Override
		public void run() {

			logger.info(getClass().getSimpleName() + "; " + Thread.currentThread().getName() + "; Status: "
					+ Thread.currentThread().getState() + ";");

			while (!Thread.currentThread().isInterrupted()) {

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if (primaryStage.isShowing()) {
							for (OverviewController controller : treeViewController.getOverviewControllerList()) {
								controller.updateComponents();
							}

							layoutController.updateResultView();
						}
					}
				});

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();

					logger.info(e.getLocalizedMessage());

					Thread.currentThread().interrupt();
				}
			}

		}

	}

	class AutoRefreshDataThread implements Runnable {

		@Override
		public void run() {

			logger.info(getClass().getSimpleName() + "; " + Thread.currentThread().getName() + "; Status: "
					+ Thread.currentThread().getState());

			while (!Thread.currentThread().isInterrupted()) {
				if (primaryStage.isShowing()) {

					for (OverviewController controller : treeViewController.getOverviewControllerList()) {
						controller.getDataFromDatabase();
					}

					layoutController.getDataTableView();

				}

				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						layoutController.setRefreshDate();
					}
				});

				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();

					logger.info(e.getLocalizedMessage());

					Thread.currentThread().interrupt();
				}

			}

		}

	}

	private void checkLicense() {

		try {
			List<String> networkMacs = LicenseManager.getNetworkMacs();

			for (int i = 0; i < networkMacs.size() && !LicenseManager.licenseValid
					&& !LicenseManager.licenseFileNotFound; i++) {

				LicenseManager.checkIfLicenseIsValid(resources.getString("appname1") + "Data" + File.separator,
						Constants.LICENSE_FILENAME,
						LicenseManager.formatMacAdress((networkMacs.get(i).replaceAll(":", ""))));

				if (LicenseManager.licenseValid) {
					logger.info("Lizenz gültig für MAC-Adresse: " + networkMacs.get(i) + " --> " + "["
							+ LicenseManager.networkName.get(i) + "]");
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!LicenseManager.licenseValid && !LicenseManager.licenseFileNotFound) {
			LicenseManager.showLicenseUnvalid();
		}
	}

	@Override
	public void init() {

		ImageView splash = new ImageView(new Image(SPLASH_IMAGE));

		loadProgress = new ProgressBar();
		loadProgress.setPrefWidth(Constants.SPLASH_WIDTH - 20);

		progressText = new Label("");
		progressText.setAlignment(Pos.CENTER);

		StringBuilder sb = new StringBuilder();
		appInfo = new Label("");

		sb.append(resources.getString("appname1"));
		sb.append(" (" + Main.VERSION + " - ");
		sb.append(Main.BUILD + " - ");
		sb.append(Main.DATE.substring(0, 26) + " $" + ")");

		appInfo.setFont(Font.font("System", FontWeight.BOLD, 11));
		appInfo.setTextFill(Color.DARKGREY);
		appInfo.setText(sb.toString().replace("$", ""));

		developerInfo = new Label("");
		developerInfo.setFont(Font.font("System", FontWeight.BOLD, 10));
		developerInfo.setTextFill(Color.DARKGREY);
		developerInfo.setText("\n" + resources.getString("companyname"));

		splashLayout = new VBox();
		splashLayout.getChildren().addAll(splash, loadProgress, progressText, developerInfo, appInfo);
		splashLayout.setStyle(
				"-fx-padding: 5; " + "-fx-background-color: #DAE6F3; " + "-fx-border-width:5; " + "-fx-border-color: "
						+ "linear-gradient(" + "to bottom, " + "#7ebcea, " + "derive(#7ebcea, 50%)" + ");");
		splashLayout.setEffect(new DropShadow());
	}

	private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
		progressText.textProperty().bind(task.messageProperty());
		loadProgress.progressProperty().bind(task.progressProperty());
		task.stateProperty().addListener((observableValue, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				loadProgress.progressProperty().unbind();
				loadProgress.setProgress(1);
				initStage.toFront();
				FadeTransition fadeSplash = new FadeTransition(Duration.seconds(fadeTransitionsTime), splashLayout);
				fadeSplash.setFromValue(1.0);
				fadeSplash.setToValue(0.0);
				fadeSplash.setOnFinished(actionEvent -> initStage.close());
				fadeSplash.play();

				initCompletionHandler.complete();
			} // todo add code to gracefully handle other task states.
		});

		Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
		final Rectangle2D bounds = Screen.getPrimary().getBounds();
		initStage.setScene(splashScene);
		initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - Constants.SPLASH_WIDTH / 2);
		initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - Constants.SPLASH_HEIGHT / 2);
		initStage.getIcons().add(new Image(APP_ICON));
		initStage.setTitle(resources.getString("appname1"));
		initStage.initStyle(StageStyle.TRANSPARENT);
		initStage.setResizable(false);

		initStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				logger.info("Programm beenden");
				ProgramChecker.closeSocket();
				Platform.exit();
				System.exit(0);

			}
		});

		initStage.setAlwaysOnTop(true);

		if (showSplashScreen)
			initStage.show();
	}

	public interface InitCompletionHandler {
		void complete();
	}
}
