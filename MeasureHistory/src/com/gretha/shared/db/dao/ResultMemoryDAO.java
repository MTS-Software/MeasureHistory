package com.gretha.shared.db.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Result;

public class ResultMemoryDAO implements ResultDAO {

	private static final Logger logger = Logger.getLogger(ResultMemoryDAO.class);

	List<Result> resultList = new ArrayList<>();

	public ResultMemoryDAO() {

		Result result;

		for (int i = 1; i <= 1000; i++) {

			// Aktuelle Zählerstand ausgeben
			System.out.println("Zählerstand: " + i);

			// Zufallszahl zwischen 7 und 11.0 erzeugen
			double randomValue = (Math.random() * 0.160) + 0.01;
			System.out.println("Zufallszahl: " + randomValue);

			// Zufallszahl auf 3 Dezimalstellen runden
			double roundValue = Math.round(randomValue * 10000) / 10000.0;
			System.out.println("Gerundet: " + roundValue);

			result = new Result();
			result.setId(i);
			result.setUpLim(new Float(0.14));
			result.setLoLim(new Float(0.05));
			result.setProcessId(6);
			result.setRemark("DummyRemark");
			result.setSerial("12345678");

			// Status IO/NIO ahnand von Istwert eruieren
			if (roundValue > result.getUpLim() || roundValue < result.getLoLim()) {
				result.setState(1);
				System.out.println("Status: NIO");
			} else if (roundValue >= 0.0601 && roundValue <= 0.0605) {
				result.setState(0);
				System.out.println("Status: UNBEWERTET");
			} else {
				result.setState(2);
				System.out.println("Status: IO");
			}

			Calendar calendar = Calendar.getInstance();
			Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
			result.setTimestamp(currentTimestamp.toString());
			result.setTimestampSql(currentTimestamp);

			result.setTypId(1);
			result.setWt("DummyWT");
			result.setValue((float) roundValue);

			resultList.add(result);

		}

	}

	@Override
	public boolean delete(Result result) throws DAOException {

		return resultList.remove(result);

	}

	@Override
	public List<Result> getResults(Process process) throws DAOException {

		process.setResults(resultList);

		return resultList;

	}

	@Override
	public boolean insert(Result result) throws DAOException {

		return resultList.add(result);

	}

	@Override
	public Result getResult(int id) throws DAOException {

		for (Result result : resultList) {
			if (result.getId() == id)
				return result;
		}
		return null;
	}

	@Override
	public void update(Result result) throws DAOException {

	}

	@Override
	public float getMin(int processId) throws DAOException {

		return (float) (Math.random() * 5);

	}

	@Override
	public float getMax(int processId) throws DAOException {
		return (float) (Math.random() * 5) + 10;

	}

	@Override
	public float getMittelwert(int processId) throws DAOException {
		return (float) (Math.random() * 5) + 5;

	}

	@Override
	public int getOk(int processId) throws DAOException {
		return 50;

	}

	@Override
	public int getNok(int processId) throws DAOException {
		return 50;

	}

	@Override
	public int getUnbewertet(int processId) throws DAOException {
		return 25;

	}

	@Override
	public int getUndefiniert(int processId) throws DAOException {
		return 2;

	}

	@Override
	public int[] getOverall(int processId) throws DAOException {
		return new int[] { 110, 10, 2, 1, 123 };

	}

	@Override
	public List<Result> getResultsFilterDateTime(Process process, Timestamp start, Timestamp end) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Result> getResultsFilterLastNfromDate(Process process, Timestamp end, int limit, int state)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Result> getLastXResults(int anzahl, int state) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Result> getResultsFilterFirstNsinceDate(Process process, Timestamp strt, int limit, int state)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

}