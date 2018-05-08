package com.gretha.shared.util;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * ProgramRestart (https://dzone.com/articles/programmatically-restart-java)
 *
 */
public class ProgramRestart {
	private static final Logger logger = Logger.getLogger(ProgramRestart.class);

	/**
	 * Sun property pointing the main class and its arguments. Might not be defined
	 * on non Hotspot VM implementations.
	 */
	public static final String SUN_JAVA_COMMAND = "sun.java.command";

	/**
	 * Restart the current Java application without asking if you really want
	 * 
	 * @param runBeforeRestart
	 *            some custom code to be run before restarting
	 * @throws IOException
	 */
	public static void restartApplicationWithoutDialog() throws IOException {

		try {
			// java binary
			String java = System.getProperty("java.home") + "/bin/java";
			// vm arguments
			List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
			StringBuffer vmArgsOneLine = new StringBuffer();
			for (String arg : vmArguments) {
				// if it's the agent argument : we ignore it otherwise the
				// address of the old application and the new one will be in
				// conflict
				if (!arg.contains("-agentlib")) {
					vmArgsOneLine.append(arg);
					vmArgsOneLine.append(" ");
				}
			}
			// init the command to execute, add the vm args
			final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);

			// program main and program arguments
			String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
			// program main is a jar
			if (mainCommand[0].endsWith(".jar")) {
				// if it's a jar, add -jar mainJar
				cmd.append("-jar " + new File(mainCommand[0]).getPath());
			} else {
				// else it's a .class, add the classpath and mainClass
				cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
			}
			// finally add program arguments
			for (int i = 1; i < mainCommand.length; i++) {
				cmd.append(" ");
				cmd.append(mainCommand[i]);
			}
			// execute the command in a shutdown hook, to be sure that all the
			// resources have been disposed before restarting the application
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					try {
						Runtime.getRuntime().exec(cmd.toString());

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			// exit
			logger.info("Programm vor Neustart beenden");
			System.exit(0);

		} catch (Exception e) {
			// something went wrong
			throw new IOException("Error while trying to restart the application", e);
		}
	}

	/**
	 * Restart the current Java application with asking if you really want
	 * 
	 * @param runBeforeRestart
	 *            some custom code to be run before restarting
	 * @throws IOException
	 */
	public static void restartApplicationWithDialog(Runnable runBeforeRestart) {

		if (showRestartDialog()) {
			try {
				restartApplicationWithoutDialog();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Only shows the restart ask
	 *
	 * @return btnOK
	 */
	public static boolean showRestartDialog() {

		boolean btnOK = false;

		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Programm neu starten");
		alert.setHeaderText("Wollen Sie das Programm wirklich neu starten?");
		alert.setContentText("");

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(Constants.ICON_RESTART));

		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			btnOK = true;
		} else {
			btnOK = false;
		}

		return btnOK;
	}
}
