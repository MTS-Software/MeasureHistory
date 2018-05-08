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
import com.gretha.shared.model.Plc;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.model.Process;

public class PlcTriggerJDBCDAO implements PlcTriggerDAO {

	private static final Logger logger = Logger.getLogger(PlcTriggerJDBCDAO.class);

	private final static String SELECT_TRIGGERS = "SELECT * FROM plctrigger";
	private final static String UPDATE_TRIGGER = "UPDATE plctrigger SET db = ?, strt_adr = ?, activated = ?, plc_id = ? WHERE id = ?";
	private final static String INSERT_TRIGGER = "INSERT plctrigger SET db = ?, strt_adr = ?, activated = ?, plc_id = ?";
	private final static String DELETE_TRIGGER = "DELETE FROM plctrigger where id = ?";
	private final static String SELECT_TRIGGER = "SELECT * FROM plctrigger where id = ?";

	private final static String SELECT_PROCESS = "SELECT * FROM process where plcTrigger_Id = ?";
	private final static String SELECT_PLC = "SELECT * FROM plc where id = ?";

	@Override
	public PlcTrigger getPlcTrigger(int id) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		PreparedStatement psPlc = null;
		PreparedStatement psProcess = null;
		Connection con = null;

		PlcTrigger plcTrigger = null;

		try {

			con = ConnectionManager.getInstance().getConnection();

			ps = con.prepareStatement(SELECT_TRIGGER);
			psPlc = con.prepareStatement(SELECT_PLC);
			psProcess = con.prepareStatement(SELECT_PROCESS);

			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {
				plcTrigger = new PlcTrigger();
				plcTrigger.setId(rs.getInt("id"));
				plcTrigger.setDb(rs.getInt("db"));
				plcTrigger.setStrtAdr(rs.getInt("strt_adr"));
				plcTrigger.setActivated(rs.getBoolean("activated"));
				plcTrigger.setPlcId(rs.getInt("plc_id"));

				String ts = rs.getString("timestamp");
				plcTrigger.setTimestamp(ts.subSequence(0, 19).toString());

				Plc plc = getPlc(psPlc, plcTrigger.getPlcId());
				plcTrigger.setPlc(plc);

				// Process
				Process process = getProcess(psProcess, plcTrigger.getId());
				plcTrigger.setProcess(process);

			}

			if (logger.isInfoEnabled()) {
				// logger.info(plctrigger);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return plcTrigger;
	}

	// Liefert nur die Trigger die einem Prozess zugeordnet sind
	@Override
	public List<PlcTrigger> getPlcTriggers() throws DAOException {

		ResultSet rs = null;
		Statement statement = null;
		PreparedStatement psProcess = null;
		PreparedStatement psPlc = null;
		Connection con = null;

		List<PlcTrigger> plcTriggers = new ArrayList<>();

		try {
			con = ConnectionManager.getInstance().getConnection();

			statement = con.createStatement();
			rs = statement.executeQuery(SELECT_TRIGGERS);
			psProcess = con.prepareStatement(SELECT_PROCESS);
			psPlc = con.prepareStatement(SELECT_PLC);

			while (rs.next()) {
				PlcTrigger plcTrigger = new PlcTrigger();
				plcTrigger.setId(rs.getInt("id"));
				plcTrigger.setDb(rs.getInt("db"));
				plcTrigger.setStrtAdr(rs.getInt("strt_adr"));
				plcTrigger.setActivated(rs.getBoolean("activated"));
				plcTrigger.setPlcId(rs.getInt("plc_id"));

				String ts = rs.getString("timestamp");
				plcTrigger.setTimestamp(ts.subSequence(0, 19).toString());

				// Process
				Process process = getProcess(psProcess, plcTrigger.getId());
				plcTrigger.setProcess(process);

				// Plc
				Plc plc = getPlc(psPlc, plcTrigger.getPlcId());
				plcTrigger.setPlc(plc);

				plcTriggers.add(plcTrigger);

			}
			if (logger.isInfoEnabled()) {
				// logger.info(plcTriggers);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return plcTriggers;

	}

	public Process getProcess(PreparedStatement psData, int plcTriggerId) throws DAOException {

		ResultSet rs = null;
		Process data = null;

		try {
			psData.setInt(1, plcTriggerId);
			rs = psData.executeQuery();

			while (rs.next()) {
				data = new Process();
				data.setId(rs.getInt("id"));
				data.setName(rs.getString("name"));
				data.setStation(rs.getString("station"));

			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return data;

	}

	public Plc getPlc(PreparedStatement psData, int id) throws DAOException {

		ResultSet rs = null;
		Plc data = null;

		try {
			psData.setInt(1, id);
			rs = psData.executeQuery();

			while (rs.next()) {
				data = new Plc();
				data.setId(rs.getInt("id"));
				data.setName(rs.getString("name"));
				data.setIp(rs.getString("ip"));

			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return data;

	}

	@Override
	public boolean updatePlcTrigger(PlcTrigger plctrigger) throws DAOException {
		int res;
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(UPDATE_TRIGGER);

			ps.setInt(1, plctrigger.getDb());
			ps.setInt(2, plctrigger.getStrtAdr());
			ps.setBoolean(3, plctrigger.isActivated());
			ps.setInt(4, plctrigger.getPlcId());
			ps.setInt(5, plctrigger.getId());
			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(plctrigger);
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
	public boolean insertPlcTrigger(PlcTrigger plctrigger) throws DAOException {
		int res;
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(INSERT_TRIGGER);

			ps.setInt(1, plctrigger.getDb());
			ps.setInt(2, plctrigger.getStrtAdr());
			ps.setBoolean(3, plctrigger.isActivated());
			ps.setInt(4, plctrigger.getPlcId());
			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(plctrigger);
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
	public boolean deletePlcTrigger(int id) throws DAOException {
		int res = 0;

		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(DELETE_TRIGGER);

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
