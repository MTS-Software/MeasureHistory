package com.gretha.shared.util;

import java.util.Comparator;

/**
 * Versuch einen String in einen Integer zu umwandeln und vergleicht diesen. Ist
 * ein umwandeln nicht möglich, wird der String als "to lower Case" verglichen.
 * 
 * Hinweis: Sortierung von IP-Adressen als String funktioniert nicht
 * 
 * @author Michael Grebesits, Ing.
 * 
 * @version 1.0
 */
public class StringCustomComparator implements Comparator<String> {
	@Override
	public int compare(String string1, String string2) {
		if (string1 == null && string2 == null)
			return 0;
		if (string1 == null)
			return -1;
		if (string2 == null)
			return 1;

		Integer int1 = null;
		Integer int2 = null;

		try {

			// Versuch Umwandlung des String in Integer
			int1 = Integer.valueOf(string1);
			int2 = Integer.valueOf(string2);

		} catch (NumberFormatException ignored) {

		}

		if (int1 == null || int2 == null)
			return string1.toLowerCase().compareTo(string2.toLowerCase());

		return int1.compareTo(int2);
	}
}
