package com.gretha.shared.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class AdhocResult {

	private ObjectProperty<Result> result1 = new SimpleObjectProperty<>();
	private ObjectProperty<Result> result2 = new SimpleObjectProperty<>();
	private ObjectProperty<Result> result3 = new SimpleObjectProperty<>();
	private ObjectProperty<Result> result4 = new SimpleObjectProperty<>();
	private ObjectProperty<Result> result5 = new SimpleObjectProperty<>();

	public AdhocResult() {

	}

	public ObjectProperty<Result> result1Property() {
		return this.result1;
	}

	public Result getResult1() {
		return this.result1Property().get();
	}

	public void setResult1(final Result result1) {
		this.result1Property().set(result1);
	}

	public ObjectProperty<Result> result2Property() {
		return this.result2;
	}

	public Result getResult2() {
		return this.result2Property().get();
	}

	public void setResult2(final Result result2) {
		this.result2Property().set(result2);
	}

	public ObjectProperty<Result> result3Property() {
		return this.result3;
	}

	public Result getResult3() {
		return this.result3Property().get();
	}

	public void setResult3(final Result result3) {
		this.result3Property().set(result3);
	}

	public ObjectProperty<Result> result4Property() {
		return this.result4;
	}

	public Result getResult4() {
		return this.result4Property().get();
	}

	public void setResult4(final Result result4) {
		this.result4Property().set(result4);
	}

	public ObjectProperty<Result> result5Property() {
		return this.result5;
	}

	public Result getResult5() {
		return this.result5Property().get();
	}

	public void setResult5(final Result result5) {
		this.result5Property().set(result5);
	}

	@Override
	public String toString() {
		return "AdHocResult [result1=" + result1 + ", result2=" + result2 + ", result3=" + result3 + ", result4="
				+ result4 + ", result5=" + result5 + "]";
	}

}