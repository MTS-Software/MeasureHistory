package com.gretha.processmanager.view.process.result;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.model.Result;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.SPCValue;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class SPCDataController implements Initializable {

	private Main main;
	private Stage dialogStage;
	private static final Logger logger = Logger.getLogger(SPCDataController.class);

	private RootPaneController rootPaneController;

	@FXML
	private URL location;

	@FXML
	private BorderPane pane;
	@FXML
	private Label lblTolMitte;
	@FXML
	private Label lblUSG;
	@FXML
	private Label lblOSG;
	@FXML
	private Label lblTol;

	@FXML
	private Label lblXmin;
	@FXML
	private Label lblXmax;
	@FXML
	private Label lblRange;
	@FXML
	private Label lblNinTol;
	@FXML
	private Label lblNoutOSG;
	@FXML
	private Label lblNoutUSG;

	@FXML
	private Label lblXquer;
	@FXML
	private Label lblSigma;
	@FXML
	private Label lblXquerMinusSigma;
	@FXML
	private Label lblXquerPlusSigma;
	@FXML
	private Label lblSechsSigma;
	@FXML
	private Label lblKSG;
	@FXML
	private Label lblCp;
	@FXML
	private Label lblCpk;
	@FXML
	private ImageView ivCpkState;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		initContexMenu();

	}

	public void setData() {

		/* Zeichnungswerte */
		// Toleranzmitte
		lblTolMitte.setText(DecimalPointFormatter.roundFloat2String(((getLastUpLim() + getLastLoLim()) / 2),
				rootPaneController.getProcess().getDecimalPoints()));
		// Untere Spezifikationsgrenze
		lblUSG.setText(DecimalPointFormatter.roundFloat2String(getLastLoLim(),
				rootPaneController.getProcess().getDecimalPoints()));
		// Obere Spezifikationsgrenze
		lblOSG.setText(DecimalPointFormatter.roundFloat2String(getLastUpLim(),
				rootPaneController.getProcess().getDecimalPoints()));
		// Toleranz
		lblTol.setText(DecimalPointFormatter.roundFloat2String(Math.abs(getLastUpLim() - getLastLoLim()),
				rootPaneController.getProcess().getDecimalPoints()));

		/* Gemessene Werte */
		// kleinster Wert
		lblXmin.setText(DecimalPointFormatter.roundFloat2String(rootPaneController.getResultData().getMin(),
				rootPaneController.getProcess().getDecimalPoints()));
		// größter Wert
		lblXmax.setText(DecimalPointFormatter.roundFloat2String(rootPaneController.getResultData().getMax(),
				rootPaneController.getProcess().getDecimalPoints()));
		// Range
		lblRange.setText(DecimalPointFormatter.roundFloat2String(rootPaneController.getResultData().getRange(),
				rootPaneController.getProcess().getDecimalPoints()));
		// Anzahl Stück in Toleranz
		lblNinTol.setText(DecimalPointFormatter.roundFloat2String(getAnzOk(), 0));
		// Anzahl Stück ausserhalb der Toleranzobergrenze
		lblNoutOSG.setText(DecimalPointFormatter.roundFloat2String(getAnzNokUpLim(), 0));
		// Anzahl Stück ausserhalb der Toleranzuntergrenze
		lblNoutUSG.setText(DecimalPointFormatter.roundFloat2String(getAnzNokLoLim(), 0));

		/* Statistische Werte */
		// Mittelwert
		lblXquer.setText(DecimalPointFormatter.roundFloat2String(getAvg(), 4));
		// Sigma
		lblSigma.setText(DecimalPointFormatter.roundFloat2String(getSigma(), 4));
		// Mittelwert minus 3 Sigma
		lblXquerMinusSigma.setText(DecimalPointFormatter.roundFloat2String(getAvg() - (3 * getSigma()), 4));
		// Mittwelwert plus 3 Sigma
		lblXquerPlusSigma.setText(DecimalPointFormatter.roundFloat2String(getAvg() + (3 * getSigma()), 4));
		// 6 Sigma
		lblSechsSigma.setText(DecimalPointFormatter.roundFloat2String(6 * getSigma(), 4));
		// Kritische Spezifikationsgrenze
		lblKSG.setText(
				DecimalPointFormatter.roundFloat2String(getKSG(), rootPaneController.getProcess().getDecimalPoints()));
		// Cp
		lblCp.setText(DecimalPointFormatter.roundFloat2String(getCp(), 2));
		// Cpk
		lblCpk.setText(DecimalPointFormatter.roundFloat2String(getCpk(), 2));
	}

	public void setMain(Main main) {
		this.main = main;

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public RootPaneController getRootPaneController() {
		return rootPaneController;
	}

	public void setRootPaneController(RootPaneController rootPaneController) {
		this.rootPaneController = rootPaneController;
	}

	private int getAnzNokLoLim() {
		int anz = 0;
		for (Result result : rootPaneController.getResults()) {
			if ((result.getValue() < result.getLoLim())) {
				anz++;
			}
		}
		return anz;
	}

	private int getAnzNokUpLim() {
		int anz = 0;
		for (Result result : rootPaneController.getResults()) {
			if ((result.getValue() > result.getUpLim())) {
				anz++;
			}
		}
		return anz;
	}

	private int getAnzOk() {
		int anz = 0;
		for (Result result : rootPaneController.getResults()) {
			if ((result.getValue() <= result.getUpLim()) && (result.getValue() >= result.getLoLim())) {
				anz++;
			}
		}
		return anz;
	}

	private float getSigma() {
		return SPCValue.getSigma(getAvg(), rootPaneController.getResults());
	}

	private float getCpk() {

		float cpk = SPCValue.getCpk(getAvg(), getLastLoLim(), getLastUpLim(), getSigma());

		if (cpk < rootPaneController.getProcess().getCpkLoLim1()) {
			Image image = new Image(getClass().getClassLoader().getResourceAsStream(Constants.ICON_SMILEY_RED));
			ivCpkState.setImage(image);
		} else if (cpk >= rootPaneController.getProcess().getCpkLoLim1()
				&& cpk < rootPaneController.getProcess().getCpkLoLim2()) {
			Image image = new Image(getClass().getClassLoader().getResourceAsStream(Constants.ICON_SMILEY_YELLOW));
			ivCpkState.setImage(image);
		} else if (cpk >= rootPaneController.getProcess().getCpkLoLim2()) {
			Image image = new Image(getClass().getClassLoader().getResourceAsStream(Constants.ICON_SMILEY_GREEN));
			ivCpkState.setImage(image);
		}

		return cpk;
	}

	private float getCp() {
		return SPCValue.getCp(getLastLoLim(), getLastUpLim(), getSigma());
	}

	private float getKSG() {
		return SPCValue.getKSG(getAvg(), getLastLoLim(), getLastUpLim());
	}

	public float getAvg() {
		return rootPaneController.getResultData().getAvg();
	}

	private float getLastLoLim() {
		return rootPaneController.getResultData().getLastLoLim();
	}

	private float getLastUpLim() {
		return rootPaneController.getResultData().getLastUpLim();
	}

	public BorderPane getPane() {
		return pane;
	}

	private void initContexMenu() {

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem miSave = new MenuItem("Speichern");
		miSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				saveAs();
			}
		});

		MenuItem miFilter = new MenuItem("Filter");
		miFilter.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				rootPaneController.getRootPaneBarController().handleFilterSettings();
			}
		});

		MenuItem miFilterReset = new MenuItem("Filter rücksetzen");
		miFilterReset.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				rootPaneController.getRootPaneBarController().handleFilterReset();
			}
		});

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miFilter, miFilterReset, new SeparatorMenuItem(), miSave);

		// When user right-click
		pane.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				contextMenu.hide();
				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {

					if (rootPaneController.getRootPaneBarController().getFilter() != null) {
						if (rootPaneController.getRootPaneBarController().getFilter().isActivated())
							miFilterReset.setDisable(false);
					} else
						miFilterReset.setDisable(true);

					contextMenu.show(pane, mouseEvent.getScreenX(), mouseEvent.getScreenY());
				}

			}
		});
	}

	public void saveAs() {

		File file = null;

		DateFormat tf = new SimpleDateFormat("HH-mm-ss");
		String timeString = tf.format(Calendar.getInstance().getTime());

		DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		String dateString = df.format(Calendar.getInstance().getTime());

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Speichern unter");
		FileChooser.ExtensionFilter extFilterPng = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
		chooser.getExtensionFilters().addAll(extFilterPng);

		chooser.setInitialFileName(rootPaneController.getProcess().getStation() + "-"
				+ rootPaneController.getProcess().getName() + "-" + dateString + "-" + timeString + ".png");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterPng)
				saveAsPng(file);
	}

	private void saveAsPng(File file) {

		WritableImage image = pane.snapshot(new SnapshotParameters(), null);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

			SaveSucessInfo info = new SaveSucessInfo(dialogStage, file);
			info.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

			FileSaveFailedAlert alert = new FileSaveFailedAlert(dialogStage, file, e);
			alert.showAndWait();

		}
	}
}
