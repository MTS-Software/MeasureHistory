package com.gretha.plcmanager.plc.dao;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.gretha.plcmanager.view.diagnose.TriggerDiagnoseController;
import com.gretha.shared.db.util.DAOException;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.PlcTrigger;
import com.gretha.shared.model.Result;

import PLCCom.BasicInfoResult;
import PLCCom.CPUModeInfoResult;
import PLCCom.OperationResult;
import PLCCom.ReadDataRequest;
import PLCCom.ReadDataRequestCollection;
import PLCCom.ReadDataResult;
import PLCCom.ReadDataResultCollection;
import PLCCom.ReadRequest;
import PLCCom.ReadResult;
import PLCCom.TCP_ISO_Device;
import PLCCom.WriteDataRequest;
import PLCCom.WriteDataResult;
import PLCCom.WriteRequest;
import PLCCom.WriteResult;
import PLCCom.eDataType;
import PLCCom.eRegion;
import javafx.application.Platform;

public class PlcComDAO implements PlcDAO {

	private static final Logger logger = Logger.getLogger(PlcComDAO.class);

	private final static eRegion SOURCE = eRegion.DataBlock;
	private final static int POLLING_TIME = 1000;

	private ReadDataRequestCollection RequestCollection = new ReadDataRequestCollection();

	private TCP_ISO_Device device;
	private Thread plcTriggerThread;

	private TriggerDiagnoseController controller;
	private PlcTrigger plcTrigger;

	private long beginTime;
	private long endTime;
	private long comTime;

	private long beginDbInsertTime;
	private long endDbInsertTime;
	private long resultDbInsertTime;

	private boolean triggerSave;
	private boolean triggerSaved;

	private String loggerText;

	private int db;
	private int startAdr;

	// Trigger
	private int save = 0;
	private int saved = 2;

	// Real Werte
	private int value = 4;
	private int loLim = 8;
	private int upLim = 12;
	private int resReal1 = 16;
	private int resReal2 = 20;

	// Integer Werte
	private int state = 24;
	private int typId = 26;
	private int resInt1 = 28;
	private int resInt2 = 30;

	// String Werte
	private int serial = 32;
	private int wtNr = 54;
	private int remark = 76;
	private int ResString1 = 98;
	private int ResString2 = 120;

	public PlcComDAO(TCP_ISO_Device device) {

		this.device = device;

	}

	private void initTriggerData() {

		db = plcTrigger.getDb();
		startAdr = plcTrigger.getStrtAdr();

		ReadDataRequest readRequest = null;

		readRequest = new ReadDataRequest(SOURCE, db, startAdr + value, eDataType.REAL, 1);
		RequestCollection.addReadDataRequest(readRequest, "value");

		readRequest = new ReadDataRequest(SOURCE, db, startAdr + loLim, eDataType.REAL, 1);
		RequestCollection.addReadDataRequest(readRequest, "loLim");

		readRequest = new ReadDataRequest(SOURCE, db, startAdr + upLim, eDataType.REAL, 1);
		RequestCollection.addReadDataRequest(readRequest, "upLim");

		readRequest = new ReadDataRequest(SOURCE, db, startAdr + state, eDataType.INT, 1);
		RequestCollection.addReadDataRequest(readRequest, "state");

		readRequest = new ReadDataRequest(SOURCE, db, startAdr + typId, eDataType.INT, 1);
		RequestCollection.addReadDataRequest(readRequest, "typId");

		readRequest = new ReadDataRequest(SOURCE, db, startAdr + serial, eDataType.S7STRING, 20);
		RequestCollection.addReadDataRequest(readRequest, "serial");

		readRequest = new ReadDataRequest(SOURCE, db, startAdr + wtNr, eDataType.S7STRING, 20);
		RequestCollection.addReadDataRequest(readRequest, "wtNr");

		readRequest = new ReadDataRequest(SOURCE, db, startAdr + remark, eDataType.S7STRING, 20);
		RequestCollection.addReadDataRequest(readRequest, "remark");

	}

