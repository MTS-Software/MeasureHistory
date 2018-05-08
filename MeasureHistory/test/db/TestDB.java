package db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.PropertyConfigurator;

import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Process;
import com.gretha.shared.util.ApplicationProperties;

public class TestDB {

	private ResourceBundle resources = ResourceBundle.getBundle("language");

	public static void main(String[] args) {
		new TestDB();

	}

	public TestDB() {
		PropertyConfigurator.configure(
				getClass().getClassLoader().getResource("log4j" + resources.getString("appname1") + ".properties"));

		ApplicationProperties.configure("application.properties", resources.getString("appname1") + "Data",
				"application.properties");
		ApplicationProperties.getInstance().setup();

		List<Process> processList = new ArrayList<>();
		processList = Service.getInstance().getProcesses();

		System.out.println("------------------- UNSORTIERT -----------------");
		for (Process process : processList) {
			System.out.println(process.getStation() + " " + process.getName());
		}

		// System.out.println("------------------- SORTIERT NACH NAME
		// -----------------");
		// Collections.sort(processList, new ProcessSortByName());
		// for (Process process : processList) {
		// System.out.println(process.getStation() + " " + process.getName());
		// }
		//
		// System.out.println("------------------- SORTIERT NACH
		// STATION-----------------");
		// Collections.sort(processList, new ProcessSortByStation());
		// for (Process process : processList) {
		// System.out.println(process.getStation() + " " + process.getName());
		// }

		// System.out.println("------------------- SORTIERT NACH
		// GOTT-----------------");
		// Collections.sort(processList, new ProcessSort());
		// for (Process process : processList) {
		// System.out.println(process.getStation() + " " + process.getName());
		// }

		System.out.println("------------------- SORTIERT NACH Jesus-----------------");
		Collections.sort(processList);
		for (Process process : processList) {
			System.out.println(process.getStation() + " " + process.getName());
		}
	}

}

class ProcessSortByName implements Comparator<Process> {
	@Override
	public int compare(Process a1, Process a2) {
		if (a1.getName() == null) {
			return -1;
		}
		if (a2.getName() == null) {
			return 1;
		}
		if (a1.getName().equals(a2.getName())) {
			return 0;
		}
		return a1.getName().compareTo(a2.getName());
	}
}

class ProcessSortByStation implements Comparator<Process> {
	@Override
	public int compare(Process a1, Process a2) {
		if (a1.getStation() == null) {
			return -1;
		}
		if (a2.getStation() == null) {
			return 1;
		}
		if (a1.getStation().equals(a2.getStation())) {
			return 0;
		}
		return a1.getStation().compareTo(a2.getStation());
	}
}

class ProcessSort implements Comparator<Process> {
	@Override
	public int compare(Process a1, Process a2) {
		if (a1.getStation() == null) {
			return -1;
		}
		if (a2.getStation() == null) {
			return 1;
		}
		if (a1.getStation().equals(a2.getStation())) {
			if (a1.getName().equals(a2.getName())) {
				return 0;
			}
		}
		return a1.getStation().compareTo(a2.getStation());
	}

}
