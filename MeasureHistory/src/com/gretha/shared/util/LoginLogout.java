package com.gretha.shared.util;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.log4j.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Ermöglicht ein Login/Logout via vordefinierte Benutzerebenen
 * 
 * @author Michael Grebesits, Ing.
 * 
 * @version 1.3
 * 
 */
public class LoginLogout {

	private static final Image IMAGE = new Image(Constants.ICON_USER_LOGIN);
	private static final Logger logger = Logger.getLogger(LoginLogout.class);
	private static final EUserLevels USER_LEVEL_NULL = null;
	public static EUserLevels actUserLevel;

	/**
	 * Setzt den UserLevel auf "GUEST = Level 0"
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return true
	 */
	public static boolean logout() {

		if (isUserLoggedIn()) {
			logger.info("Logout (UserName: " + actUserLevel.getUserName() + " / UserLevel: "
					+ actUserLevel.getUserLevel() + ")");
		}

		actUserLevel = EUserLevels.GUEST;

		return true;
	}

	/**
	 * Öffnet den Login Dialog in Abhängigkeit des minimum UserLevel (Login Dialog
	 * öffnet sich bei einer Benutzerberechtigung kleiner als minimum Level)
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @param requestetUserLevel
	 * @return loginOK
	 */
	public static boolean login(EUserLevels minimumUserLevel) {

		boolean loginOK = false;

		initActUserLevel();

		try {

			if (minimumUserLevel != EUserLevels.DEACTIVATED) {

				if ((actUserLevel.getUserLevel() < minimumUserLevel.getUserLevel())
						|| minimumUserLevel == EUserLevels.ALWAYS_LOGIN) {

					// Create the custom dialog.
					Dialog<Pair<String, String>> dialog = new Dialog<>();
					dialog.setTitle("Login");
					dialog.getDialogPane().getScene().getStylesheets().add(Constants.STYLESHEET);

					dialog.setHeaderText("Benutzer und Passworteingabe erforderlich");
					logger.info("Login erforderlich (UserName: " + minimumUserLevel.getUserName() + " / UserLevel: "
							+ minimumUserLevel.getUserLevel() + ")");

					// Set the icon (must be included in the project).
					dialog.setGraphic(new ImageView(IMAGE));

					Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
					stage.getIcons().add(IMAGE);

					// Set the button types.
					ButtonType loginButtonType = new ButtonType("Login", ButtonData.OK_DONE);
					dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

					// Create the username and password labels and fields.
					GridPane grid = new GridPane();
					grid.setHgap(10);
					grid.setVgap(10);
					grid.setPadding(new Insets(20, 150, 10, 10));

					TextField username = new TextField();
					username.setPromptText("Benutzername");
					PasswordField password = new PasswordField();
					password.setPromptText("Passwort");

					grid.add(new Label("Benutzername:"), 0, 0);
					grid.add(username, 1, 0);
					grid.add(new Label("Passwort:"), 0, 1);
					grid.add(password, 1, 1);

					// Enable/Disable login button depending on whether a
					// username
					// was
					// entered.
					Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
					loginButton.setDisable(true);

					// Do some validation (using the Java 8 lambda syntax).
					username.textProperty().addListener((observable, oldValue, newValue) -> {
						loginButton.setDisable(newValue.trim().isEmpty());
					});

					dialog.getDialogPane().setContent(grid);

					// Request focus on the username field by default.
					Platform.runLater(() -> username.requestFocus());

					// Convert the result to a username-password-pair when the
					// login
					// button
					// is clicked.
					dialog.setResultConverter(dialogButton -> {
						if (dialogButton == loginButtonType) {
							return new Pair<>(username.getText(), password.getText());
						}
						return null;
					});

					Optional<Pair<String, String>> result = dialog.showAndWait();

					boolean userAndPasswordOK = checkUserAndPassword(result, minimumUserLevel);

					if (userAndPasswordOK && (actUserLevel.getUserLevel() >= minimumUserLevel.getUserLevel())) {
						loginOK = true;
					} else if (userAndPasswordOK && (actUserLevel.getUserLevel() < minimumUserLevel.getUserLevel())
							&& minimumUserLevel != EUserLevels.ALWAYS_LOGIN) {
						loginOK = false;
						openPasswordDialogLowLevel(minimumUserLevel, actUserLevel);
					} else {
						loginOK = false;
					}

				} else {
					loginOK = true;
				}

			} else {
				loginOK = true;
			}
		} catch (NoSuchElementException e) {
			logger.info("Login abrechen");
		}

		return loginOK;

	}

