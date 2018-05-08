package chart;

import com.gretha.shared.util.jfxutils.chart.ChartPanManager;
import com.gretha.shared.util.jfxutils.chart.JFXChartUtil;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MultipleChartZoom extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	private ObservableList<NumberAxis> xAxleList;
	private ObservableList<NumberAxis> yAxleList;
	private ObservableList<LineChart<Number, Number>> chartList;
	private ObservableList<XYChart.Series<Number, Number>> seriesList;

	@Override
	public void start(Stage stage) throws Exception {

		xAxleList = FXCollections.observableArrayList();
		yAxleList = FXCollections.observableArrayList();
		chartList = FXCollections.observableArrayList();
		seriesList = FXCollections.observableArrayList();

		VBox vbox = new VBox();

		for (int i = 0; i < 2; i++) {

			xAxleList.add(new NumberAxis());
			yAxleList.add(new NumberAxis());
			chartList.add(new LineChart(xAxleList.get(i), yAxleList.get(i)));
			seriesList.add(new XYChart.Series<>());

			for (int j = 0; j < 200; j++) {
				if (i == 0)
					seriesList.get(i).getData().add(new XYChart.Data<>(j, j));
				if (i == 1)
					seriesList.get(i).getData().add(new XYChart.Data<>(j, -j));
			}

			chartList.get(i).getData().add(seriesList.get(i));

			yAxleList.get(i).setLabel("Series " + i);

			chartList.get(i).setLegendVisible(false);
			chartList.get(i).setCreateSymbols(false);
			chartList.get(i).setVerticalZeroLineVisible(false);
			chartList.get(i).setHorizontalZeroLineVisible(false);
			chartList.get(i).setAnimated(false);
			chartList.get(i).getYAxis().setPrefWidth(60);
			chartList.get(i).getYAxis().setMaxWidth(60);

			if (i > 0) {
				xAxleList.get(i).lowerBoundProperty()
						.bindBidirectional(((NumberAxis) chartList.get(0).getXAxis()).lowerBoundProperty());

				xAxleList.get(i).upperBoundProperty()
						.bindBidirectional(((NumberAxis) chartList.get(0).getXAxis()).upperBoundProperty());

				xAxleList.get(i).tickUnitProperty()
						.bindBidirectional(((NumberAxis) chartList.get(0).getXAxis()).tickUnitProperty());
			}

			vbox.getChildren().add(chartList.get(i));
			initZooming(chartList.get(i));

		}

		stage.setTitle("MultipleChartTest");

		BorderPane borderPane = new BorderPane();

		borderPane.setCenter(vbox);

		Scene scene = new Scene(borderPane, 1024, 600);
		stage.setScene(scene);

		stage.show();
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
}
