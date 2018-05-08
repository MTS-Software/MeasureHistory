package com.gretha.licensemanager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.gretha.shared.util.Constants;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class LicenseManager extends Application {

	private static TextField valueInputMacAdres;
	private static final Logger logger = Logger.getLogger(LicenseManager.class);
	public static boolean licenseValid;
	public static boolean licenseFileNotFound;
	public static final List<String> networkMacs = new ArrayList<>();
	public static final List<String> networkName = new ArrayList<>();
	public static final List<String> networkInfo = new ArrayList<>();
	public static long usedMacAdressCoded = 0;

	public static void main(String[] args) {

		launch(args);
	}

	@Override
	public void start(final Stage initStage) {

		startLicenseGenerator();

	}

	public static void startLicenseGenerator() {

		Stage stage = new Stage();

		stage.setTitle("Lizenz Generator");

		final GridPane inputGridPane = new GridPane();
		final Button saveButton = new Button("Lizenz speichern");

		saveButton.setMinWidth(135);

		valueInputMacAdres = new TextField();
		valueInputMacAdres.setPromptText("MAC-Adresse eingeben");

		double inputValuesWidth = 250;
		valueInputMacAdres.setPrefWidth(inputValuesWidth);
		valueInputMacAdres.setMinWidth(inputValuesWidth);
		valueInputMacAdres.setMaxWidth(inputValuesWidth);

		GridPane.setConstraints(valueInputMacAdres, 0, 0);

		GridPane.setConstraints(saveButton, 0, 1);
		inputGridPane.setHgap(6);
		inputGridPane.setVgap(6);
		inputGridPane.getChildren().addAll(saveButton, valueInputMacAdres);

		final Pane rootGroup = new VBox(12);
		rootGroup.getChildren().addAll(inputGridPane);
		rootGroup.setPadding(new Insets(12, 12, 12, 12));

		stage.setScene(new Scene(rootGroup));

		stage.getScene().getStylesheets().addAll(Constants.STYLESHEET);
		stage.getIcons().add(new Image(Constants.ICON_LICENSE));
		stage.setResizable(false);

		stage.show();

		saveButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				try {
					saveAs(stage);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

	}

	private static void saveAs(Stage stage) throws IOException {

		File file = null;

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Speichern unter");
		FileChooser.ExtensionFilter extFilterTxt = new FileChooser.ExtensionFilter("LICENSE files (*.license)",
				"*.license");
		chooser.getExtensionFilters().addAll(extFilterTxt);

		chooser.setInitialFileName("application" + ".license");

		file = chooser.showSaveDialog(stage);

		if (file != null) {

			if (chooser.getSelectedExtensionFilter() == extFilterTxt) {

				Writer writer = null;
				writer = new BufferedWriter(new FileWriter(file));

				writer.write(String.valueOf(formatMacAdress(valueInputMacAdres.getText().replaceAll(":", ""))));

				writer.flush();
				writer.close();

			}

		}
	}

	/**
	 * Formatiert eine MAC Adresse in einen gütligen LicenseCode
	 * 
	 * @author Michael Grebesits, Ing.
	 * @return macAdressCoded
	 */
	public static long formatMacAdress(String mac) {

		long macAdressCoded = 0;

		for (int i = 0; i < mac.length(); i++) {
			Byte actCharAsByte = (byte) mac.charAt(i);
			macAdressCoded = macAdressCoded + actCharAsByte;
		}

		return macAdressCoded * 4711;
	}

	/**
	 * Kontrolliert ob eine gültige Lizenzdatei vorhanden ist
	 * 
	 * @author Michael Grebesits, Ing.
	 * @return licenseValid
	 */
	public static boolean checkIfLicenseIsValid(String licenseFilePath, String licenseFileName,
			long usedMacAdressCoded) {

		File file = new File(licenseFilePath + licenseFileName);
		String macAdressCodedFromFile = "";
		licenseValid = false;
		licenseFileNotFound = false;

		try {

			Scanner scn = new Scanner(file);
			macAdressCodedFromFile = scn.next();

			if (String.valueOf(usedMacAdressCoded).equals(macAdressCodedFromFile)) {
				licenseValid = true;
				LicenseManager.usedMacAdressCoded = usedMacAdressCoded;
				logger.info("Lizenz gültig");

			} else {
				licenseValid = false;

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			licenseFileNotFound = true;
			showLicenseFileNotFound();
		}

		return licenseValid;

	}

	/**
	 * Liefert die gefundenen MAC-Adressen zurück
	 * 
	 * @author Michael Grebesits, Ing.
	 * @return networkMacs
	 */
	public static List<String> getNetworkMacs() throws Exception {

		networkMacs.clear();

		for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
			byte[] adr = ni.getHardwareAddress();
			if (adr == null || adr.length != 6)
				continue;
			String mac = String.format("%02X:%02X:%02X:%02X:%02X:%02X", adr[0], adr[1], adr[2], adr[3], adr[4], adr[5]);
			networkMacs.add(mac);
			networkName.add(ni.getDisplayName());
			networkInfo.add(mac + " " + "[" + ni.getDisplayName() + "]");

		}

		return networkMacs;
	}

	private static void showLicenseFileNotFound() {

		logger.info("Keine Lizenzdatei gefunden");

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Lizenzinformation");
		alert.setHeaderText("Keine Lizenzdatei gefunden");
		alert.setContentText("Es muss eine Lizenzdatei (application.license) vorhanden sein");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_LICENSE));

		alert.showAndWait();

	}

	public static void showLicenseUnvalid() {

		logger.info("Keine gültige Lizenz vorhanden");

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Lizenzinformation");

		alert.setHeaderText("Keine gültige Lizenz vorhanden");
		alert.setContentText("Es muss eine gültige Lizenz vorhanden sein");

		TextArea textArea = new TextArea("Gefundene MAC-Adressen:\n\n" + networkInfoFormatted());
		textArea.setEditable(false);
		textArea.setWrapText(true);

		// alert.getDialogPane().setContent(textArea);
		alert.getDialogPane().setExpandableContent(textArea);

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_LICENSE));

		alert.showAndWait();
	}

	public static void showLicenseInformation() {

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Lizenzinformation");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_LICENSE));

		if (!licenseValid && !licenseFileNotFound) {
			showLicenseUnvalid();
		} else if (!licenseValid && licenseFileNotFound) {
			showLicenseFileNotFound();
		} else {
			alert.setHeaderText("Lizenz in Ordnung");
			alert.setContentText("Verwendeter Lizenzschlüssel: " + usedMacAdressCoded);
			// Set the icon (must be included in the project).
			alert.setGraphic(new ImageView(Constants.ICON_OK));

			TextArea textArea = new TextArea("Gefundene MAC-Adressen:\n\n" + networkInfoFormatted());
			textArea.setEditable(false);
			textArea.setWrapText(true);

			// alert.getDialogPane().setContent(textArea);
			alert.getDialogPane().setExpandableContent(textArea);

			alert.showAndWait();
		}
	}

	private static String networkInfoFormatted() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String text : networkInfo) {
			i++;
			if (sb.length() > 0) {
				sb.append("\n");
			}
			sb.append(i + ": " + text);
		}
		return sb.toString();
	}

}