	public void start() {

		plcTriggerThread = new Thread(new Runnable() {

			long plcComTime;

			@Override
			public void run() {

				logger.info(getClass().getSimpleName() + "; " + Thread.currentThread().getName() + "; Status: "
						+ Thread.currentThread().getState() + ";" + " Process: " + plcTrigger.getProcess().getStation()
						+ " " + plcTrigger.getProcess().getName() + " [" + "CPU: " + plcTrigger.getPlc().getName() + ";"
						+ " IP: " + plcTrigger.getPlc().getIp() + ";" + " TriggerId: " + plcTrigger.getId() + "]");

				while (!Thread.currentThread().isInterrupted()) {

					resultDbInsertTime = 0;

					beginTime = System.currentTimeMillis();
					executeTrigger();
					endTime = System.currentTimeMillis();
					comTime = endTime - beginTime;

					plcComTime = comTime - resultDbInsertTime;
					logging("CPU Communication - Duration: " + plcComTime + " ms");

					try {
						Thread.sleep(POLLING_TIME);
					} catch (InterruptedException e) {
						e.printStackTrace();
						Thread.currentThread().interrupt();

					}

				}

			}
		});

		plcTriggerThread.start();

	}

	public void stop() {

		plcTriggerThread.interrupt();

	}

	private String getCPUMode() {

		String text = null;

		CPUModeInfoResult res = device.GetCPUMode();

		if (res.HasWorked()) {
			text = String.valueOf(res.CPUModeInfo());
		}

		return text;
	}

	private String getOrderNr() {

		String text = null;

		BasicInfoResult res;
		res = device.GetBasicInfo();

		if (res.HasWorked()) {
			text = String.valueOf(res.Ordernummer() + " " + res.FirmwareVersion());
		}

		return text;
	}

	private void executeTrigger() {

		boolean tempSave;
		boolean tempSaved;

		try {
			// Save Bit aus SPS abfragen und merken
			if (readTriggerBit(db, startAdr + save, 0, "save"))
				tempSave = true;
			else
				tempSave = false;

			// Saved Bit aus SPS abfragen und merken
			if (readTriggerBit(db, startAdr + saved, 0, "saved"))
				tempSaved = true;
			else
				tempSaved = false;

			if (tempSave && !tempSaved) {

				beginDbInsertTime = System.currentTimeMillis();
				if (Service.getInstance().insertResult(readOperationCollect())) {
					triggerSaved = true;
					endDbInsertTime = System.currentTimeMillis();
					resultDbInsertTime = endDbInsertTime - beginDbInsertTime;
					logging("Database Insert - Duration: " + resultDbInsertTime + " ms");
				}

			}

			if (!tempSave && tempSaved) {
				triggerSaved = false;
			}

			if (triggerSaved) {
				writeTriggerBit(db, startAdr + saved, 0, true, "save");
			} else {
				writeTriggerBit(db, startAdr + saved, 0, false, "saved");
			}

		} catch (DAOException e) {
			logger.info(e.getMessage());
			logging(e.getMessage());
			e.printStackTrace();
		}

	}

	private Result readOperationCollect() throws DAOException {

		Result result = new Result();

		result.setProcessId(plcTrigger.getProcess().getId());

		ReadDataResultCollection ResultCollection = device.readData(RequestCollection);

		for (ReadDataResult res : ResultCollection.getResults()) {
			if (res.Quality() == OperationResult.eQuality.GOOD) {

				for (Object item : res.getValues()) {

					if (res.getItemkey().equalsIgnoreCase("value"))
						result.setValue((float) item);

					if (res.getItemkey().equalsIgnoreCase("loLim"))
						result.setLoLim((float) item);

					if (res.getItemkey().equalsIgnoreCase("upLim"))
						result.setUpLim((float) item);

					if (res.getItemkey().equalsIgnoreCase("state"))
						result.setState((int) item);

					if (res.getItemkey().equalsIgnoreCase("typId"))
						result.setTypId((int) item);

					if (res.getItemkey().equalsIgnoreCase("serial"))
						result.setSerial((String) item);

					if (res.getItemkey().equalsIgnoreCase("wtNr"))
						result.setWt((String) item);

					if (res.getItemkey().equalsIgnoreCase("remark"))
						result.setRemark((String) item);

					logging("PLC -> Database [" + res.getItemkey() + "]; DB" + res.getDB() + ".DBB"
							+ res.getStartAddress() + " = " + item);

				}
			}
		}

		return result;

	}

	private Result readOperationSingle() throws DAOException {

		Result result = new Result();
		result.setProcessId(plcTrigger.getProcess().getId());

		result.setValue(readReal(db, startAdr + value));
		result.setLoLim(readReal(db, startAdr + loLim));
		result.setUpLim(readReal(db, startAdr + upLim));

		result.setState(readInt(db, startAdr + state));
		result.setTypId(readInt(db, startAdr + typId));

		result.setSerial(readS7String(db, startAdr + serial, 20));
		result.setWt(readS7String(db, startAdr + wtNr, 20));
		result.setRemark(readS7String(db, startAdr + remark, 20));

		return result;

	}

