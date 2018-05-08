package com.gretha.shared.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.gretha.shared.db.util.ConnectionManager;
import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.Settings;

public class SettingsJDBCDAO implements SettingsDAO {

	private static final Logger logger = Logger.getLogger(SettingsJDBCDAO.class);

	private final static String SELECT_SETTINGS = "SELECT * FROM settings";
	private final static String UPDATE_SETTINGS = "UPDATE settings SET shift1_strt_time = ?, shift1_end_time = ?, shift2_strt_time = ?, shift2_end_time = ?, shift3_strt_time = ?, shift3_end_time = ?  WHERE id = ?";

	@Override
	public Settings getSettings() throws DAOException {

		ResultSet rs = null;
		Statement statement = null;
		Connection con = null;

		Settings settings = new Settings();

		try {
			con = ConnectionManager.getInstance().getConnection();

			statement = con.createStatement();
			rs = statement.executeQuery(SELECT_SETTINGS);

			while (rs.next()) {
				settings.setId(rs.getInt("id"));
				settings.setShift1StartTime(rs.getString("shift1_strt_time"));
				settings.setShift1EndTime(rs.getString("shift1_end_time"));
				settings.setShift2StartTime(rs.getString("shift2_strt_time"));
				settings.setShift2EndTime(rs.getString("shift2_end_time"));
				settings.setShift3StartTime(rs.getString("shift3_strt_time"));
				settings.setShift3EndTime(rs.getString("shift3_end_time"));

				String ts = rs.getString("timestamp");
				settings.setTimestamp(ts.subSequence(0, 19).toString());

				if (logger.isInfoEnabled()) {
					// logger.info(settings);
				}
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return settings;
	}

	@Override
	public boolean updateSettings(Settings setting) throws DAOException {
		int res;
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(UPDATE_SETTINGS);

			ps.setString(1, setting.getShift1StartTime());
			ps.setString(2, setting.getShift1EndTime());
			ps.setString(3, setting.getShift2StartTime());
			ps.setString(4, setting.getShift2EndTime());
			ps.setString(5, setting.getShift3StartTime());
			ps.setString(6, setting.getShift3EndTime());
			ps.setInt(7, setting.getId());
			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(setting);
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
}