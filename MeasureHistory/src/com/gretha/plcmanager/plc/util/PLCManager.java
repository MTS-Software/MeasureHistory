package com.gretha.plcmanager.plc.util;

import java.util.ArrayList;
import java.util.List;

import com.gretha.plcmanager.plc.dao.PlcComDAO;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Plc;
import com.gretha.shared.model.PlcTrigger;

import PLCCom.TCP_ISO_Device;
import PLCCom.authentication;
import PLCCom.ePLCType;

public class PLCManager {

	private List<PlcComDAO> plcComDAO;
	private List<TCP_ISO_Device> deviceList;
	private List<Plc> plcList;

	public PLCManager() {

		authentication.Serial("73471-56153-102135-2267354");
		authentication.User("magna");

		initPlcDevices();
		initPlcTriggers();
	}

	private void initPlcDevices() {

		plcList = Service.getInstance().getPlcs();
		deviceList = new ArrayList<TCP_ISO_Device>();

		for (Plc plc : plcList) {

			TCP_ISO_Device device;
			device = new TCP_ISO_Device(plc.getIp(), plc.getRack(), plc.getSlot(), ePLCType.values()[plc.getType()]);
			device.setConnecttimeout(plc.getTimeout());

			device.setAutoConnect(true, 10000);
			plc.setDevice(device);

			deviceList.add(device);

			Thread th = new Thread(new PLCConnectionWatcher(plc));
			th.start();

			// Thread th = new Thread(new PLCAutoConnector(plc));
			// th.start();

		}

	}

	private void initPlcTriggers() {

		plcComDAO = new ArrayList<PlcComDAO>();

		for (PlcTrigger trigger : Service.getInstance().getPlcTriggers()) {

			if (trigger.isActivated())
				for (Plc plc : plcList) {
					if (trigger.getProcess() != null) {
						if (plc.getId() == trigger.getPlcId()) {
							trigger.setPlc(plc);

							PlcComDAO plcDAO = new PlcComDAO(plc.getDevice());
							plcDAO.setPlcTrigger(trigger);
							plcDAO.start();

							plcComDAO.add(plcDAO);
						}
					}
				}

		}

	}

	public List<PlcComDAO> getPlcComDAO() {
		return plcComDAO;
	}

	public List<Plc> getPlcList() {
		return plcList;
	}

}
