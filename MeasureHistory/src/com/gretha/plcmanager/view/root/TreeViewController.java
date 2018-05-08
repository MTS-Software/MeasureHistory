package com.gretha.plcmanager.view.root;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.plcmanager.Main;
import com.gretha.plcmanager.plc.dao.PlcComDAO;
import com.gretha.plcmanager.plc.util.PLCManager;
import com.gretha.plcmanager.view.diagnose.PLCDiagnoseController;
import com.gretha.plcmanager.view.diagnose.TriggerDiagnoseController;
import com.gretha.shared.util.Constants;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 
 * @author Markus Thaler
 */
public class TreeViewController implements Initializable {

	private static final Logger logger = Logger.getLogger(TreeViewController.class);

	// Reference to the main application
	private Main main;
	private PLCManager pLCManager;
	private ResourceBundle resources;
	private Stage dialogStage;

	@FXML
	private BorderPane dataPane;
	@FXML
	private AnchorPane treePane;

	@FXML
	private TreeView<String> treeView;

	public TreeViewController() {
		pLCManager = new PLCManager();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		createTree();
		showHomeScreen();
	}

	public void createTree() {

		// Diagnose
		TreeItem<String> itemDiagnose = new TreeItem<>("Diagnose");
		itemDiagnose.setExpanded(true);

		TreeItem<String> itemDiagnoseTrigger = new TreeItem<>("Trigger");
		itemDiagnoseTrigger.setGraphic(new ImageView(new Image("com/gretha/shared/resource/icons/trigger16.png")));
		itemDiagnoseTrigger.setExpanded(false);
		itemDiagnose.getChildren().add(itemDiagnoseTrigger);

		TreeItem<String> itemDiagnosePlc = new TreeItem<>("PLC");
		itemDiagnosePlc.setGraphic(new ImageView(new Image("com/gretha/shared/resource/icons/electronic16.png")));
		itemDiagnosePlc.setExpanded(false);
		itemDiagnose.getChildren().add(itemDiagnosePlc);

		treeView.setRoot(itemDiagnose);
		treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY)
					if (event.getClickCount() == 2) {

						if (treeView.getSelectionModel().getSelectedItem().getValue().equalsIgnoreCase("Trigger"))
							showTriggerDiagnose();

						if (treeView.getSelectionModel().getSelectedItem().getValue().equalsIgnoreCase("PLC"))
							showPlcDiagnose();

					}

			}
		});

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();
		SeparatorMenuItem separator = new SeparatorMenuItem();

		MenuItem miExpandCollapse = new MenuItem("Erweitern");
		miExpandCollapse.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (!treeView.getSelectionModel().getSelectedItem().isExpanded()
						&& !treeView.getSelectionModel().getSelectedItem().isLeaf()) {
					treeView.getSelectionModel().getSelectedItem().setExpanded(true);
				} else {
					treeView.getSelectionModel().getSelectedItem().setExpanded(false);
				}
			}
		});

		MenuItem miOpen = new MenuItem("Öffnen");
		miOpen.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				if (treeView.getSelectionModel().getSelectedItem().getValue().equalsIgnoreCase("Trigger"))
					showTriggerDiagnose();

				if (treeView.getSelectionModel().getSelectedItem().getValue().equalsIgnoreCase("PLC"))
					showPlcDiagnose();

			}
		});

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miExpandCollapse, separator, miOpen);

		// When user right-click
		treeView.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {

				contextMenu.hide();

				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {

					if (!treeView.getSelectionModel().getSelectedItem().isLeaf())
						miOpen.setDisable(true);
					else
						miOpen.setDisable(false);

					if (treeView.getSelectionModel().getSelectedItem() != null) {

						if (treeView.getSelectionModel().getSelectedItem().isExpanded()
								&& !treeView.getSelectionModel().getSelectedItem().isLeaf()) {

							miExpandCollapse.setText("Reduzieren");
							miExpandCollapse.setDisable(false);
						}

						else if (!treeView.getSelectionModel().getSelectedItem().isExpanded()
								&& !treeView.getSelectionModel().getSelectedItem().isLeaf()) {

							miExpandCollapse.setText("Erweitern");
							miExpandCollapse.setDisable(false);
						}

						else {
							miExpandCollapse.setText("Reduzieren");
							miExpandCollapse.setDisable(true);
						}

						contextMenu.show(treeView, mouseEvent.getScreenX(), mouseEvent.getScreenY());

					}
				}
			}
		});
	}

	public void setDialogStage(Stage dialogStage) {

		this.dialogStage = dialogStage;
	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param main
	 */
	public void setMain(Main main) {
		this.main = main;

	}

	public void showHomeScreen() {

		try {

			// Load overview.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/root/HomeScreen.fxml"));
			loader.setResources(resources);
			BorderPane pane = (BorderPane) loader.load();

			dataPane.setCenter(pane);

			// Give the controller access to the main app.
			HomeScreenController controller = loader.getController();
			controller.setMain(main);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void showTriggerDiagnose() {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/diagnose/TriggerDiagnose.fxml"));
			loader.setResources(resources);
			AnchorPane pane = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().add(new Image("com/gretha/shared/resource/icons/trigger16.png"));
			dialogStage.setTitle("Diagnose Trigger");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());
			dialogStage.setScene(scene);

			// Set the person into the controller.
			TriggerDiagnoseController controller = loader.getController();
			controller.setDialogStage(dialogStage);

			if (pLCManager != null)
				for (PlcComDAO plcDao : pLCManager.getPlcComDAO()) {
					plcDao.setController(controller);
				}

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	public void showPlcDiagnose() {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/diagnose/PLCDiagnose.fxml"));
			loader.setResources(resources);
			AnchorPane pane = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().add(new Image("com/gretha/shared/resource/icons/electronic16.png"));
			dialogStage.setTitle("Diagnose PLC");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(getClass().getResource(Constants.STYLESHEET).toExternalForm());
			dialogStage.setScene(scene);

			PLCDiagnoseController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setData(pLCManager);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	public PLCManager getPlcCommunication() {
		return pLCManager;
	}

	public void setPlcCommunication(PLCManager pLCManager) {
		this.pLCManager = pLCManager;
	}

}