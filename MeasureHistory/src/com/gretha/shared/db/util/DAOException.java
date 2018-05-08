package com.gretha.shared.db.util;

import java.sql.SQLException;

/**
 * Exceptionhandling zum Datenaustausch
 * 
 * @author Markus Thaler, Ing.
 */
public class DAOException extends Exception {

	private static final long serialVersionUID = 1L;

	public DAOException(SQLException e) {
		super(e);

	}

	public DAOException(String message) {
		super(message);
	}

}
