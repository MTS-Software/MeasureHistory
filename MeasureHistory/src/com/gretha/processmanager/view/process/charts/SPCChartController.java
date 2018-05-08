package com.gretha.processmanager.view.process.charts;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.result.SPCClassDataController;
import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.model.Result;
import com.gretha.shared.model.SPCClass;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.InputChartBound;
import com.gretha.shared.util.SPCValue;
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
import javafx.scene.chart.AreaChart;
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

/**
 * @author user
 *
 */
public class SPCChartController implements Initializable {

	private Stage dialogStage;
	private static final Logger logger = Logger.getLogger(SPCChartController.class);
	private RootPaneController rootPaneController;

	@FXML
	private AreaChart<Number, Number> chart;
	@FXML
	private NumberAxis chartxAxis;
	@FXML
	private NumberAxis chartyAxis;

	private List<SPCClass> klassenList;
	private float maxKlassenStueck;
	private int klassen;
	private boolean yAxisAbsolut;

	private boolean mouseInChartArea;
	private ContMenu contMenu;

	private ObservableList<SPCClass> spcClassChartDataList;
	private ResourceBundle resources;

	private AreaChart.Series<Number, Number> seriesXquer;
	private AreaChart.Series<Number, Number> seriesUSG;
	private AreaChart.Series<Number, Number> seriesOSG;
	private AreaChart.Series<Number, Number> seriesXquerMinusSigma;
	private AreaChart.Series<Number, Number> seriesXquerPlusSigma;
	private AreaChart.Series<Number, Number> seriesKlassen;
	private AreaChart.Series<Number, Number> seriesGaussian;

	private String[] seriesName = { "Xquer", "USG", "OSG", "Xquer-3s", "Xquer+3s", "Klassen", "Gauss" };

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

		chart.getStylesheets().add(Constants.STYLESHEET_CHART);
		chart.setId("spcchart");
		chart.lookup("#spcchart");

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

	private void setScaleOptionUgOg() {
		chartxAxis.setAutoRanging(false);
		chartyAxis.setAutoRanging(true);

		float range = Math.abs(getLastUpLim() - getLastLoLim());

		chartxAxis.setLowerBound(getLastLoLim() - (range * 0.1));
		chartxAxis.setUpperBound(getLastUpLim() + (range * 0.1));

		setTickUnit();
	}

	private void setScaleOptionMinMax() {

		chartxAxis.setAutoRanging(false);
		chartyAxis.setAutoRanging(true);

		float min = rootPaneController.getResultData().getMin();
		float max = rootPaneController.getResultData().getMax();
		float range = Math.abs(max - min);

		chartxAxis.setLowerBound(min - (range * 0.1));
		chartxAxis.setUpperBound(max + (range * 0.1));

		setTickUnit();
	}

	private void setScaleOptionSixSigma() {

		chartxAxis.setAutoRanging(false);
		chartyAxis.setAutoRanging(true);

		chartxAxis.setLowerBound(getAvg() - (3 * getSigma()) - ((3 * getSigma()) * 0.2));
		chartxAxis.setUpperBound(getAvg() + (3 * getSigma()) + ((3 * getSigma()) * 0.2));

		setTickUnit();
	}

	private void setTickUnit() {
		setTickUnitXAxis();
		setTickUnitYAxis();
	}

	private void setTickUnitXAxis() {
		chartxAxis.setTickUnit((chartxAxis.getUpperBound() - chartxAxis.getLowerBound()) / 10);
		chartxAxis.setMinorTickVisible(true);
		chartxAxis.setMinorTickCount(2);
	}

