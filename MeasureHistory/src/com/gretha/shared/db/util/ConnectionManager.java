package com.gretha.shared.db.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.gretha.shared.util.ApplicationProperties;

/**
 * Verwaltung der Datenbankverbindung
 * 
 * @author Markus Thaler, Ing.
 */
public class ConnectionManager {

	private static final Logger logger = Logger.getLogger(ConnectionManager.class);

	public static ConnectionManager getInstance() {
		if (instance == null)
			instance = new ConnectionManager();
		return instance;
	}

	private static ConnectionManager instance;

	private Connection connection;
	private Properties properties;

	public Connection getConnection() throws SQLException {

		String url = null;

		if (Service.database == EDatabase.MYSQL) {
			try {
				Class.forName("com.mysql.jdbc.Driver");
				url = "jdbc:" + ApplicationProperties.getInstance().getProperty("db_mysql_url") + "://"
						+ ApplicationProperties.getInstance().getProperty("db_host") + ":"
						+ ApplicationProperties.getInstance().getProperty("db_mysql_port") + "/"
						+ ApplicationProperties.getInstance().getProperty("db_model");

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (Service.database == EDatabase.SQLSERVER) {
			try {
				Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
				url = "jdbc:" + ApplicationProperties.getInstance().getProperty("db_sqlserver_url") + "://"
						+ ApplicationProperties.getInstance().getProperty("db_host") + "\\"
						+ ApplicationProperties.getInstance().getProperty("db_sqlserver_instance") + ";DatabaseName="
						+ ApplicationProperties.getInstance().getProperty("db_model");

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if (connection == null || connection.isClosed())
			connection = DriverManager.getConnection(url, getProperties());

		// if (connection != null) {
		// DatabaseMetaData dm = connection.getMetaData();
		// logger.info("Driver name: " + dm.getDriverName());
		// logger.info("Driver version: " + dm.getDriverVersion());
		// logger.info("Product name: " + dm.getDatabaseProductName());
		// logger.info("Product version: " + dm.getDatabaseProductVersion());
		// logger.info(dm.getURL());
		// }

		return connection;
	}

	private Properties getProperties() {

		if (properties == null) {
			properties = new Properties();

			if (Service.database == EDatabase.MYSQL) {

				properties.setProperty("user", ApplicationProperties.getInstance().getProperty("db_mysql_user"));
				properties.setProperty("password",
						ApplicationProperties.getInstance().getProperty("db_mysql_password"));
				properties.setProperty("useSSL", "false");
				properties.setProperty("autoReconnect", "true");
			}

			if (Service.database == EDatabase.SQLSERVER) {

				properties.setProperty("user", ApplicationProperties.getInstance().getProperty("db_sqlserver_user"));
				properties.setProperty("password",
						ApplicationProperties.getInstance().getProperty("db_sqlserver_password"));
			}

		}

		return properties;
	}
}
