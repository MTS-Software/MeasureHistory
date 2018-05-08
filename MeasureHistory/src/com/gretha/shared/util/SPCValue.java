package com.gretha.shared.util;

import java.util.List;

import com.gretha.shared.model.Result;

/**
 * Liefert SPC Werte unter Vorgabe der nötigen Eckdaten
 * 
 */
public class SPCValue {

	/**
	 * Liefert die Standardabweichung Sigma zurück
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @param xQuer
	 * @param messwerte
	 * @return sigma
	 */
	public static float getSigma(float xQuer, List<Result> messwerte) {
		float sigma = 0;
		float varianz = 0;
		float help = 0;

		if (messwerte.size() > 1) {
			for (int i = 0; i < messwerte.size(); i++) {
				help = help + (float) Math.pow((messwerte.get(i).getValue() - xQuer), 2);
			}
			varianz = help / (messwerte.size() - 1);
			sigma = (float) Math.sqrt(varianz);
		}
		return sigma;
	}

	/**
	 * Liefert den Cp-Wert zurück
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @param usg
	 * @param osg
	 * @param sigma
	 * @return cp
	 */
	public static float getCp(float usg, float osg, float sigma) {
		float cp = 0;
		if (sigma != 0) {
			cp = (osg - usg) / (6 * sigma);
		}
		return cp;
	}

	/**
	 * Liefert den Cpk-Wert zurück
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @param xQuer
	 * @param usg
	 * @param osg
	 * @param sigma
	 * @return cpk
	 */
	public static float getCpk(float xQuer, float usg, float osg, float sigma) {
		float cpk = 0;
		float value1 = xQuer - usg;
		float value2 = osg - xQuer;

		if (sigma != 0) {
			if (value1 < value2) {
				cpk = value1 / (3 * sigma);
			} else if (value2 < value1) {
				cpk = value2 / (3 * sigma);
			} else {
				cpk = value1 / (3 * sigma);
			}
		}
		return cpk;
	}

	/**
	 * Liefert die kritische Grenze KSG zurück
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @param xQuer
	 * @param usg
	 * @param osg
	 * @return ksg
	 */
	public static float getKSG(float xQuer, float usg, float osg) {
		float ksg = 0;
		float value1 = xQuer - usg;
		float value2 = osg - xQuer;

		if (value1 < value2) {
			ksg = usg;
		}
		if (value2 < value1) {
			ksg = osg;
		}
		return ksg;
	}

}
