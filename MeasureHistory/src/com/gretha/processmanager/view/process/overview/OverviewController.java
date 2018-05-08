package com.gretha.processmanager.view.process.overview;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.gretha.processmanager.Main;
import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Process;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

public class OverviewController implements Initializable {

	private static final Logger logger = Logger.getLogger(OverviewController.class);

	private Main main;

	private ResourceBundle resources;

	@FXML
	private BorderPane borderPane;

	@FXML
	private Button btnOpnUebersicht;

	@FXML
	private Button btnOpnGrafik;

	@FXML
	private Button btnOpnStatistik;

	@FXML
	private Label lblDatensaetze_IO;

	@FXML
	private Label lblDatensaetze_NIO;

	@FXML
	private Label lblDatensaetze_Unbewertet;

	@FXML
	private Label lblNameProcessValue;

	private Process process;

	private String name;
	private String ok;
	private String nok;
	private String unbewertet;
	private int[] overall;

	/**
	 * The constructor. The constructor is called before the initialize() method.
	 */
	public OverviewController() {

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		this.resources = resources;

	}

	/**
	 * Is called by the main application to give a reference back to itself.
	 * 
	 * @param main
	 */
	public void setMain(Main main) {
		this.main = main;

	}

	public void setData(Process process) {

		this.process = process;
		setComponents();

	}

	private void setComponents() {

		getDataFromDatabase();
		updateComponents();

	}

	public void getDataFromDatabase() {

		long beginTime = System.currentTimeMillis();

		name = process.getStation() + ": " + process.getName();

		// getDataFromDatabaseMethod1();
		getDataFromDatabaseMethod2();

		long endTime = System.currentTimeMillis();
		long duration = endTime - beginTime;

		if (logger.isInfoEnabled()) {
			// logger.info(name + ": QUERY executing time: " +
			// DurationDateAndTime.getTimestampFromMilliseconds(duration));
		}

	}

	public void getDataFromDatabaseMethod1() {

		ok = String.valueOf(Service.getInstance().getOk(process.getId()));
		nok = String.valueOf(Service.getInstance().getNok(process.getId()));
		unbewertet = String.valueOf(Service.getInstance().getUnbewertet(process.getId()));

	}

	public void getDataFromDatabaseMethod2() {

		overall = Service.getInstance().getOverall(process.getId());

		ok = String.valueOf(overall[0]);
		nok = String.valueOf(overall[1]);
		unbewertet = String.valueOf(overall[2]);

	}

	public void updateComponents() {

		lblNameProcessValue.setText(name);
		lblDatensaetze_IO.setText(ok);
		lblDatensaetze_NIO.setText(nok);
		lblDatensaetze_Unbewertet.setText(unbewertet);
	}

	@FXML
	private void handleProcessOverviewTable() {

		main.showProcessRootPaneDialog(process, 0);

	}

	@FXML
	private void handleProcessOverviewChart() {

		main.showProcessRootPaneDialog(process, 1);

	}

	@FXML
	private void handleProcessOverviewStatistic() {

		main.showProcessRootPaneDialog(process, 2);

	}

}
