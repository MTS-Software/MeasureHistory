package com.gretha.plcmanager.plc.util;

import org.apache.log4j.Logger;

import com.gretha.shared.model.Plc;

import PLCCom.ConnectionStateChangeNotifier;
import PLCCom.eConnectionState;
import PLCCom.iConnectionStateChangeEvent;

public class PLCConnectionWatcher implements iConnectionStateChangeEvent, Runnable {

	private static final Logger logger = Logger.getLogger(PLCConnectionWatcher.class);

	private Plc plc;
	private final static int POLLING_TIME = 2000;

	public PLCConnectionWatcher(Plc plc) {

		this.plc = plc;

		plc.getDevice().StateChangeNotifier = new ConnectionStateChangeNotifier(this);

	}

	@Override
	public void run() {

		while (!Thread.currentThread().isInterrupted()) {

			eConnectionState state = plc.getDevice().getConnectionState();

			switch (state) {

			case undefiniend:
				plc.setConnection(false);
				break;

			case closing:
				plc.setConnection(false);
				break;

			case closed:
				plc.setConnection(false);
				break;

			case connecting:
				plc.setConnection(false);
				break;

			case connected:
				plc.setConnection(true);
				break;

			default:
				break;
			}

			try {
				Thread.sleep(POLLING_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();

			}

		}

	}

	@Override
	public void On_ConnectionStateChange(eConnectionState arg0) {
		logger.info(plc.getDevice().getIPAdress() + " Verbindung Status: " + arg0);

	}

}
