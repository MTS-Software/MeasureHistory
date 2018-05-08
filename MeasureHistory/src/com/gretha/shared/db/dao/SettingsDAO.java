package com.gretha.shared.db.dao;

import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.model.Settings;

/**
 * Interface zum Datenaustausch
 * 
 * @author Markus Thaler, Ing.
 */
public interface SettingsDAO {

	public Settings getSettings() throws DAOException;

	public boolean updateSettings(Settings settings) throws DAOException;

}
