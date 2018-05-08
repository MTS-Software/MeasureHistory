package com.gretha.processmanager.view.process.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import com.gretha.shared.db.util.Service;
import com.gretha.shared.model.Filter;
import com.gretha.shared.model.Process;
import com.gretha.shared.model.Result;
import com.gretha.shared.model.Settings;
import com.gretha.shared.util.Constants;
import com.gretha.shared.util.DecimalPointFormatter;
import com.gretha.shared.util.DurationDateAndTime;
import com.gretha.shared.util.textvalidator.TextInputValidatorPane;
import com.gretha.shared.util.textvalidator.ValidationResult;
import com.gretha.shared.view.alert.FileLoadFailedAlert;
import com.gretha.shared.view.alert.FileSaveFailedAlert;
import com.gretha.shared.view.alert.NoResultAlert;
import com.gretha.shared.view.info.LoadSucessInfo;
import com.gretha.shared.view.info.SaveSucessInfo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import jfxtras.scene.control.LocalDateTimeTextField;

public class FilterSettingsController implements Initializable {

	private Filter filter;

	private static final Logger logger = Logger.getLogger(FilterSettingsController.class);

	private ResourceBundle resources;

	private String dbQueryLoggerText;

	private LocalDateTime ldVon;
	private LocalDateTime ldBis;

	@FXML
	private LocalDateTimeTextField ldtZeitraumVon;
	@FXML
	private LocalDateTimeTextField ldtZeitraumBis;
	@FXML
	private ComboBox<EStatus> cbStatus;
	@FXML
	private ComboBox<EParameter> cbParameter1;
	@FXML
	private ComboBox<EParameter> cbParameter2;
	@FXML
	private ComboBox<EBedingung> cbBedingung1;
	@FXML
	private ComboBox<ESortierung> cbSortierung;
	@FXML
	private TextField valueWert1;
	@FXML
	private ComboBox<ELogik> cbLogik;
	@FXML
	private ComboBox<EBedingung> cbBedingung2;
	@FXML
	private TextField valueWert2;
	@FXML
	private Button btnOK;
	@FXML
	private Button btnUebernehmen;
	@FXML
	private Button btnDateAndTimeToday;
	@FXML
	private Button btnDateAndTimeShift1;
	@FXML
	private Button btnDateAndTimeShift2;
	@FXML
	private Button btnDateAndTimeShift3;
	@FXML
	private Button btnDateAndTimeLast24h;
	@FXML
	private CheckBox cbLetztenNVor;
	@FXML
	private CheckBox cbErstenNSeit;
	@FXML
	private TextField valueAnzahlN;
	@FXML
	private StackPane spAnzahlN;
	@FXML
	private Label lblAktiv;

	private Process process;
	private List<Result> processResults;

	private boolean okClicked = false;

	private Stage dialogStage;
	private Settings settings;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.resources = resources;

		settings = Service.getInstance().getSettings();

