package com.gretha.plcmanager.view.root;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.licensemanager.LicenseManager;
import com.gretha.plcmanager.Main;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.LoginLogout;
import com.gretha.shared.util.ProgramChecker;
import com.gretha.shared.util.SystemTrayIcon;
import com.gretha.shared.util.WebBrowser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 
 * @author Markus Thaler
 */
public class LayoutController implements Initializable {

	private ResourceBundle resources;
	private static final Logger logger = Logger.getLogger(LayoutController.class);

	private Main main;

	@FXML
	private Label labelLeftStatus;
	@FXML
	private Label labelRightStatus;
	@FXML
	private Label labelCenterStatus;
	@FXML
	private MenuItem miLogin;
	@FXML
	private MenuItem miLogout;
	@FXML
	private MenuItem miLicenceGenerator;
	@FXML
	private Menu mBearbeiten;

	private Thread statusUpdateThread;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		statusUpdateThread = new Thread(new StatusUpdateThread());
		statusUpdateThread.setDaemon(true);
		statusUpdateThread.start();

		// Wenn keine gültige Lizenz vorhanden bzw. gefunden dann darf auch
		// nichts bearbeitet werden
		if (!LicenseManager.licenseValid) {
			mBearbeiten.setDisable(true);
		}
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param main
	 */
	public void setMain(Main main) {
		this.main = main;
	}

	@FXML
	private void handleSettings() {
		if (LoginLogout.login(LoginLogout.EUserLevels.ADMINISTRATOR)) {
			main.showSettingsDialog();
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

	@FXML
	private void handleAbout() {

		StringBuilder sb = new StringBuilder();

		sb.append(Main.VERSION + "\n");
		sb.append(Main.BUILD + "\n");
		sb.append(Main.DATE.substring(0, 26) + " $" + "\n");
		sb.append("JDK: " + Main.JDK);

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(resources.getString("about"));
		alert.setHeaderText(resources.getString("appname2") + "\n" + sb.toString().replace("$", ""));

		alert.setContentText(
				"Entwicklung:\n" + resources.getString("programer1") + "\n" + resources.getString("programer2"));

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_ABOUT));

		alert.show();

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
	private void openJavaWebbrowser() {
		if (LoginLogout.login(LoginLogout.EUserLevels.USER))
			WebBrowser.openURLinJavaBrowser("");

	}

	class StatusUpdateThread implements Runnable {

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
						labelCenterStatus.setText(resources.getString("companyname"));

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
}