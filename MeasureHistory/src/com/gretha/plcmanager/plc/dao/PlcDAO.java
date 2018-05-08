package com.gretha.plcmanager.plc.dao;

import com.gretha.shared.db.util.DAOException;

public interface PlcDAO {

	public boolean readTriggerBit(int dbNr, int startByte, int bitNr, String itemName) throws DAOException;

	public int readInt(int db, int startByte) throws DAOException;

	public float readReal(int db, int startByte) throws DAOException;

	public String readS7String(int db, int startByte, int length) throws DAOException;

	public void writeTriggerBit(int db, int startByte, int bitNr, boolean value, String itemName) throws DAOException;

}
