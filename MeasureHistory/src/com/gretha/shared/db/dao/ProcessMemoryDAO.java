package com.gretha.shared.db.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Unit;

public class ProcessMemoryDAO implements ProcessDAO {

	private static final Logger logger = Logger.getLogger(ProcessMemoryDAO.class);

	List<Process> processList = new ArrayList<>();
	Process process;

	@Override
	public List<Process> getProcesses() throws DAOException {

		Process process;

		for (int i = 0; i < 3; i++) {

			process = new Process();
			process.setId(i + 1);
			process.setUnitId(i);
			process.setUnit(getUnit());
			process.setName("DummyName " + i);
			process.setStation("DummyStation " + i);
			process.setSetvalue((float) (Math.random() * 5) + i);
			process.setDecimalPoints(2);
			Calendar calendar = Calendar.getInstance();
			Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
			process.setTimestamp(currentTimestamp.toString());
			processList.add(process);

		}

		return processList;
	}

	public Unit getUnit() throws DAOException {

		Unit unit = null;

		unit = new Unit();
		unit.setId(0);
		unit.setSign("mm");
		return unit;

	}

	@Override
	public Process getProcess(int processId) throws DAOException {

		Process process = null;

		for (Process pr : processList) {

			if (processId == pr.getId())
				return pr;
		}

		return process;
	}

	@Override
	public boolean updateProcess(Process process) throws DAOException {
		return false;

	}

	@Override
	public boolean insertProcess(Process process) throws DAOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean deleteProcess(int id) throws DAOException {
		// TODO Auto-generated method stub
		return false;
	}

}