package com.gretha.shared.db.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.gretha.shared.db.util.ConnectionManager;
import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Result;
import com.gretha.shared.model.Unit;
import com.gretha.shared.util.Constants;

public class ResultJDBCDAO implements ResultDAO {

	private static final Logger logger = Logger.getLogger(ResultJDBCDAO.class);

	private final static String INSERT = "INSERT INTO result(serial, typ_nr, wt_nr, remark, value, lolim, uplim, state, process_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private final static String DELETE_RESULT = "DELETE FROM result where id = ?";
	private final static String UPDATE_RESULT = "UPDATE result SET serial = ?, typ_nr = ?, wt_nr = ?, remark = ?, value = ?, lolim = ?, uplim = ?, state = ?  WHERE id = ?";

	private final static String SELECT_RESULT_BY_PROCESS = "SELECT * FROM result where process_id = ? order by id desc LIMIT ?";
	private final static String SELECT_MIN = "SELECT MIN(value)FROM (SELECT * FROM result where process_id = ? order by id desc LIMIT ?) items";
	private final static String SELECT_MAX = "SELECT MAX(value)FROM (SELECT * FROM result where process_id = ? order by id desc LIMIT ?) items";
	private final static String SELECT_MITTELWERT = "SELECT AVG(value)FROM (SELECT * FROM result where process_id = ? order by id desc LIMIT ?) items";
	private final static String SELECT_OK = "SELECT COUNT(*) FROM (select * from result where process_id = ? order by id desc limit ?) as r2 where r2.state = 2";
	private final static String SELECT_NOK = "SELECT COUNT(*) FROM (select * from result where process_id = ? order by id desc limit ?) as r2 where r2.state = 1";
	private final static String SELECT_UNBEWERTET = "SELECT COUNT(*) FROM (select * from result where process_id = ? order by id desc limit ?) as r2 where r2.state = 0";
	private final static String SELECT_UNDEFINIERT = "SELECT COUNT(*) FROM (select * from result where process_id = ? order by id desc limit ?) as r2 where r2.state < 0 OR r2.state > 2";
	private final static String SELECT_OVERALL = "SELECT * FROM result where process_id = ? order by id desc LIMIT ?";
	private final static String SELECT_FILTER_DATE_TIME = "SELECT * from result where process_id = ? and timestamp between ? and ? ORDER BY TIMESTAMP DESC LIMIT ?";
	private final static String SELECT_FILTER_LAST_N_FROM_DATE_WITH_STATE = "SELECT * from result where process_id = ? and timestamp <= ? and state = ? ORDER BY TIMESTAMP DESC LIMIT ?";
	private final static String SELECT_FILTER_LAST_N_FROM_DATE_WITH_STATE_IS_UNDEF = "SELECT * from result where process_id = ? and timestamp <= ? and state > ? ORDER BY TIMESTAMP DESC LIMIT ?";
	private final static String SELECT_FILTER_LAST_N_FROM_DATE_WITHOUT_STATE = "SELECT * from result where process_id = ? and timestamp <= ? ORDER BY TIMESTAMP DESC LIMIT ?";
	private final static String SELECT_FILTER_FIRST_N_FROM_DATE_WITH_STATE = "SELECT * from (select * from result where process_id = ? and timestamp >= ? and state = ? order by timestamp ASC LIMIT ?) AS TMP order by timestamp DESC";
	private final static String SELECT_FILTER_FIRST_N_FROM_DATE_WITH_STATE_IS_UNDEF = "SELECT * from (select * from result where process_id = ? and timestamp >= ? and state > ? order by timestamp ASC LIMIT ?) AS TMP order by timestamp DESC";
	private final static String SELECT_FILTER_FIRST_N_FROM_DATE_WITHOUT_STATE = "SELECT * from (select * from result where process_id = ? and timestamp >= ? order by timestamp ASC LIMIT ?) AS TMP order by timestamp DESC";
	private final static String SELECT_LAST_X_WITH_STATE = "SELECT * FROM result where state = ? order by timestamp desc LIMIT ?";
	private final static String SELECT_LAST_X_WITHOUT_STATE = "SELECT * FROM result order by timestamp desc LIMIT ?";
	private final static String SELECT_PROCESS = "SELECT * FROM process where id = ?";
	private final static String SELECT_UNIT = "SELECT * FROM unit where id = ?";
	private final static String SELECT_PLCTRIGGER = "SELECT * FROM plctrigger where id = ?";

