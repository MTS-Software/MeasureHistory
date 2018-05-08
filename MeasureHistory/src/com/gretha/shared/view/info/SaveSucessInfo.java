package com.gretha.shared.view.info;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class SaveSucessInfo extends Alert {

	public SaveSucessInfo(Stage dialogStage, File file) {

		super(AlertType.INFORMATION);

		initOwner(dialogStage);

		setTitle("Speichern abgeschlossen");
		setHeaderText("Datei erfolgreich gespeichert");
		setContentText(file.toString());
	}

}
