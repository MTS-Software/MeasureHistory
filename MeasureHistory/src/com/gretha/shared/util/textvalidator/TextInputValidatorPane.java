package com.gretha.shared.util.textvalidator;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextInputControl;

public class TextInputValidatorPane<C extends TextInputControl> extends ValidatorPane<C> {

	private InvalidationListener textListener = (Observable o) -> {

		final Validator v = getValidator();

		final ValidationResult result = v != null ?

				v.validate(getContent()) :

				new ValidationResult("", ValidationResult.Type.SUCCESS);

		handleValidationResult(result);

	};

	public TextInputValidatorPane() {

		contentProperty().addListener((ObservableValue<? extends C> ov, C oldValue, C newValue) -> {

			if (oldValue != null) {

				oldValue.textProperty().removeListener(textListener);

			}

			if (newValue != null) {

				newValue.textProperty().addListener(textListener);

			}

		});

	}

	public TextInputValidatorPane(C field) {

		this();

		setContent(field);

	}

}