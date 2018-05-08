package com.gretha.plcmanager.plc.util;

import org.apache.log4j.Logger;

import com.gretha.shared.model.Plc;

import PLCCom.eConnectionState;

public class PLCAutoConnector implements Runnable {

	private static final Logger logger = Logger.getLogger(PLCAutoConnector.class);

	private Plc plc;
	private final static int POLLING_TIME = 1000;

	public PLCAutoConnector(Plc plc) {

		this.plc = plc;

	}

	@Override
	public void run() {

		boolean firstRun = true;

		while (!Thread.currentThread().isInterrupted()) {

			System.out.println("Thread:" + plc.getDevice().getConnectionState());

			if (plc.getDevice().getConnectionState() == eConnectionState.connected) {
				plc.setConnection(true);

			}

			if (plc.getDevice().getConnectionState() != eConnectionState.connected) {

				plc.setConnection(false);

				if (firstRun)
					logger.info(plc.getDevice().getIPAdress() + " Verbindung getrennt");

				logger.info(plc.getDevice().getIPAdress() + " Verbindung herstellen");
				plc.getDevice().Connect();

				if (plc.getDevice().getConnectionState() == eConnectionState.connected)
					logger.info(plc.getDevice().getIPAdress() + " Verbindung hergestellt");

			}

			try {
				Thread.sleep(POLLING_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();

			}

		}

	}

}
