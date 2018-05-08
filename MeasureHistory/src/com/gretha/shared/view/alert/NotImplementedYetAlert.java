package com.gretha.shared.view.alert;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class NotImplementedYetAlert extends Alert {

	public NotImplementedYetAlert(Stage dialogStage) {

		super(AlertType.INFORMATION);

		initOwner(dialogStage);

		setTitle("Funktion nicht implementiert");
		setHeaderText("Diese Funktion wurde noch nicht implementiert");
		setContentText("Bei Fragen kontaktieren Sie die Entwickler.");
	}

}
