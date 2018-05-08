package com.gretha.processmanager.view.process.adhoc;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Result;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.InputChartBound;
import com.gretha.shared.util.jfxutils.chart.ChartPanManager;
import com.gretha.shared.util.jfxutils.chart.JFXChartUtil;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.info.ChartPointInfo;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AdhocChartController implements Initializable {

	private static final Logger logger = Logger.getLogger(AdhocChartController.class);
	private ResourceBundle resources;
	private Stage dialogStage;
	private Main main;
	private AdhocTableController adhocTableController;

	@FXML
	private VBox vBox;

	private final double yAxisWidth = 60;

	private boolean mouseInChartArea;

	private ObservableList<List<Result>> resultsList = FXCollections.observableArrayList();
	private ObservableList<Process> processList = FXCollections.observableArrayList();
	private ContMenu contMenu;

	private ObservableList<NumberAxis> xAxleList;
	private ObservableList<NumberAxis> yAxleList;
	private ObservableList<LineChart<Number, Number>> chartList;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		contMenu = new ContMenu();
		initContextMenu();

	}

	private void initZooming(LineChart<Number, Number> chart) {

		ChartPanManager panner = new ChartPanManager(chart);
		panner.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				// Diagramm verschieben mit der linken Maustaste
				if (mouseEvent.getButton() == MouseButton.PRIMARY && !mouseEvent.isShortcutDown()) {
				} else {
					mouseEvent.consume();
				}

			}
		});
		// Funktion Diagramm verschieben starten
		panner.start();

		JFXChartUtil.setupZooming(chart, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				// Markieren und markierten Bereich Zoomen mit Strg + linke
				// Maustate oder rechten Maustaste
				if ((mouseEvent.getButton() == MouseButton.PRIMARY || mouseEvent.getButton() == MouseButton.SECONDARY)
						&& mouseEvent.isShortcutDown()) {
				} else {
					mouseEvent.consume();
				}
			}

		});
	}

	private void initCharts() {

		vBox.getChildren().clear();

		xAxleList = FXCollections.observableArrayList();
		yAxleList = FXCollections.observableArrayList();
		chartList = FXCollections.observableArrayList();

		for (int i = 0; i < resultsList.size(); i++) {

			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			ObservableList<Data<Number, Number>> dataSeries = FXCollections.observableArrayList();

			series.setName(processList.get(i).getStation() + " " + processList.get(i).getName() + " ["
					+ processList.get(i).getUnit().getSign() + "]");

			for (Result result : resultsList.get(i)) {
				dataSeries.add(new XYChart.Data<>(result.getNr(), result.getValue()));
			}

			series.setData(dataSeries);

			xAxleList.add(new NumberAxis());
			yAxleList.add(new NumberAxis());

			if ((i + 1) == resultsList.size())
				xAxleList.get(i).setLabel("Nr.");

			yAxleList.get(i).setLabel(series.getName());

			chartList.add(new LineChart<Number, Number>(xAxleList.get(i), yAxleList.get(i)));
			chartList.get(i).getData().clear();
			chartList.get(i).getData().add(series);

			chartList.get(i).setCreateSymbols(false);

			chartList.get(i).setLegendVisible(false);
			chartList.get(i).setVerticalZeroLineVisible(false);
			chartList.get(i).setHorizontalZeroLineVisible(false);
			chartList.get(i).setAnimated(true);
			chartList.get(i).getYAxis().setPrefWidth(yAxisWidth);
			chartList.get(i).getYAxis().setMaxWidth(yAxisWidth);

			if (i > 0) {
				xAxleList.get(i).lowerBoundProperty()
						.bindBidirectional(((NumberAxis) chartList.get(0).getXAxis()).lowerBoundProperty());
				xAxleList.get(i).upperBoundProperty()
						.bindBidirectional(((NumberAxis) chartList.get(0).getXAxis()).upperBoundProperty());
				xAxleList.get(i).tickUnitProperty()
						.bindBidirectional(((NumberAxis) chartList.get(0).getXAxis()).tickUnitProperty());
			}

			// Defaultmäßig UG-OG für Auswahlbox vorwählen
			setScaleOptionUgOg(xAxleList.get(i), yAxleList.get(i), resultsList.get(i).size(),
					getLastLoLim(resultsList.get(i)), getLastUpLim(resultsList.get(i)));
			contMenu.rmiScaleOptionUgOg.setSelected(true);

			// Defaultmätig Punkte anzeigen ausschalten
			contMenu.rmiShowChartPoints.setSelected(false);

			VBox.setVgrow(chartList.get(i), Priority.ALWAYS);
			vBox.getChildren().add(chartList.get(i));

			styleChartLine(chartList.get(i));
			initEvents(chartList.get(i));

			// Hinweis: initZooming muss nach dem add in die VBox passieren, da die Funktion
			// sonst nicht gegeben ist
			initZooming(chartList.get(i));
		}

		dialogStage.setTitle(resources.getString("adhoc_chart") + " " + "[" + createStageTitle() + "]");

	}

	private void initContextMenu() {

		contMenu.miResetActView.setText("Aktuelle Ansicht rücksetzen");
		contMenu.miResetActView.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		contMenu.miResetActView.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				resetActView();

			}
		});

		contMenu.miRefresh.setText("Refresh (Set to default)");
		contMenu.miRefresh.setAccelerator(new KeyCodeCombination(KeyCode.F5, KeyCombination.SHORTCUT_ANY));
		contMenu.miRefresh.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				refreshCharts();

			}
		});

		contMenu.miSave.setText("Speichern");
		contMenu.miSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		contMenu.miSave.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				saveAs();
			}
		});

		contMenu.rmiScaleOptionAuto.setText("Automatisch");
		contMenu.rmiScaleOptionAuto.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				for (int i = 0; i < resultsList.size(); i++) {
					setScaleOptionAuto(xAxleList.get(i), yAxleList.get(i));
				}

			}
		});

		contMenu.rmiScaleOptionUgOg.setText("Untergrenze und Obergrenze");
		contMenu.rmiScaleOptionUgOg.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				for (int i = 0; i < resultsList.size(); i++) {
					setScaleOptionUgOg(xAxleList.get(i), yAxleList.get(i), resultsList.get(i).size(),
							getLastLoLim(resultsList.get(i)), getLastUpLim(resultsList.get(i)));
				}

			}
		});

		contMenu.rmiScaleOptionMinMax.setText("Minimum und Maximum");
		contMenu.rmiScaleOptionMinMax.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				for (int i = 0; i < resultsList.size(); i++) {
					setScaleOptionMinMax(xAxleList.get(i), yAxleList.get(i), resultsList.get(i).size(),
							getMin(resultsList.get(i)), getMax(resultsList.get(i)));
				}

			}
		});

		contMenu.miXAxleLowerBound.setText("Lower Bound");
		contMenu.miXAxleLowerBound.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				// Fix auf Index 0, da zu allen anderen Achsen eine Bidirektionale Bindung
				// hergestellt wurde

				xAxleList.get(0).setLowerBound(InputChartBound.getBound("X-Achse Lower Bound",
						DecimalPointFormatter.roundFloat2String((float) xAxleList.get(0).getLowerBound(), 0)));

				;

				setTickUnitXAxis(xAxleList.get(0));

			}
		});

		contMenu.miXAxleUpperBound.setText("Upper Bound");
		contMenu.miXAxleUpperBound.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				xAxleList.get(0).setUpperBound(InputChartBound.getBound("X-Achse Upper Bound",
						DecimalPointFormatter.roundFloat2String((float) xAxleList.get(0).getUpperBound(), 0)));

				setTickUnitXAxis(xAxleList.get(0));

			}
		});

		contMenu.mScaleXAxle.setText("X-Achse");
		contMenu.mScaleXAxle.getItems().addAll(contMenu.miXAxleLowerBound, contMenu.miXAxleUpperBound);

		contMenu.mSkalierung.setText("Skalierung");
		contMenu.mSkalierung.getItems().addAll(contMenu.mScaleXAxle, contMenu.rmiScaleOptionAuto,
				contMenu.rmiScaleOptionMinMax, contMenu.rmiScaleOptionUgOg);

		contMenu.rmiShowChartPoints.setText("Diagrammpunkte einblenden");
		contMenu.rmiShowChartPoints.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				setChartPoints();
			}
		});

		// Add MenuItem to ContextMenu
		contMenu.contextMenu.getItems().addAll(contMenu.miResetActView, contMenu.miRefresh, new SeparatorMenuItem(),
				contMenu.mSkalierung, contMenu.rmiShowChartPoints, new SeparatorMenuItem(), contMenu.miSave);

	}

	private void initEvents(LineChart<Number, Number> chart) {

		chart.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {

				mouseInChartArea = false;
				chart.getScene().setCursor(Cursor.DEFAULT);
			}
		});

		chart.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {

				mouseInChartArea = true;
				chart.getScene().setCursor(Cursor.OPEN_HAND);

				chart.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {

						if (mouseInChartArea) {

							if (event.isShortcutDown())
								chart.getScene().setCursor(Cursor.CROSSHAIR);

							if (event.getCode() == KeyCode.F5) {
								refreshCharts();
							}

							if (new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN).match(event)) {
								resetActView();
							}

							if (new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN).match(event)) {
								saveAs();
							}
						}
					}
				});

				chart.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {

						if (mouseInChartArea) {

							if (!event.isShortcutDown())
								dialogStage.getScene().setCursor(Cursor.OPEN_HAND);
						}
					}
				});

				chart.setOnMousePressed(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {

						if (event.getButton() == MouseButton.PRIMARY && !event.isShortcutDown()) {
							chart.getScene().setCursor(Cursor.CLOSED_HAND);
						}

						if ((event.getButton() == MouseButton.PRIMARY || event.getButton() == MouseButton.SECONDARY)
								&& event.isShortcutDown()) {
							chart.getScene().setCursor(Cursor.CROSSHAIR);
						}

						if (event.getButton() == MouseButton.SECONDARY && !event.isShortcutDown()) {
							chart.getScene().setCursor(Cursor.DEFAULT);
						}
					}
				});

				chart.setOnMouseReleased(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent mouseEvent) {

						contMenu.contextMenu.hide();

						if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {
							chart.getScene().setCursor(Cursor.DEFAULT);
							contMenu.contextMenu.show(chart, mouseEvent.getScreenX(), mouseEvent.getScreenY());
						}

						if (!mouseEvent.isShortcutDown())
							chart.getScene().setCursor(Cursor.OPEN_HAND);
					}
				});
			}

		});

	}

	private void resetActView() {

		for (int i = 0; i < resultsList.size(); i++) {

			chartList.get(i).setAnimated(false);

			if (contMenu.rmiScaleOptionUgOg.isSelected()) {
				setScaleOptionUgOg(xAxleList.get(i), yAxleList.get(i), resultsList.get(i).size(),
						getLastLoLim(resultsList.get(i)), getLastUpLim(resultsList.get(i)));

			}
			if (contMenu.rmiScaleOptionMinMax.isSelected()) {
				setScaleOptionMinMax(xAxleList.get(i), yAxleList.get(i), resultsList.get(i).size(),
						getMin(resultsList.get(i)), getMax(resultsList.get(i)));

			}
			if (contMenu.rmiScaleOptionAuto.isSelected()) {
				setScaleOptionAuto(xAxleList.get(i), yAxleList.get(i));

			}
		}
	}

	private void refreshCharts() {

		processList.clear();
		resultsList.clear();
		processList.addAll(adhocTableController.getProcessList());
		resultsList.addAll(adhocTableController.getResultsList());

		initCharts();

	}

	private void setScaleOptionAuto(NumberAxis xAxle, NumberAxis yAxle) {
		xAxle.setAutoRanging(true);
		yAxle.setAutoRanging(true);
	}

	private void setScaleOptionUgOg(NumberAxis xAxle, NumberAxis yAxle, int resultSize, float lastLoLim,
			float lastUpLim) {
		xAxle.setAutoRanging(false);
		yAxle.setAutoRanging(false);

		float range = Math.abs(lastUpLim - lastLoLim);

		xAxle.setLowerBound(0);
		xAxle.setUpperBound(resultSize);

		yAxle.setLowerBound(lastLoLim - (range * 0.1));
		yAxle.setUpperBound(lastUpLim + (range * 0.1));

		setTickUnit(xAxle, yAxle);

	}

	private void setScaleOptionMinMax(NumberAxis xAxle, NumberAxis yAxle, int resultSize, float min, float max) {
		xAxle.setAutoRanging(false);
		yAxle.setAutoRanging(false);

		float range = Math.abs(max - min);

		xAxle.setLowerBound(0);
		xAxle.setUpperBound(resultSize);

		yAxle.setLowerBound(min - (range * 0.1));
		yAxle.setUpperBound(max + (range * 0.1));

		setTickUnit(xAxle, yAxle);
	}

	private void setTickUnit(NumberAxis xAxle, NumberAxis yAxle) {
		setTickUnitXAxis(xAxle);
		setTickUnitYAxis(yAxle);

	}

	private void setTickUnitXAxis(NumberAxis xAxle) {
		xAxle.setTickUnit(Math.round((xAxle.getUpperBound() - xAxle.getLowerBound()) / 10));
		xAxle.setMinorTickVisible(true);
		xAxle.setMinorTickCount(2);

	}

	private void setTickUnitYAxis(NumberAxis yAxle) {
		yAxle.setTickUnit((yAxle.getUpperBound() - yAxle.getLowerBound()) / 10);
		yAxle.setMinorTickVisible(true);
		yAxle.setMinorTickCount(2);

	}

	private void setChartPoints() {

		for (LineChart<Number, Number> chart : chartList) {
			if (contMenu.rmiShowChartPoints.isSelected()) {
				chart.setCreateSymbols(true);
				getChartPointsInfo(chart, processList.get(chartList.indexOf(chart)));
			} else {
				chart.setCreateSymbols(false);
			}
		}
	}

	private void getChartPointsInfo(LineChart<Number, Number> chart, Process process) {
		float yValue;
		String sv;
		int i = 0;

		for (Series<Number, Number> s : chart.getData()) {
			i = 0;

			for (Data<Number, Number> d : s.getData()) {

				yValue = d.getYValue().floatValue();
				sv = DecimalPointFormatter.roundFloat2String(yValue, process.getDecimalPoints());

				StringBuilder sb = new StringBuilder();

				sb.append("Nr.: " + d.getXValue().toString());
				sb.append("\nMesswert: " + sv + " " + process.getUnit().getSign());

				if (!process.getResults().get(i).getSerial().isEmpty()) {
					sb.append("\nSeriennummer: " + process.getResults().get(i).getSerial());
				}

				if (i < (s.getData().size() - 1)) {

					sb.append("\nZeitpunkt: " + process.getResults().get(i).getTimestamp());

				}

				Tooltip.install(d.getNode(), new Tooltip(sb.toString()));

				d.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						chart.getScene().setCursor(Cursor.HAND);
					}
				});
				d.getNode().addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						chart.getScene().setCursor(Cursor.OPEN_HAND);
					}
				});
				d.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {

						if (e.getButton() == MouseButton.PRIMARY && !e.isShortcutDown()) {

							chart.getScene().setCursor(Cursor.HAND);
							ChartPointInfo info = new ChartPointInfo(sb.toString(), dialogStage);
							info.showAndWait();
						}

					}
				});

				i++;
			}

		}
	}

	private float getLastLoLim(List<Result> results) {
		float lastLoLim = 0;

		if (results.size() > 0) {
			try {
				lastLoLim = results.get(0).getLoLim();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return lastLoLim;
	}

	private float getLastUpLim(List<Result> results) {
		float lastUpLim = 0;

		if (results.size() > 0) {
			try {
				lastUpLim = results.get(0).getUpLim();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
		return lastUpLim;
	}

	private float getMin(List<Result> result) {
		float min = 0;
		int cnt = 0;

		for (Result r : result) {
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

	private float getMax(List<Result> result) {
		float max = 0;
		int cnt = 0;

		for (Result r : result) {
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

	private void styleChartLine(LineChart<Number, Number> chart) {

		chart.getStylesheets().add(Constants.STYLESHEET_CHART);
		chart.setId("adhocchart" + chartList.indexOf(chart));
		chart.lookup("#adhocchart" + chartList.indexOf(chart));

		chart.getYAxis().getStylesheets().add(Constants.STYLESHEET_CHART);
		chart.getYAxis().setId("adhocchart" + chartList.indexOf(chart) + "yAxle");
		chart.getYAxis().lookup("#adhocchart" + chartList.indexOf(chart) + "yAxle");

	}

	private String createStageTitle() {

		String stageTitle = "";

		for (Process process : processList) {

			if (processList.indexOf(process) == (processList.size() - 1))
				stageTitle = stageTitle + process.getStation() + " " + process.getName();
			else
				stageTitle = stageTitle + process.getStation() + " " + process.getName() + "; ";

		}

		return stageTitle;

	}

	private void saveAs() {

		File file = null;

		DateFormat tf = new SimpleDateFormat("HH-mm-ss");
		String timeString = tf.format(Calendar.getInstance().getTime());

		DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		String dateString = df.format(Calendar.getInstance().getTime());

		FileChooser chooser = new FileChooser();
		chooser.setTitle("Speichern unter");
		FileChooser.ExtensionFilter extFilterPng = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
		chooser.getExtensionFilters().addAll(extFilterPng);

		chooser.setInitialFileName("Ad-hoc-Diagramm" + "-" + dateString + "-" + timeString + ".png");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterPng) {
				saveAsPng(file);
			}
	}

	private void saveAsPng(File file) {

		WritableImage image = vBox.snapshot(new SnapshotParameters(), null);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

			SaveSucessInfo info = new SaveSucessInfo(dialogStage, file);
			info.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

			logger.error(e);

			FileSaveFailedAlert alert = new FileSaveFailedAlert(dialogStage, file, e);
			alert.showAndWait();

		}
	}

	public void setData(ObservableList<List<Result>> resultsList, ObservableList<Process> processList) {

		for (Process process : processList) {
			this.processList.add(process);
		}

		for (List<Result> results : resultsList) {
			this.resultsList.add(results);
		}

		initCharts();

	}

	public void setMain(Main main) {
		this.main = main;

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	public void setAdhocTableController(AdhocTableController adhocTableController) {
		this.adhocTableController = adhocTableController;
	}

	class ContMenu {

		ContextMenu contextMenu = new ContextMenu();

		MenuItem miFilter = new MenuItem();
		MenuItem miFilterReset = new MenuItem();
		MenuItem miResetActView = new MenuItem();
		MenuItem miSave = new MenuItem();
		MenuItem miRefresh = new MenuItem();
		MenuItem miXAxleLowerBound = new MenuItem();
		MenuItem miXAxleUpperBound = new MenuItem();

		Menu mSkalierung = new Menu();
		Menu mSeries = new Menu();
		Menu mScaleXAxle = new Menu();

		RadioMenuItem rmiScaleOptionAuto = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionUgOg = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionMinMax = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionManuell = new RadioMenuItem();
		RadioMenuItem rmiShowChartPoints = new RadioMenuItem();

		RadioMenuItem[] rmiSeries = new RadioMenuItem[6];

		ToggleGroup groupScaleOption = new ToggleGroup();

		private ContMenu() {
			init();
		}

		private void init() {

			rmiScaleOptionAuto.setToggleGroup(groupScaleOption);
			rmiScaleOptionUgOg.setToggleGroup(groupScaleOption);
			rmiScaleOptionMinMax.setToggleGroup(groupScaleOption);
			rmiScaleOptionManuell.setToggleGroup(groupScaleOption);

			rmiScaleOptionUgOg.setSelected(true);

			rmiShowChartPoints.setSelected(false);

		}

	}

}
