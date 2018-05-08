package com.gretha.shared.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ResultData {

	private IntegerProperty ok = new SimpleIntegerProperty();
	private IntegerProperty nok = new SimpleIntegerProperty();
	private IntegerProperty unbewertet = new SimpleIntegerProperty();
	private IntegerProperty undefiniert = new SimpleIntegerProperty();
	private IntegerProperty gesamt = new SimpleIntegerProperty();

	private FloatProperty okPercent = new SimpleFloatProperty();
	private FloatProperty nokPercent = new SimpleFloatProperty();
	private FloatProperty unbewertetPercent = new SimpleFloatProperty();
	private FloatProperty undefiniertPercent = new SimpleFloatProperty();
	private FloatProperty gesamtPercent = new SimpleFloatProperty();

	private FloatProperty min = new SimpleFloatProperty();
	private FloatProperty max = new SimpleFloatProperty();
	private FloatProperty lastLoLim = new SimpleFloatProperty();
	private FloatProperty lastUpLim = new SimpleFloatProperty();
	private FloatProperty avg = new SimpleFloatProperty();
	private FloatProperty range = new SimpleFloatProperty();
	private FloatProperty setvalue = new SimpleFloatProperty();
	private FloatProperty deviation = new SimpleFloatProperty();

	public IntegerProperty okProperty() {
		return this.ok;
	}

	public int getOk() {
		return this.okProperty().get();
	}

	public void setOk(final int ok) {
		this.okProperty().set(ok);
	}

	public IntegerProperty nokProperty() {
		return this.nok;
	}

	public int getNok() {
		return this.nokProperty().get();
	}

	public void setNok(final int nok) {
		this.nokProperty().set(nok);
	}

	public IntegerProperty unbewertetProperty() {
		return this.unbewertet;
	}

	public int getUnbewertet() {
		return this.unbewertetProperty().get();
	}

	public void setUnbewertet(final int unbewertet) {
		this.unbewertetProperty().set(unbewertet);
	}

	public IntegerProperty gesamtProperty() {
		return this.gesamt;
	}

	public int getGesamt() {
		return this.gesamtProperty().get();
	}

	public void setGesamt(final int all) {
		this.gesamtProperty().set(all);
	}

	public FloatProperty minProperty() {
		return this.min;
	}

	public float getMin() {
		return this.minProperty().get();
	}

	public void setMin(final float min) {
		this.minProperty().set(min);
	}

	public FloatProperty maxProperty() {
		return this.max;
	}

	public float getMax() {
		return this.maxProperty().get();
	}

	public void setMax(final float max) {
		this.maxProperty().set(max);
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

	public FloatProperty deviationProperty() {
		return this.deviation;
	}

	public float getDeviation() {
		return this.deviationProperty().get();
	}

	public void setDeviation(final float deviation) {
		this.deviationProperty().set(deviation);
	}

	public FloatProperty avgProperty() {
		return this.avg;
	}

	public float getAvg() {
		return this.avgProperty().get();
	}

	public void setAvg(final float avg) {
		this.avgProperty().set(avg);
	}

	public FloatProperty rangeProperty() {
		return this.range;
	}

	public float getRange() {
		return this.rangeProperty().get();
	}

	public void setRange(final float range) {
		this.rangeProperty().set(range);
	}

	public FloatProperty okPercentProperty() {
		return this.okPercent;
	}

	public float getOkPercent() {
		return this.okPercentProperty().get();
	}

	public void setOkPercent(final float okPercent) {
		this.okPercentProperty().set(okPercent);
	}

	public FloatProperty nokPercentProperty() {
		return this.nokPercent;
	}

	public float getNokPercent() {
		return this.nokPercentProperty().get();
	}

	public void setNokPercent(final float nokPercent) {
		this.nokPercentProperty().set(nokPercent);
	}

	public FloatProperty unbewertetPercentProperty() {
		return this.unbewertetPercent;
	}

	public float getUnbewertetPercent() {
		return this.unbewertetPercentProperty().get();
	}

	public void setUnbewertetPercent(final float unbewertetPercent) {
		this.unbewertetPercentProperty().set(unbewertetPercent);
	}

	public FloatProperty gesamtPercentProperty() {
		return this.gesamtPercent;
	}

	public float getGesamtPercent() {
		return this.gesamtPercentProperty().get();
	}

	public void setGesamtPercent(final float allPercent) {
		this.gesamtPercentProperty().set(allPercent);
	}

	public FloatProperty lastLoLimProperty() {
		return this.lastLoLim;
	}

	public float getLastLoLim() {
		return this.lastLoLimProperty().get();
	}

	public void setLastLoLim(final float lastLoLim) {
		this.lastLoLimProperty().set(lastLoLim);
	}

	public FloatProperty lastUpLimProperty() {
		return this.lastUpLim;
	}

	public float getLastUpLim() {
		return this.lastUpLimProperty().get();
	}

	public void setLastUpLim(final float lastUpLim) {
		this.lastUpLimProperty().set(lastUpLim);
	}

	public IntegerProperty getUndefiniert() {
		return undefiniert;
	}

	public void setUndefiniert(final int undefiniert) {
		this.unbewertetProperty().set(undefiniert);
	}

	public FloatProperty getUndefiniertPercent() {
		return undefiniertPercent;
	}

	public void setUndefiniertPercent(FloatProperty undefiniertPercent) {
		this.undefiniertPercent = undefiniertPercent;
	}

	@Override
	public String toString() {
		return "ResultData [ok=" + ok + ", nok=" + nok + ", unbewertet=" + unbewertet + ", undefiniert=" + undefiniert
				+ ", gesamt=" + gesamt + ", okPercent=" + okPercent + ", nokPercent=" + nokPercent
				+ ", unbewertetPercent=" + unbewertetPercent + ", undefiniertPercent=" + undefiniertPercent
				+ ", gesamtPercent=" + gesamtPercent + ", min=" + min + ", max=" + max + ", lastLoLim=" + lastLoLim
				+ ", lastUpLim=" + lastUpLim + ", avg=" + avg + ", range=" + range + ", setvalue=" + setvalue
				+ ", deviation=" + deviation + "]";
	}

}