	@Deprecated
	public boolean readTriggerBitV5(int db, int startByte, int bitNr, String itemName) throws DAOException {

		ReadRequest[] oRequest = new ReadRequest[1];
		oRequest[0] = new ReadRequest();

		try {

			oRequest[0].setRegion(SOURCE);
			oRequest[0].setDB(db);
			oRequest[0].setStartByte(startByte);
			oRequest[0].setLen(1);

			oRequest[0].setBit(Byte.valueOf((byte) bitNr));

			oRequest[0].setIsBit(true);

			ReadResult[] res = device.read(oRequest);

			if (res[0].HasWorked() & res[0].DataAvailable()) {
				boolean[] readBuffer = res[0].Bufferbool();
				for (int i = 0; i <= res[0].BufferLen() - 1; i++) {
					if (String.valueOf(readBuffer[i]).equals("true")) {
						logging("PLC -> Database [getTriggerBit() " + itemName + "]  " + "DB" + db + ".DBX" + startByte
								+ "." + bitNr + " = true");
						return true;
					} else {
						logging("PLC -> Database [getTriggerBit() " + itemName + "]  " + "DB" + db + ".DBX" + startByte
								+ "." + bitNr + " = false");
						return false;
					}

				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;

	}

	public boolean readTriggerBit(int db, int startByte, int bitNr, String itemName) throws DAOException {

		ReadDataRequest readData = new ReadDataRequest(SOURCE, db, startByte, eDataType.BIT, 1);
		ReadDataResult res = device.readData(readData);

		boolean value = false;

		if (res.Quality() == OperationResult.eQuality.GOOD) {

			for (Object item : res.getValues()) {
				value = (boolean) item;

				if (value) {
					logging("PLC -> Database [getTriggerBit() " + itemName + "]  " + "DB" + db + ".DBX" + startByte
							+ "." + bitNr + " = true");
				} else {
					logging("PLC -> Database [getTriggerBit() " + itemName + "]  " + "DB" + db + ".DBX" + startByte
							+ "." + bitNr + " = false");
				}

				break;
			}

		} else
			throw new DAOException("Verbindungsfehler: [" + res.Quality() + "]");

		return value;

	}

	public int readInt(int db, int startByte) throws DAOException {

		ReadDataRequest readData = new ReadDataRequest(SOURCE, db, startByte, eDataType.INT, 1);
		ReadDataResult res = device.readData(readData);

		int value = 0;

		if (res.Quality() == OperationResult.eQuality.GOOD) {

			for (Object item : res.getValues()) {
				value = (int) item;
				logging("PLC -> Database [getInt()]  " + "DB" + db + ".DBW" + startByte + " = "
						+ String.valueOf(value));
				break;
			}

		}

		return value;

	}

	@Deprecated
	public int readIntV5(int db, int startByte) throws DAOException {
		ReadRequest[] oRequest = new ReadRequest[1];
		oRequest[0] = new ReadRequest();

		oRequest[0].setRegion(SOURCE);
		oRequest[0].setDB(db);
		oRequest[0].setStartByte(startByte);
		oRequest[0].setLen(2);

		ReadResult[] res = device.read(oRequest);

		if (res[0].HasWorked() & res[0].DataAvailable()) {
			while (res[0].DataAvailable()) {
				int value = res[0].get_INT();
				logging("PLC -> Database [getInt()]  " + "DB" + db + ".DBW" + startByte + " = "
						+ String.valueOf(value));
				return value;
			}
		}
		return 0;

	}

	public float readReal(int db, int startByte) throws DAOException {

		ReadDataRequest readData = new ReadDataRequest(SOURCE, db, startByte, eDataType.REAL, 1);
		ReadDataResult res = device.readData(readData);

		float value = 0;

		if (res.Quality() == OperationResult.eQuality.GOOD) {

			for (Object item : res.getValues()) {
				value = (float) item;
				logging("PLC -> Database [getReal()]  " + "DB" + db + ".DBD" + startByte + " = "
						+ String.valueOf(value));
				break;
			}

		}

		return value;

	}

	@Deprecated
	public float readRealV5(int db, int startByte) throws DAOException {

		ReadRequest[] oRequest = new ReadRequest[3];
		oRequest[0] = new ReadRequest();

		oRequest[0].setRegion(SOURCE);
		oRequest[0].setDB(db);
		oRequest[0].setStartByte(startByte);
		oRequest[0].setLen(4);

		ReadResult[] res = device.read(oRequest);

		if (res[0].HasWorked() & res[0].DataAvailable()) {
			while (res[0].DataAvailable()) {
				float value = res[0].get_REAL();
				logging("PLC -> Database [getReal()]  " + "DB" + db + ".DBD" + startByte + " = "
						+ String.valueOf(value));
				return value;
			}
		}

		return 0;

	}

	public String readS7String(int db, int startByte, int length) throws DAOException {

		ReadDataRequest readData = new ReadDataRequest(SOURCE, db, startByte, eDataType.S7STRING, length);
		ReadDataResult res = device.readData(readData);

		String value = null;

		if (res.Quality() == OperationResult.eQuality.GOOD) {

			for (Object item : res.getValues()) {
				value = (String) item;
				logging("PLC -> Database [getS7String()]  " + "Laenge " + length + "; DB" + db + ".DBB" + startByte
						+ " = " + value);
				break;
			}

		}

		return value;

	}

	@Deprecated
	public String readStringV5(int db, int startByte, int length) throws DAOException {

		ReadRequest[] oRequest = new ReadRequest[1];
		oRequest[0] = new ReadRequest();

		oRequest[0].setRegion(SOURCE);
		oRequest[0].setDB(db);
		oRequest[0].setStartByte(startByte);
		oRequest[0].setLen(length);

		ReadResult[] res = device.read(oRequest);

		if (res[0].HasWorked() & res[0].DataAvailable()) {
			byte[] readBuffer = (byte[]) res[0].Buffer();
			try {
				String st = new String(readBuffer, "UTF-8");
				st = st.substring(2);
				logging("PLC -> Database [getRaw()]  " + "Laenge " + length + "; DB" + db + ".DBB" + startByte + " = "
						+ st);

				return st;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;

	}

	@Deprecated
	public void writeTriggerBitV5(int db, int startByte, int bitNr, boolean state, String itemName)
			throws DAOException {

		WriteRequest[] oWriteRequest = new WriteRequest[1];
		oWriteRequest[0] = new WriteRequest();
		oWriteRequest[0].setRegion(SOURCE);
		oWriteRequest[0].setDB(db);
		oWriteRequest[0].setStartByte(startByte);
		oWriteRequest[0].setBit(Byte.valueOf((byte) bitNr));
		// oWriteRequest[0].setBit(Byte.valueOf("0"));

		if (state == true)
			oWriteRequest[0].addBit(true);
		else
			oWriteRequest[0].addBit(false);

		// add writable Data here
		WriteResult[] res = device.write(oWriteRequest);
		// write
		String value = res[0].Message();
		logging("Database -> PLC [setTriggerBit() " + itemName + "] " + "DB" + db + ".DBX" + startByte + "." + bitNr
				+ " = " + state);

	}

	public void writeTriggerBit(int db, int startByte, int bitNr, boolean state, String itemName) throws DAOException {

		WriteDataRequest writeRequest = new WriteDataRequest(SOURCE, db, startByte, (byte) bitNr);
		writeRequest.addBit((Boolean) state);

		WriteDataResult res = device.writeData(writeRequest);

		if (res.Quality() == OperationResult.eQuality.GOOD) {

			logging("Database -> PLC [setTriggerBit() " + itemName + "] " + "DB" + db + ".DBX" + startByte + "." + bitNr
					+ " = " + state);

		}

	}

	public TCP_ISO_Device getDevice() {
		return device;
	}

	public void logging(String text) {

		// UI updaten
		Platform.runLater(new Runnable() {

			DateFormat df = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss,SSS");

			@Override
			public void run() {

				if (controller != null)
					if (controller.isStart()) {
						if (controller.getPlcTrigger() != null)

							if (controller.getPlcTrigger().getId() == plcTrigger.getId()) {

								Date date = Calendar.getInstance().getTime();
								String formatDate = df.format(date);

								loggerText = "[" + formatDate + "] " + "Thread: " + plcTriggerThread.getName()
										+ " [plcTriggerThread]; Process: " + plcTrigger.getProcess().getStation() + "; "
										+ plcTrigger.getProcess().getName() + ": " + text;
								logger.info(loggerText);

								controller.addLoggerText(loggerText);

							}

					}

			}
		});

	}

	public void setController(TriggerDiagnoseController controller) {
		this.controller = controller;
	}

	public void setPlcTrigger(PlcTrigger plcTrigger) {
		this.plcTrigger = plcTrigger;
		initTriggerData();
	}

}
