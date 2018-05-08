package com.gretha.shared.util;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.apache.log4j.Logger;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * ProgramChecker
 *
 */
public class ProgramChecker {
	private static final Logger logger = Logger.getLogger(ProgramChecker.class);
	private static ServerSocket socket;

	/**
	 * Kontrolliert ob bereits ein Programm den entsprechenden Port geoeffnet hat,
	 * und verhindert einen erneuten Start
	 * 
	 * @author Markus Thaler, Ing.
	 */
	public static void checkIfRunning(int PORT) {
		try {
			socket = new ServerSocket(PORT, 0, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));

			if (socket.isBound())
				logger.info(socket + " erfolgreich geöffnet");
			else
				logger.info(socket + " konnte nicht geöffnet werden");

		} catch (BindException e) {
			showExceptionAlertDialog(e);
			System.exit(1);
		} catch (IOException e) {
			showExceptionAlertDialog(e);
			e.printStackTrace();
			System.exit(2);
		}
	}

	public static boolean closeSocket() {
		try {
			if (!socket.isClosed()) {
				logger.info(socket + " wird geschlossen");
				socket.close();
			}

			if (socket.isClosed()) {
				logger.info(socket + " erfolgreich geschlossen");

			} else {
				logger.info(socket + " konnte nicht geschlossen werden");

			}
		} catch (IOException e) {
			showExceptionSockedDialog(e);
		}
		return socket.isClosed();
	}

	private static void showExceptionAlertDialog(Exception e) {

		logger.info("Programm läuft bereits");

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Programminformation");
		alert.setHeaderText("Programm läuft bereits");
		alert.setContentText("Das Programm kann nur einmal gestartet werden");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_ABOUT));

		alert.showAndWait();

	}

	private static void showExceptionSockedDialog(Exception e) {

		logger.info("Programm läuft bereits");

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception");
		alert.setHeaderText("Ein Fehler beim Schließen des Sockets ist aufgetreten");
		alert.setContentText(String.valueOf(e));

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		alert.showAndWait();

	}
}
