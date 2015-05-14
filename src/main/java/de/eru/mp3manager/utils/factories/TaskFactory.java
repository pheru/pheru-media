package de.eru.mp3manager.utils.factories;

import de.eru.mp3manager.data.utils.Mp3Mapper;
import de.eru.mp3manager.data.Mp3FileData;
import de.eru.mp3manager.data.Playlist;
import de.eru.mp3manager.gui.applicationwindow.main.MainPresenter;
import de.eru.mp3manager.gui.utils.TablePlaceholders;
import de.eru.mp3manager.service.FileService;
import de.eru.mp3manager.service.RenameFileException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;

/**
 * Klasse zum erzeugen von Tasks.
 *
 * @author Philipp Bruckner
 */
public final class TaskFactory {

    private TaskFactory() {
        //Utility-Klasse
    }

    /**
     * Erzeugt einen Task zum Auslesen von Dateien aus einem Verzeichnis. <br>
     * Dabei werden aus den Dateien bereits Mp3FileData-Objekte erzeugt und den
     * Tabellen-Daten hinzugefügt; <br>
     * diese enthalten aber noch keine MP3-spezifischen Informationen!
     *
     * @param directory Das auszulesende Verzeichnis.
     * @param masterData Die Liste für die Mp3FileData-Objekte.
     * @return Einen Task zum Auslesen von Dateien aus einem Verzeichnis.
     */
    public static Task<Void> createReadDirectoryTask(String directory, ObservableList<Mp3FileData> masterData, ObjectProperty<Node> placeholderProperty,
            List<Mp3FileData> playlistTitles) {
        return new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    placeholderProperty.set(TablePlaceholders.READING_DIRECTORY);
                    masterData.clear();
                });
                //Verzeichnis auslesen
                updateTitle("Lese Verzeichnis...");
                updateMessage(directory);
                updateProgress(-1, 1);
                ObservableList<File> files = FileService.collectMp3FilesFromDirectory(directory);

                //Mp3Informationen laden und am Ende der Liste hinzufügen
                updateProgress(-1, 1);
                ObservableList<Mp3FileData> loadedData = FXCollections.observableArrayList();
                for (int i = 0; i < files.size(); i++) {
                    if (isCancelled()) {
                        updateTitle("Laden der Dateien abgebrochen!");
                        updateMessage(loadedData.size() + " von " + files.size() + " Dateien wurden erfolgreich geladen.");
                        updateProgress(1, 1);
                        break;
                    }
                    updateTitle("Lade Datei " + (i + 1) + " von " + files.size() + "...");
                    updateMessage(files.get(i).getAbsolutePath());
                    boolean dataAlreadyLoaded = false;
                    for (Mp3FileData title : playlistTitles) {
                        if (title.getAbsolutePath().equals(files.get(i).getAbsolutePath())) {
                            loadedData.add(title);
                            dataAlreadyLoaded = true;
                            break;
                        }
                    }
                    if (!dataAlreadyLoaded) {
                        loadedData.add(Mp3Mapper.fileToMp3FileData(new File(files.get(i).getAbsolutePath())));
                    }
                    updateProgress(i + 1, files.size());
                }
                if (!isCancelled()) {
                    updateTitle("Laden der Dateien abgeschlossen.");
                    updateMessage(loadedData.size() + " von " + files.size() + " Dateien wurden erfolgreich geladen.");
                }

                Platform.runLater(() -> {
                    if (!loadedData.isEmpty()) {
                        masterData.addAll(loadedData);
                        placeholderProperty.set(MainPresenter.DEFAULT_TABLE_PLACEHOLDER);
                    } else {
                        placeholderProperty.set(TablePlaceholders.EMPTY_DIRECTORY);
                        updateProgress(1, 1);
                    }
                });
                return null;
            }
        };
    }

    public static Task<Void> createSaveFilesTask(final ObservableList<Mp3FileData> dataToSave, final Mp3FileData changeData) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(-1, 1);
                for (int i = 0; i < dataToSave.size(); i++) {
                    if (isCancelled()) {
                        updateTitle("Speichern der Dateien abgebrochen!");
                        updateMessage(i + " von " + dataToSave.size() + " Dateien wurden erfolgreich gespeichert.");
                        updateProgress(1, 1);
                        return null;
                    }
                    updateTitle("Speichere Datei " + (i + 1) + " von " + dataToSave.size() + "...");
                    updateMessage(dataToSave.get(i).getAbsolutePath());
                    try {
                        FileService.saveMp3File(dataToSave.get(i), changeData);
                    } catch (RenameFileException e) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setHeaderText("Speichern fehlgeschlagen!");
                            alert.setContentText("Dateiname konnte nicht geändert werden!\n\nMöglicherweise enthält der Dateiname ungültige Zeichen oder eine Datei mit diesem Namen existiert bereits.");
                            //TODO DPI-Scaling nötig?
                            alert.getDialogPane().setPrefWidth(500.0);
                            alert.showAndWait();
                        });
                        updateTitle("Speichern der Dateien fehlgeschlagen!");
                        updateMessage(i + " von " + dataToSave.size() + " Dateien wurden erfolgreich gespeichert.");
                        updateProgress(1, 1);
                        cancel();
                        return null;
                    }
                    Platform.runLater(dataToSave.get(i)::reload);
                    updateProgress(i + 1, dataToSave.size());
                }
                
                updateTitle("Speichern der Dateien abgeschlossen.");
                updateMessage(dataToSave.size() + " von " + dataToSave.size() + " Dateien wurden erfolgreich gespeichert.");
                return null;
            }
        };
    }

    public static Task<Void> createLoadPlaylistTask(Playlist playlist, File playlistFile, List<Mp3FileData> masterData) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(-1, 1);
                updateTitle("Lade Wiedergabeliste...");
                updateMessage(playlistFile.getAbsolutePath());
                List<String> filePaths = FileService.loadPlaylist(playlistFile);
                List<Mp3FileData> loadedData = new ArrayList<>();
                for (int i = 0; i < filePaths.size(); i++) {
                    boolean dataAlreadyLoaded = false;
                    for (Mp3FileData data : masterData) {
                        if (data.getAbsolutePath().equals(filePaths.get(i))) {
                            loadedData.add(data);
                            dataAlreadyLoaded = true;
                            break;
                        }
                    }
                    if (!dataAlreadyLoaded) {
                        loadedData.add(Mp3Mapper.fileToMp3FileData(new File(filePaths.get(i))));
                    }
                    updateProgress(i + 1, filePaths.size());
                }
                updateTitle("Laden der Wiedergabeliste abgeschlossen.");
                updateMessage(loadedData.size() + " Titel wurden erfolgreich geladen.");

                if (!loadedData.isEmpty()) {
                    Platform.runLater(() -> {
                        playlist.setFilePath(playlistFile.getParent());
                        playlist.setFileName(playlistFile.getName());
                        //TODO Titel sollten nicht direkt geändert werden
                        playlist.getTitles().clear();
                        playlist.getTitles().addAll(loadedData);
                        playlist.setCurrentTitleIndex(0);
                    });
                } else {
                    updateProgress(1, 1);
                }
                return null;
            }
        };
    }
}
