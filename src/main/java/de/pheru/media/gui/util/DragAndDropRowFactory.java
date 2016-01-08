package de.pheru.media.gui.util;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.control.Alert;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.util.Callback;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Philipp Bruckner
 */
public class DragAndDropRowFactory<T> implements Callback<TableView<T>, TableRow<T>> {

    private static final Logger LOGGER = LogManager.getLogger(DragAndDropRowFactory.class);

    private static final String DRAG_OVER_STYLECLASS = "drag-over";

    //VirtualFlow & TableViewSkin -> public API mit Java 9. Siehe: http://openjdk.java.net/jeps/253
    private final Callback<TableView<T>, TableRow<T>> baseFactory;
    private VirtualFlow<?> virtualFlow;
    private final T emptyData;
    private EventHandler<DropCompletedEvent> dropCompletedHandler;

    public DragAndDropRowFactory(TableView<T> table, T emptyData, Callback<TableView<T>, TableRow<T>> baseFactory) {
        this.baseFactory = baseFactory;
        this.emptyData = emptyData;
        Platform.runLater(() -> {
            TableViewSkin<?> tableSkin = (TableViewSkin<?>) table.getSkin();
            virtualFlow = (VirtualFlow<?>) tableSkin.getChildren().get(1);
        });
    }

    public DragAndDropRowFactory(TableView<T> table, T emptyData) {
        this(table, emptyData, null);
    }

    @Override
    public TableRow<T> call(TableView<T> tableView) {
        final TableRow<T> row;
        if (baseFactory == null) {
            row = new TableRow<>();
        } else {
            row = baseFactory.call(tableView);
        }

        row.setOnDragDetected(createDragDetectedHandler(row, tableView));
        row.setOnDragOver(createDragOverHandler(row));
        row.setOnDragExited(createDragExitedHandler(row));
        row.setOnDragDropped(createDragDroppedHandler(row, tableView));
        return row;
    }

    private EventHandler<MouseEvent> createDragDetectedHandler(TableRow<T> row, TableView<T> table) {
        return (MouseEvent event) -> {
            Dragboard db = row.startDragAndDrop(TransferMode.ANY);
            db.setDragView(row.snapshot(null, null));
            ClipboardContent content = new ClipboardContent();
            String indicesAsString = table.getSelectionModel().getSelectedIndices().stream()
                    .map((Integer t) -> t.toString())
                    .collect(Collectors.joining("-"));
            content.putString(indicesAsString);
            db.setContent(content);
            table.getItems().add(emptyData);
        };
    }

    private EventHandler<DragEvent> createDragOverHandler(TableRow<T> row) {
        return (DragEvent event) -> {
            if (event.getDragboard().hasString() && row.getItem() != null) {
                if (!row.getStyleClass().contains(DRAG_OVER_STYLECLASS)) {
                    row.getStyleClass().add(DRAG_OVER_STYLECLASS);
                }
                if (row.getIndex() <= virtualFlow.getFirstVisibleCell().getIndex() + 1) {
                    virtualFlow.adjustPixels(-5);
                }
                if (row.getIndex() >= virtualFlow.getLastVisibleCell().getIndex() - 1) {
                    virtualFlow.adjustPixels(5);
                }
                event.acceptTransferModes(TransferMode.ANY);
            }
        };
    }

    private EventHandler<DragEvent> createDragExitedHandler(TableRow<T> row) {
        return (DragEvent event) -> {
            row.getStyleClass().remove(DRAG_OVER_STYLECLASS);
        };
    }

    private EventHandler<DragEvent> createDragDroppedHandler(TableRow<T> row, TableView<T> table) {
        return (DragEvent event) -> {
            try {
                DropCompletedEvent dropCompletedEvent = new DropCompletedEvent();

                Dragboard db = event.getDragboard();
                int initialTargetIndex = row.getIndex();
                dropCompletedEvent.setTargetIndex(initialTargetIndex);
                String[] incomingIndices = db.getString().split("-");
                if (initialTargetIndex < 0) {
                    initialTargetIndex = 0;
                } else if (initialTargetIndex >= table.getItems().size()) {
                    initialTargetIndex = table.getItems().size() - 1;
                }
                table.getSelectionModel().clearSelection();

                int adjustedTargetIndex = initialTargetIndex;
                List<T> dragData = new ArrayList<>();
                for (String s : incomingIndices) {
                    int incomingIndex = Integer.parseInt(s);
                    dragData.add(table.getItems().remove(incomingIndex - dragData.size()));
                    if (incomingIndex < initialTargetIndex) {
                        adjustedTargetIndex--;
                    }
                }
                table.getItems().remove(emptyData);

//                for (int i = dragData.size() - 1; i >= 0; i--) {
//                    table.getItems().add(adjustedTargetIndex, dragData.get(i));
//                    table.getSelectionModel().select(adjustedTargetIndex);
//                }
                for (int i = 0; i < dragData.size(); i++) {
                    table.getItems().add(adjustedTargetIndex + i, dragData.get(i));
                    table.getSelectionModel().select(adjustedTargetIndex + i);
                    dropCompletedEvent.getMovedIndices().add(new Pair<>(Integer.parseInt(incomingIndices[i]), adjustedTargetIndex + i));
                }

                event.setDropCompleted(true);
                if (dropCompletedHandler != null) {
                    dropCompletedHandler.handle(dropCompletedEvent);
                }
            } catch (Exception e) {
                LOGGER.error("Exception while Drag&Drop!", e);
                //TODO FX-Thread? (Alert)
                Alert alert = new Alert(Alert.AlertType.ERROR, "Fehler beim Ausführen des Drag&Drop!");
                alert.showAndWait();
            }
        };
    }

    public final void setOnDropCompleted(EventHandler<DropCompletedEvent> value) {
        this.dropCompletedHandler = value;
    }

    public static class DropCompletedEvent extends Event {

        public static final EventType<DropCompletedEvent> TYPE = new EventType<>("DnDCompleted");

        private final List<Pair<Integer, Integer>> movedIndices = new ArrayList<>();
        private int targetIndex;

        public DropCompletedEvent() {
            super(TYPE);
        }

        public List<Pair<Integer, Integer>> getMovedIndices() {
            return movedIndices;
        }

        public int getTargetIndex() {
            return targetIndex;
        }

        public void setTargetIndex(int targetIndex) {
            this.targetIndex = targetIndex;
        }
    }
}