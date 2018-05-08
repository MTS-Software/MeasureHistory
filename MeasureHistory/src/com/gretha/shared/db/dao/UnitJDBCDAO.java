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
import com.gretha.shared.model.Unit;

public class UnitJDBCDAO implements UnitDAO {

	private static final Logger logger = Logger.getLogger(UnitJDBCDAO.class);

	private final static String SELECT_UNITS = "SELECT * FROM unit";
	private final static String INSERT_UNIT = "INSERT INTO unit(sign) VALUES (?)";
	private final static String UPDATE_UNIT = "UPDATE unit SET sign = ?  WHERE id = ?";
	private final static String SELECT_UNIT = "SELECT * FROM unit where id = ?";
	private final static String DELETE_UNIT = "DELETE FROM unit where id = ?";

	private final static String SELECT_LAST_ID = "select last_insert_id()";

	public Integer selectLastID() throws DAOException {

		Integer lastId = null;
		String stmt = null;

		try {

			stmt = SELECT_LAST_ID;

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);
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
	public Unit getUnit(int id) throws DAOException {

		ResultSet rs = null;
		PreparedStatement ps = null;

		Unit unit = null;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(SELECT_UNIT);
			ps.setInt(1, id);

			rs = ps.executeQuery();
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

	@Override
	public boolean insertUnit(Unit unit) throws DAOException {
		PreparedStatement ps;
		int res = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(INSERT_UNIT);

			ps.setString(1, unit.getSign());

			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(unit);
			}

		}

		catch (Exception e) {
			e.printStackTrace();
			logger.error(e);

		}

		if (res > 0) {
			unit.setId(selectLastID());
			return true;

		} else
			return false;

	}

	@Override
	public boolean updateUnit(Unit unit) throws DAOException {

		int res = 0;
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(UPDATE_UNIT);

			ps.setString(1, unit.getSign());
			ps.setInt(2, unit.getId());

			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(unit);
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
	public boolean deleteUnit(int id) throws DAOException {

		int res = 0;

		try {
			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(DELETE_UNIT);

			ps.setInt(1, id);
			res = ps.executeUpdate();
		}

		catch (SQLException e) {
			e.printStackTrace();

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

	@Override
	public List<Unit> getUnits() throws DAOException {
		ResultSet rs = null;
		Statement statement = null;
		Connection con = null;

		List<Unit> units = new ArrayList<>();

		try {
			con = ConnectionManager.getInstance().getConnection();

			statement = con.createStatement();
			rs = statement.executeQuery(SELECT_UNITS);

			while (rs.next()) {
				Unit unit = new Unit();
				unit.setId(rs.getInt("id"));
				unit.setSign(rs.getString("sign"));

				String ts = rs.getString("timestamp");
				unit.setTimestamp(ts.subSequence(0, 19).toString());

				units.add(unit);
				if (logger.isInfoEnabled()) {
					// logger.info(unit);
				}
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return units;
	}

}