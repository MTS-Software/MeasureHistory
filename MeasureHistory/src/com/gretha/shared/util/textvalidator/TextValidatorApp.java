package com.gretha.shared.util.textvalidator;

import com.gretha.shared.util.Constants;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * 
 * A sample that demonstrates text validation. If the value in the TextField is
 * 
 * a small number, the field becomes yellow. If the value in the TextField is
 * 
 * not a number, the field becomes red.
 * 
 */

public class TextValidatorApp extends Application {

	public Parent createContent() {

		String validatorCss = Constants.STYLESHEET;

		TextField dateField = new TextField();

		dateField.setPromptText("Enter a Large Number");

		dateField.setMaxHeight(Region.USE_PREF_SIZE);

		TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();

		pane.setContent(dateField);

		pane.setValidator((TextField control) -> {

			try {

				String text = control.getText();

				if (text == null || text.trim().equals("")) {

					return null;

				}

				double d = Double.parseDouble(text);

				if (d < -10000 || d > 10000) {

					return new ValidationResult("Should be > -1000000000 or < 1000000000",
							ValidationResult.Type.WARNING);

				}

				return null; // succeeded

			} catch (Exception e) {

				// failed

				return new ValidationResult("Bad number", ValidationResult.Type.ERROR);

			}

		});

		StackPane rootSP = new StackPane();

		rootSP.setPadding(new Insets(12));

		rootSP.getChildren().add(pane);

		pane.getStylesheets().add(validatorCss);

		return rootSP;

	}

	@Override

	public void start(Stage primaryStage) throws Exception {

		primaryStage.setScene(new Scene(createContent()));

		primaryStage.show();

	}

	/**
	 * 
	 * Java main for when running without JavaFX launcher
	 * 
	 */

	public static void main(String[] args) {

		launch(args);

	}

}