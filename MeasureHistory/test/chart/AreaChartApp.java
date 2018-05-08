package chart;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.stage.Stage;

/**
 * 
 * A chart that fills in the area between a line of data points and the axes.
 * 
 * Good for comparing accumulated totals over time.
 * 
 */

public class AreaChartApp extends Application {

	private AreaChart chart;

	private NumberAxis xAxis;

	private NumberAxis yAxis;

	public Parent createContent() {

		xAxis = new NumberAxis();
		xAxis.setLabel("X Values");
		yAxis = new NumberAxis();
		yAxis.setLabel("Y Values");
		yAxis.setLowerBound(1);
		yAxis.setAutoRanging(false);

		int[] stueck = new int[11];
		int j = 15;
		for (int i = 0; i < stueck.length; i++) {

			if (i <= (stueck.length) / 2)
				j = j + 1;
			else
				j = j - 1;

			stueck[i] = j;

		}
		System.out.println(stueck);

		XYChart.Series series = generateBars(2, 4, stueck.length, stueck);

		chart = new AreaChart(xAxis, yAxis);
		chart.getData().addAll(series);

		return chart;

	}

	private XYChart.Series generateBars(float xMin, float xMax, int anzahBalken, int[] stueck) {

		int deltaX = 2;
		float lastXMax = 0;
		float range = Math.abs(xMax - xMin);

		float bandWidth = range / anzahBalken;
		System.out.println(bandWidth);

		XYChart.Series series = new XYChart.Series<>();
		XYChart.Data lastData = null;

		if (anzahBalken > 0) {

			for (int i = 0; i < anzahBalken; i++) {
				System.out.println("i: " + i);

				for (int j = 0; j < 4; j++) {
					if (i == 0) {
						if (j == 0)
							series.getData().add(new XYChart.Data<>(xMin, 1.0));
						if (j == 1)
							series.getData().add(new XYChart.Data<>(xMin, stueck[i]));
						if (j == 2)
							series.getData().add(new XYChart.Data<>(xMin + bandWidth, stueck[i]));
						if (j == 3)
							series.getData().add(new XYChart.Data<>(xMin + bandWidth, 1.0));

						lastData = (Data) series.getData().get(series.getData().size() - 1);

					} else {
						lastXMax = Float.parseFloat(lastData.getXValue().toString());
						if (j == 0)
							series.getData().add(new XYChart.Data<>(lastXMax, 0));
						if (j == 1)
							series.getData().add(new XYChart.Data<>(lastXMax, stueck[i]));
						if (j == 2)
							series.getData().add(new XYChart.Data<>(lastXMax + bandWidth, stueck[i]));
						if (j == 3) {
							series.getData().add(new XYChart.Data<>(lastXMax + bandWidth, 0));
							lastData = (Data) series.getData().get(series.getData().size() - 1);

						}
					}

				}

			}

		}
		return series;

	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		primaryStage.setScene(new Scene(createContent()));

		primaryStage.show();

	}

	/**
	 * 
	 * Java main for when running without JavaFX launcher
	 * 
	 */

	public static void main(String[] args) {

		launch(args);

	}

}