package collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MergeResults {

	private List<Result> resultList1 = new ArrayList<>();
	private List<Result> resultList2 = new ArrayList<>();

	private List<Result> maxSizeList = new ArrayList<>();
	private Map<String, Integer> duplicateSerialMap = new HashMap<>();

	public static void main(String[] args) {

		new MergeResults();

	}

	public MergeResults() {

		generateTestData();

		// System.out.println("vor Filterung");
		// System.out.println("Result 1: " + resultList1);
		// System.out.println("Result 2: " + resultList2);

		getDuplicateSerials();
		removeSingleSerials();
		// find();

		System.out.println("Result 1: " + resultList1);
		System.out.println("Result 2: " + resultList2);
		// mitIterator();
		// mitForSchleife();

	}

	private void generateTestData() {

		resultList1.add(new Result(1, "123", 33));
		resultList1.add(new Result(2, "124", 23));
		// resultList1.add(new Result(3, "125", 66));

		resultList2.add(new Result(10, "123", 45));
		resultList2.add(new Result(11, "124", 48));
		resultList2.add(new Result(1, "12", 45));

	}

	private void getDuplicateSerials() {

		for (Result result : resultList1) {

			if (!duplicateSerialMap.containsKey(result.getSerial())) {
				duplicateSerialMap.put(result.getSerial(), 1);

			}

			else {
				int value = duplicateSerialMap.get(result.getSerial());
				duplicateSerialMap.put(result.getSerial(), value + 1);
			}

		}

		for (Result result : resultList2) {

			if (!duplicateSerialMap.containsKey(result.getSerial())) {
				duplicateSerialMap.put(result.getSerial(), 1);

			} else {
				int value = duplicateSerialMap.get(result.getSerial());
				duplicateSerialMap.put(result.getSerial(), value + 1);
			}

		}

		System.out.println(duplicateSerialMap);

	}

	private void removeSingleSerials() {

		Iterator<Result> i = resultList1.iterator();
		while (i.hasNext()) {

			Result result = i.next();

			for (Map.Entry<String, Integer> entry : duplicateSerialMap.entrySet()) {

				if (result.getSerial().equals(entry.getKey())) {
					if (entry.getValue() == 1)
						i.remove();
				}

			}

		}

		i = resultList2.iterator();
		while (i.hasNext()) {

			Result result = i.next();

			for (Map.Entry<String, Integer> entry : duplicateSerialMap.entrySet()) {

				if (result.getSerial().equals(entry.getKey())) {
					if (entry.getValue() == 1)
						i.remove();
				}

			}

		}

	}

}

class Result {

	private int id;
	private String serial;
	private int value;

	public Result(int id, String serial, int value) {

		this.id = id;
		this.serial = serial;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Result other = (Result) obj;
		if (serial == null) {
			if (other.serial != null)
				return false;
		} else if (!serial.equals(other.serial))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Result [serial=" + serial + "]";
	}

}
