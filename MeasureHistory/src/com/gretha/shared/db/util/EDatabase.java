package com.gretha.shared.db.util;

/**
 * Enumeration zur Selektion der Datenbank
 * 
 * @author Markus Thaler, Ing.
 */
public enum EDatabase {

	MYSQL("Oracle MySQL", "mysql"), SQLSERVER("Microsoft SQL", "sqlserver");

	private String label;
	private String vendor;

	EDatabase(String label, String vendor) {
		this.label = label;
		this.vendor = vendor;
	}

	public String getVendor() {
		return vendor;
	}

	@Override
	public String toString() {
		return label;
	}
}
