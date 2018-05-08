package com.gretha.shared.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Markus Thaler
 *
 */
public class ApplicationProperties {
	private String fileNameDefaultProperties;
	private String dirName;
	private String fileName;

	private Properties defaultProperties;
	private Properties applicationProperties;

	private File file;

	private static ApplicationProperties instance;

	/**
	 * Verwaltet eine Properties Datei.
	 * <p>
	 * 
	 * @param fileNameDefaultProperties
	 *            Name der DefaultProperties Datei
	 * 
	 * @param dirName
	 *            Verzeichnisname
	 * 
	 * @param fileName
	 *            Dateiname
	 */
	private ApplicationProperties(String fileNameDefaultProperties, String dirName, String fileName) {

		this.fileNameDefaultProperties = fileNameDefaultProperties;
		this.dirName = dirName;
		this.fileName = fileName;

	}

	/**
	 * @param fileNameDefaultProperties
	 * @param dirName
	 * @param fileName
	 */
	public static void configure(String fileNameDefaultProperties, String dirName, String fileName) {
		if (instance == null)
			instance = new ApplicationProperties(fileNameDefaultProperties, dirName, fileName);

	}

	/**
	 * @return
	 */
	public static ApplicationProperties getInstance() {

		return instance;

	}

	/**
	 * Properties aus Datei laden und Default Properties überschreiben <br>
	 * 
	 * @return true wenn die Properties fehlerfrei geladen wurden, sonst false
	 */
	public boolean setup() {

		FileInputStream in;

		// Lade Default Properties
		defaultProperties = new Properties();

		try {

			defaultProperties.load(getClass().getResourceAsStream("/" + fileNameDefaultProperties));

		} catch (FileNotFoundException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}

		// create application properties with default
		applicationProperties = new Properties(defaultProperties);

		// now load properties
		// from last invocation
		try {
			createFiles();

			in = new FileInputStream(file);

			applicationProperties.load(in);
			// Damit man im propertie file backslash setzen kann, noch zum
			// testen
			// applicationProperties.load(new
			// StringReader(in.toString().replace("\\", "\\\\")));
			in.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * Erzeuge Verzeichnis und Datei
	 */
	private void createFiles() throws IOException {

		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdir();

		file = new File(dirName + File.separator + fileName);
		if (!file.exists()) {
			file.createNewFile();
			save(defaultProperties);

		}

	}

	/**
	 * geladene Properties bearbeiten oder <br>
	 * neue hinzufügen
	 */
	public void edit(String key, String value) {

		applicationProperties.setProperty(key, value);

	}

	/**
	 * Properties in File speichern
	 * 
	 * @return true wenn die Properties fehlerfrei gespeichert wurden, sonst false
	 */
	public boolean save() {
		FileOutputStream out;
		try {
			out = new FileOutputStream(file);
			applicationProperties.store(out, "---No Comment---");
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * Speichere Default Properties in Properties File
	 * 
	 * @param properties
	 * @return
	 */
	private boolean save(Properties properties) {

		FileOutputStream out;

		try {
			out = new FileOutputStream(file);
			properties.store(out, "---No Comment---");
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Automatisch generierter Erfassungsblock
			e.printStackTrace();
		}
		return false;

	}

	public String getProperty(String key) {

		return applicationProperties.getProperty(key);

	}

}
