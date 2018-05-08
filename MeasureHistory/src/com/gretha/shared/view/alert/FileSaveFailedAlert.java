package com.gretha.shared.view.alert;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class FileSaveFailedAlert extends Alert {

	public FileSaveFailedAlert(Stage dialogStage, File file, Exception e) {

		super(AlertType.ERROR);

		initOwner(dialogStage);

		setTitle("Datei speichern");
		setHeaderText("Datei speichern fehlgeschlagen");
		setContentText("Vorgang konnte nicht abgeschlossen werden.\n" + "(" + file + ")\n\n");

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		getDialogPane().setExpandableContent(textArea);
	}

}
