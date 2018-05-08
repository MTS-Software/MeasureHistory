package com.gretha.shared.model;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class SPCClass {

	private IntegerProperty id = new SimpleIntegerProperty();
	private FloatProperty min = new SimpleFloatProperty();
	private FloatProperty max = new SimpleFloatProperty();
	private IntegerProperty stueckAbsolut = new SimpleIntegerProperty();
	private FloatProperty stueckRelativ = new SimpleFloatProperty();

	private static int gesamtStueck;

	public static int getGesamtStueck() {
		return gesamtStueck;
	}

	public static void setGesamtStueck(int gesamtStueck) {
		SPCClass.gesamtStueck = gesamtStueck;
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

	public IntegerProperty stueckAbsolutProperty() {
		return this.stueckAbsolut;
	}

	public int getStueckAbsolut() {
		return this.stueckAbsolutProperty().get();
	}

	public void setStueckAbsolut(final int stueckAbsolut) {
		this.stueckAbsolutProperty().set(stueckAbsolut);
	}

	public FloatProperty stueckRelativProperty() {
		return this.stueckRelativ;
	}

	public float getStueckRelativ() {
		return this.stueckRelativProperty().get();
	}

	public void setStueckRelativ(final float stueckRelativ) {
		this.stueckRelativProperty().set(stueckRelativ);
	}

	@Override
	public String toString() {
		return "SPCKlasse [id=" + id + ", min=" + min + ", max=" + max + ", stueckAbsolut=" + stueckAbsolut
				+ ", stueckRelativ=" + stueckRelativ + "]";
	}

}
