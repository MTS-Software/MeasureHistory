package math;

import com.gretha.shared.util.DecimalPointFormatter;

public class MathRound {

	public static void main(String[] args) {

		// float v1 = (float) 1.03;
		// int i = 5;
		// Math.round(v1 * i);
		// System.out.println(Math.round(v1 * i * 100) / 100.0);

		// new MathRound();
		System.out.println(DecimalPointFormatter.roundFloat2String(1.127f, 4));
		;
	}

	public MathRound() {

		float v = (float) 10.1237;
		float v2 = (float) -10.1237;
		int points = 4;

		System.out.println(Math.ceil(v * 1000.0));
		System.out.println(Math.floor(v2 * 1000));

		formatDecPoints(v, points);
		System.out.println("\n");
		System.out.println(DecimalPointFormatter.roundFloat2String(v, points));
		;

	}

	private String formatDecPoints(float value, int decPoints) {

		System.out.println("Normal: " + value);
		float rndValue = 0;

		if (decPoints == 0) {
			rndValue = (float) (Math.round(value * 1) / 1);
		}
		if (decPoints == 1) {
			rndValue = (float) (Math.round(value * 10.0) / 10.0);
		}
		if (decPoints == 2) {
			rndValue = (float) (Math.round(value * 100.0) / 100.0);
		}
		if (decPoints == 3) {
			rndValue = (float) (Math.round(value * 1000.0) / 1000.0);
		}
		if (decPoints == 4) {
			rndValue = (float) (Math.round(value * 10000.0) / 10000.0);
		}

		System.out.println("Gerundet: " + rndValue);

		String v = String.format("%." + decPoints + "f", value);

		System.out.println("Text: " + v);

		return v.replace(",", ".");

	}

}
