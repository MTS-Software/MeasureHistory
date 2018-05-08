package com.gretha.processmanager.view.process.charts;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.result.TimeChartDataController;
import com.gretha.processmanager.view.process.result.TimeDataController;
import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.model.TimeChartData;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.DurationDateAndTime;
import com.gretha.shared.util.InputChartBound;
import com.gretha.shared.util.jfxutils.chart.ChartPanManager;
import com.gretha.shared.util.jfxutils.chart.JFXChartUtil;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.info.ChartPointInfo;
import com.gretha.shared.view.info.SaveSucessInfo;
import com.sun.javafx.charts.Legend;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class TimeChartController implements Initializable {

	private Stage dialogStage;
	private static final Logger logger = Logger.getLogger(TimeChartController.class);
	private RootPaneController rootPaneController;

	@FXML
	private LineChart<Number, Number> chart;
	@FXML
	private NumberAxis chartxAxis;
	@FXML
	private NumberAxis chartyAxis;

	private ResourceBundle resources;

	private ObservableList<TimeChartData> timeChartDataList;

	private boolean mouseInChartArea;
	private ContMenu contMenu;

	private XYChart.Series<Number, Number> seriesValue;
	private XYChart.Series<Number, Number> seriesAvg;

	private String[] seriesName = { "Zeitdifferenz", "Mittelwert" };

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		chart.getStylesheets().add(Constants.STYLESHEET_CHART);
		chart.setId("timechart");
		chart.lookup("#timechart");

		contMenu = new ContMenu();

		initZooming();
		initContextMenu();
		initEvents();

	}

	private void initEvents() {

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

							if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
								rootPaneController.getRootPaneBarController().handleFilterSettings();
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

							if (rootPaneController.getRootPaneBarController().getFilter() != null) {
								if (rootPaneController.getRootPaneBarController().getFilter().isActivated())
									contMenu.miFilterReset.setDisable(false);
							} else
								contMenu.miFilterReset.setDisable(true);

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

	private void setScaleOptionAuto() {
		chartxAxis.setAutoRanging(true);
		chartyAxis.setAutoRanging(true);
	}

	private void setScaleOptionAvg() {
		chartxAxis.setAutoRanging(false);
		chartyAxis.setAutoRanging(false);

		chartxAxis.setLowerBound(0);
		chartxAxis.setUpperBound(rootPaneController.getResults().size());

		chartyAxis.setLowerBound(getAvg() - (getAvg() * 0.1));
		chartyAxis.setUpperBound(getAvg() + (getAvg() * 0.1));

		setTickUnit();
	}

	private void setScaleOptionMinMax() {
		chartxAxis.setAutoRanging(false);
		chartyAxis.setAutoRanging(false);

		float min = rootPaneController.getTimeData().getMin();
		float max = rootPaneController.getTimeData().getMax();
		float range = Math.abs(max - min);

		chartxAxis.setLowerBound(0);
		chartxAxis.setUpperBound(rootPaneController.getResults().size());

		chartyAxis.setLowerBound(min - (range * 0.1));
		chartyAxis.setUpperBound(max + (range * 0.1));

		setTickUnit();
	}

	private void setTickUnit() {
		setTickUnitXAxis();
		setTickUnitYAxis();

	}

	private void setTickUnitXAxis() {
		chartxAxis.setTickUnit(Math.round((chartxAxis.getUpperBound() - chartxAxis.getLowerBound()) / 10));
		chartxAxis.setMinorTickVisible(true);
		chartxAxis.setMinorTickCount(2);
	}

	private void setTickUnitYAxis() {
		chartyAxis.setTickUnit((chartyAxis.getUpperBound() - chartyAxis.getLowerBound()) / 10);
		chartyAxis.setMinorTickVisible(true);
		chartyAxis.setMinorTickCount(2);

	}

	private void setChartPoints() {
		if (contMenu.rmiShowChartPoints.isSelected()) {
			chart.setCreateSymbols(true);
			getChartPointsInfo();
		}

		else {
			chart.setCreateSymbols(false);
		}

	}

	private void initTimeChartDataList() {
		int i = 0;
		String serial = "";

		timeChartDataList = FXCollections.observableArrayList();
		TimeChartData timeChartData;

		for (Series<Number, Number> s : chart.getData()) {
			i = 0;

			if (s.getName().equalsIgnoreCase("Zeitdifferenz"))
				for (Data<Number, Number> d : s.getData()) {

					if (!rootPaneController.getResults().get(i).getSerial().isEmpty())
						serial = rootPaneController.getResults().get(i).getSerial();

					if (i < (s.getData().size())) {

						Timestamp fromTimestamp = rootPaneController.getResults().get(i + 1).getTimestampSql();
						Timestamp toTimestamp = rootPaneController.getResults().get(i).getTimestampSql();

						LocalDateTime fromDateTime = TimeDataController.getLocalDateTime(fromTimestamp);
						LocalDateTime toDateTime = TimeDataController.getLocalDateTime(toTimestamp);

						timeChartData = new TimeChartData();
						timeChartData.setNr(String.valueOf(i + 1));
						timeChartData.setDurationSeconds(
								String.valueOf(DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime)));
						timeChartData
								.setDurationTimestamp(DurationDateAndTime.getPeriodInTime(fromDateTime, toDateTime));
						timeChartData.setSerial(serial);
						timeChartData.setTimestamp(rootPaneController.getResults().get(i).getTimestamp());

						timeChartDataList.add(timeChartData);
					}

					i++;
				}
		}
	}

	private void getChartPointsInfo() {
		int i = 0;

		for (Series<Number, Number> s : chart.getData()) {
			i = 0;

			if (s.getName().equalsIgnoreCase("Zeitdifferenz"))
				for (Data<Number, Number> d : s.getData()) {

					StringBuilder sb = new StringBuilder();

					sb.append("Nr.: " + d.getXValue().toString());

					if (!rootPaneController.getResults().get(i).getSerial().isEmpty()) {
						sb.append("\nSeriennummer: " + rootPaneController.getResults().get(i).getSerial());
					}

					if (i < (s.getData().size())) {

						Timestamp fromTimestamp = rootPaneController.getResults().get(i + 1).getTimestampSql();
						Timestamp toTimestamp = rootPaneController.getResults().get(i).getTimestampSql();

						LocalDateTime fromDateTime = TimeDataController.getLocalDateTime(fromTimestamp);
						LocalDateTime toDateTime = TimeDataController.getLocalDateTime(toTimestamp);

						sb.append("\nZeitdifferenz: " + DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime)
								+ " s");

						if (DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime) >= 60)
							sb.append(" (" + DurationDateAndTime.getPeriodInTime(fromDateTime, toDateTime) + ")");

						sb.append("\nZeitpunkt: " + rootPaneController.getResults().get(i).getTimestamp());
					}

					Tooltip.install(d.getNode(), new Tooltip(sb.toString()));

					d.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							dialogStage.getScene().setCursor(Cursor.HAND);
						}
					});
					d.getNode().addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {
							dialogStage.getScene().setCursor(Cursor.OPEN_HAND);
						}
					});
					d.getNode().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent e) {

							if (e.getButton() == MouseButton.PRIMARY && !e.isShortcutDown()) {

								dialogStage.getScene().setCursor(Cursor.HAND);
								ChartPointInfo info = new ChartPointInfo(sb.toString(), dialogStage);
								info.showAndWait();
							}

						}
					});

					i++;
				}
		}
	}

	private void initZooming() {

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

	private void resetActView() {

		chart.setAnimated(false);

		if (contMenu.rmiScaleOptionAuto.isSelected()) {
			setScaleOptionAuto();
		}

		if (contMenu.rmiScaleOptionAvg.isSelected()) {
			setScaleOptionAvg();
		}

		if (contMenu.rmiScaleOptionMinMax.isSelected()) {
			setScaleOptionMinMax();
		}
	}

	private void initChart() {

		chart.setAnimated(true);
		chart.setTitle(rootPaneController.getProcess().getStation() + ": " + rootPaneController.getProcess().getName()
				+ " - Zeitdiagramm");

		chart.getData().clear();

		chart.setHorizontalGridLinesVisible(true);
		chart.setVerticalGridLinesVisible(true);
		chart.setHorizontalZeroLineVisible(false);
		chart.setVerticalZeroLineVisible(false);

		chart.setCreateSymbols(false);

		chartxAxis.setAutoRanging(true);
		chartyAxis.setAutoRanging(true);

		chartxAxis.setLabel("Nr.");
		chartyAxis.setLabel("Zeitdifferenz [ s ]");

		seriesValue = new XYChart.Series<>();
		seriesValue.setName(seriesName[0]);

		seriesAvg = new XYChart.Series<>();
		seriesAvg.setName(seriesName[1]);

		ObservableList<Data<Number, Number>> dataSeriesValue = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesAvg = FXCollections.observableArrayList();

		if (rootPaneController.getResults().size() > 0) {

			for (int i = 0; i < (rootPaneController.getResults().size() - 1); i++) {

				Timestamp fromTimestamp = rootPaneController.getResults().get(i + 1).getTimestampSql();
				Timestamp toTimestamp = rootPaneController.getResults().get(i).getTimestampSql();

				LocalDateTime fromDateTime = TimeDataController.getLocalDateTime(fromTimestamp);
				LocalDateTime toDateTime = TimeDataController.getLocalDateTime(toTimestamp);

				dataSeriesValue.add(
						new XYChart.Data<>(i + 1, DurationDateAndTime.getPeriodInSeconds(fromDateTime, toDateTime)));

				dataSeriesAvg.add(new XYChart.Data<>(i + 1, getAvg()));
			}
		}

		seriesValue.setData(dataSeriesValue);
		seriesAvg.setData(dataSeriesAvg);

		chart.getData().addAll(seriesValue, seriesAvg);

		for (int i = 0; i < contMenu.rmiSeries.length; i++) {
			contMenu.rmiSeries[i].setSelected(true);
		}

		setSeriesVisibility();

		// Defaultmäßig Standard für Auswahlbox vorwählen
		setScaleOptionMinMax();
		contMenu.rmiScaleOptionMinMax.setSelected(true);

		// Defaultmätig Punkte anzeigen ausschalten
		contMenu.rmiShowChartPoints.setSelected(false);

		// Tooltipp erzeugen
		Tooltip t = new Tooltip();

		if (seriesValue.getNode() != null)
			seriesValue.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					t.setText(seriesName[0]);
					Tooltip.install(seriesValue.getNode(), t);
				}
			});

		if (seriesAvg.getNode() != null)
			seriesAvg.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					t.setText(seriesName[1]);
					Tooltip.install(seriesAvg.getNode(), t);
				}
			});
	}

	private void setSeriesVisibility() {

		for (int i = 0; i < chart.getData().size(); i++) {
			chart.getData().get(i).getNode().setVisible(contMenu.rmiSeries[i].isSelected());

		}

		for (Node node : chart.getChildrenUnmodifiable()) {
			if (node instanceof Legend) {
				Legend legend = (Legend) node;

				for (Legend.LegendItem legendItem : legend.getItems()) {

					for (XYChart.Series<Number, Number> series : chart.getData()) {
						if (series.getName().equals(legendItem.getText())) {
							// Hint user that legend symbol is clickable
							legendItem.getSymbol().setCursor(Cursor.HAND);

							legendItem.getSymbol().setOnMouseClicked(event -> {
								if (event.getButton() == MouseButton.PRIMARY) {
									// Toggle visibility of line
									series.getNode().setVisible(!series.getNode().isVisible());

									for (int i = 0; i < chart.getData().size(); i++) {
										contMenu.rmiSeries[i].setSelected(chart.getData().get(i).getNode().isVisible());
									}

									for (XYChart.Data<Number, Number> data : series.getData()) {
										if (data.getNode() != null) {
											// Toggle visibility of every node in the series
											data.getNode().setVisible(series.getNode().isVisible());

										}

									}
								}
							});
							break;
						}
					}
				}
			}
		}

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
				initChart();
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

		contMenu.miTimeChartData.setText("Daten Zeitdiagramm");
		contMenu.miTimeChartData.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showTimeChartData();
			}
		});

		contMenu.rmiScaleOptionAuto.setText("Automatisch");
		contMenu.rmiScaleOptionAuto.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chart.setAnimated(false);
				setScaleOptionAuto();

			}
		});

		contMenu.rmiScaleOptionAvg.setText("Mittelwert");
		contMenu.rmiScaleOptionAvg.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setScaleOptionAvg();
			}
		});

		contMenu.rmiScaleOptionMinMax.setText("Minimum und Maximum");
		contMenu.rmiScaleOptionMinMax.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setScaleOptionMinMax();
			}
		});

		contMenu.miXAxleLowerBound.setText("Lower Bound");
		contMenu.miXAxleLowerBound.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				chartxAxis.setLowerBound(InputChartBound.getBound("X-Achse Lower Bound",
						DecimalPointFormatter.roundFloat2String((float) chartxAxis.getLowerBound(), 0)));

				setTickUnitXAxis();

			}
		});

		contMenu.miXAxleUpperBound.setText("Upper Bound");
		contMenu.miXAxleUpperBound.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				chartxAxis.setUpperBound(InputChartBound.getBound("X-Achse Upper Bound",
						DecimalPointFormatter.roundFloat2String((float) chartxAxis.getUpperBound(), 0)));

				setTickUnitXAxis();

			}
		});

		contMenu.miYAxleLowerBound.setText("Lower Bound");
		contMenu.miYAxleLowerBound.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				chartyAxis.setLowerBound(InputChartBound.getBound("Y-Achse Lower Bound",
						DecimalPointFormatter.roundFloat2String((float) chartyAxis.getLowerBound(),
								rootPaneController.getProcess().getDecimalPoints())));

				setTickUnitYAxis();

			}
		});

		contMenu.miYAxleUpperBound.setText("Upper Bound");
		contMenu.miYAxleUpperBound.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				chartyAxis.setUpperBound(InputChartBound.getBound("Y-Achse Upper Bound",
						DecimalPointFormatter.roundFloat2String((float) chartyAxis.getUpperBound(),
								rootPaneController.getProcess().getDecimalPoints())));

				setTickUnitYAxis();

			}
		});

		contMenu.mScaleXAxle.setText("X-Achse");
		contMenu.mScaleXAxle.getItems().addAll(contMenu.miXAxleLowerBound, contMenu.miXAxleUpperBound);

		contMenu.mScaleYAxle.setText("Y-Achse");
		contMenu.mScaleYAxle.getItems().addAll(contMenu.miYAxleLowerBound, contMenu.miYAxleUpperBound);

		contMenu.mSkalierung.setText("Skalierung");
		contMenu.mSkalierung.getItems().addAll(contMenu.mScaleXAxle, contMenu.mScaleYAxle, contMenu.rmiScaleOptionAuto,
				contMenu.rmiScaleOptionMinMax, contMenu.rmiScaleOptionAvg);

		contMenu.rmiShowChartPoints.setText("Diagrammpunkte einblenden");
		contMenu.rmiShowChartPoints.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				setChartPoints();

			}
		});

		contMenu.miFilter.setText("Filter");
		contMenu.miFilter.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
		contMenu.miFilter.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				rootPaneController.getRootPaneBarController().handleFilterSettings();

			}
		});

		contMenu.miFilterReset.setText("Filter rücksetzen");
		contMenu.miFilterReset.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				rootPaneController.getRootPaneBarController().handleFilterReset();

			}
		});

		contMenu.mSeries.setText("Linien");

		for (int i = 0; i < seriesName.length; i++) {
			contMenu.rmiSeries[i].setText(seriesName[i]);
			contMenu.mSeries.getItems().add(contMenu.rmiSeries[i]);
		}

		contMenu.rmiSeries[0].setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setSeriesVisibility();
			}
		});

		contMenu.rmiSeries[1].setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setSeriesVisibility();
			}
		});

		// Add MenuItem to ContextMenu
		contMenu.contextMenu.getItems().addAll(contMenu.miResetActView, contMenu.miRefresh, new SeparatorMenuItem(),
				contMenu.mSkalierung, contMenu.mSeries, contMenu.rmiShowChartPoints, contMenu.miFilter,
				contMenu.miFilterReset, contMenu.miTimeChartData, new SeparatorMenuItem(), contMenu.miSave);
	}

	public void setMain(Main main) {

	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
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

		chooser.setInitialFileName(rootPaneController.getProcess().getStation() + "-"
				+ rootPaneController.getProcess().getName() + "-" + dateString + "-" + timeString + ".png");

		file = chooser.showSaveDialog(dialogStage);

		if (file != null)
			if (chooser.getSelectedExtensionFilter() == extFilterPng)
				saveAsPng(file);
	}

	private void saveAsPng(File file) {

		WritableImage image = chart.snapshot(new SnapshotParameters(), null);

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

	private void showTimeChartData() {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/process/result/TimeChartData.fxml"));
			loader.setResources(resources);
			AnchorPane pane = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.setTitle(rootPaneController.getProcess().getStation() + ": "
					+ rootPaneController.getProcess().getName() + " - " + "Daten Zeitdiagramm");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(Constants.STYLESHEET);
			dialogStage.setScene(scene);

			// Set the person into the controller.
			TimeChartDataController timeChartDataController = loader.getController();
			timeChartDataController.setDialogStage(dialogStage);
			timeChartDataController.setParentController(this);
			timeChartDataController.setData(rootPaneController.getProcess());
			initTimeChartDataList();
			timeChartDataController.setTimeChartData(timeChartDataList);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

		}

	}

	private float getAvg() {
		return rootPaneController.getTimeData().getAvg();
	}

	public void setData() {
		initChart();
	}

	public RootPaneController getRootPaneController() {
		return rootPaneController;
	}

	public void setRootPaneController(RootPaneController rootPaneController) {
		this.rootPaneController = rootPaneController;
	}

	class ContMenu {

		ContextMenu contextMenu = new ContextMenu();

		MenuItem miFilter = new MenuItem();
		MenuItem miFilterReset = new MenuItem();
		MenuItem miResetActView = new MenuItem();
		MenuItem miSave = new MenuItem();
		MenuItem miRefresh = new MenuItem();
		MenuItem miTimeChartData = new MenuItem();
		MenuItem miXAxleLowerBound = new MenuItem();
		MenuItem miXAxleUpperBound = new MenuItem();
		MenuItem miYAxleLowerBound = new MenuItem();
		MenuItem miYAxleUpperBound = new MenuItem();

		Menu mSkalierung = new Menu();
		Menu mSeries = new Menu();
		Menu mScaleXAxle = new Menu();
		Menu mScaleYAxle = new Menu();

		RadioMenuItem rmiScaleOptionAuto = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionMinMax = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionAvg = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionManuell = new RadioMenuItem();
		RadioMenuItem rmiShowChartPoints = new RadioMenuItem();

		RadioMenuItem[] rmiSeries = new RadioMenuItem[2];

		ToggleGroup groupScaleOption = new ToggleGroup();

		private ContMenu() {
			init();
		}

		private void init() {

			rmiScaleOptionAuto.setToggleGroup(groupScaleOption);
			rmiScaleOptionMinMax.setToggleGroup(groupScaleOption);
			rmiScaleOptionAvg.setToggleGroup(groupScaleOption);
			rmiScaleOptionManuell.setToggleGroup(groupScaleOption);

			rmiScaleOptionMinMax.setSelected(true);

			for (int i = 0; i < rmiSeries.length; i++) {
				rmiSeries[i] = new RadioMenuItem();
				rmiSeries[i].setSelected(true);

			}
		}

	}
}
