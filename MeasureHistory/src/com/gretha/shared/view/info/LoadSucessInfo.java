package com.gretha.shared.view.info;

import java.io.File;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class LoadSucessInfo extends Alert {

	public LoadSucessInfo(Stage dialogStage, File file) {

		super(AlertType.INFORMATION);

		initOwner(dialogStage);

		setTitle("Laden abgeschlossen");
		setHeaderText("Datei erfolgreich geladen");
		setContentText(file.toString());
	}

}
