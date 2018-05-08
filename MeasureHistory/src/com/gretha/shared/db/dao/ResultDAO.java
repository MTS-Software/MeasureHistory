package com.gretha.shared.db.dao;

import java.sql.Timestamp;
import java.util.List;

import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Result;

/**
 * Interface zum Datenaustausch
 * 
 * @author Markus Thaler, Ing.
 */
public interface ResultDAO {

	public Result getResult(int id) throws DAOException;

	public List<Result> getResults(Process process) throws DAOException;

	public List<Result> getResultsFilterDateTime(Process process, Timestamp start, Timestamp end) throws DAOException;

	public List<Result> getResultsFilterLastNfromDate(Process process, Timestamp end, int limit, int state)
			throws DAOException;

	public List<Result> getResultsFilterFirstNsinceDate(Process process, Timestamp strt, int limit, int state)
			throws DAOException;

	public boolean insert(Result result) throws DAOException;

	public void update(Result result) throws DAOException;

	public boolean delete(Result result) throws DAOException;

	public float getMin(int processId) throws DAOException;

	public float getMax(int processId) throws DAOException;

	public float getMittelwert(int processId) throws DAOException;

	public int getOk(int processId) throws DAOException;

	public int getNok(int processId) throws DAOException;

	public int getUnbewertet(int processId) throws DAOException;

	public int getUndefiniert(int processId) throws DAOException;

	public int[] getOverall(int processId) throws DAOException;

	public List<Result> getLastXResults(int anzahl, int state) throws DAOException;

}