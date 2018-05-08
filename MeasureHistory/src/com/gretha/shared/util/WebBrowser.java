package com.gretha.shared.util;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Ermöglicht ein browsen im Internet ohne Standardbrowser
 * 
 * @author Michael Grebesits, Ing.
 * 
 * @version 1.0
 * 
 */
public class WebBrowser {

	private static final int MAX_TAB_NAME_LENGTH = 40;
	private static final String NAME_NEW_TAB = generateTabTitle("Neuer Tab", MAX_TAB_NAME_LENGTH);
	private static final String SIGN_NEW_TAB = "+";
	private static final String ABOUT_BLANK = "about:blank";
	private static final String EMPTY_PAGE = "Leere Seite";
	private static final String PAGE_NOT_FOUND = "Fehler: Server nicht gefunden";
	private static final String PAGE_NOT_FOUND_HTML = "<html dir=ltr><head></head><body contenteditable=true><p><font size=6 color=red><b>Fehler: Seite nicht gefunden!</b></font></p><p><b><font size=5>Bitte überprüfen Sie die Adresse.</font></b></p><p><b><span x-large;=><font size=5>Wenn Sie auch keine andere Website aufrufen können, überprüfen Sie bitte die Netzwerk-/Internetverbindung.</font></span></b></p></body></html>";

	private static TabPane tabPane;
	private static Stage stage;

	/**
	 * Öffnet den Custom JavaBrowser mit der vorgegebenen URL
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @param webadress
	 * @category Example for parameter webadress: http://www.magna.com/
	 * @category Example for parameter webadress: "" -> Empty Site
	 * @category Example for parameter webadress: "about:blank" -> Empty Site
	 */
	public static void openURLinJavaBrowser(String webadress) {

		Dimension dimenstion = Toolkit.getDefaultToolkit().getScreenSize();

		stage = new Stage();
		stage.getIcons().add(new Image(Constants.ICON_WEB_BROWSER));
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setWidth(dimenstion.getWidth() - 250);
		stage.setHeight(dimenstion.getHeight() - 250);
		stage.setMaximized(false);

		Scene scene = new Scene(new Group());
		scene.getStylesheets().add(Constants.STYLESHEET);

		tabPane = new TabPane();
		// Preferred Size of TabPane.
		tabPane.setPrefWidth(dimenstion.getWidth());
		tabPane.setPrefHeight(dimenstion.getHeight());
		// Placement of TabPane.
		tabPane.setSide(Side.TOP);

		final Tab newTab = new Tab();
		final Tooltip newTabTooltip = new Tooltip("neuer Tab (Strg+T)");
		newTab.setText(SIGN_NEW_TAB);
		newTab.setClosable(false);
		newTab.setTooltip(newTabTooltip);
		// Addition of New Tab to the tabpane.
		tabPane.getTabs().add(newTab);
		// createAndSelectNewTab(tabPane);
		createAndShowTab(webadress);
		initContexMenu();

		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldSelectedTab, Tab newSelectedTab) {

				tabPane.setOnMousePressed(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {

						if (event.getButton() == MouseButton.PRIMARY)

							if (newSelectedTab == newTab) {
								createAndShowTab("");
							}

					}
				});

