package com.gretha.processmanager.view.config.root;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class OverviewButtonsController {

	private static final Logger logger = Logger.getLogger(OverviewButtonsController.class);

	@FXML
	private Button newButton;
	@FXML
	private Button editButton;
	@FXML
	private Button deleteButton;
	@FXML
	private Button refreshButton;
	@FXML
	private Button clearButton;
	@FXML
	private TextField searchField;
	@FXML
	private Label refreshDateLabel;
	@FXML
	private Label foundValuesLabel;

	public OverviewButtonsController() {
	}

	@FXML
	private void initialize() {

		clearButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				searchField.setText("");
			}
		});

		// Wenn das Searchfild leer ist, wird der LöschenButton Disabled
		BooleanBinding bb = new BooleanBinding() {
			{
				super.bind(searchField.textProperty());
			}

			@Override
			protected boolean computeValue() {
				return (searchField.getText().isEmpty());
			}
		};

		clearButton.disableProperty().bind(bb);

	}

	public Button getNewButton() {
		return newButton;
	}

	public Button getEditButton() {
		return editButton;
	}

	public Button getDeleteButton() {
		return deleteButton;
	}

	public Button getClearButton() {
		return clearButton;
	}

	public TextField getSearchFiled() {
		return searchField;
	}

	public Button getRefreshButton() {
		return refreshButton;
	}

	public void setRefreshDate() {
		DateFormat df = new SimpleDateFormat("EEEE, dd. MMMM yyyy HH:mm:ss");
		Date date = Calendar.getInstance().getTime();
		String formatDate = df.format(date);
		refreshDateLabel.setText(formatDate);

	}

	public Label getfoundValuesLabel() {
		return foundValuesLabel;
	}

}