package math;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.math.BigDecimal;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class GleitpunktKorrektur extends JFrame {

	private static final long serialVersionUID = 1L;

	private JList<String> list;
	private DefaultListModel<String> listModel;

	public static void main(String[] args) {
		new GleitpunktKorrektur();

	}

	private void initUI() {

		setSize(new Dimension(600, 400));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		listModel = new DefaultListModel<>();
		list = new JList<>(listModel);
		getContentPane().setLayout(new BorderLayout());
		JScrollPane sp = new JScrollPane(list);

		getContentPane().add(sp, null);
		setVisible(false);
	}

	public GleitpunktKorrektur() {

		float f1 = 1.1f;
		float f2 = 1.354f;

		double d1 = 5.1242d;
		double d2 = 1.3d;

		int anzKlassen = 10;

		initUI();

		// testFloat(f1, f2);
		// testFloatAndBigDecimal();
		// testDouble(new Double(f1), new Double(f2));
		// testDouble(d1, d2);
		// testBigDecimal(f1, f2);

		System.out.println("Fraktion Min: " + fraction(f1));
		System.out.println("Fraktion Max: " + fraction(f2));
		generateKlassenFloat(1.1f, 1.354f, 51);

		// generateKlassenDouble(1.1d, 1.354d, 52);

		// generateKlassenFloat(0.08f, 0.17f, anzKlassen);
		// generateKlassenFloat(51.0f, 549.0f, 100);

	}

	private int getDecimalPoints(float f1) {

		int decPoints = (String.valueOf(f1).length() - String.valueOf(f1).indexOf('.') - 1);

		return decPoints;

	}

	private int getDecimalPointsDouble(double d1) {

		int decPoints = (String.valueOf(d1).length() - String.valueOf(d1).indexOf('.') - 1);

		return decPoints;

	}

	private double fraction(float x) {
		if (x > 0.0) {
			return x - Math.floor(x);
		} else {
			return x - Math.ceil(x);
		}
	}

	private void generateKlassenFloat(float xMin, float xMax, int anzKlassen) {

		// Die erste Ungenauigkeit entsteht beim Subtrahieren, was auch
		// verständlich ist (Möglichkeit runden oder mit BigDecimal)
		// Des weiteren wirkt sich diese nur beim Addieren von den Bandbreiten
		// aus, aber nicht beim Multiplizieren = (bessere Variante)

		// Kahan summation algorithm evtl. genauer ansehen

		float min = 0;
		float max = 0;

		float lastMax = 0;
		float lastMin = 0;

		float range = Math.abs(xMax - xMin);

		BigDecimal t = new BigDecimal("1.1").subtract(new BigDecimal("1.354"));
		System.out.println("Range von BigDecimal: " + t);
		System.out.println("Range Float von BigDecimal: " + t.floatValue());

		float bandWidth = range / anzKlassen;
		System.out.println("Range: " + range);
		System.out.println(new BigDecimal(range).precision());
		System.out.println("Bandbreite: " + bandWidth);
		System.out.println(new BigDecimal(bandWidth).precision());
		System.out.println();
		listModel.addElement("Range: " + range);
		listModel.addElement("Bandbreite: " + bandWidth);

		// Maximale Dezimalstelle berechnen
		int decMin = getDecimalPoints(xMin);
		int decMax = getDecimalPoints(xMax);
		int dec;

		if (decMin > decMax)
			dec = decMin;
		else
			dec = decMax;

		System.out.println("Bandwidth Maximalkommaanzahl: " + getDecimalPoints(bandWidth));

		System.out.println("Maximalkommaanzahl: " + dec);

		for (int i = 0; i < anzKlassen; i++) {

			if (i == 0) {
				min = xMin;
				max = min + bandWidth;
			}

			if (i > 0 && i < anzKlassen - 1) {
				min = lastMax;
				max = lastMax + bandWidth;
			}
			if (anzKlassen > 1)
				if (i == (anzKlassen - 1)) {
					min = lastMax;
					max = lastMax + bandWidth;
				}

			System.out.println("=== Klasse: " + (i + 1) + " ===");
			System.out.println("Min: " + min);
			System.out.println("Max: " + max);
			listModel.addElement("=== Klasse: " + (i + 1) + " ===");

			listModel.addElement("Min: " + min);
			listModel.addElement("Max: " + max);

			// lastMax = max;
			// lastMin = min;
			// Geanuigkeit pruefen
			System.out.println("Genauigkeit min: " + new BigDecimal(min).precision());

			lastMin = Math.round(min * 100000) / 100000.0f;
			lastMax = Math.round(max * 100000) / 100000.0f;

			System.out.println("lastMin: " + lastMin);
			System.out.println("lastMin gerundet: " + lastMin);

			System.out.println("lastMax: " + lastMax);
			System.out.println("lastMax gerundet: " + lastMax);

			System.out.println("=== Klasse: " + (i + 1) + " ===");
			System.out.println();
			listModel.addElement("lastMin: " + lastMin);
			listModel.addElement("lastMax: " + lastMax);
			listModel.addElement("=== Klasse: " + (i + 1) + " ===");
			listModel.addElement(" ");

		}

		if (max == xMax) {
			System.out.println("Letzes Ergebnis stimmt exakt.");
		} else
			System.out.println("Letzes Ergebnis: " + max + "; Maximalwert: " + xMax);

		float lastX;
		// Berechnung mit Addition
		lastX = xMin;
		for (int i = 0; i < anzKlassen; i++) {
			lastX += bandWidth;

		}
		System.out.println("Berechnung mit Addition: " + lastX);

		// Berechnung mit Multiplikation
		lastX = xMin + (bandWidth * anzKlassen);
		System.out.println("Berechnung mit Multiplikation: " + lastX);
		// if (Math.round(lastMin * 100) / 100.0f == xMax)
		// System.out.println("Letzes Ergebnis stimmt exakt");

	}

	private void generateKlassenDouble(double xMin, double xMax, int anzKlassen) {

		double min = 0;
		double max = 0;

		double lastMax = 0;
		double lastMin = 0;

		double range = Math.abs(xMax - xMin);
		double bandWidth = range / anzKlassen;

		System.out.println("Range: " + range);
		System.out.println(new BigDecimal(range).precision());
		System.out.println("Bandbreite: " + bandWidth);
		System.out.println(new BigDecimal(bandWidth).precision());
		System.out.println();
		listModel.addElement("Range: " + range);
		listModel.addElement("Bandbreite: " + bandWidth);

		// Maximale Dezimalstelle berechnen
		int decMin = getDecimalPointsDouble(xMin);
		int decMax = getDecimalPointsDouble(xMax);
		int dec;

		if (decMin > decMax)
			dec = decMin;
		else
			dec = decMax;

		System.out.println("Bandwidth Maximalkommaanzahl: " + getDecimalPointsDouble(bandWidth));

		System.out.println("Maximalkommaanzahl: " + dec);

		for (int i = 0; i < anzKlassen; i++) {

			if (i == 0) {
				min = xMin;
				max = min + bandWidth;
			}

			if (i > 0 && i < anzKlassen - 1) {
				min = lastMax;
				max = lastMax + bandWidth;
			}
			if (anzKlassen > 1)
				if (i == (anzKlassen - 1)) {
					min = lastMax;
					max = lastMax + bandWidth;
				}

			System.out.println("=== Klasse: " + (i + 1) + " ===");
			System.out.println("Min: " + min);
			System.out.println("Max: " + max);
			listModel.addElement("=== Klasse: " + (i + 1) + " ===");

			listModel.addElement("Min: " + min);
			listModel.addElement("Max: " + max);

			// lastMax = max;
			// lastMin = min;
			// Geanuigkeit pruefen
			System.out.println(new BigDecimal(lastMin).precision());

			lastMin = Math.round(min * 100000) / 100000.0f;
			lastMax = Math.round(max * 100000) / 100000.0f;

			System.out.println("lastMin: " + lastMin);
			System.out.println("lastMin gerundet: " + lastMin);

			System.out.println("lastMax: " + lastMax);
			System.out.println("lastMax gerundet: " + lastMax);

			System.out.println("=== Klasse: " + (i + 1) + " ===");
			System.out.println();
			listModel.addElement("lastMin: " + lastMin);
			listModel.addElement("lastMax: " + lastMax);
			listModel.addElement("=== Klasse: " + (i + 1) + " ===");
			listModel.addElement(" ");

		}

		if (lastMax == xMax) {
			System.out.println("Letzes Ergebnis stimmt exakt.");
		}
		// if (Math.round(lastMin * 100) / 100.0f == xMax)
		// System.out.println("Letzes Ergebnis stimmt exakt");

	}

	private void testFloatAndBigDecimal() {

		System.out.println("--- Normal -----");
		System.out.println(2.00 - 1.1);
		System.out.println(2.00 - 1.2);
		System.out.println(2.00 - 1.3);
		System.out.println(2.00 - 1.4);
		System.out.println(2.00 - 1.5);
		System.out.println(2.00 - 1.6);
		System.out.println(2.00 - 1.7);
		System.out.println("--- BigDecimal-----");
		System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.1")));
		System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.2")));
		System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.3")));
		System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.4")));
		System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.5")));
		System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.6")));
		System.out.println(new BigDecimal("2.00").subtract(new BigDecimal("1.7")));
		BigDecimal bd = new BigDecimal("1234.34567");
		bd = bd.setScale(3, BigDecimal.ROUND_CEILING);
		System.out.println(bd);
	}

	private void testFloat(float v1, float v2) {

		float res;
		res = v1 + v2;
		System.out.println("=== Float ===");
		System.out.println("normal: " + res);
		System.out.println("gerundet: " + Math.round(res * 1000) / 1000.0);
		System.out.println("=== Float ===\n");

		BigDecimal big = new BigDecimal(res);
		System.out.println(big.unscaledValue());
	}

	private void testDouble(double v1, double v2) {
		double res;
		res = v1 + v2;
		System.out.println("=== Double ===");
		System.out.println("normal: " + res);
		System.out.println("gerundet: " + Math.round(res * 1000) / 1000.0);
		System.out.println("=== Double ===\n");
	}

	private void testBigDecimal(float v1, float v2) {

		BigDecimal res;

		double d = new BigDecimal(String.valueOf(v1 + v2)).doubleValue();

		System.out.println("=== BigDecimal ===");
		System.out.println("normal: " + d);
		System.out.println("gerundet: " + Math.round(d * 1000) / 1000.0);
		System.out.println("=== BigDecimal ===\n");
	}

}