		generateFilterOptions();

	}

	private void generateFilterOptions() {

		lblAktiv.setVisible(false);

		ObservableList<EStatus> filterStatus = FXCollections.observableArrayList(EStatus.values());
		cbStatus.setItems(filterStatus);

		ObservableList<EParameter> filterParameter = FXCollections.observableArrayList(EParameter.values());
		cbParameter1.setItems(filterParameter);
		cbParameter2.setItems(filterParameter);

		ObservableList<EBedingung> filterBedingung = FXCollections.observableArrayList(EBedingung.values());
		cbBedingung1.setItems(filterBedingung);
		cbBedingung2.setItems(filterBedingung);

		ObservableList<ELogik> filterLogik = FXCollections.observableArrayList(ELogik.values());
		cbLogik.setItems(filterLogik);

		ObservableList<ESortierung> filterSortierung = FXCollections.observableArrayList(ESortierung.values());
		cbSortierung.setItems(filterSortierung);

		cbSortierung.getSelectionModel().select(ESortierung.DESC);

		cbStatus.getSelectionModel().select(EStatus.ALL);

		cbLetztenNVor.setSelected(false);
		cbErstenNSeit.setSelected(false);
		valueAnzahlN.setDisable(true);
		valueAnzahlN.setText("");

		cbParameter1.getSelectionModel().select(EParameter.KEIN);
		cbBedingung1.getSelectionModel().select(EBedingung.GLEICH);
		valueWert1.setText("");

		cbLogik.getSelectionModel().select(ELogik.KEIN);

		cbParameter2.getSelectionModel().select(EParameter.KEIN);
		cbBedingung2.getSelectionModel().select(EBedingung.GLEICH);
		valueWert2.setText("");

		cbBedingung1.setDisable(true);
		valueWert1.setDisable(true);

		cbLogik.setDisable(true);

		cbParameter2.setDisable(true);
		cbBedingung2.setDisable(true);
		valueWert2.setDisable(true);

		cbParameter1.setOnAction((event) -> {

			if (cbParameter1.getSelectionModel().getSelectedItem() == EParameter.KEIN) {

				cbBedingung1.getSelectionModel().select(EBedingung.GLEICH);
				valueWert1.setText("");

				cbLogik.getSelectionModel().select(ELogik.KEIN);

				cbParameter2.getSelectionModel().select(EParameter.KEIN);
				cbBedingung2.getSelectionModel().select(EBedingung.GLEICH);
				valueWert2.setText("");

				cbBedingung1.setDisable(true);
				valueWert1.setDisable(true);

				cbLogik.setDisable(true);

				cbParameter2.setDisable(true);
				cbBedingung2.setDisable(true);
				valueWert2.setDisable(true);
			}

			else {

				cbBedingung1.setDisable(false);
				valueWert1.setDisable(false);

				cbLogik.setDisable(false);
			}
		});

		cbParameter2.setOnAction((event) -> {

			if (cbParameter2.getSelectionModel().getSelectedItem() == EParameter.KEIN) {

				cbBedingung2.getSelectionModel().select(EBedingung.GLEICH);
				valueWert2.setText("");

				cbBedingung2.setDisable(true);
				valueWert2.setDisable(true);
			}

			else {

				cbBedingung2.setDisable(false);
				valueWert2.setDisable(false);
			}
		});

		cbLetztenNVor.setOnAction((event) -> {

			if (cbLetztenNVor.isSelected()) {

				valueAnzahlN.setDisable(false);
				ldtZeitraumVon.setDisable(true);
				ldtZeitraumBis.setDisable(false);
				cbErstenNSeit.setSelected(false);

				if (valueAnzahlN.getText() == null || !valueAnzahlN.getText().matches("[0-9]*")
						|| valueAnzahlN.getText().isEmpty()) {

					btnOK.setDisable(true);
					btnUebernehmen.setDisable(true);

				} else {

					btnOK.setDisable(false);
					btnUebernehmen.setDisable(false);
				}

			} else {

				valueAnzahlN.setDisable(true);
				ldtZeitraumVon.setDisable(false);
				ldtZeitraumBis.setDisable(false);

				btnOK.setDisable(false);
				btnUebernehmen.setDisable(false);

			}

		});

		cbErstenNSeit.setOnAction((event) -> {

			if (cbErstenNSeit.isSelected()) {

				valueAnzahlN.setDisable(false);
				ldtZeitraumVon.setDisable(false);
				ldtZeitraumBis.setDisable(true);
				cbLetztenNVor.setSelected(false);

				if (valueAnzahlN.getText() == null || !valueAnzahlN.getText().matches("[0-9]*")
						|| valueAnzahlN.getText().isEmpty()) {

					btnOK.setDisable(true);
					btnUebernehmen.setDisable(true);

				} else {

					btnOK.setDisable(false);
					btnUebernehmen.setDisable(false);
				}

			} else {

				valueAnzahlN.setDisable(true);
				ldtZeitraumVon.setDisable(false);
				ldtZeitraumBis.setDisable(false);

				btnOK.setDisable(false);
				btnUebernehmen.setDisable(false);

			}

		});

		cbLogik.setOnAction((event) -> {

			if (cbLogik.getSelectionModel().getSelectedItem() == ELogik.KEIN) {

				cbParameter2.getSelectionModel().select(EParameter.KEIN);
				cbBedingung2.getSelectionModel().select(EBedingung.GLEICH);
				valueWert2.setText("");

				cbParameter2.setDisable(true);
				cbBedingung2.setDisable(true);
				valueWert2.setDisable(true);
			}

			else {

				cbParameter2.setDisable(false);
				cbBedingung2.setDisable(false);
				valueWert2.setDisable(false);
			}

		});

		valueAnzahlN.setOnKeyPressed((event) -> {

			TextInputValidatorPane<TextField> pane = new TextInputValidatorPane<TextField>();
			pane.setContent(valueAnzahlN);
			spAnzahlN.getChildren().add(pane);

			pane.setValidator((TextField control) -> {
				try {
					String text = control.getText();

					if (text == null || text.trim().equals("")) {
						btnOK.setDisable(true);
						btnUebernehmen.setDisable(true);
						valueAnzahlN.setDisable(false);
						return null;
					}

					int i = Integer.parseInt(text);
					if (i < 0 || i > Constants.LIMIT_RESULTS_FILTER_SHOWN) {
						btnOK.setDisable(true);
						btnUebernehmen.setDisable(true);
						valueAnzahlN.setDisable(false);
						return new ValidationResult("Should be > 0 or <= Configured Value in Constans",
								ValidationResult.Type.WARNING);
					}

					if (valueAnzahlN.getText().contains(".")) {
						btnOK.setDisable(true);
						btnUebernehmen.setDisable(true);
						valueAnzahlN.setDisable(false);
						return new ValidationResult("Should be a non decimalvalue", ValidationResult.Type.ERROR);

					}

					else {
						btnOK.setDisable(false);
						btnUebernehmen.setDisable(false);
						return null;
					}

				} catch (Exception e) {

					if (valueAnzahlN.getText().contains(",")) {
						btnOK.setDisable(true);
						btnUebernehmen.setDisable(true);
						valueAnzahlN.setDisable(false);
						return new ValidationResult("Should be a non decimalvalue", ValidationResult.Type.ERROR);

					}

					else {
						// failed
						btnOK.setDisable(true);
						btnUebernehmen.setDisable(true);
						valueAnzahlN.setDisable(false);
						return new ValidationResult("Bad number", ValidationResult.Type.ERROR);
					}
				}
			});
		});

		btnDateAndTimeToday.setOnAction((event) -> {
			setDateAndTimeToday();
		});

		btnDateAndTimeShift1.setOnAction((event) -> {
			setDateAndTimeToShiftX(1);
		});

		btnDateAndTimeShift2.setOnAction((event) -> {
			setDateAndTimeToShiftX(2);
		});

		btnDateAndTimeShift3.setOnAction((event) -> {
			setDateAndTimeToShiftX(3);
		});

		btnDateAndTimeLast24h.setOnAction((event) -> {
			setDateAndTimeToLast24h();
		});

	}

	private List<Result> getFilterResults(List<Result> results) {

		Set<Result> resultSet = new HashSet<>();

		List<Result> filterStatus = new ArrayList<>();
		List<Result> filterBedingung1 = new ArrayList<>();
		List<Result> filterBedingung2 = new ArrayList<>();
		List<Result> filterEndResult = new ArrayList<>();

		// Status
		if (filter.isLetztenNVor()) {
			filterStatus = results;

		} else
			filterStatus = filterStatus(results);

		if (cbParameter1.getSelectionModel().getSelectedItem() == EParameter.KEIN) {

			Collections.sort(filterStatus, new ResultSort());

			reorderResultNr(filterStatus);

			return filterStatus;
		}

		// Bedingung 1
		// Filterergebnis von Status durchfiltern
		filterBedingung1 = manageFilterParameter(filterStatus, cbParameter1, cbBedingung1, valueWert1.getText());

		if (cbLogik.getSelectionModel().getSelectedItem() == ELogik.KEIN) {

			Collections.sort(filterBedingung1, new ResultSort());

			reorderResultNr(filterBedingung1);

			return filterBedingung1;
		}

		// Bedingung 2
		// Filterergebnis von Bedingung 1 nochmal durchfiltern -> UND
		if (cbLogik.getSelectionModel().getSelectedItem() == ELogik.UND) {

			filterBedingung2 = manageFilterParameter(filterBedingung1, cbParameter2, cbBedingung2,
					valueWert2.getText());

			Collections.sort(filterBedingung2, new ResultSort());

			reorderResultNr(filterBedingung2);

			return filterBedingung2;
		}

		// Filterergebnis von Status durchfiltern und doppelte Einträge
		// entfernen -> ODER
		if (cbLogik.getSelectionModel().getSelectedItem() == ELogik.ODER) {

			filterBedingung2 = manageFilterParameter(filterStatus, cbParameter2, cbBedingung2, valueWert2.getText());

			List<Result> res = new ArrayList<>();
			res.addAll(filterBedingung1);
			res.addAll(filterBedingung2);

			// Damit es keine doppelten Ergebnisse gibt, wird hier ein Set
			// verwendet
			for (Result result : res) {
				resultSet.add(result);
			}

			// Über das Set iterieren und zur Liste hinzufügen um zu sortieren
			Iterator<Result> iterator = resultSet.iterator();
			while (iterator.hasNext()) {
				Result element = iterator.next();
				filterEndResult.add(element);

			}

			Collections.sort(filterEndResult, new ResultSort());

			reorderResultNr(filterEndResult);

			return filterEndResult;

		}

		return filterStatus;

	}

	private List<Result> reorderResultNr(List<Result> results) {

		int i = 0;

		for (Result result : results) {

			i++;
			result.setNr(i);

		}

		return results;

	}

	private List<Result> manageFilterParameter(List<Result> results, ComboBox<EParameter> cbParameter,
			ComboBox<EBedingung> cb, String textfield) {

		List<Result> res = new ArrayList<>();

		switch (cbParameter.getSelectionModel().getSelectedItem()) {

		case KEIN:
			break;
		case VALUE:
			res = filterValue(results, cb, textfield);
			break;
		case SERIAL:
			res = filterSerial(results, cb, textfield);
			break;
		case REMARK:
			res = filterRemark(results, cb, textfield);
			break;
		case WT:
			res = filterWT(results, cb, textfield);
			break;
		case OG:
			res = filterOG(results, cb, textfield);
			break;
		case UG:
			res = filterUG(results, cb, textfield);
			break;
		case TYP:
			res = filterTyp(results, cb, textfield);
			break;

		default:
			break;
		}

		return res;
	}

	private List<Result> filterTyp(List<Result> results, ComboBox<EBedingung> cb, String textfield)
			throws NumberFormatException {

		List<Result> res = new ArrayList<>();

		try {
			switch (cb.getSelectionModel().getSelectedItem()) {

			case GLEICH:
				res = filter(results, (Result a) -> a.getTypId() == Integer.parseInt(textfield));
				break;
			case UNGLEICH:
				res = filter(results, (Result a) -> a.getTypId() != Integer.parseInt(textfield));
				break;
			case KLEINER:
				res = filter(results, (Result a) -> a.getTypId() < Integer.parseInt(textfield));
				break;
			case KLEINER_GLEICH:
				res = filter(results, (Result a) -> a.getTypId() <= Integer.parseInt(textfield));
				break;
			case GROESSER:
				res = filter(results, (Result a) -> a.getTypId() > Integer.parseInt(textfield));
				break;
			case GROESSER_GLEICH:
				res = filter(results, (Result a) -> a.getTypId() >= Integer.parseInt(textfield));
				break;

			default:
				break;
			}

		} catch (NumberFormatException e) {
			throw new NumberFormatException();
		}

		return res;

	}

	private List<Result> filterValue(List<Result> results, ComboBox<EBedingung> cb, String textfield)
			throws NumberFormatException {

		List<Result> res = new ArrayList<>();

		try {
			switch (cb.getSelectionModel().getSelectedItem()) {

			case GLEICH:
				res = filter(results, (Result a) -> a.getValue() == Float.parseFloat(textfield));
				break;
			case UNGLEICH:
				res = filter(results, (Result a) -> a.getValue() != Float.parseFloat(textfield));
				break;
			case KLEINER:
				res = filter(results, (Result a) -> a.getValue() < Float.parseFloat(textfield));
				break;
			case KLEINER_GLEICH:
				res = filter(results, (Result a) -> a.getValue() <= Float.parseFloat(textfield));
				break;
			case GROESSER:
				res = filter(results, (Result a) -> a.getValue() > Float.parseFloat(textfield));
				break;
			case GROESSER_GLEICH:
				res = filter(results, (Result a) -> a.getValue() >= Float.parseFloat(textfield));
				break;

			default:
				break;
			}

		} catch (NumberFormatException e) {
			throw new NumberFormatException();
		}

		return res;

	}

	private List<Result> filterUG(List<Result> results, ComboBox<EBedingung> cb, String textfield)
			throws NumberFormatException {

		List<Result> res = new ArrayList<>();

		try {
			switch (cb.getSelectionModel().getSelectedItem()) {

			case GLEICH:
				res = filter(results, (Result a) -> a.getLoLim() == Float.parseFloat(textfield));
				break;
			case UNGLEICH:
				res = filter(results, (Result a) -> a.getLoLim() != Float.parseFloat(textfield));
				break;
			case KLEINER:
				res = filter(results, (Result a) -> a.getLoLim() < Float.parseFloat(textfield));
				break;
			case KLEINER_GLEICH:
				res = filter(results, (Result a) -> a.getLoLim() <= Float.parseFloat(textfield));
				break;
			case GROESSER:
				res = filter(results, (Result a) -> a.getLoLim() > Float.parseFloat(textfield));
				break;
			case GROESSER_GLEICH:
				res = filter(results, (Result a) -> a.getLoLim() >= Float.parseFloat(textfield));
				break;

			default:
				break;
			}

		} catch (NumberFormatException e) {
			throw new NumberFormatException();
		}

		return res;

	}

	private List<Result> filterOG(List<Result> results, ComboBox<EBedingung> cb, String textfield)
			throws NumberFormatException {

		List<Result> res = new ArrayList<>();

		try {
			switch (cb.getSelectionModel().getSelectedItem()) {

			case GLEICH:
				res = filter(results, (Result a) -> a.getUpLim() == Float.parseFloat(textfield));
				break;
			case UNGLEICH:
				res = filter(results, (Result a) -> a.getUpLim() != Float.parseFloat(textfield));
				break;
			case KLEINER:
				res = filter(results, (Result a) -> a.getUpLim() < Float.parseFloat(textfield));
				break;
			case KLEINER_GLEICH:
				res = filter(results, (Result a) -> a.getUpLim() <= Float.parseFloat(textfield));
				break;
			case GROESSER:
				res = filter(results, (Result a) -> a.getUpLim() > Float.parseFloat(textfield));
				break;
			case GROESSER_GLEICH:
				res = filter(results, (Result a) -> a.getUpLim() >= Float.parseFloat(textfield));
				break;

			default:
				break;
			}

		} catch (NumberFormatException e) {
			throw new NumberFormatException();
		}

		return res;

	}

	private List<Result> filterSerial(List<Result> results, ComboBox<EBedingung> cb, String textfield) {

		switch (cb.getSelectionModel().getSelectedItem()) {

		case GLEICH:
			results = filter(results, (Result a) -> a.getSerial().equalsIgnoreCase(textfield));
			break;
		case UNGLEICH:
			results = filter(results, (Result a) -> !a.getSerial().equalsIgnoreCase(textfield));
			break;
		case KLEINER:
			results = filter(results, (Result a) -> a.getSerial().compareTo(textfield) < 0);
			break;
		case KLEINER_GLEICH:
			results = filter(results, (Result a) -> a.getSerial().compareTo(textfield) <= 0);
			break;
		case GROESSER:
			results = filter(results, (Result a) -> a.getSerial().compareTo(textfield) > 0);
			break;
		case GROESSER_GLEICH:
			results = filter(results, (Result a) -> a.getSerial().compareTo(textfield) >= 0);
			break;

		default:
			break;
		}

		return results;

	}

	private List<Result> filterRemark(List<Result> results, ComboBox<EBedingung> cb, String textfield) {

		switch (cb.getSelectionModel().getSelectedItem()) {

		case GLEICH:
			results = filter(results, (Result a) -> a.getRemark().equalsIgnoreCase(textfield));
			break;
		case UNGLEICH:
			results = filter(results, (Result a) -> !a.getRemark().equalsIgnoreCase(textfield));
			break;
		case KLEINER:
			results = filter(results, (Result a) -> a.getRemark().compareTo(textfield) < 0);
			break;
		case KLEINER_GLEICH:
			results = filter(results, (Result a) -> a.getRemark().compareTo(textfield) <= 0);
			break;
		case GROESSER:
			results = filter(results, (Result a) -> a.getRemark().compareTo(textfield) > 0);
			break;
		case GROESSER_GLEICH:
			results = filter(results, (Result a) -> a.getRemark().compareTo(textfield) >= 0);
			break;

		default:
			break;
		}

		return results;

	}

	private List<Result> filterWT(List<Result> results, ComboBox<EBedingung> cb, String textfield) {

		switch (cb.getSelectionModel().getSelectedItem()) {

		case GLEICH:
			results = filter(results, (Result a) -> a.getWt().equalsIgnoreCase(textfield));
			break;
		case UNGLEICH:
			results = filter(results, (Result a) -> !a.getWt().equalsIgnoreCase(textfield));
			break;
		case KLEINER:
			results = filter(results, (Result a) -> a.getWt().compareTo(textfield) < 0);
			break;
		case KLEINER_GLEICH:
			results = filter(results, (Result a) -> a.getWt().compareTo(textfield) <= 0);
			break;
		case GROESSER:
			results = filter(results, (Result a) -> a.getWt().compareTo(textfield) > 0);
			break;
		case GROESSER_GLEICH:
			results = filter(results, (Result a) -> a.getWt().compareTo(textfield) >= 0);
			break;

		default:
			break;
		}

		return results;

	}

	public List<Result> filter(List<Result> results, Predicate<Result> p) {
		List<Result> result = new ArrayList<>();
		for (Result r : results) {
			if (p.test(r)) {
				result.add(r);
			}
		}
		return result;
	}

	private List<Result> filterStatus(List<Result> results) {

		List<Result> filterStatus = new ArrayList<>();

		switch (cbStatus.getSelectionModel().getSelectedItem()) {
		case ALL:
			filterStatus = filter(results, (Result a) -> true);
			break;
		case UNBEWERTET:
			filterStatus = filter(results, (Result a) -> a.getState() == 0);
			break;
		case NOK:
			filterStatus = filter(results, (Result a) -> a.getState() == 1);
			break;
		case OK:
			filterStatus = filter(results, (Result a) -> a.getState() == 2);
			break;
		case UNDEFINIERT:
			filterStatus = filter(results, (Result a) -> a.getState() < 0 || a.getState() > 2);
			break;

		default:
			filterStatus = filter(results, (Result a) -> true);
			break;
		}

		return filterStatus;

	}

	private void checkFilterWert() {

		// Filter Wert 1 und 2 Beistrich durch Punkt ersetzen (abhängig vom
		// ausgewählten Parameter)
		if (cbParameter1.getSelectionModel().getSelectedItem() == EParameter.VALUE
				|| cbParameter1.getSelectionModel().getSelectedItem() == EParameter.UG
				|| cbParameter1.getSelectionModel().getSelectedItem() == EParameter.OG) {

			valueWert1.setText(valueWert1.getText().replace(",", "."));

		}

		if ((cbParameter2.getSelectionModel().getSelectedItem() == EParameter.VALUE
				|| cbParameter2.getSelectionModel().getSelectedItem() == EParameter.UG
				|| cbParameter2.getSelectionModel().getSelectedItem() == EParameter.OG)
				&& (cbLogik.getSelectionModel().getSelectedItem() != ELogik.KEIN)) {

			valueWert2.setText(valueWert2.getText().replace(",", "."));

		}
	}

	@FXML
	private void handleOK() {
		getFilteredResults(true);

	}

	public void getFilteredResults(boolean okClicked) {

		long beginTime = System.currentTimeMillis();

		checkFilterWert();
		getFilterItems();

		filter.setActivated(true);
		lblAktiv.setVisible(filter.isActivated());

		Timestamp timestampVon = Timestamp.valueOf(filter.getLdtZeitraumVon());
		Timestamp timestampBis = Timestamp.valueOf(filter.getLdtZeitraumBis());
		List<Result> results = null;

		if (process != null) {

			if (filter.isLetztenNVor() && (filter.getAnzahlN() != null)) {
				results = Service.getInstance().getResultsLastNfromDate(process, timestampBis,
						Integer.parseInt(filter.getAnzahlN()), mergeState());

			} else if (filter.isErstenNSeit() && (filter.getAnzahlN() != null)) {
				results = Service.getInstance().getResultsFirstNsinceDate(process, timestampVon,
						Integer.parseInt(filter.getAnzahlN()), mergeState());

			} else {
				results = Service.getInstance().getResults(process, timestampVon, timestampBis);
			}

			for (Result res : results) {

				float value;
				int decPoints;

				decPoints = process.getDecimalPoints();

				value = Float.parseFloat(DecimalPointFormatter.roundFloat2String(res.getValue(), decPoints));
				res.setValue(value);

				value = Float.parseFloat(DecimalPointFormatter.roundFloat2String(res.getUpLim(), decPoints));
				res.setUpLim(value);

				value = Float.parseFloat(DecimalPointFormatter.roundFloat2String(res.getLoLim(), decPoints));
				res.setLoLim(value);
			}

			try {

				processResults = getFilterResults(results);

				if (!Service.getInstance().isErrorStatus()) {

					if (okClicked) {
						this.okClicked = okClicked;
						this.dialogStage.close();
					}
				}

				if (processResults.size() == 0)
					showNoResultAlert();

			} catch (NumberFormatException e) {
				showNoResultAlert();
				e.printStackTrace();
			}

		} else {

			if (okClicked) {
				this.okClicked = okClicked;
				this.dialogStage.close();

			}
		}

		long endTime = System.currentTimeMillis();
		long duration = endTime - beginTime;

		if (process != null) {
			if (processResults.size() == 0) {
				dbQueryLoggerText = "Keine Ergebnisse gefunden (Ansicht gefiltert)";
			} else {
				dbQueryLoggerText = (String.format("%d Ergebnisse sichtbar (Executing time: %s - Ansicht gefiltert)",
						processResults.size(), DurationDateAndTime.getTimestampFromMilliseconds(duration)));
			}

			if (logger.isInfoEnabled()) {
				// logger.info(dbQueryLoggerText);
			}
		}

	}

	@FXML
	private void handleClose() {
		okClicked = false;
		this.dialogStage.close();
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;

	}

	private void initDateFilter() {

		if (!filter.isActivated()) {
			if (processResults != null) {
				if (processResults.size() > 0) {
					ldVon = processResults.get(processResults.size() - 1).getTimestampSql().toLocalDateTime();
					ldBis = processResults.get(0).getTimestampSql().toLocalDateTime();
				}

				else {

					setDateAndTimeToday();
				}

			} else {

				setDateAndTimeToLast24h();
			}
		}

		if (filter.isActivated()) {
			ldVon = filter.getLdtZeitraumVon();
			ldBis = filter.getLdtZeitraumBis();

		}

		ldtZeitraumVon.setLocalDateTime(ldVon);
		ldtZeitraumBis.setLocalDateTime(ldBis);

	}

	private void setDateAndTimeToShiftX(int reqShift) {

		int actShift = 0;
		int lastShift = 0;
		boolean actshift3IsAfterMidnight = false;
		boolean actshift3IsBeforeMidnight = false;

		LocalTime actTime = LocalTime.now();

		LocalTime shift1StartTime = LocalTime.parse(settings.getShift1StartTime());
		LocalTime shift2StartTime = LocalTime.parse(settings.getShift2StartTime());
		LocalTime shift3StartTime = LocalTime.parse(settings.getShift3StartTime());

		LocalTime shift1EndTime = shift2StartTime;
		LocalTime shift2EndTime = shift3StartTime;
		LocalTime shift3EndTime = shift1StartTime;

		if (actTime.isAfter(shift1StartTime) && actTime.isBefore(shift1EndTime)) {
			actShift = 1;
			lastShift = 3;

		} else if (actTime.isAfter(shift2StartTime) && actTime.isBefore(shift2EndTime)) {
			actShift = 2;
			lastShift = 1;

		} else {
			actShift = 3;
			lastShift = 2;

			if (actTime.getHour() >= shift3StartTime.getHour())
				actshift3IsBeforeMidnight = true;
			else
				actshift3IsAfterMidnight = true;
		}

		if (reqShift == 1) {
			if (actShift == 3 && actshift3IsAfterMidnight) {
				ldVon = LocalDateTime.of(LocalDate.now().minusDays(1), shift1StartTime);
				ldBis = LocalDateTime.of(LocalDate.now().minusDays(1), shift1EndTime);

			} else {
				ldVon = LocalDateTime.of(LocalDate.now(), shift1StartTime);
				ldBis = LocalDateTime.of(LocalDate.now(), shift1EndTime);

			}
		}

		if (reqShift == 2) {
			if (actShift == 3 && actshift3IsAfterMidnight) {
				ldVon = LocalDateTime.of(LocalDate.now().minusDays(1), shift2StartTime);
				ldBis = LocalDateTime.of(LocalDate.now().minusDays(1), shift2EndTime);

			} else if (actShift == 1) {
				ldVon = LocalDateTime.of(LocalDate.now().minusDays(1), shift2StartTime);
				ldBis = LocalDateTime.of(LocalDate.now().minusDays(1), shift2EndTime);

			} else {
				ldVon = LocalDateTime.of(LocalDate.now(), shift2StartTime);
				ldBis = LocalDateTime.of(LocalDate.now(), shift2EndTime);

			}
		}

		if (reqShift == 3) {
			if (actShift == 3 && actshift3IsBeforeMidnight) {
				ldVon = LocalDateTime.of(LocalDate.now(), shift3StartTime);
				ldBis = LocalDateTime.of(LocalDate.now().plusDays(1), shift3EndTime);

			} else {
				ldVon = LocalDateTime.of(LocalDate.now().minusDays(1), shift3StartTime);
				ldBis = LocalDateTime.of(LocalDate.now(), shift3EndTime);

			}
		}

		ldtZeitraumVon.setLocalDateTime(ldVon);
		ldtZeitraumBis.setLocalDateTime(ldBis);

	}

	private void setDateAndTimeToLast24h() {
		Calendar cal = Calendar.getInstance();
		ldVon = LocalDateTime.of(LocalDate.now().minusDays(1),
				LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));
		ldBis = LocalDateTime.of(LocalDate.now(),
				LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));

		ldtZeitraumVon.setLocalDateTime(ldVon);
		ldtZeitraumBis.setLocalDateTime(ldBis);
	}

	private void setDateAndTimeToday() {
		ldVon = LocalDateTime.of(LocalDate.now(), LocalTime.parse("00:00:00"));
		ldBis = LocalDateTime.of(LocalDate.now(), LocalTime.parse("23:59:59"));

		ldtZeitraumVon.setLocalDateTime(ldVon);
		ldtZeitraumBis.setLocalDateTime(ldBis);
	}

	public void setFilterData(Filter filter) {

		this.filter = filter;

		if (process != null)
			this.dialogStage.setTitle(
					process.getStation() + ": " + process.getName() + " - " + resources.getString("filtersettings"));
		else
			this.dialogStage.setTitle(resources.getString("filtersettings"));

		setFilterItems();
		initDateFilter();

		lblAktiv.setVisible(filter.isActivated());
	}

	public Filter getFilterData() {
		return this.filter;
	}

	public String getDbQueryLoggerText() {
		return dbQueryLoggerText;
	}

	public List<Result> getProcessResults() {
		return processResults;
	}

	public void setProcessResults(List<Result> processResults) {
		this.processResults = processResults;
	}

	public void setData(Process process, List<Result> processResults) {
		this.process = process;
		this.processResults = processResults;
	}

	public boolean isOkClicked() {
		return okClicked;
	}

	public Button getUebernehmenButton() {
		return btnUebernehmen;
	}

	private int mergeState() {

		if (EStatus.ALL == cbStatus.getSelectionModel().getSelectedItem())
			return -1;

		if (EStatus.UNDEFINIERT == cbStatus.getSelectionModel().getSelectedItem())
			return -2;

		if (EStatus.UNBEWERTET == cbStatus.getSelectionModel().getSelectedItem())
			return 0;

		if (EStatus.NOK == cbStatus.getSelectionModel().getSelectedItem())
			return 1;

		if (EStatus.OK == cbStatus.getSelectionModel().getSelectedItem())
			return 2;

		return 0;

	}

	public enum EStatus {

		ALL("alle"), OK("in Ordnung"), NOK("nicht in Ordnung"), UNBEWERTET("unbewertet"), UNDEFINIERT("undefiniert");

		private String label;

		EStatus(String label) {
			this.label = label;

		}

		@Override
		public String toString() {
			return label;
		}
	}

	public enum EBedingung {

		GLEICH("gleich"), UNGLEICH("ungleich"), KLEINER("kleiner"), KLEINER_GLEICH("kleiner gleich"), GROESSER(
				"größer"), GROESSER_GLEICH("größer gleich");

		private String label;

		EBedingung(String label) {
			this.label = label;

		}

		@Override
		public String toString() {
			return label;
		}
	}

	public enum EParameter {

		KEIN("kein"), VALUE("Messwert", "value"), UG("Untergrenze", "min"), OG("Obergrenze", "max"), SERIAL(
				"Seriennummer",
				"serial"), TYP("Typen-Nummer", "typ_nr"), WT("WT-Nummer", "wt_nr"), REMARK("Bemerkung", "remark");

		private String label;
		private String dbColName;

		EParameter(String label) {
			this.label = label;

		}

		EParameter(String label, String dbColName) {
			this.label = label;
			this.dbColName = dbColName;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	public enum ELogik {

		KEIN("kein"), UND("und"), ODER("oder");

		private String label;

		ELogik(String label) {
			this.label = label;

		}

		@Override
		public String toString() {
			return label;
		}
	}

	public enum ESortierung {

		DESC("Zeitstempel absteigend"), ASC("Zeitstempel aufsteigend");

		private String label;

		ESortierung(String label) {
			this.label = label;

		}

		@Override
		public String toString() {
			return label;
		}
	}

	@FXML
	public void handleSave() {

		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("filter files", "*.filter"));

		File file = chooser.showSaveDialog(dialogStage);
		if (file != null) {
			try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {

				getFilterItems();
				out.writeObject(filter);

				SaveSucessInfo info = new SaveSucessInfo(dialogStage, file);
				info.showAndWait();

			} catch (Exception e) {
				e.printStackTrace();

				logger.error(e);

				FileSaveFailedAlert alert = new FileSaveFailedAlert(this.dialogStage, file, e);
				alert.showAndWait();

			}
		}
	}

	@FXML
	public void handleLoad() {

		FileChooser chooser = new FileChooser();
		chooser.getExtensionFilters().add(new ExtensionFilter("filter files", "*.filter"));

		File file = chooser.showOpenDialog(dialogStage);

		if (file != null) {
			try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {

				filter = (Filter) in.readObject();
				setFilterItems();

				LoadSucessInfo info = new LoadSucessInfo(dialogStage, file);
				info.showAndWait();

			} catch (Exception e) {
				e.printStackTrace();

				logger.error(e);

				FileLoadFailedAlert alert = new FileLoadFailedAlert(this.dialogStage, file, e);
				alert.showAndWait();

			}
		}
	}

	private void getFilterItems() {

		filter.setLdtZeitraumVon(ldtZeitraumVon.getLocalDateTime());
		filter.setLdtZeitraumBis(ldtZeitraumBis.getLocalDateTime());

		filter.setBedingung1(cbBedingung1.getSelectionModel().getSelectedIndex());
		filter.setBedingung2(cbBedingung2.getSelectionModel().getSelectedIndex());
		filter.setLogik(cbLogik.getSelectionModel().getSelectedIndex());
		filter.setParameter1(cbParameter1.getSelectionModel().getSelectedIndex());
		filter.setParameter2(cbParameter2.getSelectionModel().getSelectedIndex());
		filter.setStatus(cbStatus.getSelectionModel().getSelectedIndex());
		filter.setWert1(valueWert1.getText());
		filter.setWert2(valueWert2.getText());
		filter.setAnzahlN(valueAnzahlN.getText());
		filter.setLetztenNVor(cbLetztenNVor.isSelected());
		filter.setErstenNSeit(cbErstenNSeit.isSelected());

		filter.setSortierung(cbSortierung.getSelectionModel().getSelectedIndex());

	}

	private void setFilterItems() {

		cbBedingung1.getSelectionModel().select(filter.getBedingung1());
		cbBedingung2.getSelectionModel().select(filter.getBedingung2());
		valueWert1.setText(filter.getWert1());
		valueWert2.setText(filter.getWert2());
		cbParameter1.getSelectionModel().select(filter.getParameter1());
		cbParameter2.getSelectionModel().select(filter.getParameter2());
		cbLogik.getSelectionModel().select(filter.getLogik());
		cbStatus.getSelectionModel().select(filter.getStatus());
		valueAnzahlN.setText(filter.getAnzahlN());
		cbLetztenNVor.setSelected(filter.isLetztenNVor());
		cbErstenNSeit.setSelected(filter.isErstenNSeit());
		cbSortierung.getSelectionModel().select(filter.getSortierung());

		if (filter.getParameter1() > 0) {
			cbBedingung1.setDisable(false);
			valueWert1.setDisable(false);
			cbLogik.setDisable(false);
		}

		if (filter.getLogik() > 0) {
			cbParameter2.setDisable(false);
			cbBedingung2.setDisable(false);
			valueWert2.setDisable(false);
		}

		if (filter.isLetztenNVor()) {
			valueAnzahlN.setDisable(false);
			ldtZeitraumVon.setDisable(true);
		}

		if (filter.isErstenNSeit()) {
			valueAnzahlN.setDisable(false);
			ldtZeitraumBis.setDisable(true);
		}

		if (filter.getLdtZeitraumVon() != null)
			ldtZeitraumVon.setLocalDateTime(filter.getLdtZeitraumVon());

		if (filter.getLdtZeitraumBis() != null)
			ldtZeitraumBis.setLocalDateTime(filter.getLdtZeitraumBis());
	}

	private void showNoResultAlert() {
		NoResultAlert alert = new NoResultAlert(this.dialogStage, null);

		if (process != null)
			alert = new NoResultAlert(this.dialogStage, "'" + process.getStation() + ": " + process.getName() + "'");

		alert.showAndWait();

	}

	class ResultSort implements Comparator<Result> {
		@Override
		public int compare(Result a1, Result a2) {

			if (a1.getTimestamp() == null || a2.getTimestamp() == null)
				return a1.getNr() - a2.getNr();

			if (cbSortierung.getSelectionModel().getSelectedItem() == ESortierung.ASC)
				return a1.getTimestamp().toLowerCase().compareTo(a2.getTimestamp().toLowerCase()) * 1;

			if (cbSortierung.getSelectionModel().getSelectedItem() == ESortierung.DESC)
				return a1.getTimestamp().toLowerCase().compareTo(a2.getTimestamp().toLowerCase()) * -1;

			return a1.getTimestamp().toLowerCase().compareTo(a2.getTimestamp().toLowerCase()) * -1;
		}
	}
}