	@Override
	public List<Result> getResults(Process process) throws DAOException {

		ResultSet rs = null;
		PreparedStatement ps = null;
		List<Result> results = new ArrayList<Result>();
		String stmt = null;

		stmt = SELECT_RESULT_BY_PROCESS;

		int nr = 1;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, process.getId());
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();
			while (rs.next()) {
				Result result = new Result();
				result.setId(rs.getInt("id"));
				result.setNr(nr++);
				result.setTypId(rs.getInt("typ_nr"));
				result.setValue(rs.getFloat("value"));
				result.setLoLim(rs.getFloat("lolim"));
				result.setUpLim(rs.getFloat("uplim"));
				result.setState(rs.getInt("state"));
				result.setSerial(rs.getString("serial"));
				result.setWt(rs.getString("wt_nr"));
				result.setRemark(rs.getString("remark"));

				String ts = rs.getString("timestamp");
				result.setTimestamp(ts.subSequence(0, 19).toString());
				result.setTimestampSql(rs.getTimestamp("timestamp"));

				results.add(result);

			}
			process.setResults(results);
			if (logger.isInfoEnabled()) {
				// logger.info(results);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return results;

	}

	@Override
	public List<Result> getResultsFilterDateTime(Process process, Timestamp start, Timestamp end) throws DAOException {

		ResultSet rs = null;
		PreparedStatement ps = null;
		List<Result> results = new ArrayList<Result>();
		String stmt = null;

		stmt = SELECT_FILTER_DATE_TIME;

		int nr = 1;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, process.getId());
			ps.setTimestamp(2, start);
			ps.setTimestamp(3, end);
			ps.setInt(4, Constants.LIMIT_RESULTS_FILTER_SHOWN);

			rs = ps.executeQuery();
			while (rs.next()) {
				Result result = new Result();
				result.setId(rs.getInt("id"));
				result.setNr(nr++);
				result.setTypId(rs.getInt("typ_nr"));
				result.setValue(rs.getFloat("value"));
				result.setLoLim(rs.getFloat("lolim"));
				result.setUpLim(rs.getFloat("uplim"));
				result.setState(rs.getInt("state"));
				result.setSerial(rs.getString("serial"));
				result.setWt(rs.getString("wt_nr"));
				result.setRemark(rs.getString("remark"));

				String ts = rs.getString("timestamp");
				result.setTimestamp(ts.subSequence(0, 19).toString());
				result.setTimestampSql(rs.getTimestamp("timestamp"));

				results.add(result);

			}
			process.setResults(results);
			if (logger.isInfoEnabled()) {
				// logger.info(results);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return results;

	}

	@Override
	public List<Result> getResultsFilterLastNfromDate(Process process, Timestamp end, int limit, int state)
			throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		List<Result> results = new ArrayList<Result>();
		String stmt = null;

		int nr = 1;

		if (state == -1)
			stmt = SELECT_FILTER_LAST_N_FROM_DATE_WITHOUT_STATE;

		else if (state == -2)
			stmt = SELECT_FILTER_LAST_N_FROM_DATE_WITH_STATE_IS_UNDEF;

		else
			stmt = SELECT_FILTER_LAST_N_FROM_DATE_WITH_STATE;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			if (state == -1) {
				ps.setInt(1, process.getId());
				ps.setTimestamp(2, end);
				ps.setInt(3, limit);
			}

			else if (state == -2) {
				ps.setInt(1, process.getId());
				ps.setTimestamp(2, end);
				ps.setInt(3, 2);
				ps.setInt(4, limit);
			}

			else {
				ps.setInt(1, process.getId());
				ps.setTimestamp(2, end);
				ps.setInt(3, state);
				ps.setInt(4, limit);
			}

