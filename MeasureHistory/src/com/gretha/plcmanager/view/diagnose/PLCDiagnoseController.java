package com.gretha.plcmanager.view.diagnose;

import java.net.URL;
import java.util.ResourceBundle;

import com.gretha.plcmanager.Main;
import com.gretha.plcmanager.plc.util.PLCManager;
import com.gretha.shared.model.Plc;
import com.gretha.shared.util.StringCustomComparator;
import com.gretha.shared.util.WebBrowser;
import com.gretha.shared.view.alert.NoSelectionAlert;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * 
 * @author Markus Thaler
 */
public class PLCDiagnoseController implements Initializable {

	private ResourceBundle resources;

	private Main main;
	private Stage dialogStage;
	private PLCManager pLCManager;

	@FXML
	private TableView<Plc> tblPLC;
	@FXML
	private TableColumn<Plc, String> colName;
	@FXML
	private TableColumn<Plc, String> colIP;
	@FXML
	private TableColumn<Plc, Boolean> colConnection;

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public PLCDiagnoseController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		initUI();

	}

	public void initUI() {

		colName.setCellValueFactory(new PropertyValueFactory<Plc, String>("name"));
		colName.setComparator(new StringCustomComparator());
		colName.setCellFactory(column -> {
			return new TableCell<Plc, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(String.valueOf(item));
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		colIP.setCellValueFactory(new PropertyValueFactory<Plc, String>("ip"));
		colIP.setComparator(new StringCustomComparator());
		colIP.setCellFactory(column -> {
			return new TableCell<Plc, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						setText(String.valueOf(item));
						setAlignment(Pos.CENTER);

					}
				}
			};
		});

		colConnection.setCellValueFactory(new PropertyValueFactory<Plc, Boolean>("connection"));
		colConnection.setCellFactory(CheckBoxTableCell.forTableColumn(colConnection));

		// Auto resize columns
		tblPLC.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		initContexMenuDataTable();

	}

	private void initContexMenuDataTable() {

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();

		Menu mPlcInfo = new Menu("Webinterface");

		MenuItem miShowPlcInfoinJavaBrowser = new MenuItem("Öffnen mit Javabrowser");
		miShowPlcInfoinJavaBrowser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Plc selectedData = tblPLC.getSelectionModel().getSelectedItem();
				if (selectedData != null) {

					WebBrowser.openURLinJavaBrowser("http://" + selectedData.getIp() + "/");

				} else {

					NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
					alert.showAndWait();
				}
			}
		});

		MenuItem miShowPlcInfoinStandardBrowser = new MenuItem("Öffnen mit Standardbrowser");
		miShowPlcInfoinStandardBrowser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Plc selectedData = tblPLC.getSelectionModel().getSelectedItem();
				if (selectedData != null) {

					WebBrowser.openURLinStandardBrowser(selectedData.getIp());

				} else {

					NoSelectionAlert alert = new NoSelectionAlert(dialogStage);
					alert.showAndWait();
				}
			}
		});

		mPlcInfo.getItems().addAll(miShowPlcInfoinStandardBrowser, miShowPlcInfoinJavaBrowser);

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(mPlcInfo);

		// When user right-click
		tblPLC.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				contextMenu.hide();
				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {
					contextMenu.show(tblPLC, mouseEvent.getScreenX(), mouseEvent.getScreenY());
				}
			}
		});

	}

	public void setData(PLCManager pLCManager) {

		this.pLCManager = pLCManager;

		tblPLC.setItems(FXCollections.observableArrayList(pLCManager.getPlcList()));

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

	@FXML
	private void handleClose() {
		this.dialogStage.close();
	}

	class UpdateThread implements Runnable {

		@Override
		public void run() {

			while (!Thread.currentThread().isInterrupted()) {

				// UI updaten
				Platform.runLater(new Runnable() {
					@Override
					public void run() {

					}
				});
				// Thread schlafen
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}

		}

	}

}