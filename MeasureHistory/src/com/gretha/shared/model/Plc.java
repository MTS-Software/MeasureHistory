package com.gretha.shared.model;

import java.util.List;

import PLCCom.TCP_ISO_Device;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Plc implements Comparable<Plc> {

	private IntegerProperty id = new SimpleIntegerProperty();
	private StringProperty name = new SimpleStringProperty();
	private StringProperty ip = new SimpleStringProperty();
	private IntegerProperty rack = new SimpleIntegerProperty();
	private IntegerProperty slot = new SimpleIntegerProperty();
	private IntegerProperty type = new SimpleIntegerProperty();
	private IntegerProperty timeout = new SimpleIntegerProperty();
	private StringProperty timestamp = new SimpleStringProperty();

	// Shadow Informationen
	private TCP_ISO_Device device;
	private BooleanProperty connection = new SimpleBooleanProperty();
	private StringProperty status = new SimpleStringProperty();

	private List<PlcTrigger> plcTriggers;

	public List<PlcTrigger> getPlcTriggers() {
		return plcTriggers;
	}

	public void setPlcTriggers(List<PlcTrigger> plcTrigger) {
		this.plcTriggers = plcTrigger;
	}

	public IntegerProperty idProperty() {
		return this.id;
	}

	public int getId() {
		return this.idProperty().get();
	}

	public void setId(final int id) {
		this.idProperty().set(id);
	}

	public StringProperty nameProperty() {
		return this.name;
	}

	public String getName() {
		return this.nameProperty().get();
	}

	public void setName(final String name) {
		this.nameProperty().set(name);
	}

	public StringProperty ipProperty() {
		return this.ip;
	}

	public String getIp() {
		return this.ipProperty().get();
	}

	public void setIp(final String ip) {
		this.ipProperty().set(ip);
	}

	public TCP_ISO_Device getDevice() {
		return device;
	}

	public void setDevice(TCP_ISO_Device device) {
		this.device = device;
	}

	public BooleanProperty connectionProperty() {
		return this.connection;
	}

	public boolean isConnection() {
		return this.connectionProperty().get();
	}

	public void setConnection(final boolean connection) {
		this.connectionProperty().set(connection);
	}

	public StringProperty statusProperty() {
		return this.status;
	}

	public String getStatus() {
		return this.statusProperty().get();
	}

	public void setStatus(final String status) {
		this.statusProperty().set(status);
	}

	public IntegerProperty rackProperty() {
		return this.rack;
	}

	public int getRack() {
		return this.rackProperty().get();
	}

	public void setRack(final int rack) {
		this.rackProperty().set(rack);
	}

	public IntegerProperty slotProperty() {
		return this.slot;
	}

	public int getSlot() {
		return this.slotProperty().get();
	}

	public void setSlot(final int slot) {
		this.slotProperty().set(slot);
	}

	public IntegerProperty typeProperty() {
		return this.type;
	}

	public int getType() {
		return this.typeProperty().get();
	}

	public void setType(final int type) {
		this.typeProperty().set(type);
	}

	public IntegerProperty timeoutProperty() {
		return this.timeout;
	}

	public int getTimeout() {
		return this.timeoutProperty().get();
	}

	public void setTimeout(final int timeout) {
		this.timeoutProperty().set(timeout);
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

	@Override
	public String toString() {
		return "Plc [id=" + id + ", name=" + name + ", ip=" + ip + ", rack=" + rack + ", slot=" + slot + ", type="
				+ type + ", timeout=" + timeout + ", timestamp=" + timestamp + "]";
	}

	@Override
	public int compareTo(Plc o) {

		return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());
	}

}
