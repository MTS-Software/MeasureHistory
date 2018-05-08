package com.gretha.shared.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.gretha.shared.db.util.ConnectionManager;
import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Unit;

public class ProcessJDBCDAO implements ProcessDAO {

	private static final Logger logger = Logger.getLogger(ProcessJDBCDAO.class);

	private final static String SELECT_PROCESSES = "SELECT * FROM process order by station asc";
	private final static String SELECT_UNIT = "SELECT * FROM unit where id = ?";
	private final static String SELECT_PLCTRIGGER = "SELECT * FROM plctrigger where id = ?";
	private final static String DELETE_PROCESS = "DELETE FROM process where id = ?";
	private final static String SELECT_PROCESS = "SELECT * FROM process where id = ?";
	private final static String UPDATE_PROCESS = "UPDATE process SET station = ?, name = ?, setvalue_used = ?, setvalue = ?, decpoints = ?, nr_avg = ?, nr_spc_class = ?, cpk_lolim1 = ?, cpk_lolim2 = ?, unit_id = ?, plctrigger_id = ? WHERE id = ?";
	private final static String INSERT_PROCESS = "INSERT process SET station = ?, name = ?, setvalue_used = ?, setvalue = ?, decpoints = ?, nr_avg = ?, nr_spc_class = ?, cpk_lolim1 = ?, cpk_lolim2 = ?, unit_id = ?, plctrigger_id = ?";

	public Integer selectLastID() throws DAOException {

		Integer lastId = null;

		try {
			PreparedStatement ps = ConnectionManager.getInstance().getConnection()
					.prepareStatement("select last_insert_id()");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				lastId = rs.getInt(1);
			}

			if (logger.isInfoEnabled()) {
				// logger.info(lastId);
			}
		} catch (SQLException e) {
			throw new DAOException(e);
		}

