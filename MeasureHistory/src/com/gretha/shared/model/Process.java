package com.gretha.shared.model;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Process implements Comparable<Process> {

	private IntegerProperty id = new SimpleIntegerProperty();
	private StringProperty station = new SimpleStringProperty();
	private StringProperty name = new SimpleStringProperty();
	private BooleanProperty setvalueUsed = new SimpleBooleanProperty();
	private FloatProperty setvalue = new SimpleFloatProperty();
	private IntegerProperty decimalPoints = new SimpleIntegerProperty();
	private StringProperty timestamp = new SimpleStringProperty();
	private IntegerProperty nrAvg = new SimpleIntegerProperty();
	private IntegerProperty nrSpcClass = new SimpleIntegerProperty();
	private FloatProperty CpkLoLim1 = new SimpleFloatProperty();
	private FloatProperty CpkLoLim2 = new SimpleFloatProperty();

	private List<Result> results;

	private int unitId;
	private Unit unit;

	private int plcTriggerId;
	private PlcTrigger plcTrigger;

	private IntegerProperty nr = new SimpleIntegerProperty();

	public IntegerProperty idProperty() {
		return this.id;
	}

	public int getId() {
		return this.idProperty().get();
	}

	public void setId(final int id) {
		this.idProperty().set(id);
	}

	public StringProperty stationProperty() {
		return this.station;
	}

	public String getStation() {
		return this.stationProperty().get();
	}

	public void setStation(final String station) {
		this.stationProperty().set(station);
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

	public StringProperty timestampProperty() {
		return this.timestamp;
	}

	public String getTimestamp() {
		return this.timestampProperty().get();
	}

	public void setTimestamp(final String timestamp) {
		this.timestampProperty().set(timestamp);
	}

	public FloatProperty setvalueProperty() {
		return this.setvalue;
	}

	public float getSetvalue() {
		return this.setvalueProperty().get();
	}

	public void setSetvalue(final float setvalue) {
		this.setvalueProperty().set(setvalue);
	}

	public IntegerProperty decimalPointsProperty() {
		return this.decimalPoints;
	}

	public int getDecimalPoints() {
		return this.decimalPointsProperty().get();
	}

	public void setDecimalPoints(final int decimalPoints) {
		this.decimalPointsProperty().set(decimalPoints);
	}

	public IntegerProperty nrAvgProperty() {
		return this.nrAvg;
	}

	public int getNrAvg() {
		return this.nrAvgProperty().get();
	}

	public void setNrAvg(final int nrAvg) {
		this.nrAvgProperty().set(nrAvg);
	}

	public int getUnitId() {
		return unitId;
	}

	public void setUnitId(int unitId) {
		this.unitId = unitId;
	}

	public int getPlcTriggerId() {
		return plcTriggerId;
	}

	public void setPlcTriggerId(int plcTriggerId) {
		this.plcTriggerId = plcTriggerId;
	}

	public IntegerProperty nrProperty() {
		return this.nr;
	}

	public int getNr() {
		return this.nrProperty().get();
	}

	public void setNr(final int nr) {
		this.nrProperty().set(nr);
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public FloatProperty CpkLoLim1Property() {
		return this.CpkLoLim1;
	}

	public float getCpkLoLim1() {
		return this.CpkLoLim1Property().get();
	}

	public void setCpkLoLim1(final float CpkLoLim1) {
		this.CpkLoLim1Property().set(CpkLoLim1);
	}

	public FloatProperty CpkLoLim2Property() {
		return this.CpkLoLim2;
	}

	public float getCpkLoLim2() {
		return this.CpkLoLim2Property().get();
	}

	public void setCpkLoLim2(final float CpkLoLim2) {
		this.CpkLoLim2Property().set(CpkLoLim2);
	}

	public IntegerProperty nrSpcClassProperty() {
		return this.nrSpcClass;
	}

	public int getNrSpcClass() {
		return this.nrSpcClassProperty().get();
	}

	public void setNrSpcClass(final int nrSpcClass) {
		this.nrSpcClassProperty().set(nrSpcClass);
	}

	public BooleanProperty setvalueUsedProperty() {
		return this.setvalueUsed;
	}

	public boolean isSetvalueUsed() {
		return this.setvalueUsedProperty().get();
	}

	public void setSetvalueUsed(final boolean setvalueUsed) {
		this.setvalueUsedProperty().set(setvalueUsed);
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public PlcTrigger getPlcTrigger() {
		return plcTrigger;
	}

	public void setPlcTrigger(PlcTrigger plcTrigger) {
		this.plcTrigger = plcTrigger;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int compareTo(Process o) {

		if (this.getStation() == null || o.getStation() == null)
			return 0;

		if (this.getStation().toLowerCase().compareTo(o.getStation().toLowerCase()) == 0)
			return this.getName().toLowerCase().compareTo(o.getName().toLowerCase());

		return this.getStation().toLowerCase().compareTo(o.getStation().toLowerCase());
	}

}