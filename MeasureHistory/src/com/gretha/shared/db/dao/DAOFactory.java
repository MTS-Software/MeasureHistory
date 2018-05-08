package com.gretha.shared.db.dao;

/**
 * Abstrahiert den Datenbankzugriff mit Anwendung nach dem DAO Pattern
 * 
 * @author Markus Thaler, Ing.
 *
 */
public class DAOFactory {

	private ResultDAO resultDAO;
	private ProcessDAO processDAO;
	private UnitDAO unitDAO;
	private PlcDAO plcDAO;
	private PlcTriggerDAO plcTriggerDAO;
	private SettingsDAO settingsDAO;

	public DAOFactory(EDAOType eDAOType) {

		if (eDAOType == EDAOType.JDBC) {
			resultDAO = new ResultJDBCDAO();
			processDAO = new ProcessJDBCDAO();
			unitDAO = new UnitJDBCDAO();
			plcDAO = new PlcJDBCDAO();
			plcTriggerDAO = new PlcTriggerJDBCDAO();
			settingsDAO = new SettingsJDBCDAO();
		}

		if (eDAOType == EDAOType.XML) {

		}

		if (eDAOType == EDAOType.MEMORY) {
			processDAO = new ProcessMemoryDAO();
			resultDAO = new ResultMemoryDAO();
		}

	}

	public PlcTriggerDAO getTriggerDAO() {
		return plcTriggerDAO;
	}

	public ResultDAO getResultDAO() {
		return resultDAO;
	}

	public ProcessDAO getProcessDAO() {
		return processDAO;
	}

	public UnitDAO getUnitDAO() {
		return unitDAO;
	}

	public PlcDAO getPlcDAO() {
		return plcDAO;
	}

	public SettingsDAO getSettingsDAO() {
		return settingsDAO;
	}

}