		return lastId;
	}

	@Override
	public List<Process> getProcesses() throws DAOException {

		ResultSet rs = null;
		Statement statement = null;
		PreparedStatement psUnit = null;
		PreparedStatement psPlcTrigger = null;
		Connection con = null;

		List<Process> processes = new ArrayList<>();

		try {
			con = ConnectionManager.getInstance().getConnection();

			statement = con.createStatement();
			rs = statement.executeQuery(SELECT_PROCESSES);
			psUnit = con.prepareStatement(SELECT_UNIT);
			psPlcTrigger = con.prepareStatement(SELECT_PLCTRIGGER);

			while (rs.next()) {
				Process process = new Process();
				process.setId(rs.getInt("id"));
				process.setStation(rs.getString("station"));
				process.setName(rs.getString("name"));
				process.setSetvalue(rs.getFloat("setvalue"));
				process.setSetvalueUsed(rs.getBoolean("setvalue_used"));
				process.setDecimalPoints(rs.getInt("decpoints"));
				process.setNrAvg(rs.getInt("nr_avg"));
				process.setNrSpcClass(rs.getInt("nr_spc_class"));
				process.setCpkLoLim1(rs.getFloat("cpk_lolim1"));
				process.setCpkLoLim2(rs.getFloat("cpk_lolim2"));
				process.setUnitId(rs.getInt("unit_Id"));
				process.setPlcTriggerId(rs.getInt("plctrigger_Id"));

				String ts = rs.getString("timestamp");
				process.setTimestamp(ts.subSequence(0, 19).toString());

				Unit unit = getUnit(psUnit, process.getUnitId());
				process.setUnit(unit);

				PlcTrigger plcTrigger = getPlcTrigger(psPlcTrigger, process.getPlcTriggerId());
				process.setPlcTrigger(plcTrigger);

				processes.add(process);
				if (logger.isInfoEnabled()) {
					// logger.info(process.getStation() + " " +
					// process.getName());
				}
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return processes;
	}

	public Unit getUnit(PreparedStatement psUnit, int id) throws DAOException {

		ResultSet rs = null;
		Unit unit = null;

		try {
			psUnit.setInt(1, id);
			rs = psUnit.executeQuery();

			while (rs.next()) {
				unit = new Unit();
				unit.setId(rs.getInt("id"));
				unit.setSign(rs.getString("sign"));

				String ts = rs.getString("timestamp");
				unit.setTimestamp(ts.subSequence(0, 19).toString());

			}

			if (logger.isInfoEnabled()) {
				// logger.info(unit);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return unit;

	}

	public PlcTrigger getPlcTrigger(PreparedStatement psData, int plcTriggerId) throws DAOException {

		ResultSet rs = null;
		PlcTrigger plcTrigger = null;

		try {
			psData.setInt(1, plcTriggerId);
			rs = psData.executeQuery();

			while (rs.next()) {
				plcTrigger = new PlcTrigger();
				plcTrigger.setId(rs.getInt("id"));
				plcTrigger.setDb(rs.getInt("db"));
				plcTrigger.setStrtAdr(rs.getInt("strt_adr"));
				plcTrigger.setActivated(rs.getBoolean("activated"));
				plcTrigger.setPlcId(rs.getInt("plc_id"));

				String ts = rs.getString("timestamp");
				plcTrigger.setTimestamp(ts.subSequence(0, 19).toString());

			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return plcTrigger;

	}

	@Override
	public boolean updateProcess(Process process) throws DAOException {

		int res;
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(UPDATE_PROCESS);

			ps.setString(1, process.getStation());
			ps.setString(2, process.getName());
			ps.setBoolean(3, process.isSetvalueUsed());
			ps.setFloat(4, process.getSetvalue());
			ps.setInt(5, process.getDecimalPoints());
			ps.setInt(6, process.getNrAvg());
			ps.setInt(7, process.getNrSpcClass());
			ps.setFloat(8, process.getCpkLoLim1());
			ps.setFloat(9, process.getCpkLoLim2());
			ps.setInt(10, process.getUnitId());
			ps.setInt(12, process.getId());

			if (process.getPlcTrigger() != null && process.getPlcTriggerId() != 0) {
				ps.setInt(11, process.getPlcTriggerId());
			} else {
				ps.setNull(11, 0);
			}

			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(process);
			}

		}

		catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e);
		}

		if (res > 0) {
			return true;

		} else
			return false;

	}

	@Override
	public Process getProcess(int processId) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		PreparedStatement psUnit = null;
		PreparedStatement psPlcTrigger = null;
		Statement statement = null;
		Connection con = null;

		Process process = null;

		try {

			con = ConnectionManager.getInstance().getConnection();

			ps = con.prepareStatement(SELECT_PROCESS);
			psUnit = con.prepareStatement(SELECT_UNIT);
			psPlcTrigger = con.prepareStatement(SELECT_PLCTRIGGER);

			ps.setInt(1, processId);

			rs = ps.executeQuery();

			while (rs.next()) {
				process = new Process();
				process.setId(rs.getInt("id"));
				process.setStation(rs.getString("station"));
				process.setName(rs.getString("name"));
				process.setSetvalueUsed(rs.getBoolean("setvalue_used"));
				process.setSetvalue(rs.getFloat("setvalue"));
				process.setDecimalPoints(rs.getInt("decpoints"));
				process.setNrAvg(rs.getInt("nr_avg"));
				process.setNrSpcClass(rs.getInt("nr_spc_class"));
				process.setCpkLoLim1(rs.getFloat("cpk_lolim1"));
				process.setCpkLoLim2(rs.getFloat("cpk_lolim2"));
				process.setUnitId(rs.getInt("unit_Id"));
				process.setPlcTriggerId(rs.getInt("plctrigger_Id"));

				String ts = rs.getString("timestamp");
				process.setTimestamp(ts.subSequence(0, 19).toString());

				Unit unit = getUnit(psUnit, process.getUnitId());
				process.setUnit(unit);

				PlcTrigger plcTrigger = getPlcTrigger(psPlcTrigger, process.getPlcTriggerId());
				process.setPlcTrigger(plcTrigger);

				if (logger.isInfoEnabled()) {
					// logger.info(process.getName() + " " + process.getUnit());
				}
			}

			if (logger.isInfoEnabled()) {
				// logger.info(process);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return process;
	}

	@Override
	public boolean insertProcess(Process process) throws DAOException {
		int res;
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(INSERT_PROCESS);

			ps.setString(1, process.getStation());
			ps.setString(2, process.getName());
			ps.setBoolean(3, process.isSetvalueUsed());
			ps.setFloat(4, process.getSetvalue());
			ps.setInt(5, process.getDecimalPoints());
			ps.setInt(6, process.getNrAvg());
			ps.setInt(7, process.getNrSpcClass());
			ps.setFloat(8, process.getCpkLoLim1());
			ps.setFloat(9, process.getCpkLoLim2());
			ps.setInt(10, process.getUnitId());

			if (process.getPlcTrigger() != null && process.getPlcTriggerId() != 0) {
				ps.setInt(11, process.getPlcTriggerId());
			} else {
				ps.setNull(11, 0);
			}

			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(process);
			}

		}

		catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e);
		}

		if (res > 0) {
			return true;

		} else
			return false;

	}

	@Override
	public boolean deleteProcess(int id) throws DAOException {
		int res = 0;

		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(DELETE_PROCESS);

			ps.setInt(1, id);
			res = ps.executeUpdate();

		}

		catch (SQLException e) {
			if (e.getErrorCode() == 547 || e.getErrorCode() == 1451)
				throw new DAOException("Entfernen nicht erlaubt, da die Daten verwendet werden.");
			else
				throw new DAOException(e);
		}

		if (res > 0)
			return true;
		else
			return false;
	}

}