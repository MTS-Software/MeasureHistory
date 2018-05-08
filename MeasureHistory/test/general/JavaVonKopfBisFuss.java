package general;

public class JavaVonKopfBisFuss {
	public static void main(String[] args) {

		bierLied();
		// mischMasch();

	}

	private static void bierLied() {
		int bierAnzahl = 99;

		String wort = "Flaschen";

		while (bierAnzahl > 0) {

			System.out.println(bierAnzahl + " " + wort + " Bier im Kühlschrank");
			System.out.println(bierAnzahl + " " + wort + " Bier.");
			System.out.println("Hol eine raus.");
			System.out.println("Und lass sie rumgehen.");

			bierAnzahl--;

			if (bierAnzahl == 1)
				wort = "Flasche";

			if (bierAnzahl < 1)
				System.out.println("Kein Bier mehr im Kühlschrank");

		}

	}

	private static void mischMasch() {

		int x = 3;
		while (x > 0) {

			if (x > 2) {
				System.out.print("a");
				System.out.print("-");
			}

			if (x == 2) {
				System.out.print("b c");
				System.out.print("-");
			}

			if (x == 1)
				System.out.print("d");

			x--;

		}

	}
}
