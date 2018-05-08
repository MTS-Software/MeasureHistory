package com.gretha.processmanager.view.process.charts;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.gretha.processmanager.view.process.root.RootPaneController;
import com.gretha.shared.model.Result;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
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
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StatisticChartController implements Initializable {

	private Stage dialogStage;
	private static final Logger logger = Logger.getLogger(StatisticChartController.class);
	private RootPaneController rootPaneController;

	private PieChart.Data dataPieNokLoLim;
	private PieChart.Data dataPieNokUpLim;
	private PieChart.Data dataPieNokUndef;

	private PieChart.Data dataPieOk;
	private PieChart.Data dataPieNok;
	private PieChart.Data dataPieUnbewertet;

	private BarChart.Data<String, Number> dataBarNokLoLim;
	private BarChart.Data<String, Number> dataBarNokUpLim;
	private BarChart.Data<String, Number> dataBarNokUndef;

	private BarChart.Data<String, Number> dataBarOk;
	private BarChart.Data<String, Number> dataBarNok;
	private BarChart.Data<String, Number> dataBarUnbewertet;

	@FXML
	private HBox hboxCharts;
	@FXML
	private PieChart pieChart;
	@FXML
	private BarChart<NumberAxis, CategoryAxis> barChart;
	@FXML
	private CategoryAxis barChartXaxis;
	@FXML
	private NumberAxis barChartYaxis;

	private boolean mouseInChartArea;
	private ContMenu contMenu;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		pieChart.setVisible(true);
		barChart.setVisible(true);

		pieChart.setAnimated(true);
		barChart.setAnimated(true);

		contMenu = new ContMenu();

		initContextMenu();
		initEvents();

	}

	private void initEvents() {

		hboxCharts.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {

				mouseInChartArea = false;
				hboxCharts.getScene().setCursor(Cursor.DEFAULT);
			}
		});

		hboxCharts.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {

				mouseInChartArea = true;

				hboxCharts.getScene().setOnKeyPressed(new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {

						if (mouseInChartArea) {

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

				hboxCharts.setOnMouseReleased(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent mouseEvent) {

						contMenu.contextMenu.hide();

						if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {

							if (rootPaneController.getRootPaneBarController().getFilter() != null) {
								if (rootPaneController.getRootPaneBarController().getFilter().isActivated())
									contMenu.miFilterReset.setDisable(false);
							} else
								contMenu.miFilterReset.setDisable(true);

							hboxCharts.getScene().setCursor(Cursor.DEFAULT);

							contMenu.contextMenu.show(hboxCharts, mouseEvent.getScreenX(), mouseEvent.getScreenY());
						}
					}
				});
			}
		});
	}

	private void resetActView() {

		if (contMenu.rmiDiagrammBar.isSelected()) {
			barChart.setAnimated(false);
			pieChart.setAnimated(false);
			initBarChart();
			showBarChart();
		}
		if (contMenu.rmiDiagrammPie.isSelected()) {
			barChart.setAnimated(false);
			pieChart.setAnimated(false);
			initPieChart();
			showPieChart();
		}
		if (contMenu.rmiDiagrammStandard.isSelected()) {
			barChart.setAnimated(false);
			pieChart.setAnimated(false);
			initPieAndBarChart();
			showPieAndBarChart();
		}

	}

	private void initPieChart() {

		int pieceValue;
		float pieceValuePercent;

		pieChart.setStartAngle(0);
		pieChart.setTitle(rootPaneController.getProcess().getStation() + ": "
				+ rootPaneController.getProcess().getName() + " - Tortendiagramm");

		pieChart.setStartAngle(0);
		pieChart.getData().clear();
		contMenu.rmiDiagrammPieAll.setSelected(true);

		// Tortendiagramm IO Daten erzeugen
		pieceValue = rootPaneController.getResultData().getOk();
		pieceValuePercent = rootPaneController.getResultData().getOkPercent();

		if (pieceValue > 0) {
			dataPieOk = new PieChart.Data(pieceValue + " Stk." + " (" + pieceValuePercent + " %)", pieceValue);

			dataPieOk.nodeProperty().addListener(new ChangeListener<Node>() {
				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightgreen; -fx-border-width: 0.0px;");
					}
				}
			});

			pieChart.getData().add(dataPieOk);

		}

		// Tortendiagramm NIO Daten erzeugen
		pieceValue = rootPaneController.getResultData().getNok();
		pieceValuePercent = rootPaneController.getResultData().getNokPercent();

		if (pieceValue > 0) {

			dataPieNok = new PieChart.Data(pieceValue + " Stk." + " (" + pieceValuePercent + " %)", pieceValue);

			dataPieNok.nodeProperty().addListener(new ChangeListener<Node>() {

				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightcoral; -fx-border-width: 2.5px;");
					}
				}
			});

			pieChart.getData().add(dataPieNok);

			dataPieNok.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent e) {

					float anzNok;
					int anzNokOg;
					int anzNokUg;
					float pieceValuePercent;
					int anzNokUndef;
					Tooltip t = new Tooltip();
					StringBuilder sb = new StringBuilder();

					// Anzahl an NIO ermitteln welche die OG überschritten bzw.
					// die UG unterschritten haben
					anzNokOg = getAnzNokOg();
					anzNokUg = getAnzNokUg();
					anzNokUndef = getAnzNokUndef();

					// Gesamtanzahl der NIO unabhängig ob OG oder UG verletzt
					anzNok = anzNokOg + anzNokUg + anzNokUndef;

					if (anzNokUndef > 0) {
						pieceValuePercent = (float) (Math.round(100.0 * anzNokUndef / anzNok * 100) / 100.0);
						sb.append(
								"Plausibilitätsfehler: " + anzNokUndef + " Stk." + " (" + pieceValuePercent + " %)\n");
					}

					if (anzNokOg > 0) {
						pieceValuePercent = (float) (Math.round(100.0 * anzNokOg / anzNok * 100) / 100.0);
						sb.append("OG verletzt: " + anzNokOg + " Stk." + " (" + pieceValuePercent + " %)\n");
					}

					if (anzNokUg > 0) {
						pieceValuePercent = (float) (Math.round(100.0 * anzNokUg / anzNok * 100) / 100.0);
						sb.append("UG verletzt: " + anzNokUg + " Stk." + " (" + pieceValuePercent + " %)");
					}

					t.setText(sb.toString());
					Tooltip.install(dataPieNok.getNode(), t);

				}
			});

			// DrillDown Chart Daten erzeugen/aufrufen
			initDrillDownPieData();
		}

		// Tortendiagramm Unbwertet Daten erzeugen
		pieceValue = rootPaneController.getResultData().getUnbewertet();
		pieceValuePercent = rootPaneController.getResultData().getUnbewertetPercent();

		if (pieceValue > 0)

		{

			dataPieUnbewertet = new PieChart.Data(pieceValue + " Stk." + " (" + pieceValuePercent + " %)", pieceValue);

			dataPieUnbewertet.nodeProperty().addListener(new ChangeListener<Node>() {
				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightgrey; -fx-border-width: 0.0px;");
					}
				}
			});

			pieChart.getData().add(dataPieUnbewertet);

		}

	}

	private void initDrillDownPieData() {

		// Mauszeiger je nach Situation ändern
		dataPieNok.getNode().setOnMouseExited((event) -> {
			dialogStage.getScene().setCursor(Cursor.DEFAULT);
		});

		dataPieNok.getNode().setOnMouseEntered((event) -> {
			dialogStage.getScene().setCursor(Cursor.HAND);
		});

		dataPieNok.getNode().setOnMouseClicked((event) -> {
			if (event.getButton() == MouseButton.PRIMARY && !event.isShortcutDown())
				showDrillDownPieChart();
		});
	}

	private void showDrillDownPieChart() {

		float anzNok;
		int anzNokUpLim;
		int anzNokLoLim;
		int anzNokUndef;

		contMenu.rmiDiagrammPieDrill.setSelected(true);

		// Anzahl an NIO ermitteln welche die OG überschritten bzw. die UG
		// unterschritten haben
		anzNokUpLim = getAnzNokOg();
		anzNokLoLim = getAnzNokUg();
		anzNokUndef = getAnzNokUndef();

		// Gesamtanzahl der NIO unabhängig ob OG oder UG verletzt
		anzNok = anzNokUpLim + anzNokLoLim + anzNokUndef;

		float pieceValuePercent;

		pieChart.setAnimated(true);
		pieChart.setStartAngle(90);
		pieChart.getData().clear();

		if (anzNokUndef > 0) {
			pieceValuePercent = (float) (Math.round(100.0 * anzNokUndef / anzNok * 100) / 100.0);

			dataPieNokUndef = new PieChart.Data(
					"Plausibilitätsfehler:\n" + anzNokUndef + " Stk." + " (" + pieceValuePercent + " %)", anzNokUndef);
			dataPieNokUndef.nodeProperty().addListener(new ChangeListener<Node>() {
				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightcoral; -fx-border-width: 0.0px;");
					}
				}
			});
		}

		if (anzNokUpLim > 0) {
			pieceValuePercent = (float) (Math.round(100.0 * anzNokUpLim / anzNok * 100) / 100.0);

			dataPieNokUpLim = new PieChart.Data(
					"OG verletzt:\n" + anzNokUpLim + " Stk." + " (" + pieceValuePercent + " %)", anzNokUpLim);
			dataPieNokUpLim.nodeProperty().addListener(new ChangeListener<Node>() {

				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightcoral; -fx-border-width: 0.0px;");
					}
				}
			});
		}

		if (anzNokLoLim > 0) {
			pieceValuePercent = (float) (Math.round(100.0 * anzNokLoLim / anzNok * 100) / 100.0);

			dataPieNokLoLim = new PieChart.Data(
					"UG verletzt:\n" + anzNokLoLim + " Stk." + " (" + pieceValuePercent + " %)", anzNokLoLim);

			dataPieNokLoLim.nodeProperty().addListener(new ChangeListener<Node>() {
				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightcoral; -fx-border-width: 0.0px;");
					}
				}
			});
		}

		if (dataPieNokUndef != null && anzNokUndef > 0)

		{
			pieChart.getData().add(dataPieNokUndef);
		}

		if (dataPieNokUpLim != null && anzNokUpLim > 0) {
			pieChart.getData().add(dataPieNokUpLim);
		}

		if (dataPieNokLoLim != null && anzNokLoLim > 0) {
			pieChart.getData().add(dataPieNokLoLim);
		}

	}

	private void initBarChart() {

		int pieceValue;
		float pieceValuePercent;

		barChartXaxis.setAutoRanging(true);
		barChartYaxis.setAutoRanging(true);
		barChartYaxis.setMinorTickVisible(false);

		contMenu.rmiDiagrammBarAll.setSelected(true);

		barChart.setTitle(rootPaneController.getProcess().getStation() + ": "
				+ rootPaneController.getProcess().getName() + " - Balkendiagramm");

		barChart.getData().clear();

		BarChart.Series seriesData = new BarChart.Series();

		seriesData.getData().clear();

		// Balkendiagramm IO Daten erzeugen
		pieceValue = rootPaneController.getResultData().getOk();
		pieceValuePercent = rootPaneController.getResultData().getOkPercent();

		if (pieceValue > 0) {
			dataBarOk = new BarChart.Data(
					"in Ordnung: " + String.valueOf(pieceValue) + " Stk." + " (" + pieceValuePercent + " %)",
					pieceValue);

			dataBarOk.nodeProperty().addListener(new ChangeListener<Node>() {
				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightgreen");
					}
				}
			});

			seriesData.getData().add(dataBarOk);

		}

		// Balkendiagramm NIO Daten erzeugen
		pieceValue = rootPaneController.getResultData().getNok();
		pieceValuePercent = rootPaneController.getResultData().getNokPercent();

		if (pieceValue > 0) {
			dataBarNok = new BarChart.Data(
					"nicht in Ordnung: " + String.valueOf(pieceValue) + " Stk." + " (" + pieceValuePercent + " %)",
					pieceValue);

			dataBarNok.nodeProperty().addListener(new ChangeListener<Node>() {

				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightcoral; -fx-border-width: 2.5px;");
					}
				}
			});

			seriesData.getData().add(dataBarNok);

		}

		// Balkendiagramm Unbewertet Daten erzeugen
		pieceValue = rootPaneController.getResultData().getUnbewertet();
		pieceValuePercent = rootPaneController.getResultData().getUnbewertetPercent();

		if (pieceValue > 0) {
			dataBarUnbewertet = new BarChart.Data(
					"unbewertet: " + String.valueOf(pieceValue) + " Stk." + " (" + pieceValuePercent + " %)",
					pieceValue);

			dataBarUnbewertet.nodeProperty().addListener(new ChangeListener<Node>() {
				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightgrey;");
					}
				}
			});

			seriesData.getData().add(dataBarUnbewertet);

		}

		barChart.getData().add(seriesData);

		if (dataBarNok != null)

		{
			dataBarNok.getNode().addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent e) {

					float anzNok;
					int anzNokOg;
					int anzNokUg;
					float pieceValuePercent;
					int anzNokUndef;
					Tooltip t = new Tooltip();
					StringBuilder sb = new StringBuilder();

					// Anzahl an NIO ermitteln welche die OG überschritten bzw.
					// die UG unterschritten haben
					anzNokOg = getAnzNokOg();
					anzNokUg = getAnzNokUg();
					anzNokUndef = getAnzNokUndef();

					// Gesamtanzahl der NIO unabhängig ob OG oder UG verletzt
					anzNok = anzNokOg + anzNokUg + anzNokUndef;

					if (anzNokUndef > 0) {
						pieceValuePercent = (float) (Math.round(100.0 * anzNokUndef / anzNok * 100) / 100.0);
						sb.append(
								"Plausibilitätsfehler: " + anzNokUndef + " Stk." + " (" + pieceValuePercent + " %)\n");
					}

					if (anzNokOg > 0) {
						pieceValuePercent = (float) (Math.round(100.0 * anzNokOg / anzNok * 100) / 100.0);
						sb.append("OG verletzt: " + anzNokOg + " Stk." + " (" + pieceValuePercent + " %)\n");
					}

					if (anzNokUg > 0) {
						pieceValuePercent = (float) (Math.round(100.0 * anzNokUg / anzNok * 100) / 100.0);
						sb.append("UG verletzt: " + anzNokUg + " Stk." + " (" + pieceValuePercent + " %)");
					}

					t.setText(sb.toString());
					Tooltip.install(dataBarNok.getNode(), t);

				}
			});

			// DrillDown Chart Daten erzeugen/aufrufen
			initDrillDownBarData();
		}

	}

	private void initDrillDownBarData() {

		// Mauszeiger je nach Situation ändern
		dataBarNok.getNode().setOnMouseExited((event) -> {
			dialogStage.getScene().setCursor(Cursor.DEFAULT);
		});

		dataBarNok.getNode().setOnMouseEntered((event) -> {
			dialogStage.getScene().setCursor(Cursor.HAND);
		});

		dataBarNok.getNode().setOnMouseClicked((event) -> {
			if (event.getButton() == MouseButton.PRIMARY && !event.isShortcutDown())
				showDrillDownBarChart();

		});
	}

	private void showDrillDownBarChart() {

		float anzNok;
		int anzNokUpLim;
		int anzNokLoLim;
		int anzNokUndef;

		contMenu.rmiDiagrammBarDrill.setSelected(true);

		// Anzahl an NIO ermitteln welche die OG überschritten bzw. die UG
		// unterschritten haben
		anzNokUpLim = getAnzNokOg();
		anzNokLoLim = getAnzNokUg();
		anzNokUndef = getAnzNokUndef();

		// Gesamtanzahl der NIO unabhängig ob OG oder UG verletzt
		anzNok = anzNokUpLim + anzNokLoLim + anzNokUndef;

		BarChart.Series seriesData = new BarChart.Series();

		float pieceValuePercent;

		barChart.setAnimated(true);
		barChart.getData().clear();
		seriesData.getData().clear();

		if (anzNokUndef > 0) {
			pieceValuePercent = (float) (Math.round(100.0 * anzNokUndef / anzNok * 100) / 100.0);

			dataBarNokUndef = new BarChart.Data(
					"Plausibilitätsfehler: " + anzNokUndef + " Stk." + " (" + pieceValuePercent + " %)", anzNokUndef);
			dataBarNokUndef.nodeProperty().addListener(new ChangeListener<Node>() {
				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightcoral; -fx-border-width: 0.0px;");
					}
				}
			});
		}

		if (anzNokUpLim > 0) {
			pieceValuePercent = (float) (Math.round(100.0 * anzNokUpLim / anzNok * 100) / 100.0);

			dataBarNokUpLim = new BarChart.Data(
					"OG verletzt: " + anzNokUpLim + " Stk." + " (" + pieceValuePercent + " %)", anzNokUpLim);
			dataBarNokUpLim.nodeProperty().addListener(new ChangeListener<Node>() {

				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightcoral; -fx-border-width: 0.0px;");
					}
				}
			});
		}

		if (anzNokLoLim > 0) {
			pieceValuePercent = (float) (Math.round(100.0 * anzNokLoLim / anzNok * 100) / 100.0);

			dataBarNokLoLim = new BarChart.Data(
					"UG verletzt: " + anzNokLoLim + " Stk." + " (" + pieceValuePercent + " %)", anzNokLoLim);
			dataBarNokLoLim.nodeProperty().addListener(new ChangeListener<Node>() {
				@Override
				public void changed(ObservableValue<? extends Node> ov, Node oldNode, Node newNode) {
					if (newNode != null) {
						newNode.setStyle("-fx-background-color: lightcoral; -fx-border-width: 0.0px;");
					}
				}
			});
		}

		if (dataBarNokUndef != null && anzNokUndef > 0)

		{
			seriesData.getData().add(dataBarNokUndef);
		}

		if (dataBarNokLoLim != null && anzNokLoLim > 0) {
			seriesData.getData().add(dataBarNokLoLim);
		}

		if (dataBarNokUpLim != null && anzNokUpLim > 0) {
			seriesData.getData().add(dataBarNokUpLim);
		}

		barChart.getData().add(seriesData);

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
				contMenu.mDataBarChart.setDisable(false);
				contMenu.mDataPieChart.setDisable(false);
				barChart.setAnimated(true);
				pieChart.setAnimated(true);
				initPieAndBarChart();

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

		contMenu.rmiDiagrammStandard.setText("Standard");
		contMenu.rmiDiagrammStandard.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				contMenu.mDataBarChart.setDisable(false);
				contMenu.mDataPieChart.setDisable(false);
				showPieAndBarChart();

			}
		});

		contMenu.rmiDiagrammPie.setText("Tortendiagramm");
		contMenu.rmiDiagrammPie.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				contMenu.mDataBarChart.setDisable(true);
				contMenu.mDataPieChart.setDisable(false);
				showPieChart();
			}
		});

		contMenu.rmiDiagrammBar.setText("Balkendiagramm");
		contMenu.rmiDiagrammBar.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				contMenu.mDataBarChart.setDisable(false);
				contMenu.mDataPieChart.setDisable(true);
				showBarChart();
			}
		});

		contMenu.rmiDiagrammPieAll.setText("Gesamt (iO, niO, unbewertet)");
		contMenu.rmiDiagrammPieAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				initPieChart();
			}
		});

		contMenu.rmiDiagrammPieDrill.setText("Drill-Down (nur niO)");
		contMenu.rmiDiagrammPieDrill.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showDrillDownPieChart();
			}
		});

		contMenu.mDataPieChart.setText("Daten Tortendiagramm");
		contMenu.mDataPieChart.getItems().addAll(contMenu.rmiDiagrammPieAll, contMenu.rmiDiagrammPieDrill);

		contMenu.rmiDiagrammBarAll.setText("Gesamt (iO, niO, unbewertet)");
		contMenu.rmiDiagrammBarAll.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				initBarChart();
			}
		});

		contMenu.rmiDiagrammBarDrill.setText("Drill-Down (nur niO)");
		contMenu.rmiDiagrammBarDrill.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				showDrillDownBarChart();
			}
		});

		contMenu.mDataBarChart.setText("Daten Balkendiagramm");
		contMenu.mDataBarChart.getItems().addAll(contMenu.rmiDiagrammBarAll, contMenu.rmiDiagrammBarDrill);

		contMenu.mView.setText("Ansicht");
		contMenu.mView.getItems().addAll(contMenu.rmiDiagrammStandard, contMenu.rmiDiagrammPie,
				contMenu.rmiDiagrammBar);

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

		// Add MenuItem to ContextMenu
		contMenu.contextMenu.getItems().addAll(contMenu.miResetActView, contMenu.miRefresh, new SeparatorMenuItem(),
				contMenu.mView, contMenu.miFilter, contMenu.miFilterReset, contMenu.mDataPieChart,
				contMenu.mDataBarChart, new SeparatorMenuItem(), contMenu.miSave);
	}

	private void showPieChart() {
		pieChart.setVisible(true);
		barChart.setVisible(false);
		hboxCharts.getChildren().clear();
		hboxCharts.getChildren().addAll(pieChart);

	}

	private void showBarChart() {
		pieChart.setVisible(false);
		barChart.setVisible(true);
		hboxCharts.getChildren().clear();
		hboxCharts.getChildren().addAll(barChart);

	}

	private void showPieAndBarChart() {
		contMenu.rmiDiagrammStandard.setSelected(true);
		pieChart.setVisible(true);
		barChart.setVisible(true);
		hboxCharts.getChildren().clear();
		hboxCharts.getChildren().addAll(pieChart, barChart);

	}

	private void initPieAndBarChart() {
		contMenu.mDataBarChart.setDisable(false);
		contMenu.mDataPieChart.setDisable(false);
		initBarChart();
		initPieChart();
		showPieAndBarChart();

	}

	private int getAnzNokUg() {

		int anz = 0;
		for (Result result : rootPaneController.getResults()) {
			if ((result.getState() == 1) && (result.getValue() < result.getLoLim())) {
				anz++;
			}
		}
		return anz;
	}

	private int getAnzNokOg() {

		int anz = 0;
		for (Result result : rootPaneController.getResults()) {
			if ((result.getState() == 1) && (result.getValue() > result.getUpLim())) {
				anz++;
			}
		}

		return anz;

	}

	private int getAnzNokUndef() {

		int anz = 0;
		for (Result result : rootPaneController.getResults()) {
			if ((result.getState() == 1) && (result.getValue() <= result.getUpLim())
					&& (result.getValue() >= result.getLoLim())
					|| (result.getState() == 2)
							&& ((result.getValue() > result.getUpLim()) || (result.getValue() < result.getLoLim()))) {
				anz++;
			}
		}

		return anz;

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

		WritableImage image = hboxCharts.snapshot(new SnapshotParameters(), null);

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

	public void setData() {
		pieChart.setAnimated(true);
		barChart.setAnimated(true);
		initPieAndBarChart();

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

		Menu mView = new Menu();
		Menu mDataPieChart = new Menu();
		Menu mDataBarChart = new Menu();

		RadioMenuItem rmiDiagrammStandard = new RadioMenuItem();
		RadioMenuItem rmiDiagrammPie = new RadioMenuItem();
		RadioMenuItem rmiDiagrammPieAll = new RadioMenuItem();
		RadioMenuItem rmiDiagrammPieDrill = new RadioMenuItem();
		RadioMenuItem rmiDiagrammBar = new RadioMenuItem();
		RadioMenuItem rmiDiagrammBarAll = new RadioMenuItem();
		RadioMenuItem rmiDiagrammBarDrill = new RadioMenuItem();

		ToggleGroup groupView = new ToggleGroup();
		ToggleGroup groupBar = new ToggleGroup();;
		ToggleGroup groupPie = new ToggleGroup();

		private ContMenu() {
			init();
		}

		private void init() {

			rmiDiagrammStandard.setToggleGroup(groupView);
			rmiDiagrammPie.setToggleGroup(groupView);
			rmiDiagrammBar.setToggleGroup(groupView);

			rmiDiagrammBarAll.setToggleGroup(groupBar);
			rmiDiagrammBarDrill.setToggleGroup(groupBar);

			rmiDiagrammPieAll.setToggleGroup(groupPie);
			rmiDiagrammPieDrill.setToggleGroup(groupPie);

			rmiDiagrammStandard.setSelected(true);
			rmiDiagrammBarAll.setSelected(true);
			rmiDiagrammPieAll.setSelected(true);
		}

	}

}
