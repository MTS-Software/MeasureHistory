package chart;

public class Gaussian2 {

	protected double stdDeviation, variance, mean;

	public Gaussian2(double mean, double stdDeviation) {

		this.stdDeviation = stdDeviation;
		variance = stdDeviation * stdDeviation;
		this.mean = mean;

	}

	public double getY(double x) {

		return Math.pow(Math.exp(-(((x - mean) * (x - mean)) / ((2 * variance)))),
				1 / (stdDeviation * Math.sqrt(2 * Math.PI)));

	}

}