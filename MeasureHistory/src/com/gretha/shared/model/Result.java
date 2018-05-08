package com.gretha.shared.model;

import java.sql.Timestamp;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Result {

	private IntegerProperty id = new SimpleIntegerProperty();
	private IntegerProperty typId = new SimpleIntegerProperty();
	private StringProperty serial = new SimpleStringProperty();
	private StringProperty wt = new SimpleStringProperty();
	private StringProperty remark = new SimpleStringProperty();
	private FloatProperty value = new SimpleFloatProperty();
	private FloatProperty loLim = new SimpleFloatProperty();
	private FloatProperty upLim = new SimpleFloatProperty();
	private IntegerProperty state = new SimpleIntegerProperty();
	private StringProperty timestamp = new SimpleStringProperty();
	private Timestamp timestampSql;

	private int processId;
	private Process process;

	public Process getProcess() {
		return process;
	}

	public void setProcess(Process process) {
		this.process = process;
	}

	private IntegerProperty nr = new SimpleIntegerProperty();

	public final IntegerProperty nrProperty() {
		return this.nr;
	}

	public final int getNr() {
		return this.nrProperty().get();
	}

	public final void setNr(final int nr) {
		this.nrProperty().set(nr);
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

	public IntegerProperty typIdProperty() {
		return this.typId;
	}

	public int getTypId() {
		return this.typIdProperty().get();
	}

	public void setTypId(final int typId) {
		this.typIdProperty().set(typId);
	}

	public StringProperty serialProperty() {
		return this.serial;
	}

	public String getSerial() {
		return this.serialProperty().get();
	}

	public void setSerial(final String serial) {
		this.serialProperty().set(serial);
	}

	public StringProperty wtProperty() {
		return this.wt;
	}

	public String getWt() {
		return this.wtProperty().get();
	}

	public void setWt(final String wt) {
		this.wtProperty().set(wt);
	}

	public StringProperty remarkProperty() {
		return this.remark;
	}

	public String getRemark() {
		return this.remarkProperty().get();
	}

	public void setRemark(final String remark) {
		this.remarkProperty().set(remark);
	}

	public FloatProperty valueProperty() {
		return this.value;
	}

	public float getValue() {
		return this.valueProperty().get();
	}

	public void setValue(final float value) {
		this.valueProperty().set(value);
	}

	public FloatProperty loLimProperty() {
		return this.loLim;
	}

	public float getLoLim() {
		return this.loLimProperty().get();
	}

	public void setLoLim(final float loLim) {
		this.loLimProperty().set(loLim);
	}

	public FloatProperty upLimProperty() {
		return this.upLim;
	}

	public float getUpLim() {
		return this.upLimProperty().get();
	}

	public void setUpLim(final float upLim) {
		this.upLimProperty().set(upLim);
	}

	public IntegerProperty stateProperty() {
		return this.state;
	}

	public int getState() {
		return this.stateProperty().get();
	}

	public void setState(final int state) {
		this.stateProperty().set(state);
	}

	public void setTimestamp(final String timestamp) {
		this.timestampProperty().set(timestamp);
	}

	public StringProperty timestampProperty() {
		return this.timestamp;
	}

	public String getTimestamp() {
		return this.timestampProperty().get();
	}

	public Timestamp getTimestampSql() {
		return timestampSql;
	}

	public void setTimestampSql(Timestamp timestampSql) {
		this.timestampSql = timestampSql;
	}

	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
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

	@Override
	public String toString() {
		return "Result [typId=" + typId + ", serial=" + serial + ", wt=" + wt + ", remark=" + remark + ", value="
				+ value + ", loLim=" + loLim + ", upLim=" + upLim + ", state=" + state + ", processId=" + processId
				+ "]";
	}

}