			rs = ps.executeQuery();
			while (rs.next()) {
				Result result = new Result();
				result.setId(rs.getInt("id"));
				result.setNr(nr++);
				result.setTypId(rs.getInt("typ_nr"));
				result.setValue(rs.getFloat("value"));
				result.setLoLim(rs.getFloat("lolim"));
				result.setUpLim(rs.getFloat("uplim"));
				result.setState(rs.getInt("state"));
				result.setSerial(rs.getString("serial"));
				result.setWt(rs.getString("wt_nr"));
				result.setRemark(rs.getString("remark"));

				String ts = rs.getString("timestamp");
				result.setTimestamp(ts.subSequence(0, 19).toString());
				result.setTimestampSql(rs.getTimestamp("timestamp"));

				results.add(result);

			}
			process.setResults(results);
			if (logger.isInfoEnabled()) {
				// logger.info(results);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return results;
	}

	@Override
	public List<Result> getResultsFilterFirstNsinceDate(Process process, Timestamp strt, int limit, int state)
			throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		List<Result> results = new ArrayList<Result>();
		String stmt = null;

		int nr = 1;

		if (state == -1)
			stmt = SELECT_FILTER_FIRST_N_FROM_DATE_WITHOUT_STATE;

		else if (state == -2)
			stmt = SELECT_FILTER_FIRST_N_FROM_DATE_WITH_STATE_IS_UNDEF;

		else
			stmt = SELECT_FILTER_FIRST_N_FROM_DATE_WITH_STATE;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			if (state == -1) {
				ps.setInt(1, process.getId());
				ps.setTimestamp(2, strt);
				ps.setInt(3, limit);
			}

			else if (state == -2) {
				ps.setInt(1, process.getId());
				ps.setTimestamp(2, strt);
				ps.setInt(3, 2);
				ps.setInt(4, limit);
			}

			else {
				ps.setInt(1, process.getId());
				ps.setTimestamp(2, strt);
				ps.setInt(3, state);
				ps.setInt(4, limit);
			}

			rs = ps.executeQuery();
			while (rs.next()) {
				Result result = new Result();
				result.setId(rs.getInt("id"));
				result.setNr(nr++);
				result.setTypId(rs.getInt("typ_nr"));
				result.setValue(rs.getFloat("value"));
				result.setLoLim(rs.getFloat("lolim"));
				result.setUpLim(rs.getFloat("uplim"));
				result.setState(rs.getInt("state"));
				result.setSerial(rs.getString("serial"));
				result.setWt(rs.getString("wt_nr"));
				result.setRemark(rs.getString("remark"));

				String ts = rs.getString("timestamp");
				result.setTimestamp(ts.subSequence(0, 19).toString());
				result.setTimestampSql(rs.getTimestamp("timestamp"));

				results.add(result);

			}
			process.setResults(results);
			if (logger.isInfoEnabled()) {
				// logger.info(results);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return results;
	}