	private void setTickUnitYAxis() {
		if (yAxisAbsolut)
			chartyAxis.setTickUnit(Math.round((chartyAxis.getUpperBound() - chartyAxis.getLowerBound()) / 10));
		else
			chartyAxis.setTickUnit(chartyAxis.getUpperBound() - chartyAxis.getLowerBound());

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

	private void getChartPointsInfo() {
		float xValue;
		int i;
		String sv;
		SPCClass klasse;
		int aktKlasse;

		for (Series<Number, Number> s : chart.getData()) {

			aktKlasse = 1;
			i = 0;

			if (s.getName().equalsIgnoreCase("Klassen"))
				for (Data<Number, Number> d : s.getData()) {

					xValue = d.getXValue().floatValue();

					sv = DecimalPointFormatter.roundFloat2String(xValue,
							this.rootPaneController.getProcess().getDecimalPoints());

					klasse = klassenList.get(aktKlasse - 1);

					StringBuilder sb = new StringBuilder();

					sb.append("Klasse: " + klasse.getId() + "\n");
					sb.append("Klassenbereich: "
							+ DecimalPointFormatter.roundFloat2String(klasse.getMin(),
									rootPaneController.getProcess().getDecimalPoints())
							+ " - " + DecimalPointFormatter.roundFloat2String(klasse.getMax(),
									rootPaneController.getProcess().getDecimalPoints())
							+ "\n");
					sb.append("absolute Häufigkeit: " + klasse.getStueckAbsolut() + " Stk." + "\n");
					sb.append("relative Häufigkeit: " + klasse.getStueckRelativ() + " %");

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

					// Wenn der Tooltip für jeden 4. Punkt erstellt wurde,
					// Klassenzaehler inkrementieren
					if (i % 4 == 0) {
						aktKlasse++;
					}
				}
		}
	}

	private void initChart() {

		yAxisAbsolut = contMenu.rmiViewYaxisAbsolut.isSelected();

		float lastLoLim = getLastLoLim();
		float lastUpLim = getLastUpLim();
		float min = rootPaneController.getResultData().getMin();
		float max = rootPaneController.getResultData().getMax();

		chart.setAnimated(true);
		chart.setTitle(rootPaneController.getProcess().getStation() + ": " + rootPaneController.getProcess().getName()
				+ " - Histogramm");

		if (yAxisAbsolut) {
			chartyAxis.setLabel("absolute Häufigkeit [ Stk. ]  --->");
			chartxAxis.setLabel("Messwert [ " + rootPaneController.getProcess().getUnit().getSign() + " ]");
		} else {
			chartyAxis.setLabel("relative Häufigkeit [ % ]  --->");
			chartxAxis.setLabel("Messwert [ " + rootPaneController.getProcess().getUnit().getSign() + " ]");
		}

		// Anzahl an SPC-Klassen
		klassen = rootPaneController.getProcess().getNrSpcClass();

		chart.getData().clear();

		chart.setHorizontalZeroLineVisible(false);
		chart.setVerticalZeroLineVisible(false);
		chart.setHorizontalGridLinesVisible(true);
		chart.setVerticalGridLinesVisible(true);
		chart.setHorizontalZeroLineVisible(false);
		chart.setVerticalZeroLineVisible(false);

		chart.setCreateSymbols(false);

		chartxAxis.setAutoRanging(true);
		chartyAxis.setAutoRanging(true);

		seriesXquer = new AreaChart.Series<>();
		seriesUSG = new AreaChart.Series<>();
		seriesOSG = new AreaChart.Series<>();
		seriesXquerMinusSigma = new AreaChart.Series<>();
		seriesXquerPlusSigma = new AreaChart.Series<>();
		seriesKlassen = generateKlassen(min, max, klassen, yAxisAbsolut);
		seriesGaussian = generateGaussian(min, max, maxKlassenStueck, getAvg(), getSigma());

		seriesXquer.setName(seriesName[0]);
		seriesUSG.setName(seriesName[1]);
		seriesOSG.setName(seriesName[2]);
		seriesXquerMinusSigma.setName(seriesName[3]);
		seriesXquerPlusSigma.setName(seriesName[4]);
		seriesKlassen.setName(seriesName[5]);
		seriesGaussian.setName(seriesName[6]);

		ObservableList<Data<Number, Number>> dataSeriesXquer = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesUSG = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesOSG = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesXquerMinusSigma = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesXquerPlusSigma = FXCollections.observableArrayList();

		dataSeriesXquer.add(new AreaChart.Data<>(getAvg(), 0));
		dataSeriesXquer.add(new AreaChart.Data<>(getAvg(), maxKlassenStueck));

		dataSeriesUSG.add(new AreaChart.Data<>(lastLoLim, 0));
		dataSeriesUSG.add(new AreaChart.Data<>(lastLoLim, maxKlassenStueck));

		dataSeriesOSG.add(new AreaChart.Data<>(lastUpLim, 0));
		dataSeriesOSG.add(new AreaChart.Data<>(lastUpLim, maxKlassenStueck));

		dataSeriesXquerMinusSigma.add(new AreaChart.Data<>(getAvg() - (3 * getSigma()), 0));
		dataSeriesXquerMinusSigma.add(new AreaChart.Data<>(getAvg() - (3 * getSigma()), maxKlassenStueck));

		dataSeriesXquerPlusSigma.add(new AreaChart.Data<>(getAvg() + (3 * getSigma()), 0));
		dataSeriesXquerPlusSigma.add(new AreaChart.Data<>(getAvg() + (3 * getSigma()), maxKlassenStueck));

		seriesXquer.setData(dataSeriesXquer);
		seriesUSG.setData(dataSeriesUSG);
		seriesOSG.setData(dataSeriesOSG);
		seriesXquerMinusSigma.setData(dataSeriesXquerMinusSigma);
		seriesXquerPlusSigma.setData(dataSeriesXquerPlusSigma);

		chart.getData().addAll(seriesXquer, seriesUSG, seriesOSG, seriesXquerMinusSigma, seriesXquerPlusSigma,
				seriesKlassen, seriesGaussian);

		for (int i = 0; i < contMenu.rmiSeries.length; i++) {
			contMenu.rmiSeries[i].setSelected(true);
		}

		setSeriesVisibility();

		// Defaultmäßig UG-OG für Auswahlbox vorwählen
		setScaleOptionSixSigma();
		contMenu.rmiScaleSixSigma.setSelected(true);

		// Defaultmätig Punkte anzeigen ausschalten
		contMenu.rmiShowChartPoints.setSelected(false);

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

		contMenu.rmiViewYaxisAbsolut.setText("absolute Häufigkeit");
		contMenu.rmiViewYaxisAbsolut.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				initChart();
			}
		});

		contMenu.rmiViewYaxisRelativ.setText("relative Häufigkeit");
		contMenu.rmiViewYaxisRelativ.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				initChart();
			}
		});

		contMenu.mView.setText("Ansicht");
		contMenu.mView.getItems().addAll(contMenu.rmiViewYaxisRelativ, contMenu.rmiViewYaxisAbsolut);

		contMenu.rmiScaleOptionAuto.setText("Automatisch");
		contMenu.rmiScaleOptionAuto.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				chart.setAnimated(false);
				setScaleOptionAuto();
			}
		});

		contMenu.rmiScaleOptionUgOg.setText("Untergrenze und Obergrenze");
		contMenu.rmiScaleOptionUgOg.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setScaleOptionUgOg();
			}
		});

		contMenu.rmiScaleOptionMinMax.setText("Minimum und Maximum");
		contMenu.rmiScaleOptionMinMax.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setScaleOptionMinMax();
			}
		});

		contMenu.rmiScaleSixSigma.setText("Six Sigma");
		contMenu.rmiScaleSixSigma.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setScaleOptionSixSigma();
			}
		});

		contMenu.miXAxleLowerBound.setText("Lower Bound");
		contMenu.miXAxleLowerBound.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				chartxAxis.setLowerBound(InputChartBound.getBound("X-Achse Lower Bound",
						DecimalPointFormatter.roundFloat2String((float) chartxAxis.getLowerBound(),
								rootPaneController.getProcess().getDecimalPoints())));

				setTickUnitXAxis();

			}
		});

		contMenu.miXAxleUpperBound.setText("Upper Bound");
		contMenu.miXAxleUpperBound.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				chartxAxis.setUpperBound(InputChartBound.getBound("X-Achse Upper Bound",
						DecimalPointFormatter.roundFloat2String((float) chartxAxis.getUpperBound(),
								rootPaneController.getProcess().getDecimalPoints())));

				setTickUnitXAxis();

			}
		});

		contMenu.mScaleXAxle.setText("X-Achse");
		contMenu.mScaleXAxle.getItems().addAll(contMenu.miXAxleLowerBound, contMenu.miXAxleUpperBound);

		contMenu.mSkalierung.setText("Skalierung");
		contMenu.mSkalierung.getItems().addAll(contMenu.mScaleXAxle, contMenu.rmiScaleOptionAuto,
				contMenu.rmiScaleOptionMinMax, contMenu.rmiScaleOptionUgOg, contMenu.rmiScaleSixSigma);

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

		contMenu.miSPCClassesChartData.setText("Daten SPC-Klassen");
		contMenu.miSPCClassesChartData.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				showSPCClassChartData();

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

		contMenu.rmiSeries[2].setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setSeriesVisibility();
			}
		});

		contMenu.rmiSeries[3].setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setSeriesVisibility();
			}
		});

		contMenu.rmiSeries[4].setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setSeriesVisibility();
			}
		});

		contMenu.rmiSeries[5].setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setSeriesVisibility();
			}
		});

		contMenu.rmiSeries[6].setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setSeriesVisibility();
			}
		});

		// Add MenuItem to ContextMenu
		contMenu.contextMenu.getItems().addAll(contMenu.miResetActView, contMenu.miRefresh, new SeparatorMenuItem(),
				contMenu.mView, contMenu.mSkalierung, contMenu.mSeries, contMenu.rmiShowChartPoints, contMenu.miFilter,
				contMenu.miFilterReset, contMenu.miSPCClassesChartData, new SeparatorMenuItem(), contMenu.miSave);
	}

	private AreaChart.Series<Number, Number> generateGaussian(float min, float max, float maxStueckZahl, float avg,
			float sigma) {

		AreaChart.Series<Number, Number> series = new AreaChart.Series<>();
		List<Float> xData = new ArrayList<>();
		Gaussian gauss;

		float range = Math.abs(max - min);
		float sigma3 = (3 * sigma);
		float proz = (sigma3 / 100 * 20);

		// Prozentsatz einrechnen, damit die Kurve weiter als 3Sigma
		// gezeichnet wird
		float minCurveX = avg - sigma3 - proz;
		float maxCurveX = avg + sigma3 + proz;
		float curvePosX = minCurveX;

		if (sigma != 0) {
			gauss = new Gaussian(maxStueckZahl, avg, sigma);

			// Daten anhand von Wertebereichen für die X-Achse berechnen
			while (curvePosX < Float.MAX_VALUE) {

				xData.add(curvePosX);

				if (range < 1)
					curvePosX = (float) (curvePosX + 0.001);

				if (range >= 1 && range < 10)
					curvePosX = (float) (curvePosX + 0.01);

				if (range >= 10 && range < 100)
					curvePosX = (float) (curvePosX + 0.1);

				if (range >= 100 && range < 1000)
					curvePosX = (float) (curvePosX + 1);

				if (range >= 1000 && range < 10000)
					curvePosX = (float) (curvePosX + 10);

				if (range >= 10000)
					curvePosX = (float) (curvePosX + 100);

				// Wurde der Maximalwert der Kurve erreicht Schleife beenden
				if (curvePosX > maxCurveX)
					break;

			}

			// Kurvendaten auf X und Y Variablen übertragen
			for (int i = 0; i < xData.size(); i++) {
				series.getData().add(new AreaChart.Data<>(xData.get(i), gauss.value(xData.get(i))));

			}
		}

		return series;

	}

	private AreaChart.Series<Number, Number> generateKlassen(float xMin, float xMax, int anzKlassen, boolean absolut) {

		AreaChart.Series<Number, Number> series = new AreaChart.Series<>();
		SPCClass klasse = null;
		spcClassChartDataList = FXCollections.observableArrayList();

		int yStart = 0;
		float min = 0;
		float max = 0;
		int sumStueck = 0;
		float range = Math.abs(xMax - xMin);
		float bandWidth = range / anzKlassen;

		klassenList = new ArrayList<>();
		int maxKlassenStueckAbsolut = 0;
		float maxKlassenStueckRelativ = (float) 0.00;

		if (anzKlassen > 0) {

			for (int i = 0; i < anzKlassen; i++) {

				// 1. Klasse berechnen mit Startwerten da es ja noch keine
				// vorherige Klasse gibt
				if (i == 0) {
					min = xMin;
					max = min + bandWidth;
				}
				// Wird nur ausgefuert um alle Klassen zwischen 1. und letzter
				// zu berechnen
				if (i > 0 && i < anzKlassen - 1) {
					min = klasse.getMax();
					max = klasse.getMax() + bandWidth;
				}
				// Letzte Klasse muss mit xMax Wert berechnet werden, da sich
				// durch die Division vom Range die Kommastelle verschiebt und
				// dies zu Ungenauigkeiten führt (Letze Stueck sind dann nicht
				// mehr im Range)
				// Darf nur durchgefuehrt werden wenn Klassenanzahl > 1 ist
				if (anzKlassen > 1)
					if (i == (anzKlassen - 1)) {
						min = klasse.getMax();
						max = xMax;
					}

				klasse = getKlassenStueck(min, max, i + 1);
				klassenList.add(klasse);
				spcClassChartDataList.add(klasse);

				if (absolut) {
					// Linien zeichnen bei Darstellung Absolut
					for (int j = 0; j < 4; j++) {
						if (j == 0) {
							series.getData().add(new AreaChart.Data<>(klasse.getMin(), yStart));
						}
						if (j == 1) {
							series.getData().add(new AreaChart.Data<>(klasse.getMin(), klasse.getStueckAbsolut()));
						}
						if (j == 2) {
							series.getData().add(new AreaChart.Data<>(klasse.getMax(), klasse.getStueckAbsolut()));
						}
						if (j == 3) {
							series.getData().add(new AreaChart.Data<>(klasse.getMax(), yStart));
						}

					}
				} else {

					// Linien zeichnen bei Darstellung Relativ
					for (int j = 0; j < 4; j++) {
						if (j == 0) {
							series.getData().add(new AreaChart.Data<>(klasse.getMin(), yStart));
						}
						if (j == 1) {
							series.getData().add(new AreaChart.Data<>(klasse.getMin(), klasse.getStueckRelativ()));
						}
						if (j == 2) {
							series.getData().add(new AreaChart.Data<>(klasse.getMax(), klasse.getStueckRelativ()));
						}
						if (j == 3) {
							series.getData().add(new AreaChart.Data<>(klasse.getMax(), yStart));
						}

					}
				}
			}
		}

		// Gesamtstueck und maximale Stueck ermitteln
		for (SPCClass kl : klassenList) {
			sumStueck = sumStueck + kl.getStueckAbsolut();
			if (kl.getStueckAbsolut() > maxKlassenStueckAbsolut) {
				maxKlassenStueckAbsolut = kl.getStueckAbsolut();
			}
		}

		maxKlassenStueckRelativ = (float) (100.0 * maxKlassenStueckAbsolut / sumStueck);
		maxKlassenStueckRelativ = (float) (Math.round(maxKlassenStueckRelativ * 100.0) / 100.0);

		if (absolut)
			maxKlassenStueck = maxKlassenStueckAbsolut;
		else
			maxKlassenStueck = maxKlassenStueckRelativ;

		SPCClass.setGesamtStueck(sumStueck);

		return series;

	}

	private SPCClass getKlassenStueck(float min, float max, int aktKlasse) {

		SPCClass klasse;
		int stkAbsolut = 0;
		float stkRelativ = (float) 0.00;
		int stkGesamt = rootPaneController.getResults().size();

		if (stkGesamt > 0) {

			for (Result result : rootPaneController.getResults()) {
				if (aktKlasse < klassen) {
					if (result.getValue() >= min && result.getValue() < max) {
						stkAbsolut++;
						stkRelativ = (float) (100.0 * stkAbsolut / stkGesamt);
						stkRelativ = (float) (Math.round(stkRelativ * 100.0) / 100.0);

					}
				}
				if (aktKlasse >= klassen) {
					if (result.getValue() >= min && result.getValue() <= max) {
						stkAbsolut++;
						stkRelativ = (float) (100.0 * stkAbsolut / stkGesamt);
						stkRelativ = (float) (Math.round(stkRelativ * 100.0) / 100.0);
					}
				}

			}
		}

		klasse = new SPCClass();
		klasse.setId(aktKlasse);
		klasse.setMin(min);
		klasse.setMax(max);
		klasse.setStueckAbsolut(stkAbsolut);
		klasse.setStueckRelativ(stkRelativ);

		// logger.info(klasse);

		return klasse;
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

		if (contMenu.rmiScaleOptionUgOg.isSelected()) {
			setScaleOptionUgOg();
		}

		if (contMenu.rmiScaleOptionMinMax.isSelected()) {
			setScaleOptionMinMax();
		}

		if (contMenu.rmiScaleSixSigma.isSelected()) {
			setScaleOptionSixSigma();
		}
	}

	private void showSPCClassChartData() {
		try {
			// Load the fxml file and create a new stage for the popup dialog.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/process/result/SPCClassData.fxml"));
			loader.setResources(resources);
			AnchorPane pane = (AnchorPane) loader.load();

			// Create the dialog Stage.
			Stage dialogStage = new Stage();
			dialogStage.getIcons().addAll(this.dialogStage.getIcons());
			dialogStage.setTitle(rootPaneController.getProcess().getStation() + ": "
					+ rootPaneController.getProcess().getName() + " - " + "Daten SPC-Klassen");
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(this.dialogStage);
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(Constants.STYLESHEET);
			dialogStage.setScene(scene);

			// Set the person into the controller.
			SPCClassDataController spcClassChartDataController = loader.getController();
			spcClassChartDataController.setDialogStage(dialogStage);
			spcClassChartDataController.setParentController(this);
			spcClassChartDataController.setData(rootPaneController.getProcess());
			spcClassChartDataController.setSPCClassChartData(spcClassChartDataList);

			// Show the dialog and wait until the user closes it
			dialogStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();

		}

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

	private float getAvg() {
		return rootPaneController.getResultData().getAvg();
	}

	private float getLastLoLim() {
		return rootPaneController.getResultData().getLastLoLim();
	}

	private float getLastUpLim() {
		return rootPaneController.getResultData().getLastUpLim();
	}

	private float getSigma() {
		return SPCValue.getSigma(getAvg(), rootPaneController.getResults());
	}

	public void setData() {
		contMenu.rmiViewYaxisRelativ.setSelected(true);
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
		MenuItem miSPCClassesChartData = new MenuItem();
		MenuItem miXAxleLowerBound = new MenuItem();
		MenuItem miXAxleUpperBound = new MenuItem();

		Menu mView = new Menu();
		Menu mSkalierung = new Menu();
		Menu mSeries = new Menu();
		Menu mScaleXAxle = new Menu();

		RadioMenuItem rmiScaleOptionAuto = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionUgOg = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionMinMax = new RadioMenuItem();
		RadioMenuItem rmiScaleSixSigma = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionManuell = new RadioMenuItem();
		RadioMenuItem rmiShowChartPoints = new RadioMenuItem();
		RadioMenuItem rmiViewYaxisRelativ = new RadioMenuItem();
		RadioMenuItem rmiViewYaxisAbsolut = new RadioMenuItem();

		RadioMenuItem[] rmiSeries = new RadioMenuItem[7];

		ToggleGroup groupScaleOption = new ToggleGroup();
		ToggleGroup groupView = new ToggleGroup();

		private ContMenu() {
			init();
		}

		private void init() {

			rmiViewYaxisAbsolut.setToggleGroup(groupView);
			rmiViewYaxisRelativ.setToggleGroup(groupView);

			rmiScaleOptionAuto.setToggleGroup(groupScaleOption);
			rmiScaleOptionUgOg.setToggleGroup(groupScaleOption);
			rmiScaleOptionMinMax.setToggleGroup(groupScaleOption);
			rmiScaleSixSigma.setToggleGroup(groupScaleOption);
			rmiScaleOptionManuell.setToggleGroup(groupScaleOption);

			rmiScaleSixSigma.setSelected(true);
			rmiViewYaxisRelativ.setSelected(true);
			rmiViewYaxisAbsolut.setSelected(false);

			for (int i = 0; i < rmiSeries.length; i++) {
				rmiSeries[i] = new RadioMenuItem();
				rmiSeries[i].setSelected(true);

			}

		}

	}

}
