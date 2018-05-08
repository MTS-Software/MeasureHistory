package general;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TableViewDragAndDrop extends Application {
	// The default URL.
	public static final String DEFAULT_URL = "about:blank";

	// For initialization of Browser front end stage.
	@Override
	public void start(Stage primaryStage) {
		TableView<Item> table = new TableView<>();

		Button button = new Button("A");

		// d&d source providing next char
		button.setOnDragDetected(evt -> {
			Dragboard db = button.startDragAndDrop(TransferMode.MOVE);
			ClipboardContent content = new ClipboardContent();
			content.putString(button.getText());
			db.setContent(content);
		});
		button.setOnDragDone(evt -> {
			if (evt.isAccepted()) {
				// next char
				button.setText(Character.toString((char) (button.getText().charAt(0) + 1)));
			}
		});

		// accept for empty table too
		table.setOnDragOver(evt -> {
			if (evt.getDragboard().hasString()) {
				evt.acceptTransferModes(TransferMode.COPY_OR_MOVE);
			}
			evt.consume();
		});
		table.setOnDragDropped(evt -> {
			Dragboard db = evt.getDragboard();
			if (db.hasString()) {
				table.getItems().add(new Item(db.getString()));
				evt.setDropCompleted(true);
			}
			evt.consume();
		});

		TableColumn<Item, String> col = new TableColumn<>("value");
		col.setCellValueFactory(new PropertyValueFactory<>("value"));
		table.getColumns().add(col);

		// let rows accept drop too
		table.setRowFactory(tv -> {
			TableRow<Item> row = new TableRow();
			row.setOnDragOver(evt -> {
				if (evt.getDragboard().hasString()) {
					evt.acceptTransferModes(TransferMode.COPY_OR_MOVE);
				}
				evt.consume();
			});
			row.setOnDragDropped(evt -> {
				Dragboard db = evt.getDragboard();
				if (db.hasString()) {
					Item item = new Item(db.getString());
					if (row.isEmpty()) {
						// row is empty (at the end -> append item)
						table.getItems().add(item);
					} else {
						// decide based on drop position whether to add the element before or after
						int offset = evt.getY() > row.getHeight() / 2 ? 1 : 0;
						table.getItems().add(row.getIndex() + offset, item);
						evt.setDropCompleted(true);
					}
				}
				evt.consume();
			});
			return row;
		});

		Scene scene = new Scene(new VBox(button, table));

		primaryStage.setScene(scene);
		primaryStage.show();
	}

	public class Item {

		public Item() {
		}

		public Item(String value) {
			this.value.set(value);
		}

		private final StringProperty value = new SimpleStringProperty();

		public String getValue() {
			return value.get();
		}

		public void setValue(String val) {
			value.set(val);
		}

		public StringProperty valueProperty() {
			return value;
		}

	}

	public static void main(String args[]) {
		launch(args);
	}
}