package com.gretha.shared.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Brechnet die Zeitdauer in Jahre, Monate, Tage, Stunden, Minuten, Sekunden
 * zwischen zwei Zeitstempel
 * 
 * @author Michael Grebesits, Ing.
 * 
 * @version 1.0
 */
public class DurationDateAndTime {

	/**
	 * Berechnet die gesamte Zeitdauer
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return [0] Jahre [1] Monate [2] Tage [3] Stunden [4] Minuten [5] Sekunden
	 *         [6] Sekunden gesamt
	 */
	public static long[] getPeriodAsArray(LocalDateTime fromDateTime, LocalDateTime toDateTime) {

		LocalDateTime tempFrom = LocalDateTime.from(fromDateTime);

		long years = Math.abs(tempFrom.until(toDateTime, ChronoUnit.YEARS));
		tempFrom = tempFrom.plusYears(years);

		long months = Math.abs(tempFrom.until(toDateTime, ChronoUnit.MONTHS));
		tempFrom = tempFrom.plusMonths(months);

		long days = Math.abs(tempFrom.until(toDateTime, ChronoUnit.DAYS));
		tempFrom = tempFrom.plusDays(days);

		long hours = Math.abs(tempFrom.until(toDateTime, ChronoUnit.HOURS));
		tempFrom = tempFrom.plusHours(hours);

		long minutes = Math.abs(tempFrom.until(toDateTime, ChronoUnit.MINUTES));
		tempFrom = tempFrom.plusMinutes(minutes);

		long seconds = Math.abs(tempFrom.until(toDateTime, ChronoUnit.SECONDS));

		Duration duration = Duration.between(fromDateTime, toDateTime);
		long secondsTotal = Math.abs(duration.getSeconds());

		return new long[] { years, months, days, hours, minutes, seconds, secondsTotal };
	}

	/**
	 * Berechnet die gesamte Zeitdauer
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return x Jahre x Monate x Tage x Stunden x Minuten x Sekunden
	 */
	public static String getPeriodAsString(LocalDateTime fromDateTime, LocalDateTime toDateTime) {

		StringBuilder sb = new StringBuilder();
		long dateTime[] = getPeriodAsArray(fromDateTime, toDateTime);

		if (dateTime[0] > 0)
			sb.append(dateTime[0] + " Jahre ");

		if (dateTime[1] > 0)
			sb.append(dateTime[1] + " Monate ");

		if (dateTime[2] > 0)
			sb.append(dateTime[2] + " Tage ");

		if (dateTime[3] > 0)
			sb.append(dateTime[3] + " Stunden ");

		if (dateTime[4] > 0)
			sb.append(dateTime[4] + " Minuten ");

		if (dateTime[5] > 0)
			sb.append(dateTime[5] + " Sekunden");

		return sb.toString();
	}

	/**
	 * Berechnet die gesamte Zeitdauer
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return Gesamte Zeitdauer in Sekunden
	 */
	public static long getPeriodInSeconds(LocalDateTime fromDateTime, LocalDateTime toDateTime) {

		long dateTime[] = getPeriodAsArray(fromDateTime, toDateTime);

		return dateTime[6];
	}

	/**
	 * Berechnet die gesamte Zeitdauer
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return Gesamte Zeitdauer in hh:mm:ss
	 */
	public static String getPeriodInTime(LocalDateTime fromDateTime, LocalDateTime toDateTime) {

		StringBuilder sb = new StringBuilder();
		long dateTime[] = getPeriodAsArray(fromDateTime, toDateTime);
		long hours = dateTime[6] / 3600;
		long minutes = dateTime[6] % 3600 / 60;
		long seconds = dateTime[6] % 3600 % 60;

		sb.append(String.format("%02d", hours) + ":");
		sb.append(String.format("%02d", minutes) + ":");
		sb.append(String.format("%02d", seconds));

		return sb.toString();
	}

	/**
	 * Rechnet Sekunden in hh:mm:ss um
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return Zeit in hh:mm:ss
	 */
	public static String getTimeFromTotalSeconds(long totalSeconds) {

		StringBuilder sb = new StringBuilder();
		long hours = Math.abs(totalSeconds / 3600);
		long minutes = Math.abs(totalSeconds % 3600 / 60);
		long seconds = Math.abs(totalSeconds % 3600 % 60);

		sb.append(String.format("%02d", hours) + ":");
		sb.append(String.format("%02d", minutes) + ":");
		sb.append(String.format("%02d", seconds));

		return sb.toString();
	}

	/**
	 * Rechnet Millisekunden in hh:mm:ss.SSS um
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @return Zeit in hh:mm:ss.SSS
	 */
	public static String getTimestampFromMilliseconds(long milliseconds) {
		long ms = Math.abs(milliseconds % 1000);
		long s = Math.abs((milliseconds / 1000) % 60);
		long m = Math.abs(((milliseconds / (1000 * 60)) % 60));
		long h = Math.abs(((milliseconds / (1000 * 60 * 60)) % 24));

		return String.format("%02d:%02d:%02d.%03d", h, m, s, ms);
	}
}
