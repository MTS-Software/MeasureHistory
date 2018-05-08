package com.gretha.shared.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PlcTrigger implements Comparable<PlcTrigger> {

	private IntegerProperty id = new SimpleIntegerProperty();
	private IntegerProperty db = new SimpleIntegerProperty();
	private IntegerProperty strtAdr = new SimpleIntegerProperty();
	private BooleanProperty activated = new SimpleBooleanProperty();
	private IntegerProperty plcId = new SimpleIntegerProperty();
	private StringProperty timestamp = new SimpleStringProperty();

	private int processId;
	private Process process;

	private ObjectProperty<Plc> plc = new SimpleObjectProperty<>();

	public IntegerProperty idProperty() {
		return this.id;
	}

	public int getId() {
		return this.idProperty().get();
	}

	public void setId(final int id) {
		this.idProperty().set(id);
	}

	public IntegerProperty dbProperty() {
		return this.db;
	}

	public int getDb() {
		return this.dbProperty().get();
	}

	public void setDb(final int db) {
		this.dbProperty().set(db);
	}

	public IntegerProperty strtAdrProperty() {
		return this.strtAdr;
	}

	public int getStrtAdr() {
		return this.strtAdrProperty().get();
	}

	public void setStrtAdr(final int strtAdr) {
		this.strtAdrProperty().set(strtAdr);
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public String getPlcTriggerInfo() {
		return "Id: " + getId() + ";" + " CPU: " + getPlc().getName() + ";" + " IP: " + getPlc().getIp() + ";" + " DB: "
				+ getDb() + ";" + " Adr: " + getStrtAdr() + ";" + " aktiv: " + isActivated();
	}

	public StringProperty processStationProperty() {
		if (this.process != null)
			return this.process.stationProperty();
		else
			return null;
	}

	public StringProperty processNameProperty() {
		if (this.process != null)
			return this.process.nameProperty();
		else
			return null;
	}

	public StringProperty timestampProperty() {
		return this.timestamp;
	}

	public String getTimestamp() {
		return this.timestampProperty().get();
	}

	public void setTimestamp(final String timestamp) {
		this.timestampProperty().set(timestamp);
	}

	public BooleanProperty activatedProperty() {
		return this.activated;
	}

	public boolean isActivated() {
		return this.activatedProperty().get();
	}

	public void setActivated(final boolean activated) {
		this.activatedProperty().set(activated);
	}

	public IntegerProperty plcIdProperty() {
		return this.plcId;
	}

	public int getPlcId() {
		return this.plcIdProperty().get();
	}

	public void setPlcId(final int plcId) {
		this.plcIdProperty().set(plcId);
	}

	public ObjectProperty<Plc> plcProperty() {
		return this.plc;
	}

	public Plc getPlc() {
		return this.plcProperty().get();
	}

	public void setPlc(final Plc plc) {
		this.plcProperty().set(plc);
	}

	@Override
	public String toString() {
		return "PlcTrigger [id=" + id + ", db=" + db + ", strtAdr=" + strtAdr + ", activated=" + activated + ", plcId="
				+ plcId + ", plc=" + plc + "]";
	}

	@Override
	public int compareTo(PlcTrigger o) {

		if (this.getPlc() == null || o.getPlc() == null)
			return 0;

		if (this.getPlc().getName().toLowerCase().compareTo(o.getPlc().getName().toLowerCase()) == 0)
			if (Integer.compare(this.getDb(), o.getDb()) == 0)
				return Integer.compare(this.getStrtAdr(), o.getStrtAdr());
			else
				return (Integer.compare(this.getDb(), o.getDb()));

		return this.getPlc().getName().toLowerCase().compareTo(o.getPlc().getName().toLowerCase());
	}

}
