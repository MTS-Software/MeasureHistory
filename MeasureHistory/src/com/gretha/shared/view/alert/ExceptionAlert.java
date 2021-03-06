package com.gretha.shared.view.alert;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ExceptionAlert extends Alert {

	public ExceptionAlert(Stage dialogStage, Exception e) {

		super(AlertType.ERROR);

		initOwner(dialogStage);

		setTitle("Fehler");
		setHeaderText("Ein unerwarteter Fehler ist aufgetreten");

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
