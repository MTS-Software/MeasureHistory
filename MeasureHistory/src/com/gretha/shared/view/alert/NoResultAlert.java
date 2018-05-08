package com.gretha.shared.view.alert;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class NoResultAlert extends Alert {

	public NoResultAlert(Stage dialogStage, String contentText) {

		super(AlertType.INFORMATION);

		initOwner(dialogStage);

		setTitle("Kein Ergebnis");
		setHeaderText("Suchergebnis");

		setContentText("Keine Ergebnisse gefunden.");

		if (contentText != null)
			if (contentText != "")
				setContentText("Keine Ergebnisse für " + contentText + " gefunden.");

	}

}
