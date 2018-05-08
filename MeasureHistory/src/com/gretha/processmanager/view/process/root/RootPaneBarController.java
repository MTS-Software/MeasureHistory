package com.gretha.processmanager.view.process.root;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.filter.FilterSettingsController;
import com.gretha.shared.model.Filter;
import com.gretha.shared.util.Constants;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RootPaneBarController implements Initializable {

	private static final Logger logger = Logger.getLogger(RootPaneBarController.class);
	private ResourceBundle resources;
	private Stage dialogStage;

	private Filter filter;
	private RootPaneController rootPaneController;
	private FilterSettingsController filterSettingsController;

	@FXML
	private Button btnRefresh;
	@FXML
	private Button btnOpnTableFilter;
	@FXML
	private Button btnResetFilter;
	@FXML
	private Label lblRefreshDate;
	@FXML
	private TextField searchField;
	@FXML
	private Button clearButton;
	@FXML
	private Label foundValuesLabel;

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public RootPaneBarController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		btnResetFilter.setDisable(true);

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

	public void setData() {

		this.dialogStage.setTitle(
				rootPaneController.getProcess().getStation() + ": " + rootPaneController.getProcess().getName());

		DateFormat df = new SimpleDateFormat("EEEE, dd. MMMM yyyy HH:mm:ss");
		Date date = Calendar.getInstance().getTime();
		String formatDate = df.format(date);
		lblRefreshDate.setText(formatDate);

	}

	public void setMain(Main main) {

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	@FXML
	public void handleFilterSettings() {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/process/filter/FilterSettings.fxml"));
			loader.setResources(resources);
			BorderPane pane = (BorderPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			dialogStage.setResizable(false);

			Scene scene = new Scene(pane);
			scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());
			dialogStage.setScene(scene);

			// Set the settings into the controller.
			filterSettingsController = loader.getController();
			filterSettingsController.setDialogStage(dialogStage);
			filterSettingsController.setData(rootPaneController.getProcess(), rootPaneController.getResults());

			if (filter == null)
				filter = new Filter();

			filterSettingsController.setFilterData(filter);

			filterSettingsController.getUebernehmenButton().setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {

					// Für den Fall das der Filter über File geladen wurde muss dieser hier
					// übernommen werden
					filter = filterSettingsController.getFilterData();

					filterSettingsController.getFilteredResults(false);
					setFilteredData(filterSettingsController);
					setItemsFilterActivated();

				}
			});

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

			if (filterSettingsController.isOkClicked()) {

				// Für den Fall das der Filter über File geladen wurde muss dieser hier
				// übernommen werden
				filter = filterSettingsController.getFilterData();

				setFilteredData(filterSettingsController);
				setItemsFilterActivated();

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void setFilteredData(FilterSettingsController filterSettingsController) {
		rootPaneController.setResults(filterSettingsController.getProcessResults());
		rootPaneController.setControllerData();

	}

	private void setItemsFilterActivated() {
		if (filter.isActivated()) {
			Image image = new Image(getClass().getClassLoader()
					.getResourceAsStream("com/gretha/shared/resource/icons/filter_activ_24.png"));
			btnOpnTableFilter.setGraphic(new ImageView(image));

			btnResetFilter.setDisable(false);

		} else {
			Image image = new Image(getClass().getClassLoader()
					.getResourceAsStream("com/application/resource/icons/filter_inactiv24.png"));
			btnOpnTableFilter.setGraphic(new ImageView(image));

			btnResetFilter.setDisable(true);

		}
	}

	public Button getBtnRefresh() {
		return btnRefresh;
	}

	public TextField getSearchFiled() {
		return searchField;
	}

	@FXML
	public void handleFilterReset() {

		Image image = new Image(getClass().getClassLoader()
				.getResourceAsStream("com/gretha/shared/resource/icons/filter_inactiv_24.png"));
		btnOpnTableFilter.setGraphic(new ImageView(image));

		filter = null;

		btnResetFilter.setDisable(true);

		rootPaneController.setData(rootPaneController.getProcess());

	}

	public Filter getFilter() {
		return filter;
	}

	public FilterSettingsController getFilterSettingsController() {
		return filterSettingsController;
	}

	public RootPaneController getRootPaneController() {
		return rootPaneController;
	}

	public void setRootPaneController(RootPaneController rootPaneController) {
		this.rootPaneController = rootPaneController;
	}

	public Label getFoundValuesLabel() {
		return foundValuesLabel;
	}

}
