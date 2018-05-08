package com.gretha.shared.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Settings {

	private IntegerProperty id = new SimpleIntegerProperty();
	private StringProperty shift1StartTime = new SimpleStringProperty();
	private StringProperty shift1EndTime = new SimpleStringProperty();
	private StringProperty shift2StartTime = new SimpleStringProperty();
	private StringProperty shift2EndTime = new SimpleStringProperty();
	private StringProperty shift3StartTime = new SimpleStringProperty();
	private StringProperty shift3EndTime = new SimpleStringProperty();
	private StringProperty timestamp = new SimpleStringProperty();

	public IntegerProperty idProperty() {
		return this.id;
	}

	public int getId() {
		return this.idProperty().get();
	}

	public void setId(final int id) {
		this.idProperty().set(id);
	}

	public StringProperty shift1StartTimeProperty() {
		return this.shift1StartTime;
	}

	public String getShift1StartTime() {
		return this.shift1StartTimeProperty().get();
	}

	public void setShift1StartTime(final String shift1StartTime) {
		this.shift1StartTimeProperty().set(shift1StartTime);
	}

	public StringProperty shift1EndTimeProperty() {
		return this.shift1EndTime;
	}

	public String getShift1EndTime() {
		return this.shift1EndTimeProperty().get();
	}

	public void setShift1EndTime(final String shift1EndTime) {
		this.shift1EndTimeProperty().set(shift1EndTime);
	}

	public StringProperty shift2StartTimeProperty() {
		return this.shift2StartTime;
	}

	public String getShift2StartTime() {
		return this.shift2StartTimeProperty().get();
	}

	public void setShift2StartTime(final String shift2StartTime) {
		this.shift2StartTimeProperty().set(shift2StartTime);
	}

	public StringProperty shift2EndTimeProperty() {
		return this.shift2EndTime;
	}

	public String getShift2EndTime() {
		return this.shift2EndTimeProperty().get();
	}

	public void setShift2EndTime(final String shift2EndTime) {
		this.shift2EndTimeProperty().set(shift2EndTime);
	}

	public StringProperty shift3StartTimeProperty() {
		return this.shift3StartTime;
	}

	public String getShift3StartTime() {
		return this.shift3StartTimeProperty().get();
	}

	public void setShift3StartTime(final String shift3StartTime) {
		this.shift3StartTimeProperty().set(shift3StartTime);
	}

	public StringProperty shift3EndTimeProperty() {
		return this.shift3EndTime;
	}

	public String getShift3EndTime() {
		return this.shift3EndTimeProperty().get();
	}

	public void setShift3EndTime(final String shift3EndTime) {
		this.shift3EndTimeProperty().set(shift3EndTime);
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
		return "Settings [id=" + id + ", shift1StartTime=" + shift1StartTime + ", shift1EndTime=" + shift1EndTime
				+ ", shift2StartTime=" + shift2StartTime + ", shift2EndTime=" + shift2EndTime + ", shift3StartTime="
				+ shift3StartTime + ", shift3EndTime=" + shift3EndTime + ", timestamp=" + timestamp + "]";
	}

}
