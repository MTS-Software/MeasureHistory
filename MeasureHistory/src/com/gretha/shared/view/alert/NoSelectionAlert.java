package com.gretha.shared.view.alert;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class NoSelectionAlert extends Alert {

	public NoSelectionAlert(Stage dialogStage) {

		super(AlertType.INFORMATION);

		initOwner(dialogStage);

		setTitle("Keine Auswahl");
		setHeaderText("Keine Zeile selektiert");
		setContentText("Bitte eine Zeile selektieren.");
	}

}
