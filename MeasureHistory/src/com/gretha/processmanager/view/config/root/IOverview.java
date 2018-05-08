package com.gretha.processmanager.view.config.root;

import javafx.fxml.FXML;

/**
 * Interface fuer Overview Funktionen
 * 
 * @author Markus Thaler, Ing.
 */
public interface IOverview {

	@FXML
	public void handleNew();

	@FXML
	public void handleEdit();

	@FXML
	public void handleDelete();

}
