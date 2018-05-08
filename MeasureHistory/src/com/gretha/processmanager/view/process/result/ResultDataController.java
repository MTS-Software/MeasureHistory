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
import com.gretha.shared.model.ResultData;
import com.gretha.shared.util.DecimalPointFormatter;
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
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ResultDataController implements Initializable {

	private Main main;
	private static final Logger logger = Logger.getLogger(ResultDataController.class);
	private Stage dialogStage;
	private ResultData resultData;
	private RootPaneController rootPaneController;

	@FXML
	private BorderPane pane;
	@FXML
	private URL location;

	@FXML
	private Label lblOk;
	@FXML
	private Label lblNok;
	@FXML
	private Label lblUnbewertet;
	@FXML
	private Label lblGesamt;

	@FXML
	private Label lblOkProzent;
	@FXML
	private Label lblNokProzent;
	@FXML
	private Label lblUnbewertetProzent;
	@FXML
	private Label lblGesamtProzent;

	@FXML
	private Label lblMittelwert;
	@FXML
	private Label lblRange;
	@FXML
	private Label lblMax;
	@FXML
	private Label lblMin;

	@FXML
	private Label lblProzessIstwert;
	@FXML
	private Label lblProzessSollwert;
	@FXML
	private Label lblProzessAbweichung;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		initContextMenu();

	}

	public void setData() {

		float min = getMin();
		float max = getMax();
		float range = max - min;
		int ok = getOk();
		int nok = getNok();
		int unbewertet = getUnbewertet();
		int undefiniert = getUndefiniert();
		int gesamt = getGesamt();
		float avg = getAvg();
		float setvalue = getSetvalue();
		float lastLoLim = getLastLoLim();
		float lastUpLim = getLastUpLim();
		float deviation = getDeviation(avg, setvalue);
		float anzOkPercent = getPercent(gesamt, ok);
		float anzNokPercent = getPercent(gesamt, nok);
		float anzUnbewertetPercent = getPercent(gesamt, unbewertet);
		float anzUndefiniertPercent = getPercent(gesamt, undefiniert);
		float anzGesamtPercent = getPercent(gesamt, gesamt);
		int decimalPoints = rootPaneController.getProcess().getDecimalPoints();
		String sign = rootPaneController.getProcess().getUnit().getSign();

		this.resultData = new ResultData();

		this.resultData.setOk(ok);
		this.resultData.setNok(nok);
		this.resultData.setUnbewertet(unbewertet);
		this.resultData.setUndefiniert(undefiniert);
		this.resultData.setGesamt(gesamt);

		this.resultData.setOkPercent(getPercent(gesamt, ok));
		this.resultData.setNokPercent(getPercent(gesamt, nok));
		this.resultData.setUnbewertetPercent(getPercent(gesamt, unbewertet));
		this.resultData.setUnbewertetPercent(getPercent(gesamt, undefiniert));
		this.resultData.setGesamtPercent(getPercent(gesamt, gesamt));

		this.resultData.setAvg(avg);
		this.resultData.setMin(min);
		this.resultData.setMax(max);
		this.resultData.setRange(range);

		this.resultData.setSetvalue(setvalue);
		this.resultData.setDeviation(deviation);
		this.resultData.setLastLoLim(lastLoLim);
		this.resultData.setLastUpLim(lastUpLim);

		lblMin.setText(DecimalPointFormatter.roundFloat2String(min, decimalPoints) + " " + sign);
		lblMax.setText(DecimalPointFormatter.roundFloat2String(max, decimalPoints) + " " + sign);
		lblRange.setText(DecimalPointFormatter.roundFloat2String(range, decimalPoints) + " " + sign);
		lblMittelwert.setText(DecimalPointFormatter.roundFloat2String(avg, decimalPoints) + " " + sign);
		lblProzessIstwert.setText(DecimalPointFormatter.roundFloat2String(avg, decimalPoints) + " " + sign);
		lblProzessSollwert.setText(DecimalPointFormatter.roundFloat2String(setvalue, decimalPoints) + " " + sign);

		lblProzessAbweichung.setText(DecimalPointFormatter.roundFloat2String(deviation, decimalPoints) + " " + sign);

		lblOk.setText(String.valueOf(ok) + " Stk.");
		lblOkProzent.setText("(" + anzOkPercent + " %)");

		lblNok.setText(String.valueOf(nok) + " Stk.");
		lblNokProzent.setText("(" + anzNokPercent + " %)");

		lblUnbewertet.setText(String.valueOf(unbewertet) + " Stk.");
		lblUnbewertetProzent.setText("(" + anzUnbewertetPercent + " %)");

		lblGesamt.setText(String.valueOf(gesamt) + " Stk.");
		lblGesamtProzent.setText("(" + anzGesamtPercent + " %)");

		rootPaneController.setResultData(resultData);

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

	private float getAvg() {

		float v = 0;
		int cnt = 0;
		float avg = 0;

		if (rootPaneController.getResults().size() > 0) {
			for (Result r : rootPaneController.getResults()) {
				v = r.getValue() + v;
				cnt++;
			}
			avg = v / cnt;
		}
		return avg;
	}

	private float getSetvalue() {
		float setvalue = 0;
		if (rootPaneController.getProcess().isSetvalueUsed()) {
			setvalue = rootPaneController.getProcess().getSetvalue();
		} else {
			setvalue = (getLastUpLim() + getLastLoLim()) / 2;

		}
		return setvalue;
	}

	private float getDeviation(float ist, float soll) {

		return ist - soll;
	}

	private float getMin() {

		float min = 0;
		int cnt = 0;

		for (Result r : rootPaneController.getResults()) {

			if (cnt == 0)
				min = r.getValue();
			if (cnt > 0) {
				if (r.getValue() < min)
					min = r.getValue();
			}

			cnt++;

		}

		return min;

	}

	private float getMax() {

		float max = 0;
		int cnt = 0;

		for (Result r : rootPaneController.getResults()) {

			if (cnt == 0)
				max = r.getValue();
			if (cnt > 0) {
				if (r.getValue() > max)
					max = r.getValue();
			}

			cnt++;

		}

		return max;

	}

	private int getOk() {

		int cnt = 0;

		for (Result r : rootPaneController.getResults()) {

			if (r.getState() == 2)
				cnt++;

		}

		return cnt;

	}

	private int getNok() {

		int cnt = 0;

		for (Result r : rootPaneController.getResults()) {

			if (r.getState() == 1)
				cnt++;

		}

		return cnt;

	}

	private int getUnbewertet() {

		int cnt = 0;

		for (Result r : rootPaneController.getResults()) {

			if (r.getState() == 0)
				cnt++;

		}

		return cnt;

	}

	private int getUndefiniert() {

		int cnt = 0;

		for (Result r : rootPaneController.getResults()) {

			if (r.getState() < 0 || r.getState() > 2)
				cnt++;

		}

		return cnt;

	}

	private int getGesamt() {

		return rootPaneController.getResults().size();

	}

	private float getPercent(int anzGesamt, int anz) {

		float proz = (float) (Math.round(100.0 * anz / anzGesamt * 100) / 100.0);

		return proz;

	}

	private float getLastLoLim() {
		float lastLoLim = 0;

		if (rootPaneController.getResults().size() > 0) {
			try {
				lastLoLim = rootPaneController.getResults().get(0).getLoLim();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return lastLoLim;
	}

	private float getLastUpLim() {
		float lastUpLim = 0;

		if (rootPaneController.getResults().size() > 0) {
			try {
				lastUpLim = rootPaneController.getResults().get(0).getUpLim();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return lastUpLim;
	}

	public BorderPane getPane() {
		return pane;
	}

	private void initContextMenu() {

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
