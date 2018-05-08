package com.gretha.shared.view.info;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class AdhocTableMaxSizeReached extends Alert {

	public AdhocTableMaxSizeReached(Stage dialogStage) {

		super(AlertType.INFORMATION);

		initOwner(dialogStage);

		setTitle("Information");
		setHeaderText("Ad-Hoc Tabelle");
		setContentText("Maximale Anzahl an Spalten (Prozesse) erreicht!");
	}

}
