package math;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

public class Mittelwert {

	private List<Float> results = new ArrayList<>();

	public static void main(String[] args) {
		new Mittelwert();

	}

	public Mittelwert() {

		// generateTestValues();
		// calculateAvg();

		float a = 1;
		float b = 2;

		float j = Math.round(1 / 2);
		System.out.println("anzahlAvg:" + j);

	}

	private void calculateAvg() {

		int anzahlAvg = 10;
		int lastSavedAvgIndex = 0;
		float sum = 0;

		List<Float> resultsforAvg = new ArrayList<>();
		List<Float> avgList = new ArrayList<>();

		for (int i = 0; i < results.size(); i++) {

			sum = 0;

			resultsforAvg.add(results.get(i));
			System.out.println(resultsforAvg);

			if (resultsforAvg.size() == anzahlAvg) {
				System.out.println("10 Werte erreicht");

				for (int j = 0; j < anzahlAvg; j++) {

					float wert = resultsforAvg.get(j);
					sum = sum + wert;

				}
				System.out.println(sum);
				System.out.println(sum / anzahlAvg);
				avgList.add(new Float(sum / anzahlAvg));
				resultsforAvg.clear();
			}

			if ((i % anzahlAvg) == 0 && i > 0) {
				System.out.println("Mittelwerte berechnen!");
				lastSavedAvgIndex = i;
				System.out.println("letzer gespeicherter Mittelwert: " + lastSavedAvgIndex);
			}

		}
		System.out.println("Mittelwerte: " + avgList);
		System.out.println(avgList.size());
	}

	private void generateTestValues() {

		String ret = JOptionPane.showInputDialog(null);
		int anz = Integer.parseInt(ret);

		for (int i = 1; i <= anz; i++) {

			results.add(new Float(i));

		}

		System.out.println(results);

	}

}
