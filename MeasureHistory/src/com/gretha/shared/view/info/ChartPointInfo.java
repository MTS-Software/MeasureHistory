package com.gretha.shared.view.info;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class ChartPointInfo extends Alert {

	public ChartPointInfo(String chartPointInfo, Stage dialogStage) {

		super(AlertType.INFORMATION);

		initOwner(dialogStage);

		setTitle("Information");
		setHeaderText("Diagrammpunkt");
		setContentText(chartPointInfo);
	}

}
