package com.gretha.shared.db.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import com.gretha.shared.db.dao.DAOFactory;
import com.gretha.shared.db.dao.EDAOType;
import com.gretha.shared.db.dao.PlcDAO;
import com.gretha.shared.db.dao.PlcTriggerDAO;
import com.gretha.shared.db.dao.ProcessDAO;
import com.gretha.shared.db.dao.ResultDAO;
import com.gretha.shared.db.dao.SettingsDAO;
import com.gretha.shared.db.dao.UnitDAO;
import com.gretha.shared.model.Plc;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Result;
import com.gretha.shared.model.Settings;
import com.gretha.shared.model.Unit;
import com.gretha.shared.util.ApplicationProperties;
import com.gretha.shared.util.Constants;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Service Layer zur Trennung von DAO und UI
 * 
 * @author Markus Thaler, Ing.
 *
 */
public class Service {

	private static Service instance;
	private static EDAOType source;
	public static EDatabase database;

	private DAOFactory daoFactory;

	private boolean errorStatus = false;

	private ResultDAO resultDAO;
	private ProcessDAO processDAO;
	private UnitDAO unitDAO;
	private PlcDAO plcDAO;
	private PlcTriggerDAO plcTriggerDAO;
	private SettingsDAO settingsDAO;

	private Service() {

		if (ApplicationProperties.getInstance().getProperty("demomode").equalsIgnoreCase("true"))
			source = EDAOType.MEMORY;
		else
			source = EDAOType.JDBC;

		if (ApplicationProperties.getInstance().getProperty("db_vendor").equalsIgnoreCase("mysql"))
			database = EDatabase.MYSQL;
		if (ApplicationProperties.getInstance().getProperty("db_vendor").equalsIgnoreCase("sqlserver"))
			database = EDatabase.SQLSERVER;

		daoFactory = new DAOFactory(source);

		resultDAO = daoFactory.getResultDAO();
		processDAO = daoFactory.getProcessDAO();
		plcTriggerDAO = daoFactory.getTriggerDAO();
		unitDAO = daoFactory.getUnitDAO();
		plcDAO = daoFactory.getPlcDAO();
		settingsDAO = daoFactory.getSettingsDAO();
	}

	public synchronized static Service getInstance() {

		if (instance == null) {
			instance = new Service();
		}

		return instance;

	}

