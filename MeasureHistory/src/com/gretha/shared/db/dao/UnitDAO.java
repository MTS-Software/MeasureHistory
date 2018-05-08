package com.gretha.shared.db.dao;

import java.util.List;

import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.Unit;

/**
 * Interface zum Datenaustausch
 * 
 * @author Markus Thaler, Ing.
 */
public interface UnitDAO {

	public Unit getUnit(int id) throws DAOException;

	public List<Unit> getUnits() throws DAOException;

	public boolean updateUnit(Unit unit) throws DAOException;

	public boolean insertUnit(Unit unit) throws DAOException;

	public boolean deleteUnit(int id) throws DAOException;

}