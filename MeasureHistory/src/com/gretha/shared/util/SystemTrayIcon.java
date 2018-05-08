package com.gretha.shared.util;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import org.apache.log4j.Logger;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class SystemTrayIcon {

	private static String appName;
	private static TrayIcon trayIcon;

	private static final Logger logger = Logger.getLogger(SystemTrayIcon.class);

	public static void setTrayIcon(Stage primaryStage, String appName, URL file) {

		SystemTrayIcon.appName = appName;

		SystemTray sTray = null;
		sTray = SystemTray.getSystemTray();

		Image image = Toolkit.getDefaultToolkit().getImage(file);

		ActionListener listenerShow = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						logger.info("Programm öffnen");
						primaryStage.show();
					}
				});
			}
		};

		ActionListener listenerClose = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				logger.info("Programm beenden");
				ProgramChecker.closeSocket();
				Platform.exit();
				System.exit(0);

			}
		};

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {
				primaryStage.hide();
			}
		});

		PopupMenu popup = new PopupMenu();
		MenuItem showItem = new MenuItem("Öffnen");
		MenuItem exitItem = new MenuItem("Beenden");

		showItem.addActionListener(listenerShow);
		exitItem.addActionListener(listenerClose);

		popup.add(showItem);
		popup.add(exitItem);

		TrayIcon trayIcon = new TrayIcon(image, appName, popup);
		trayIcon.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						if (e.getButton() == MouseEvent.BUTTON1) {
							if (e.getClickCount() == 2) {
								logger.info("Programm öffnen");
								primaryStage.show();
							}
						}

					}
				});

				super.mouseClicked(e);
			}
		});
		SystemTrayIcon.trayIcon = trayIcon;
		trayIcon.setImageAutoSize(true);

		try {
			sTray.add(trayIcon);
		} catch (AWTException e) {

			System.err.println(e);
		}

	}

	public static void showInfoMessage(String text) {

		trayIcon.displayMessage(appName, text, TrayIcon.MessageType.INFO);

	}

	public static void showErrorMessage(String text) {

		trayIcon.displayMessage(appName, text, TrayIcon.MessageType.ERROR);

	}

	public static void showWarningMessage(String text) {

		trayIcon.displayMessage(appName, text, TrayIcon.MessageType.WARNING);

	}

}