	public boolean insertResult(Result result) {
		boolean res = false;

		try {
			res = resultDAO.insert(result);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;

	}

	public List<Process> getProcesses() {

		List<Process> processes = null;

		try {
			processes = processDAO.getProcesses();
			Collections.sort(processes);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);

		} catch (NullPointerException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return processes;

	}

	public List<Plc> getPlcs() {

		List<Plc> plcs = null;

		try {
			plcs = plcDAO.getPlcs();
			Collections.sort(plcs);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);

		} catch (NullPointerException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return plcs;

	}

	public Settings getSettings() {

		Settings settings = null;

		try {
			settings = settingsDAO.getSettings();
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return settings;

	}

	public List<PlcTrigger> getPlcTriggers() {

		List<PlcTrigger> plcTriggers = null;

		try {
			plcTriggers = plcTriggerDAO.getPlcTriggers();
			Collections.sort(plcTriggers);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);

		} catch (NullPointerException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return plcTriggers;

	}

	public List<Result> getResults(Process process) {

		List<Result> results = null;

		try {
			results = resultDAO.getResults(process);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return results;

	}

	public List<Result> getResults(Process process, Timestamp start, Timestamp end) {

		List<Result> results = null;

		try {
			results = resultDAO.getResultsFilterDateTime(process, start, end);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return results;

	}

	public List<Result> getResultsLastNfromDate(Process process, Timestamp end, int limit, int state) {

		List<Result> results = null;

		try {
			results = resultDAO.getResultsFilterLastNfromDate(process, end, limit, state);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return results;

	}

	public List<Result> getResultsFirstNsinceDate(Process process, Timestamp strt, int limit, int state) {

		List<Result> results = null;

		try {
			results = resultDAO.getResultsFilterFirstNsinceDate(process, strt, limit, state);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return results;

	}

	public float getMin(int processId) {

		float min = 0;

		try {
			min = resultDAO.getMin(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return min;

	}

	public Process getProcess(int processId) {

		Process process = null;

		try {
			process = processDAO.getProcess(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return process;

	}

	public List<Result> getLastXResults(int anzahl, int state) {

		List<Result> results = null;

		try {
			results = resultDAO.getLastXResults(anzahl, state);

			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return results;

	}

	public Plc getPlc(int id) {

		Plc plc = null;

		try {
			plc = plcDAO.getPlc(id);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return plc;

	}

	public PlcTrigger getPlcTrigger(int id) {

		PlcTrigger plctrigger = null;

		try {
			plctrigger = plcTriggerDAO.getPlcTrigger(id);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return plctrigger;

	}

	public float getMax(int processId) {

		float max = 0;

		try {
			max = resultDAO.getMax(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return max;

	}

	public float getAvg(int processId) {

		float avg = 0;

		try {
			avg = resultDAO.getMittelwert(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return avg;

	}

	public int getOk(int processId) {

		int cnt = 0;

		try {
			cnt = resultDAO.getOk(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return cnt;

	}

	public int getNok(int processId) {

		int cnt = 0;

		try {
			cnt = resultDAO.getNok(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return cnt;

	}

	public int getUnbewertet(int processId) {

		int cnt = 0;

		try {
			cnt = resultDAO.getUnbewertet(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return cnt;

	}

	public int getUndefiniert(int processId) {

		int cnt = 0;

		try {
			cnt = resultDAO.getUndefiniert(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		return cnt;

	}

	public int[] getOverall(int processId) {

		int[] cnt = null;

		try {
			cnt = resultDAO.getOverall(processId);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

		/*
		 * cnt[0] = Anzahl OK; cnt[1] = Anzahl NOK; cnt[2] = Anzahl UNBWERTET; cnt[3] =
		 * Anzahl UNDEFINIERT; cnt[4] = Anzahl GESAMT
		 */
		return new int[] { cnt[0], cnt[1], cnt[2], cnt[3], cnt[4] };

	}

	public List<Unit> getUnits() {
		List<Unit> units = null;

		try {
			units = unitDAO.getUnits();
			Collections.sort(units);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);

		} catch (NullPointerException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return units;
	}

	public boolean updateProcess(Process process) {

		boolean res = false;
		try {
			res = processDAO.updateProcess(process);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;

	}

	public boolean insertProcess(Process process) {

		boolean res = false;
		try {
			res = processDAO.insertProcess(process);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;

	}

	public void updateResult(Result result) {

		try {
			resultDAO.update(result);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}

	}

	public boolean updateUnit(Unit unit) {
		boolean res = false;
		try {
			res = unitDAO.updateUnit(unit);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean updatePlc(Plc plc) {
		boolean res = false;
		try {
			res = plcDAO.updatePlc(plc);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean updatePlcTrigger(PlcTrigger plctrigger) {
		boolean res = false;
		try {
			res = plcTriggerDAO.updatePlcTrigger(plctrigger);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean updateSettings(Settings settings) {
		boolean res = false;
		try {
			res = settingsDAO.updateSettings(settings);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean deleteResult(Result result) {

		boolean res = false;

		try {
			res = resultDAO.delete(result);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean deleteUnit(int id) {

		boolean res = false;

		try {
			res = unitDAO.deleteUnit(id);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean deleteProcess(int id) {

		boolean res = false;

		try {
			res = processDAO.deleteProcess(id);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean deletePlc(int id) {

		boolean res = false;

		try {
			res = plcDAO.deletePlc(id);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean deletePlcTrigger(int id) {

		boolean res = false;

		try {
			res = plcTriggerDAO.deletePlcTrigger(id);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean insertUnit(Unit unit) {

		boolean res = false;

		try {
			res = unitDAO.insertUnit(unit);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean insertPlc(Plc plc) {

		boolean res = false;

		try {
			res = plcDAO.insertPlc(plc);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean insertPlcTrigger(PlcTrigger plctigger) {

		boolean res = false;

		try {
			res = plcTriggerDAO.insertPlcTrigger(plctigger);
			errorStatus = false;
		} catch (DAOException e) {

			e.printStackTrace();
			showExceptionMessage(e);
		}
		return res;
	}

	public boolean isErrorStatus() {
		return errorStatus;
	}

	private void showExceptionMessage(Exception e) {
		errorStatus = true;
		showExceptionAlertDialog(e);
	}

	private void showExceptionAlertDialog(Exception e) {

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception Dialog");
		alert.setContentText(e.getMessage() + "\nException: " + e.getClass().getName());
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(getClass().getResource(Constants.STYLESHEET).toExternalForm());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was: ");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();

	}

}
