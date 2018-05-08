package com.gretha.processmanager.view.process.charts;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.processmanager.view.process.result.TimeDataController;
import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.model.Result;
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
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ResultChartController implements Initializable {

	private Stage dialogStage;
	private static final Logger logger = Logger.getLogger(ResultChartController.class);
	private RootPaneController rootPaneController;

	@FXML
	private LineChart<Number, Number> chart;
	@FXML
	private NumberAxis chartxAxis;
	@FXML
	private NumberAxis chartyAxis;

	private int anzahlAvg;
	private boolean mouseInChartArea;
	private ContMenu contMenu;

	private XYChart.Series<Number, Number> seriesValue;
	private XYChart.Series<Number, Number> seriesLoLim;
	private XYChart.Series<Number, Number> seriesUpLim;
	private XYChart.Series<Number, Number> seriesSetvalue;
	private XYChart.Series<Number, Number> seriesAvg;
	private XYChart.Series<Number, Number> seriesStkAvg;

	private String[] seriesName = { "Messwert", "Untergrenze", "Obergrenze", "Prozessmitte-Soll", "Mittelwert",
			"Mittelwert [n Stk.]" };

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		chart.getStylesheets().add(Constants.STYLESHEET_CHART);
		chart.setId("resultchart");
		chart.lookup("#resultchart");

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
		chartyAxis.setAutoRanging(false);

		float range = Math.abs(getLastUpLim() - getLastLoLim());

		chartxAxis.setLowerBound(0);
		chartxAxis.setUpperBound(rootPaneController.getResults().size());

		chartyAxis.setLowerBound(getLastLoLim() - (range * 0.1));
		chartyAxis.setUpperBound(getLastUpLim() + (range * 0.1));

		setTickUnit();

	}

	private void setScaleOptionMinMax() {
		chartxAxis.setAutoRanging(false);
		chartyAxis.setAutoRanging(false);

		float min = rootPaneController.getResultData().getMin();
		float max = rootPaneController.getResultData().getMax();
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

	private void getChartPointsInfo() {
		float yValue;
		String sv;
		int i = 0;

		for (Series<Number, Number> s : chart.getData()) {
			i = 0;

			if (s.getName().equalsIgnoreCase("Messwert"))
				for (Data<Number, Number> d : s.getData()) {

					yValue = d.getYValue().floatValue();
					sv = DecimalPointFormatter.roundFloat2String(yValue,
							this.rootPaneController.getProcess().getDecimalPoints());

					StringBuilder sb = new StringBuilder();

					sb.append("Nr.: " + d.getXValue().toString());
					sb.append("\nMesswert: " + sv + " " + rootPaneController.getProcess().getUnit().getSign());

					if (!rootPaneController.getResults().get(i).getSerial().isEmpty()) {
						sb.append("\nSeriennummer: " + rootPaneController.getResults().get(i).getSerial());
					}

					if (i < (s.getData().size() - 1)) {

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
	}

	private void initChart() {

		chart.setAnimated(true);
		chart.setTitle(rootPaneController.getProcess().getStation() + ": " + rootPaneController.getProcess().getName()
				+ " - Messwertdiagramm");

		chart.getData().clear();

		chart.setHorizontalGridLinesVisible(true);
		chart.setVerticalGridLinesVisible(true);
		chart.setHorizontalZeroLineVisible(false);
		chart.setVerticalZeroLineVisible(false);

		chart.setCreateSymbols(false);

		chartxAxis.setAutoRanging(true);
		chartyAxis.setAutoRanging(true);

		chartxAxis.setLabel("Nr.");
		chartyAxis.setLabel("Messwert [ " + rootPaneController.getProcess().getUnit().getSign() + " ]");

		// Teilemittelwert alle x Teile auslesen
		anzahlAvg = rootPaneController.getProcess().getNrAvg();

		seriesValue = new XYChart.Series<>();
		seriesLoLim = new XYChart.Series<>();
		seriesUpLim = new XYChart.Series<>();
		seriesSetvalue = new XYChart.Series<>();
		seriesAvg = new XYChart.Series<>();
		seriesStkAvg = new XYChart.Series<>();

		seriesValue.setName(seriesName[0]);
		seriesLoLim.setName(seriesName[1]);
		seriesUpLim.setName(seriesName[2]);
		seriesSetvalue.setName(seriesName[3]);
		seriesAvg.setName(seriesName[4]);

		seriesName[5] = seriesName[4] + " [" + anzahlAvg + " Stk.]";
		seriesStkAvg.setName(seriesName[5]);

		ObservableList<Data<Number, Number>> dataSeriesValue = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesLoLim = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesUpLim = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesSetvalue = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesAvg = FXCollections.observableArrayList();
		ObservableList<Data<Number, Number>> dataSeriesStkAvg = FXCollections.observableArrayList();

		// Prozessmitte-Sollwert auslesen
		float setvalue = rootPaneController.getResultData().getSetvalue();

		if (anzahlAvg > 0) {

			/*
			 * Mittwelwert alle x Teile (Teilermittelwert z.B.: alle 10 Teile -->
			 * Verlaufvisu im Diagramm)
			 */
			List<Result> avgList = getStkAvg();

			float j = Math.round(anzahlAvg / 2.00);

			for (int i = 0; i < avgList.size(); i++) {
				dataSeriesStkAvg.add(new XYChart.Data<>(j, avgList.get(i).getValue()));
				j = j + anzahlAvg;
			}
		}

		for (Result res : rootPaneController.getResults()) {

			dataSeriesValue.add(new XYChart.Data<>(res.getNr(), res.getValue()));
			dataSeriesLoLim.add(new XYChart.Data<>(res.getNr(), res.getLoLim()));
			dataSeriesUpLim.add(new XYChart.Data<>(res.getNr(), res.getUpLim()));
			dataSeriesSetvalue.add(new XYChart.Data<>(res.getNr(), setvalue));
			dataSeriesAvg.add(new XYChart.Data<>(res.getNr(), getAvg()));

		}

		seriesStkAvg.setData(dataSeriesStkAvg);
		seriesValue.setData(dataSeriesValue);
		seriesLoLim.setData(dataSeriesLoLim);
		seriesUpLim.setData(dataSeriesUpLim);
		seriesSetvalue.setData(dataSeriesSetvalue);
		seriesAvg.setData(dataSeriesAvg);

		for (int i = 0; i < contMenu.rmiSeries.length; i++) {
			contMenu.rmiSeries[i].setSelected(true);
		}

		if (anzahlAvg > 0) {
			chart.getData().addAll(seriesValue, seriesLoLim, seriesUpLim, seriesSetvalue, seriesAvg, seriesStkAvg);

			contMenu.rmiSeries[5].setSelected(true);
			contMenu.rmiSeries[5].setDisable(false);
			contMenu.rmiSeries[5].setVisible(true);
			contMenu.rmiSeries[5].setText(seriesName[5]);

		} else {
			chart.getData().addAll(seriesValue, seriesLoLim, seriesUpLim, seriesSetvalue, seriesAvg);

			contMenu.rmiSeries[5].setSelected(false);
			contMenu.rmiSeries[5].setDisable(true);
			contMenu.rmiSeries[5].setVisible(false);
			contMenu.rmiSeries[5].setText(seriesName[5]);

		}

		setSeriesVisibility();

		// Defaultmäßig UG-OG für Auswahlbox vorwählen
		setScaleOptionUgOg();
		contMenu.rmiScaleOptionUgOg.setSelected(true);

		// Defaultmätig Punkte anzeigen ausschalten
		contMenu.rmiShowChartPoints.setSelected(false);

		// Tooltipp erzeigen
		Tooltip t = new Tooltip();

		if (anzahlAvg > 0) {
			// Tooltipp Text generieren
			if (seriesStkAvg.getNode() != null)
				seriesStkAvg.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent e) {
						t.setText(seriesName[5]);
						Tooltip.install(seriesStkAvg.getNode(), t);
					}
				});
		}

		if (seriesSetvalue.getNode() != null)
			seriesSetvalue.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					t.setText(seriesName[3]);
					Tooltip.install(seriesSetvalue.getNode(), t);
				}
			});

		if (seriesAvg.getNode() != null)
			seriesAvg.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					t.setText(seriesName[4]);
					Tooltip.install(seriesAvg.getNode(), t);
				}
			});

		if (seriesUpLim.getNode() != null)
			seriesUpLim.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					t.setText(seriesName[2]);
					Tooltip.install(seriesUpLim.getNode(), t);
				}
			});

		if (seriesLoLim.getNode() != null)
			seriesLoLim.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					t.setText(seriesName[1]);
					Tooltip.install(seriesLoLim.getNode(), t);
				}
			});

		if (seriesValue.getNode() != null)
			seriesValue.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent e) {
					t.setText(seriesName[0]);
					Tooltip.install(seriesValue.getNode(), t);
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
				contMenu.rmiScaleOptionMinMax, contMenu.rmiScaleOptionUgOg);

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

		// Add MenuItem to ContextMenu
		contMenu.contextMenu.getItems().addAll(contMenu.miResetActView, contMenu.miRefresh, new SeparatorMenuItem(),
				contMenu.mSkalierung, contMenu.mSeries, contMenu.rmiShowChartPoints, contMenu.miFilter,
				contMenu.miFilterReset, new SeparatorMenuItem(), contMenu.miSave);

	}

	private List<Result> getStkAvg() {

		float sum = 0;

		List<Result> resultsforAvg = new ArrayList<>();
		List<Result> avgList = new ArrayList<>();

		for (int i = 0; i < rootPaneController.getResults().size(); i++) {

			sum = 0;

			resultsforAvg.add(rootPaneController.getResults().get(i));

			if (resultsforAvg.size() == anzahlAvg) {

				for (int j = 0; j < anzahlAvg; j++) {

					float wert = resultsforAvg.get(j).getValue();
					sum = sum + wert;
				}

				Result result = new Result();
				result.setValue(sum / anzahlAvg);
				avgList.add(result);
				resultsforAvg.clear();
			}
		}

		return avgList;
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
		MenuItem miXAxleLowerBound = new MenuItem();
		MenuItem miXAxleUpperBound = new MenuItem();
		MenuItem miYAxleLowerBound = new MenuItem();
		MenuItem miYAxleUpperBound = new MenuItem();

		Menu mSkalierung = new Menu();
		Menu mSeries = new Menu();
		Menu mScaleXAxle = new Menu();
		Menu mScaleYAxle = new Menu();

		RadioMenuItem rmiScaleOptionAuto = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionUgOg = new RadioMenuItem();
		RadioMenuItem rmiScaleOptionMinMax = new RadioMenuItem();
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

			rmiScaleOptionUgOg.setSelected(true);

			for (int i = 0; i < rmiSeries.length; i++) {
				rmiSeries[i] = new RadioMenuItem();
				rmiSeries[i].setSelected(true);

			}

		}

	}

}
