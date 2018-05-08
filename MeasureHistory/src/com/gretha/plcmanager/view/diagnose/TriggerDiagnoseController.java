package com.gretha.plcmanager.view.diagnose;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.plcmanager.Main;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.util.Constants;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * 
 * @author Markus Thaler
 */
public class TriggerDiagnoseController implements Initializable {

	private static final Logger logger = Logger.getLogger(TriggerDiagnoseController.class);
	private ResourceBundle resources;

	private Main main;
	private Stage dialogStage;

	@FXML
	private ListView<String> listLogger;
	@FXML
	private ComboBox<PlcTrigger> cbPlcTrigger;
	@FXML
	private Button btnStartPause;
	@FXML
	private Button btnStop;
	@FXML
	private Button btnExport;
	@FXML
	private ImageView ivStartPause;
	@FXML
	private ImageView ivStop;

	private boolean start = false;
	private boolean pause = false;
	private boolean stop = true;

	private String selectedPlcTrigger;

	private String loggerText;
	private PlcTrigger plcTrigger;

	private MenuItem miExport = new MenuItem("Exportieren");

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public TriggerDiagnoseController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		List<PlcTrigger> triggerList = Service.getInstance().getPlcTriggers();
		Collections.sort(triggerList, new PlcTriggerSort());

		for (PlcTrigger trigger : triggerList) {
			if (trigger.isActivated() && trigger.getProcess() != null)
				cbPlcTrigger.getItems().add(trigger);
		}

		Image image_play = new Image(getClass().getClassLoader().getResourceAsStream(Constants.ICON_PLAY));
		Image image_pause = new Image(getClass().getClassLoader().getResourceAsStream(Constants.ICON_PAUSE));
		Image image_stop = new Image(getClass().getClassLoader().getResourceAsStream(Constants.ICON_STOP));

		ivStartPause.setImage(image_play);
		btnStartPause.setDisable(true);
		btnStop.setDisable(true);
		btnExport.setDisable(true);
		miExport.setDisable(true);

		cbPlcTrigger.setConverter(new StringConverter<PlcTrigger>() {

			@Override
			public String toString(PlcTrigger object) {
				return object.getProcess().getStation() + " " + object.getProcess().getName() + " ["
						+ object.getPlcTriggerInfo() + "]";
			}

			@Override
			public PlcTrigger fromString(String string) {
				return null;
			}
		});
		cbPlcTrigger.setOnAction((event) -> {

			btnStartPause.setDisable(false);
			btnStop.setDisable(true);
			btnExport.setDisable(false);
			miExport.setDisable(false);

			start = false;
			pause = false;
			stop = true;

			ivStartPause.setImage(image_play);
			plcTrigger = cbPlcTrigger.getSelectionModel().getSelectedItem();

		});

		btnStartPause.setOnAction((event) -> {
			if (stop) {
				start = true;
				pause = false;
				stop = false;
				ivStartPause.setImage(image_pause);
				handleFensterLeeren();
				btnStop.setDisable(false);

			} else if (pause) {
				start = true;
				pause = false;
				stop = false;
				ivStartPause.setImage(image_pause);
				btnStop.setDisable(false);

			} else {
				start = false;
				pause = true;
				stop = false;
				ivStartPause.setImage(image_play);
				btnStop.setDisable(false);
			}
		});

		btnStop.setOnAction((event) -> {
			if (start) {
				start = false;
				pause = false;
				stop = true;
				ivStartPause.setImage(image_play);

			} else if (pause) {
				start = false;
				pause = false;
				stop = true;
				ivStartPause.setImage(image_play);
				handleFensterLeeren();
				btnStop.setDisable(true);

			} else {
				start = false;
				pause = false;
				stop = true;
				ivStartPause.setImage(image_play);
				handleFensterLeeren();
				btnStop.setDisable(true);
			}
		});

		listLogger.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (cbPlcTrigger.getSelectionModel().getSelectedItem() != null) {
					if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event))
						exportTo();
				}
			}
		});

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();
		SeparatorMenuItem sparator = new SeparatorMenuItem();

		MenuItem miFensterLeeren = new MenuItem("Fensterinhalt leeren");
		miFensterLeeren.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				handleFensterLeeren();
			}
		});

		miExport.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		miExport.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				exportTo();
			}
		});

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miFensterLeeren, sparator, miExport);

		// When user right-click
		listLogger.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {

				contextMenu.hide();

				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {

					contextMenu.show(listLogger, mouseEvent.getScreenX(), mouseEvent.getScreenY());

				}
			}
		});

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setMain(Main main) {
		this.main = main;

	}

	private void handleFensterLeeren() {
		listLogger.getItems().clear();
	}

	@FXML
	private void handleClose() {
		this.dialogStage.close();
	}

	@FXML
	public void exportTo() {

		selectedPlcTrigger = cbPlcTrigger.getSelectionModel().getSelectedItem().getProcess().getStation() + " "
				+ cbPlcTrigger.getSelectionModel().getSelectedItem().getProcess().getName();

		File file = null;

		DateFormat tf = new SimpleDateFormat("HH-mm-ss");
		String timeString = tf.format(Calendar.getInstance().getTime());

		DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		String dateString = df.format(Calendar.getInstance().getTime());

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Exportieren nach");
		FileChooser.ExtensionFilter extFilterTxt = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
		chooser.getExtensionFilters().addAll(extFilterTxt);

		chooser.setInitialFileName(selectedPlcTrigger + "-" + dateString + "-" + timeString + ".trigger.log.txt");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterTxt)
				exportToTxt(file);
	}

	private void exportToTxt(File file) {

		Writer writer = null;

		try {
			writer = new BufferedWriter(new FileWriter(file));

			for (int i = 0; i < listLogger.getItems().size(); i++) {
				writer.write(listLogger.getItems().get(i).toString() + "\r\n");
			}

			writer.flush();
			writer.close();

			SaveSucessInfo info = new SaveSucessInfo(dialogStage, file);
			info.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

			logger.error(e);

			FileSaveFailedAlert alert = new FileSaveFailedAlert(dialogStage, file, e);
			alert.showAndWait();

		}
	}

	public boolean isStart() {
		return start;
	}

	public void setStart(boolean start) {
		this.start = start;
	}

	public void addLoggerText(String loggerText) {
		this.loggerText = loggerText;

		listLogger.getItems().add(0, loggerText);

	}

	public PlcTrigger getPlcTrigger() {
		return plcTrigger;
	}

	class PlcTriggerSort implements Comparator<PlcTrigger> {
		@Override
		public int compare(PlcTrigger a1, PlcTrigger a2) {

			if (a1.getProcess() == null || a2.getProcess() == null)
				return 0;

			if (a1.getProcess().getStation().toLowerCase().compareTo(a2.getProcess().getStation().toLowerCase()) == 0)
				return a1.getProcess().getName().compareTo(a2.getProcess().getName());

			return a1.getProcess().getStation().toLowerCase().compareTo(a2.getProcess().getStation().toLowerCase());
		}
	}
}