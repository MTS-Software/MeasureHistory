package general;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Example of displaying a splash page for a standalone JavaFX application
 */
public class FadeApp extends Application {
	public static final String APPLICATION_ICON = "com/application/resource/icons/MeasureHistory_128.png";
	public static final String SPLASH_IMAGE = "http://fxexperience.com/wp-content/uploads/2010/06/logo.png";

	private Pane splashLayout;
	private ProgressBar loadProgress;
	private Label progressText;
	private Stage mainStage;
	private static final int SPLASH_WIDTH = 676;
	private static final int SPLASH_HEIGHT = 227;

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	@Override
	public void init() {

		ImageView splash = new ImageView(new Image(SPLASH_IMAGE));
		loadProgress = new ProgressBar();
		loadProgress.setPrefWidth(SPLASH_WIDTH - 20);
		progressText = new Label("Will find processes. . .");
		splashLayout = new VBox();
		splashLayout.getChildren().addAll(splash, loadProgress, progressText);
		progressText.setAlignment(Pos.CENTER);
		splashLayout.setStyle(
				"-fx-padding: 5; " + "-fx-background-color: cornsilk; " + "-fx-border-width:5; " + "-fx-border-color: "
						+ "linear-gradient(" + "to bottom, " + "chocolate, " + "derive(chocolate, 50%)" + ");");
		splashLayout.setEffect(new DropShadow());
	}

	@Override
	public void start(final Stage initStage) throws Exception {
		final Task<ObservableList<String>> modulTask = new Task<ObservableList<String>>() {
			@Override
			protected ObservableList<String> call() throws InterruptedException {
				ObservableList<String> foundProcess = FXCollections.<String>observableArrayList();
				ObservableList<String> availableProcess = FXCollections.observableArrayList("M0100 Markus der Gott",
						"M0100 Michi der Jesus", "M0200 Geht nie gescheit", "M0200 Geht immer super",
						"M0300 Kann ab und zu ned gehen", "M1100 wird nie gehen");

				updateMessage("Prozesse laden...");
				for (int i = 0; i < availableProcess.size(); i++) {
					Thread.sleep(400);
					updateProgress(i + 1, availableProcess.size());
					String nextProcess = availableProcess.get(i);
					foundProcess.add(nextProcess);
					updateMessage("Prozess . . . gefunden " + nextProcess);
				}
				Thread.sleep(400);
				updateMessage("Alle Prozesse geladen.");

				return foundProcess;
			}
		};

		showSplash(initStage, modulTask, () -> showMainStage(modulTask.valueProperty()));
		new Thread(modulTask).start();
	}

	private void showMainStage(ReadOnlyObjectProperty<ObservableList<String>> process) {
		mainStage = new Stage(StageStyle.DECORATED);
		mainStage.setTitle("Meine Prozesse");
		mainStage.getIcons().add(new Image(APPLICATION_ICON));

		final ListView<String> processView = new ListView<>();
		processView.itemsProperty().bind(process);

		mainStage.setScene(new Scene(processView));
		mainStage.show();
	}

	private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
		progressText.textProperty().bind(task.messageProperty());
		loadProgress.progressProperty().bind(task.progressProperty());
		task.stateProperty().addListener((observableValue, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				loadProgress.progressProperty().unbind();
				loadProgress.setProgress(1);
				initStage.toFront();
				FadeTransition fadeSplash = new FadeTransition(Duration.seconds(1.2), splashLayout);
				fadeSplash.setFromValue(1.0);
				fadeSplash.setToValue(0.0);
				fadeSplash.setOnFinished(actionEvent -> initStage.hide());
				fadeSplash.play();

				initCompletionHandler.complete();
			} // todo add code to gracefully handle other task states.
		});

		Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
		final Rectangle2D bounds = Screen.getPrimary().getBounds();
		initStage.setScene(splashScene);
		initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
		initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
		initStage.initStyle(StageStyle.TRANSPARENT);
		initStage.setAlwaysOnTop(true);
		initStage.show();
	}

	public interface InitCompletionHandler {
		void complete();
	}
}
