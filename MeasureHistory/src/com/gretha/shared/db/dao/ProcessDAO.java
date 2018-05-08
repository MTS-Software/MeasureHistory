package com.gretha.shared.db.dao;

import java.util.List;

import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.Process;

/**
 * Interface zum Datenaustausch
 * 
 * @author Markus Thaler, Ing.
 */
public interface ProcessDAO {

	public List<Process> getProcesses() throws DAOException;

	public Process getProcess(int processId) throws DAOException;

	public boolean updateProcess(Process process) throws DAOException;

	public boolean insertProcess(Process process) throws DAOException;

	public boolean deleteProcess(int id) throws DAOException;

}