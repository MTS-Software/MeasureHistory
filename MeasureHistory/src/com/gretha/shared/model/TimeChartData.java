package com.gretha.shared.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class TimeChartData {
	private StringProperty nr = new SimpleStringProperty();
	private StringProperty serial = new SimpleStringProperty();
	private StringProperty durationSeconds = new SimpleStringProperty();
	private StringProperty durationTimestamp = new SimpleStringProperty();
	private StringProperty timestamp = new SimpleStringProperty();

	public StringProperty nrProperty() {
		return this.nr;
	}

	public String getNr() {
		return this.nrProperty().get();
	}

	public void setNr(final String nr) {
		this.nrProperty().set(nr);
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

	public StringProperty durationSecondsProperty() {
		return this.durationSeconds;
	}

	public String getDurationSeconds() {
		return this.durationSecondsProperty().get();
	}

	public void setDurationSeconds(final String durationSeconds) {
		this.durationSecondsProperty().set(durationSeconds);
	}

	public StringProperty durationTimestampProperty() {
		return this.durationTimestamp;
	}

	public String getDurationTimestamp() {
		return this.durationTimestampProperty().get();
	}

	public void setDurationTimestamp(final String durationTimestamp) {
		this.durationTimestampProperty().set(durationTimestamp);
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
		return "TimeChartDataModel [nr=" + nr + ", seriennummer=" + serial + ", durationSeconds=" + durationSeconds
				+ ", durationTimestamp=" + durationTimestamp + ", timestamp=" + timestamp + "]";
	}

}