				if (!tabPane.getSelectionModel().getSelectedItem().getText().equals(NAME_NEW_TAB))
					stage.setTitle(tabPane.getSelectionModel().getSelectedItem().getText());
				else
					stage.setTitle(EMPTY_PAGE);
			}
		});

		tabPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN).match(event)) {

					createAndShowTab("");

				}
			}
		});

		stage.setScene(scene);
		scene.setRoot(tabPane);
		stage.show();
	}

	private static void createAndShowTab(String webadress) {

		final Tab tab = new Tab(NAME_NEW_TAB);

		final AnchorPane anchorPane = new AnchorPane();

		final HBox hBoxTabTop = new HBox();
		final TextField urlInput = new TextField();

		urlInput.setPromptText("Adresse eingeben");

		final VBox vBoxState = new VBox();
		final ProgressBar progressBar = new ProgressBar();
		final Label stateLabel = new Label();

		stateLabel.setTextFill(Color.BLACK);
		stateLabel.setFont(Font.font("System", 11));
		progressBar.setPrefWidth(300);
		vBoxState.setAlignment(Pos.CENTER_LEFT);
		vBoxState.getChildren().addAll(progressBar, stateLabel);

		urlInput.setPrefWidth(tabPane.getPrefWidth() - progressBar.getPrefWidth());

		hBoxTabTop.setAlignment(Pos.CENTER);
		hBoxTabTop.getChildren().addAll(urlInput, vBoxState);
		hBoxTabTop.setPadding(new Insets(10, 10, 10, 10));
		hBoxTabTop.setSpacing(10);
		AnchorPane.setRightAnchor(hBoxTabTop, 0.0);
		AnchorPane.setLeftAnchor(hBoxTabTop, 0.0);

		StackPane stackPaneBrowser = new StackPane();
		WebView webView = new WebView();

		stackPaneBrowser.getChildren().addAll(webView);
		AnchorPane.setLeftAnchor(stackPaneBrowser, 0.0);
		AnchorPane.setRightAnchor(stackPaneBrowser, 0.0);
		AnchorPane.setTopAnchor(stackPaneBrowser, 55.0);
		AnchorPane.setBottomAnchor(stackPaneBrowser, 0.0);

		final WebEngine webEngine = webView.getEngine();
		webEngine.load(formatWebadress(webadress));
		stage.setTitle(EMPTY_PAGE);

		if (webEngine.getLocation() != null) {

			if (!webEngine.getLocation().isEmpty() && !webEngine.getLocation().equals(ABOUT_BLANK)) {

				urlInput.setText(webEngine.getLocation());
				stage.setTitle(webEngine.getLocation());
				tab.setText(generateTabTitle(webEngine.getLocation(), MAX_TAB_NAME_LENGTH));
			}

		}

		// A Worker load the page
		Worker<Void> worker = webEngine.getLoadWorker();

		// Listening to the status of worker
		worker.stateProperty().addListener(new ChangeListener<State>() {

			@Override
			public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {

				stateLabel.setText("State: " + newValue.toString());

				if (newValue == Worker.State.SUCCEEDED || newValue == Worker.State.RUNNING) {

					if (webEngine.getLocation() != null) {

						if (!webEngine.getLocation().isEmpty()) {
							urlInput.setText(webEngine.getLocation());
						}
					}

					if (webEngine.getLocation().equals(ABOUT_BLANK)) {
						stage.setTitle(EMPTY_PAGE);
						tab.setText(generateTabTitle(EMPTY_PAGE, MAX_TAB_NAME_LENGTH));

					}

					if (webEngine.getTitle() != null) {

						if (!webEngine.getTitle().isEmpty()) {
							stage.setTitle(webEngine.getTitle());
							tab.setText(generateTabTitle(webEngine.getTitle(), MAX_TAB_NAME_LENGTH));
						}

					}
				}

				if (newValue == Worker.State.FAILED) {
					urlInput.setText(webEngine.getLocation());
					stage.setTitle(PAGE_NOT_FOUND);
					tab.setText(generateTabTitle(PAGE_NOT_FOUND, MAX_TAB_NAME_LENGTH));
					webView.getEngine().loadContent(PAGE_NOT_FOUND_HTML, "text/html");

				}
			}
		});

		// Bind the progress property of ProgressBar
		// with progress property of Worker
		progressBar.progressProperty().bind(worker.progressProperty());

		urlInput.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.F5) {
					webEngine.load(formatWebadress(urlInput.getText()));

				}

				if (event.getCode() == KeyCode.ESCAPE) {
					webEngine.getLoadWorker().cancel();

				}
			}
		});

		webView.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.F5) {

					if (!urlInput.getText().isEmpty())
						webEngine.load(formatWebadress(urlInput.getText()));
				}

				if (event.getCode() == KeyCode.ESCAPE) {
					webEngine.getLoadWorker().cancel();

				}
			}
		});

		anchorPane.getChildren().addAll(hBoxTabTop, stackPaneBrowser);
		AnchorPane.setRightAnchor(anchorPane, 0.0);
		AnchorPane.setLeftAnchor(anchorPane, 0.0);
		AnchorPane.setTopAnchor(anchorPane, 0.0);
		AnchorPane.setBottomAnchor(anchorPane, 0.0);

		tab.setContent(anchorPane);
		tab.closableProperty().bind(Bindings.size(tabPane.getTabs()).greaterThan(0));
		tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab);
		tabPane.getSelectionModel().select(tab);

	}

	private static String formatWebadress(String webadress) {

		String formatedWebadress = webadress;

		if (!webadress.isEmpty() && !webadress.equals(ABOUT_BLANK)) {

			if (webadress.startsWith("http://") && !webadress.startsWith("https://"))
				formatedWebadress = webadress.startsWith("http://") ? webadress : "http://" + webadress;

			else if (!webadress.startsWith("http://") && webadress.startsWith("https://"))
				formatedWebadress = webadress.startsWith("https://") ? webadress : "http://" + webadress;

			else
				formatedWebadress = webadress.startsWith("http://") ? webadress : "http://" + webadress;

		}

		return formatedWebadress;
	}

	private static void initContexMenu() {

		// Create ContextMenu
		ContextMenu contextMenu = new ContextMenu();

		MenuItem miCloseAllTabs = new MenuItem("Alle Tabs schließen");
		miCloseAllTabs.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {

				final ObservableList<Tab> tabsToRemove = FXCollections.observableArrayList();

				for (int i = 0; i < tabPane.getTabs().size(); i++) {
					if (tabPane.getTabs().get(i).isClosable()
							&& !tabPane.getTabs().get(i).getText().equals(SIGN_NEW_TAB)) {
						tabsToRemove.addAll(tabPane.getTabs().get(i));
					}
				}

				for (int i = 0; i < tabsToRemove.size(); i++) {
					tabPane.getTabs().remove(tabsToRemove.get(i));
				}

				createAndShowTab("");
			}

		});

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(miCloseAllTabs);

		// When user right-click
		tabPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				contextMenu.hide();

				if (mouseEvent.getButton() == MouseButton.SECONDARY && !mouseEvent.isShortcutDown()) {
					contextMenu.show(tabPane, mouseEvent.getScreenX(), mouseEvent.getScreenY());

				}
			}
		});

	}

	/**
	 * Öffnet den Standardbrowser mit der vorgegebenen URL
	 * 
	 * @author Michael Grebesits, Ing.
	 * 
	 * @param webadress
	 * @category Example for parameter webadress: http://www.google.com/
	 */
	public static void openURLinStandardBrowser(String webadress) {

		URL url = null;

		try {
			url = new URL("http://" + webadress);

		} catch (MalformedURLException e) {
			e.printStackTrace();
			showExceptionMessage(e);

		}

		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(url.toURI());

			} catch (IOException e) {
				e.printStackTrace();
				showExceptionMessage(e);

			} catch (URISyntaxException e) {
				e.printStackTrace();
				showExceptionMessage(e);
			}
		}
	}

	private static String generateTabTitle(String tabName, int maxTabNameLength) {
		String s = String.format("%s" + "%" + maxTabNameLength + "s", tabName, "");
		return s.substring(0, maxTabNameLength);
	}

	private static void showExceptionMessage(Exception e) {
		showExceptionAlertDialog(e);
	}

	private static void showExceptionAlertDialog(Exception e) {

		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Exception Dialog");
		alert.setContentText(e.getMessage() + "\nException: " + e.getClass().getName());
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().addAll(Constants.STYLESHEET);

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String exceptionText = sw.toString();

		Label label = new Label("The exception stacktrace was: ");

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.setWrapText(true);

		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setExpandableContent(expContent);

		alert.showAndWait();

	}

}
