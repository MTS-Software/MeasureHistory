package com.gretha.shared.db.dao;

import java.util.List;

import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.PlcTrigger;

public interface PlcTriggerDAO {

	public PlcTrigger getPlcTrigger(int id) throws DAOException;

	public List<PlcTrigger> getPlcTriggers() throws DAOException;

	public boolean updatePlcTrigger(PlcTrigger plctrigger) throws DAOException;

	public boolean insertPlcTrigger(PlcTrigger plctrigger) throws DAOException;

	public boolean deletePlcTrigger(int id) throws DAOException;

}
