package com.gretha.shared.util;

/**
 * Runden eines Float Wertes unter Angabe der Dezimalstellen und Formatierung in
 * den gewuenschten Datentyp.
 *
 */
public class DecimalPointFormatter {

	/**
	 * Rundet einen Float Wert und gibt diesen als String zurueck.
	 * 
	 * @author Markus Thaler, Ing.
	 * 
	 * @param value
	 * @param decPoints
	 * @return value
	 */
	public static String roundFloat2String(float value, int decPoints) {

		float rndValue = 0;
		String rndText;

		try {
			if (decPoints < 0 || decPoints > 4)
				throw new DecimalPointFormatException();
		} catch (DecimalPointFormatException e1) {
			e1.printStackTrace();
		}

		if (decPoints == 0)
			rndValue = Math.round(value * 1) / 1;

		if (decPoints == 1)
			rndValue = Math.round(value * 10.0f) / 10.0f;

		if (decPoints == 2)
			rndValue = Math.round(value * 100.0f) / 100.0f;

		if (decPoints == 3)
			rndValue = Math.round(value * 1000.0f) / 1000.0f;

		if (decPoints == 4)
			rndValue = Math.round(value * 10000.0f) / 10000.0f;

		rndText = String.format("%." + decPoints + "f", rndValue);

		return rndText.replace(",", ".");
	}

	/**
	 * Rundet einen Float Wert und gibt diesen zurueck.
	 * 
	 * @author Markus Thaler, Ing.
	 * 
	 * @param value
	 * @param decPoints
	 * @return value
	 */
	public static float roundFloat2Float(float value, int decPoints) {

		float rndValue = 0;

		try {
			if (decPoints < 0 || decPoints > 4)
				throw new DecimalPointFormatException();
		} catch (DecimalPointFormatException e1) {
			e1.printStackTrace();
		}

		if (decPoints == 0)
			rndValue = Math.round(value * 1) / 1;

		if (decPoints == 1)
			rndValue = Math.round(value * 10.0f) / 10.0f;

		if (decPoints == 2)
			rndValue = Math.round(value * 100.0f) / 100.0f;

		if (decPoints == 3)
			rndValue = Math.round(value * 1000.0f) / 1000.0f;

		if (decPoints == 4)
			rndValue = Math.round(value * 10000.0f) / 10000.0f;

		return rndValue;
	}

}

class DecimalPointFormatException extends Exception {

	private static final long serialVersionUID = 1L;

	DecimalPointFormatException() {
		super("unerlaubte Anzahl an Dezimalstellen als Parameter uebergeben");
	}
}
