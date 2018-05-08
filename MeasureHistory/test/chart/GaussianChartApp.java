package chart;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * 
 * A chart that fills in the area between a line of data points and the axes.
 * 
 * Good for comparing accumulated totals over time.
 * 
 */

public class GaussianChartApp extends Application {

	private AreaChart chart;
	private NumberAxis xAxis;
	private NumberAxis yAxis;

	private float avg = 10;
	private float stdAbweichungSigma = 5;
	private float range = 20;

	private float k = 0;

	public Parent createContent() {

		xAxis = new NumberAxis();
		xAxis.setLabel("Messwert");
		xAxis.setForceZeroInRange(true);
		xAxis.setUpperBound(40.0);
		xAxis.setLowerBound(-40.0);
		xAxis.setTickUnit(1.0);
		xAxis.setAutoRanging(false);
		yAxis = new NumberAxis();
		yAxis.setLabel("Stueckzahl");

		chart = new AreaChart(xAxis, yAxis);
		chart.getData().add(generateApache());
		chart.getData().add(generateGauss1());
		// chart.getData().add(generateThaler());

		return chart;

	}

	private XYChart.Series generateApache() {

		XYChart.Series series = new XYChart.Series<>();
		series.setName("Apache");

		org.apache.commons.math3.analysis.function.Gaussian g = new org.apache.commons.math3.analysis.function.Gaussian(
				0.0, stdAbweichungSigma);

		for (int i = -10; i <= avg * 4; i++) {

			series.getData().add(new XYChart.Data<>(i, g.value(i)));
		}

		return series;

	}

	private XYChart.Series generateThaler() {

		List<Integer> xData = new ArrayList<Integer>();
		List<Double> yData = new ArrayList<Double>();

		XYChart.Series seriesGauss2 = new XYChart.Series<>();
		seriesGauss2.setName("Thaler");
		GaussianByThaler gaus2 = new GaussianByThaler(avg, stdAbweichungSigma);
		// Gaussian2 gaus2 = new Gaussian2(avg, stdAbweichungSigma);

		for (int i = 0; i <= avg * 2; i++) {
			xData.add(i);

		}

		for (int i = 0; i < xData.size(); i++) {

			seriesGauss2.getData().add(new XYChart.Data<>(xData.get(i), gaus2.getY(i)));

		}
		// int i = 0;
		// k = 0;
		// while (k < 400) {
		//
		// System.out.println("k: " + k);
		//
		// seriesGauss2.getData().add(new XYChart.Data<>(k, gaus2.getY(k)));
		// k = (float) (k + 0.1);
		// i++;
		//
		// }
		return seriesGauss2;

	}

	private XYChart.Series generateGauss1() {

		Gaussian gaus = new Gaussian(avg, stdAbweichungSigma);
		XYChart.Series seriesGauss1 = new XYChart.Series<>();
		seriesGauss1.setName("Gauss");
		seriesGauss1 = gaus.getSeries();
		return seriesGauss1;

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