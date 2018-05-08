package com.gretha.shared.util;

import java.util.Optional;

import com.gretha.processmanager.Main;

import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Ermöglicht die Eingabe eines Wertes und liefert diesen zurück
 * 
 */
public class InputChartBound {

	/**
	 * Öffnet den TextInputDialog
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @param boundText
	 * @param actBound
	 */
	public static double getBound(String boundText, String actBound) {

		TextInputDialog dialog = new TextInputDialog(String.valueOf(actBound));
		dialog.getDialogPane().getScene().getStylesheets().add(Constants.STYLESHEET);
		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Main.APP_ICON));
		dialog.setTitle(boundText);
		dialog.setHeaderText("Eingabe für " + boundText);
		dialog.setContentText(boundText);

		Optional<String> result = dialog.showAndWait();

		if (result.isPresent()) {

			double inputBound = 0;

			try {

				inputBound = Double.valueOf(result.get().replace(",", "."));
				return inputBound;

			} catch (Exception e) {

			}

		}

		return Double.valueOf(actBound);
	}
}
