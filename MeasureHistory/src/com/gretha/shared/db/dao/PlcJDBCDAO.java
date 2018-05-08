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

public class PlcJDBCDAO implements PlcDAO {

	private static final Logger logger = Logger.getLogger(PlcJDBCDAO.class);

	private final static String SELECT_PLCS = "SELECT * FROM plc";
	private final static String INSERT_PLC = "INSERT plc SET name = ?, ip = ?, rack = ?, slot = ?, type = ?, timeout = ?";
	private final static String UPDATE_PLC = "UPDATE plc SET name = ?, ip = ?, rack = ?, slot = ?, type = ?, timeout = ?  WHERE id = ?";
	private final static String SELECT_PLC = "SELECT * FROM plc where id = ?";
	private final static String DELETE_PLC = "DELETE FROM plc where id = ?";

	private final static String SELECT_PLCTRIGGER = "SELECT * FROM plctrigger where plc_id = ?";

	@Override
	public List<Plc> getPlcs() throws DAOException {

		ResultSet rs = null;
		PreparedStatement ps = null;
		Statement statement = null;
		Connection con = null;

		List<Plc> plcs = new ArrayList<>();
		List<PlcTrigger> plcTriggers = new ArrayList<>();

		try {
			con = ConnectionManager.getInstance().getConnection();

			statement = con.createStatement();
			rs = statement.executeQuery(SELECT_PLCS);
			ps = con.prepareStatement(SELECT_PLCTRIGGER);

			while (rs.next()) {
				Plc plc = new Plc();
				plc.setId(rs.getInt("id"));
				plc.setName(rs.getString("name"));
				plc.setIp(rs.getString("ip"));
				plc.setRack(rs.getInt("rack"));
				plc.setSlot(rs.getInt("slot"));
				plc.setType(rs.getInt("type"));
				plc.setTimeout(rs.getInt("timeout"));

				String ts = rs.getString("timestamp");
				plc.setTimestamp(ts.subSequence(0, 19).toString());

				plcs.add(plc);

				// PLCTrigger Liste
				plcTriggers = getPlcTrigger(ps, plc.getId());
				plc.setPlcTriggers(plcTriggers);

				if (logger.isInfoEnabled()) {
					// logger.info(plc);
				}
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return plcs;
	}

	public List<PlcTrigger> getPlcTrigger(PreparedStatement psData, int plcId) throws DAOException {

		ResultSet rs = null;
		PlcTrigger data = null;

		List<PlcTrigger> plcTriggers = new ArrayList<>();

		try {
			psData.setInt(1, plcId);
			rs = psData.executeQuery();

			while (rs.next()) {
				data = new PlcTrigger();
				data.setId(rs.getInt("id"));
				data.setDb(rs.getInt("db"));
				data.setStrtAdr(rs.getInt("strt_adr"));
				data.setActivated(rs.getBoolean("activated"));
				data.setPlcId(rs.getInt("plc_id"));

				String ts = rs.getString("timestamp");
				data.setTimestamp(ts.subSequence(0, 19).toString());

				plcTriggers.add(data);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return plcTriggers;

	}

	@Override
	public Plc getPlc(int id) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		Statement statement = null;
		Connection con = null;

		Plc plc = null;

		try {

			con = ConnectionManager.getInstance().getConnection();

			ps = con.prepareStatement(SELECT_PLC);

			ps.setInt(1, id);

			rs = ps.executeQuery();

			while (rs.next()) {
				plc = new Plc();
				plc.setId(rs.getInt("id"));
				plc.setName(rs.getString("name"));
				plc.setIp(rs.getString("ip"));
				plc.setRack(rs.getInt("rack"));
				plc.setSlot(rs.getInt("slot"));
				plc.setType(rs.getInt("type"));
				plc.setTimeout(rs.getInt("timeout"));

				String ts = rs.getString("timestamp");
				plc.setTimestamp(ts.subSequence(0, 19).toString());

			}

			if (logger.isInfoEnabled()) {
				// logger.info(plc);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return plc;
	}

	@Override
	public boolean updatePlc(Plc plc) throws DAOException {
		int res;
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(UPDATE_PLC);

			ps.setString(1, plc.getName());
			ps.setString(2, plc.getIp());
			ps.setInt(3, plc.getRack());
			ps.setInt(4, plc.getSlot());
			ps.setInt(5, plc.getType());
			ps.setInt(6, plc.getTimeout());
			ps.setInt(7, plc.getId());
			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(plc);
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
	public boolean insertPlc(Plc plc) throws DAOException {
		int res;
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(INSERT_PLC);

			ps.setString(1, plc.getName());
			ps.setString(2, plc.getIp());
			ps.setInt(3, plc.getRack());
			ps.setInt(4, plc.getSlot());
			ps.setInt(5, plc.getType());
			ps.setInt(6, plc.getTimeout());
			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(plc);
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
	public boolean deletePlc(int id) throws DAOException {
		int res = 0;

		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(DELETE_PLC);

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