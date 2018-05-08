package com.gretha.shared.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Unit implements Comparable<Unit> {

	private IntegerProperty id = new SimpleIntegerProperty();

	private StringProperty sign = new SimpleStringProperty();
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

	public StringProperty signProperty() {
		return this.sign;
	}

	public String getSign() {
		return this.signProperty().get();
	}

	public void setSign(final String sign) {
		this.signProperty().set(sign);
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
		return "Unit [id=" + id + ", sign=" + sign + "]";
	}

	@Override
	public int compareTo(Unit o) {

		if (this.getSign() == null || o.getSign() == null)
			return 0;

		return this.getSign().toLowerCase().compareTo(o.getSign().toLowerCase());
	}

}