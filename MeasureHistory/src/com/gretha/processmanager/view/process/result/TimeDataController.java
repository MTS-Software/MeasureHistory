package com.gretha.processmanager.view.process.result;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.model.TimeData;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.DurationDateAndTime;
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

public class TimeDataController implements Initializable {

	private Main main;
	private Stage dialogStage;
	private TimeData timeData;
	private RootPaneController rootPaneController;
	private static final Logger logger = Logger.getLogger(TimeDataController.class);

	@FXML
	private BorderPane pane;
	@FXML
	private URL location;
	@FXML
	private Label lblMittelwert;
	@FXML
	private Label lblRange;
	@FXML
	private Label lblMax;
	@FXML
	private Label lblMin;
	@FXML
	private Label lblMittelwertFormatted;
	@FXML
	private Label lblRangeFormatted;
	@FXML
	private Label lblMaxFormatted;
	@FXML
	private Label lblMinFormatted;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initContexMenu();

	}

	public void setData() {

		long min = getMin();
		long max = getMax();
		long range = max - min;
		float avg = getAvg();

		this.timeData = new TimeData();

		this.timeData.setMin(min);
		this.timeData.setMax(max);
		this.timeData.setRange(range);
		this.timeData.setAvg(avg);

		lblMin.setText(min + " s");
		lblMax.setText(max + " s");
		lblRange.setText(range + " s");
		lblMittelwert.setText(DecimalPointFormatter.roundFloat2String(avg, 2) + " s");

		lblMinFormatted.setText("(" + DurationDateAndTime.getTimeFromTotalSeconds(min) + ")");
		lblMaxFormatted.setText("(" + DurationDateAndTime.getTimeFromTotalSeconds(max) + ")");
		lblRangeFormatted.setText("(" + DurationDateAndTime.getTimeFromTotalSeconds(range) + ")");
		lblMittelwertFormatted.setText(
				"(" + DurationDateAndTime.getTimeFromTotalSeconds((long) DecimalPointFormatter.roundFloat2Float(avg, 0))
						+ ")");

		rootPaneController.setTimeData(timeData);

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
		int i = 0;
		float avg = 0;

		if (rootPaneController.getResults().size() > 0) {

			for (i = 0; i < (rootPaneController.getResults().size() - 1); i++) {

				Timestamp fromTimestamp = rootPaneController.getResults().get(i + 1).getTimestampSql();
				Timestamp toTimestamp = rootPaneController.getResults().get(i).getTimestampSql();

				LocalDateTime fromDateTime = getLocalDateTime(fromTimestamp);
				LocalDateTime toDateTime = getLocalDateTime(toTimestamp);

				v = DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime) + v;

			}

			avg = v / i;
		}

		return avg;
	}

	private long getMin() {

		long min = 0;

		if (rootPaneController.getResults().size() > 0) {

			for (int i = 0; i < (rootPaneController.getResults().size() - 1); i++) {

				Timestamp fromTimestamp = rootPaneController.getResults().get(i + 1).getTimestampSql();
				Timestamp toTimestamp = rootPaneController.getResults().get(i).getTimestampSql();

				LocalDateTime fromDateTime = getLocalDateTime(fromTimestamp);
				LocalDateTime toDateTime = getLocalDateTime(toTimestamp);

				if (i == 0)
					min = DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime);
				if (i > 0) {
					if (DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime) < min)
						min = DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime);
				}
			}
		}

		return min;
	}

	private long getMax() {

		long max = 0;

		if (rootPaneController.getResults().size() > 0) {

			for (int i = 0; i < (rootPaneController.getResults().size() - 1); i++) {

				Timestamp fromTimestamp = rootPaneController.getResults().get(i + 1).getTimestampSql();
				Timestamp toTimestamp = rootPaneController.getResults().get(i).getTimestampSql();

				LocalDateTime fromDateTime = getLocalDateTime(fromTimestamp);
				LocalDateTime toDateTime = getLocalDateTime(toTimestamp);

				if (i == 0)
					max = DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime);
				if (i > 0) {
					if (DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime) > max)
						max = DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime);
				}

			}
		}

		return max;
	}

	public static LocalDateTime getLocalDateTime(Timestamp timestamp) {

		Calendar cal = Calendar.getInstance();
		Date dt = new Date(timestamp.getTime());
		cal.setTime(dt);

		LocalDateTime dateTime = LocalDateTime.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND));

		return dateTime;

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
