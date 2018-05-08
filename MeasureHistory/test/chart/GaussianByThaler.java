package chart;

/**
 * @author Markus Thaler
 *         <p>
 *         Berechnung von Gauss nach Thaler'schen Formel
 */
public class GaussianByThaler {

	// Beispiel Implementierung
	// GaussianByThaler gaussian;
	// XYChart.Series series2 = new XYChart.Series<>();
	// for (int i = -50; i <= 50; i++) {
	// gaussian = new GaussianByThaler(0, 5);
	// series2.getData().add(new XYChart.Data<>(i, gaus2.getY(i) * 1000));
	//
	// }
	// chart.getData().add(series2);

	protected double standardAbw, varianz, mittel;

	public GaussianByThaler(double mittel, double standardAbw) {

		this.standardAbw = standardAbw;
		varianz = standardAbw * standardAbw;
		this.mittel = mittel;

	}

	// Thaler'sche Formel
	public double getY(double x) {

		return Math.pow(Math.exp(-(((x - mittel) * (x - mittel)) / ((2 * varianz)))),
				1 / (standardAbw * Math.sqrt(2 * Math.PI)));

	}

}