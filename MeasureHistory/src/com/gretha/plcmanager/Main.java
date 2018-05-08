package com.gretha.plcmanager;

import java.awt.SystemTray;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.gretha.licensemanager.LicenseManager;
import com.gretha.plcmanager.view.root.LayoutController;
import com.gretha.plcmanager.view.root.SettingsController;
import com.gretha.plcmanager.view.root.SettingsController.EStartWindow;
import com.gretha.plcmanager.view.root.TreeViewController;
import com.gretha.shared.db.util.EDatabase;
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
	public final static String BUILD = "$Rev: 1655 $";
	public final static String DATE = "$Date: 2017-10-15 18:15:22 +0200 (So, 15 Okt 2017) $";

	// Java Entwicklungsversion
	public final static String JDK = "1.8.0_141";

	private static final Logger logger = Logger.getLogger(Main.class);
	private ResourceBundle resources = ResourceBundle.getBundle("language");

	public final static String APP_ICON = Constants.APP_ICON_PLCMANAGER;

	// Port für ProgrammChecker
	public final static int PORT = 9998;

	// Für Splashscreen
	public static final String SPLASH_IMAGE = Constants.SPLASHSCREEN_IMAGE_PLCMANAGER;

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
	private BorderPane rootLayout;

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
				getClass().getClassLoader().getResource("log4j" + resources.getString("appname2") + ".properties"));

		logger.info("Programm starten");

		ProgramChecker.checkIfRunning(PORT);

		if (LicenseManager.licenseValid == false) {
			logger.info("Kontrolle ob Lizenz gültig" + " (" + resources.getString("appname2") + "Data" + File.separator
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

					ApplicationProperties.configure("application.properties", resources.getString("appname2") + "Data",
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
						SystemTrayIcon.setTrayIcon(primaryStage, resources.getString("appname2"),
								getClass().getClassLoader().getResource(Main.APP_ICON));
					}

					if (ApplicationProperties.getInstance().getProperty("db_vendor")
							.equalsIgnoreCase(EDatabase.MYSQL.getVendor()))
						dbVendor = EDatabase.MYSQL.toString();
					if (ApplicationProperties.getInstance().getProperty("db_vendor")
							.equalsIgnoreCase(EDatabase.SQLSERVER.getVendor()))
						dbVendor = EDatabase.SQLSERVER.toString();

					primaryStage.setTitle(resources.getString("appname2") + " @" + dbVendor + " on "
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
			Scene scene = new Scene(rootLayout);
			scene.getStylesheets().add(Constants.STYLESHEET);

			primaryStage.setWidth(Constants.DEFAULT_ROOT_LAYOUT_WIDTH);
			primaryStage.setHeight(Constants.DEFAULT_PROCESS_ROOT_PANE_HEIGTH);
			primaryStage.setScene(scene);

			// Give the controller access to the main app.
			LayoutController controller = loader.getController();
			controller.setMain(this);

			// Wenn keine gültige Lizenz vorhanden bzw. gefunden dann darf auch
			// nichts bearbeitet werden
			if (LicenseManager.licenseValid) {
				showTreeView();
			}

			if (ApplicationProperties.getInstance().getProperty("start_window")
					.equalsIgnoreCase(EStartWindow.MINIMIZED.getNr())) {
				logger.info("Programm wurde minimiert gestartet");
				SystemTrayIcon.showInfoMessage("Programm wurde minimiert gestartet");
			} else {
				primaryStage.show();
			}

		} catch (IOException e) {

			if (logger.isInfoEnabled()) {
				logger.error(e);
			}

			e.printStackTrace();
		}

	}

	public void showTreeView() {

		try {

			// Load overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/root/TreeView.fxml"));
			loader.setResources(resources);
			AnchorPane pane = (AnchorPane) loader.load();

			rootLayout.setCenter(pane);

			// Give the controller access to the main app.
			TreeViewController controller = loader.getController();
			controller.setDialogStage(primaryStage);

			controller.setMain(this);

		} catch (IOException e) {
			e.printStackTrace();
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
			dialogStage.setTitle(resources.getString("appname2") + " - " + resources.getString("settings"));
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(Constants.STYLESHEET);
			dialogStage.setScene(scene);

			// Set the person into the controller.
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

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	private void checkLicense() {

		try {
			List<String> networkMacs = LicenseManager.getNetworkMacs();

			for (int i = 0; i < networkMacs.size() && !LicenseManager.licenseValid
					&& !LicenseManager.licenseFileNotFound; i++) {

				LicenseManager.checkIfLicenseIsValid(resources.getString("appname2") + "Data" + File.separator,
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

		sb.append(resources.getString("appname2"));
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
		initStage.getIcons().add(new Image(Main.APP_ICON));
		initStage.setTitle(resources.getString("appname2"));
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
