package general;

import java.io.File;
import java.util.ResourceBundle;

import com.gretha.shared.model.Result;
import com.gretha.shared.util.ApplicationProperties;

public class TestDataGenerator {

	private ResourceBundle resources = ResourceBundle.getBundle("language");

	public static void main(String[] args) {
		new TestDataGenerator();

	}

	public TestDataGenerator() {

		int cntNIO = 0;
		int cntIO = 0;
		int cntUnbewertet = 0;

		ApplicationProperties.configure("application.properties",
				"c:" + File.separator + "Daten" + File.separator + resources.getString("appname"),
				"application.properties");
		ApplicationProperties.getInstance().setup();

		Result result;

		for (int i = 1; i <= 1025; i++) {

			// Aktuellen Zählerstand ausgeben
			System.out.println("Zählerstand: " + i);

			// Zufallszahl erzeugen
			double randomValue = (Math.random() * 0.1) + 0.035;
			System.out.println("Zufallszahl: " + randomValue);

			// Zufallszahl auf x Dezimalstellen runden
			double roundValue = Math.round(randomValue * 1000) / 1000.0;
			System.out.println("Gerundet: " + roundValue);

			result = new Result();
			result.setLoLim(new Float(0.04));
			result.setUpLim(new Float(0.15));
			result.setProcessId(22);
			result.setTypId(1);
			result.setRemark("Produktion");
			result.setWt("WTxxx");
			result.setSerial(String.valueOf(i));
			System.out.println("Seriennummer: " + i);

			// Status IO/NIO ahnand von Istwert eruieren
			if (roundValue >= 0.0700 && roundValue <= 0.0705) {
				result.setState(0);
				System.out.println("Status: UNBEWERTET");
				cntUnbewertet++;
			} else if (roundValue <= result.getUpLim() && roundValue >= result.getLoLim()) {
				result.setState(2);
				System.out.println("Status: IO");
				cntIO++;
			} else {
				result.setState(1);
				System.out.println("Status: NIO");
				cntNIO++;
			}

			result.setValue((float) roundValue);

			// Insert in die Datenbank
			// Service.getInstance().insertResult(result);

			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
			}

		}

		System.out.println("Anzahl IO: " + cntIO);
		System.out.println("Anzahl NIO: " + cntNIO);
		System.out.println("Anzahl unbwertet: " + cntUnbewertet);

	}

}
