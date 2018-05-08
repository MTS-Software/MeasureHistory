package com.gretha.shared.util.textvalidator;

import javafx.scene.control.Control;

public interface Validator<C extends Control> {

	public ValidationResult validate(C control);

}