	@Override
	public boolean insert(Result result) throws DAOException {
		PreparedStatement ps;
		int res = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(INSERT);

			ps.setString(1, result.getSerial());
			ps.setInt(2, result.getTypId());
			ps.setString(3, result.getWt());
			ps.setString(4, result.getRemark());
			ps.setFloat(5, result.getValue());
			ps.setFloat(6, result.getLoLim());
			ps.setFloat(7, result.getUpLim());
			ps.setInt(8, result.getState());
			ps.setInt(9, result.getProcessId());

			res = ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(result);
			}

		}

		catch (Exception e) {
			e.printStackTrace();
			logger.error(e);

		}

		if (res > 0)
			return true;
		else
			return false;

	}

	@Override
	public Result getResult(int id) throws DAOException {
		return null;
	}

	@Override
	public void update(Result result) throws DAOException {
		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(UPDATE_RESULT);

			ps.setString(1, result.getSerial());
			ps.setInt(2, result.getTypId());
			ps.setString(3, result.getWt());
			ps.setString(4, result.getRemark());
			ps.setFloat(5, result.getValue());
			ps.setFloat(6, result.getLoLim());
			ps.setFloat(7, result.getUpLim());
			ps.setInt(8, result.getState());
			ps.setInt(9, result.getId());
			ps.executeUpdate();

			if (logger.isInfoEnabled()) {
				// logger.info(result);
			}

		}

		catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e);
		}

	}

	@Override
	public boolean delete(Result result) throws DAOException {

		int res = 0;

		try {

			PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(DELETE_RESULT);

			ps.setInt(1, result.getId());
			res = ps.executeUpdate();

		}

		catch (SQLException e) {
			e.printStackTrace();
			throw new DAOException(e);
		}

		if (res > 0)
			return true;
		else
			return false;

	}

	@Override
	public float getMin(int processId) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;

		String stmt = null;

		stmt = SELECT_MIN;

		float min = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, processId);
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();

			while (rs.next()) {
				min = rs.getFloat(1);
			}

			if (logger.isInfoEnabled()) {
				// logger.info(min);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return min;
	}

	@Override
	public float getMax(int processId) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		String stmt = null;

		stmt = SELECT_MAX;

		float max = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, processId);
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();

			while (rs.next()) {
				max = rs.getFloat(1);
			}

			if (logger.isInfoEnabled()) {
				// logger.info(max);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return max;
	}

	@Override
	public float getMittelwert(int processId) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		String stmt = null;

		stmt = SELECT_MITTELWERT;

		float mw = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, processId);
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();

			while (rs.next()) {
				mw = rs.getFloat(1);
			}

			if (logger.isInfoEnabled()) {
				// logger.info(mw);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return mw;
	}

	@Override
	public int getOk(int processId) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		String stmt = null;

		stmt = SELECT_OK;

		int cnt = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, processId);
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();

			while (rs.next()) {
				cnt = rs.getInt(1);
			}

			if (logger.isInfoEnabled()) {
				// logger.info(cnt);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return cnt;
	}

	@Override
	public int getNok(int processId) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		String stmt = null;

		stmt = SELECT_NOK;

		int cnt = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, processId);
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();

			while (rs.next()) {
				cnt = rs.getInt(1);
			}

			if (logger.isInfoEnabled()) {
				// logger.info(cnt);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return cnt;
	}

	@Override
	public int getUnbewertet(int processId) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		String stmt = null;

		stmt = SELECT_UNBEWERTET;

		int cnt = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, processId);
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();

			while (rs.next()) {
				cnt = rs.getInt(1);
			}

			if (logger.isInfoEnabled()) {
				// logger.info(cnt);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return cnt;
	}

	@Override
	public int getUndefiniert(int processId) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		String stmt = null;

		stmt = SELECT_UNDEFINIERT;

		int cnt = 0;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, processId);
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();

			while (rs.next()) {
				cnt = rs.getInt(1);
			}

			if (logger.isInfoEnabled()) {
				// logger.info(cnt);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return cnt;
	}

	@Override
	public int[] getOverall(int processId) throws DAOException {

		ResultSet rs = null;
		PreparedStatement ps = null;
		String stmt = null;
		int ok = 0;
		int nok = 0;
		int unbewertet = 0;
		int undefiniert = 0;
		int gesamt = 0;

		stmt = SELECT_OVERALL;

		try {
			ps = ConnectionManager.getInstance().getConnection().prepareStatement(stmt);

			ps.setInt(1, processId);
			ps.setInt(2, Constants.LIMIT_RESULTS_STANDARD_SHOWN);

			rs = ps.executeQuery();
			while (rs.next()) {

				if (rs.getInt("state") == 0)
					unbewertet++;

				if (rs.getInt("state") == 1)
					nok++;

				if (rs.getInt("state") == 2)
					ok++;

				if (rs.getInt("state") < 0 || rs.getInt("state") > 2)
					undefiniert++;

				gesamt = unbewertet + ok + nok + undefiniert;

			}

			if (logger.isInfoEnabled()) {
				// logger.info("OK: " + ok + " NOK: " + nok + " UNBEWERTET: " +
				// unbewertet + " UNDEFINIERT: " + undefiniert
				// + " GESAMT: " + gesamt);

			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return new int[] { ok, nok, unbewertet, undefiniert, gesamt };

	}

	@Override
	public List<Result> getLastXResults(int anzahl, int state) throws DAOException {
		ResultSet rs = null;
		PreparedStatement ps = null;
		PreparedStatement psProcess = null;
		List<Result> results = new ArrayList<Result>();
		String stmt = null;
		Connection con = null;

		int nr = 1;

		// State -1 shows last x result without state
		if (state == -1)
			stmt = SELECT_LAST_X_WITHOUT_STATE;
		else
			stmt = SELECT_LAST_X_WITH_STATE;

		try {
			con = ConnectionManager.getInstance().getConnection();

			ps = con.prepareStatement(stmt);
			psProcess = con.prepareStatement(SELECT_PROCESS);

			if (state == -1)
				ps.setInt(1, anzahl);
			else {
				ps.setInt(1, state);
				ps.setInt(2, anzahl);
			}

			rs = ps.executeQuery();
			while (rs.next()) {
				Result result = new Result();
				result.setId(rs.getInt("id"));
				result.setNr(nr++);
				result.setTypId(rs.getInt("typ_nr"));
				result.setValue(rs.getFloat("value"));
				result.setLoLim(rs.getFloat("lolim"));
				result.setUpLim(rs.getFloat("uplim"));
				result.setState(rs.getInt("state"));
				result.setSerial(rs.getString("serial"));
				result.setWt(rs.getString("wt_nr"));
				result.setRemark(rs.getString("remark"));

				String ts = rs.getString("timestamp");
				result.setTimestamp(ts.subSequence(0, 19).toString());
				result.setTimestampSql(rs.getTimestamp("timestamp"));

				result.setProcessId(rs.getInt("process_id"));

				// Process
				Process process = getProcess(psProcess, result.getProcessId());
				result.setProcess(process);

				results.add(result);

			}

			if (logger.isInfoEnabled()) {
				// logger.info(results);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return results;

	}

	public Process getProcess(PreparedStatement psData, int processId) throws DAOException {

		ResultSet rs = null;
		Process data = null;
		PreparedStatement psUnit = null;
		PreparedStatement psPlcTrigger = null;
		Connection con = null;

		try {

			con = ConnectionManager.getInstance().getConnection();

			psData.setInt(1, processId);
			rs = psData.executeQuery();
			psUnit = con.prepareStatement(SELECT_UNIT);
			psPlcTrigger = con.prepareStatement(SELECT_PLCTRIGGER);

			while (rs.next()) {
				data = new Process();
				data.setId(rs.getInt("id"));
				data.setName(rs.getString("name"));
				data.setStation(rs.getString("station"));
				data.setSetvalue(rs.getFloat("setvalue"));
				data.setSetvalueUsed(rs.getBoolean("setvalue_used"));
				data.setDecimalPoints(rs.getInt("decpoints"));
				data.setNrAvg(rs.getInt("nr_avg"));
				data.setNrSpcClass(rs.getInt("nr_spc_class"));
				data.setCpkLoLim1(rs.getFloat("cpk_lolim1"));
				data.setCpkLoLim2(rs.getFloat("cpk_lolim2"));
				data.setUnitId(rs.getInt("unit_Id"));
				data.setPlcTriggerId(rs.getInt("plctrigger_Id"));

				String ts = rs.getString("timestamp");
				data.setTimestamp(ts.subSequence(0, 19).toString());

				Unit unit = getUnit(psUnit, data.getUnitId());
				data.setUnit(unit);

				PlcTrigger plcTrigger = getPlcTrigger(psPlcTrigger, data.getPlcTriggerId());
				data.setPlcTrigger(plcTrigger);

			}

			if (logger.isInfoEnabled()) {
				// logger.info(data);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return data;

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

			if (logger.isInfoEnabled()) {
				// logger.info(plcTrigger);
			}

		} catch (SQLException e) {

			throw new DAOException(e);
		}

		return plcTrigger;

	}
}