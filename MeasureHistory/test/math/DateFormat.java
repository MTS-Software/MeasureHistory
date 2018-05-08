package math;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;

public class DateFormat {

	public static void main(String[] args) {

		new DateFormat();

	}

	public DateFormat() {

		Calendar cal = Calendar.getInstance();
		LocalDateTime ldVon = LocalDateTime.of(LocalDate.now().minusDays(1),
				LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
		LocalDateTime ldBis = LocalDateTime.of(LocalDate.now(),
				LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));

		System.out.println(ldVon);
		System.out.println(ldBis);

	}

}
