package com.gretha.shared.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleLongProperty;

public class TimeData {

	private LongProperty min = new SimpleLongProperty();
	private LongProperty max = new SimpleLongProperty();
	private LongProperty range = new SimpleLongProperty();
	private FloatProperty avg = new SimpleFloatProperty();

	public LongProperty minProperty() {
		return this.min;
	}

	public long getMin() {
		return this.minProperty().get();
	}

	public void setMin(final long min) {
		this.minProperty().set(min);
	}

	public LongProperty maxProperty() {
		return this.max;
	}

	public long getMax() {
		return this.maxProperty().get();
	}

	public void setMax(final long max) {
		this.maxProperty().set(max);
	}

	public LongProperty rangeProperty() {
		return this.range;
	}

	public long getRange() {
		return this.rangeProperty().get();
	}

	public void setRange(final long range) {
		this.rangeProperty().set(range);
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

	@Override
	public String toString() {
		return "TimeData [min=" + min + ", max=" + max + ", range=" + range + ", avg=" + avg + "]";
	}

}
