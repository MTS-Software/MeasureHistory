package com.gretha.shared.db.dao;

import java.util.List;

import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.Plc;

/**
 * Interface zum Datenaustausch
 * 
 * @author Markus Thaler, Ing.
 */
public interface PlcDAO {

	public List<Plc> getPlcs() throws DAOException;

	public Plc getPlc(int id) throws DAOException;

	public boolean updatePlc(Plc plc) throws DAOException;

	public boolean insertPlc(Plc plc) throws DAOException;

	public boolean deletePlc(int id) throws DAOException;

}
