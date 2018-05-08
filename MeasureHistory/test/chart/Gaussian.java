package chart;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;

/**
 * @author Markus Thaler
 *
 */
public class Gaussian {

	private XYChart.Series series = new XYChart.Series<>();

	private List<Integer> xData = new ArrayList<Integer>();
	private List<Double> yData = new ArrayList<Double>();

	/**
	 * Erzeugt eine Gaussian Kurve
	 * <p>
	 * 
	 * @param mean
	 *            Mean Value
	 * 
	 * @param std
	 *            Standard Deviation
	 */
	public Gaussian(float mean, float std) {

		// 2x mean damit die Kurve komplett angezeigt wird, kann aber noch
		// angepasst werden
		for (int i = 0; i < mean * 2; i++) {
			xData.add(i);
		}

		yData = getYAxis(xData, mean, std);

		for (int j = 0; j < xData.size(); j++) {
			series.getData().add(new AreaChart.Data<>(xData.get(j), yData.get(j)));
		}

	}

	private List<Double> getYAxis(List<Integer> xData, double mean, double std) {

		List<Double> yData = new ArrayList<Double>(xData.size());

		for (int i = 0; i < xData.size(); i++) {
			yData.add((1 / (std * Math.sqrt(2 * Math.PI)))
					* Math.exp(-(((xData.get(i) - mean) * (xData.get(i) - mean)) / ((2 * std * std)))));
		}
		return yData;
	}

	public XYChart.Series getSeries() {
		return series;
	}

}