	private static void openPasswordDialogFail(EUserLevels minimumUserLevel) {

		logger.info("Login fehlgeschlagen");

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Login fehlgeschlagen");
		alert.setHeaderText("Benutzer und/oder Passworteingabe falsch");
		alert.setContentText("Eingabe kontrollieren bzw. erneut durchführen!");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(IMAGE);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			login(minimumUserLevel);
		}

	}

	private static void openPasswordDialogLowLevel(EUserLevels minimumUserLevel, EUserLevels actUserLevel) {

		logger.info("Login Benutzerberechtigung zu niedrig");

		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Login Benutzerberechtigung");
		alert.setHeaderText("Benutzer '" + actUserLevel.getUserName() + "' hat keine Berechtigung");
		alert.setContentText("Es ist eine höhere Benutzerberechtigung erforderlich!");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(IMAGE);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			login(minimumUserLevel);
		}

	}

	private static boolean checkUserAndPassword(Optional<Pair<String, String>> result, EUserLevels minimumUserLevel) {

		boolean userAndPasswordOK = false;

		try {
			if (result.get().getKey().matches(EUserLevels.GUEST.getUserName())
					&& result.get().getValue().matches(EUserLevels.GUEST.getUserPassword())) {

				actUserLevel = EUserLevels.GUEST;
				userAndPasswordOK = true;

				logger.info("Login erfolgreich (UserName: " + EUserLevels.GUEST.getUserName() + " / UserLevel: "
						+ EUserLevels.GUEST.getUserLevel() + ")");

			} else if (result.get().getKey().matches(EUserLevels.USER.getUserName())
					&& result.get().getValue().matches(EUserLevels.USER.getUserPassword())) {

				actUserLevel = EUserLevels.USER;
				userAndPasswordOK = true;

				logger.info("Login erfolgreich (UserName: " + EUserLevels.USER.getUserName() + " / UserLevel: "
						+ EUserLevels.USER.getUserLevel() + ")");

			} else if (result.get().getKey().matches(EUserLevels.ADMINISTRATOR.getUserName())
					&& result.get().getValue().matches(EUserLevels.ADMINISTRATOR.getUserPassword())) {

				actUserLevel = EUserLevels.ADMINISTRATOR;
				userAndPasswordOK = true;

				logger.info("Login erfolgreich (UserName: " + EUserLevels.ADMINISTRATOR.getUserName() + " / UserLevel: "
						+ EUserLevels.ADMINISTRATOR.getUserLevel() + ")");
			}

			else {
				openPasswordDialogFail(minimumUserLevel);
			}

		} catch (

		NoSuchElementException e) {
			logger.info("Login abrechen");
		}
		return userAndPasswordOK;
	}

	private static void initActUserLevel() {

		if (actUserLevel == USER_LEVEL_NULL) {
			actUserLevel = EUserLevels.GUEST;
		}
	}

	/**
	 * Liefert den aktuellen UserLevel als Enum zurück
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return actUserLevel as Enum
	 */
	public static EUserLevels getUserLevelEnum() {

		initActUserLevel();

		return actUserLevel;
	}

	/**
	 * Liefert den den UserLevel als Integer zurück
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return actUserLevel as Integer
	 */
	public static Integer getUserLevelInteger() {

		initActUserLevel();

		return actUserLevel.getUserLevel();
	}

	/**
	 * Liefert den aktuellen UserName als String zurück
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return actUserLevel as String from UserName
	 */
	public static String getUserNameString() {

		initActUserLevel();

		return actUserLevel.getUserName();
	}

	/**
	 * Liefert den Status des aktuellen UserLevel in Abhängigkeit des minimum
	 * UserLevel (LevelOK bei einer Benutzerberechtigung größer gleich minimum
	 * Level)
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return actUserLevelOK
	 */
	public static boolean isUserLevelOK(EUserLevels minimumUserLevel) {

		initActUserLevel();

		if (actUserLevel.getUserLevel() >= minimumUserLevel.getUserLevel()) {
			return true;
		}

		else {
			return false;
		}

	}

	/**
	 * Liefert zurück ob ein User angemeldet ist --> UserLevel höher "0"
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return isUserLoggedIn
	 */
	public static boolean isUserLoggedIn() {

		initActUserLevel();

		if (actUserLevel.getUserLevel() != 0)
			return true;
		else
			return false;
	}

	/**
	 * Enumerator Benutzerbereichtigungen
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @category GUEST = Level 0 = default
	 * @category USER = Level 1
	 * @category ADMINISTRATOR = Level = 100
	 * @category ALWASS_LOGIN = Level 110 = Login Dialog öffnet sich immer
	 * @category DEACTIVATED = 120 = Login wird deaktiviert
	 */
	public static enum EUserLevels {

		GUEST("guest", 0, ""), USER("user", 1, "user"), ADMINISTRATOR("admin", 100,
				"mongo"), ALWAYS_LOGIN("always login", 110, ""), DEACTIVATED("deactivated", 120, "");

		private String userName;
		private int userLevel;
		private String userPassword;

		EUserLevels(String userName, int userLevel, String userPassword) {
			this.userName = userName;
			this.userLevel = userLevel;
			this.userPassword = userPassword;
		}

		public String getUserName() {
			return userName;
		}

		public int getUserLevel() {
			return userLevel;
		}

		public String getUserPassword() {
			return userPassword;
		}
	}
}
