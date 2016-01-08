package de.pheru.media.task;

import de.pheru.media.data.Mp3FileData;
import de.pheru.media.data.Playlist;
import de.pheru.media.exceptions.Mp3FileDataException;
import de.pheru.media.util.FileUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Philipp Bruckner
 */
public class LoadPlaylistTask extends PheruMediaTask {

    private static final Logger LOGGER = LogManager.getLogger(LoadPlaylistTask.class);

    private final Playlist playlist;
    private final File playlistFileToLoad;
    private final List<Mp3FileData> masterData;

    public LoadPlaylistTask(Playlist playlist, File playlistFileToLoad, List<Mp3FileData> masterData) {
        this.playlist = playlist;
        this.playlistFileToLoad = playlistFileToLoad;
        this.masterData = masterData;
    }

    //TODO callImpl zu lange
    @Override
    protected void callImpl() {
        updateProgress(-1, 1);
        updateTitle("Lade Wiedergabeliste " + playlistFileToLoad.getName()+ "...");
        updateMessage("Lese Datei...");
        List<Mp3FileData> loadedData = new ArrayList<>();
        List<String> failedToLoadFilePaths = new ArrayList<>();
        List<String> filePaths;
        try {
            filePaths = FileUtil.readLinesFromFile(playlistFileToLoad, true);
            for (int i = 0; i < filePaths.size(); i++) {
                if (isCancelled()) {
                    updateTitle("Laden der Wiedergabeliste abgebrochen!");
                    updateMessage(loadedData.size() + " von " + filePaths.size() + " Dateien wurden erfolgreich geladen.");
                    updateProgress(1, 1);
                    setStatus(PheruMediaTaskStatus.INSUFFICIENT);
                    break;
                }
                updateMessage("Lade Titel " + (i + 1) + " von " + filePaths.size() + "...");
                boolean dataAlreadyInMasterData = false;
                for (Mp3FileData data : masterData) {
                    if (data.getAbsolutePath().equals(filePaths.get(i))) {
                        loadedData.add(data);
                        dataAlreadyInMasterData = true;
                        break;
                    }
                }
                if (!dataAlreadyInMasterData) {
                    try {
                        loadedData.add(new Mp3FileData(new File(filePaths.get(i))));
                    } catch (Mp3FileDataException e) {
                        LOGGER.error("Exception loading Mp3FileData!", e);
                        failedToLoadFilePaths.add(filePaths.get(i));
                    }
                }
                updateProgress(i + 1, filePaths.size());
            }
        } catch (IOException e) {
            LOGGER.error("Exception loading playlist!", e);
            setStatus(PheruMediaTaskStatus.FAILED);
            updateTitle("Laden der Wiedergabeliste fehlgeschlagen!");
            updateProgress(1, 1);
            //TODO Keine GUI in Task
//            Alert alert = new Alert(Alert.AlertType.ERROR, "Fehler beim Laden der Wiedergabiste \"" + playlistFileToLoad.getName() + "\"!");
//            Platform.runLater(alert::showAndWait);
            return;
        }
        updateTitle("Laden der Wiedergabeliste abgeschlossen.");
        updateMessage(loadedData.size() + " Titel wurden erfolgreich geladen.");

        if (loadedData.isEmpty()) {
            setStatus(PheruMediaTaskStatus.FAILED);
            showFailedAlert(playlistFileToLoad.getAbsolutePath(), failedToLoadFilePaths);
        } else if (loadedData.size() < filePaths.size()) {
            setStatus(PheruMediaTaskStatus.INSUFFICIENT);
            showFailedAlert(playlistFileToLoad.getAbsolutePath(), failedToLoadFilePaths);
        } else {
            setStatus(PheruMediaTaskStatus.SUCCESSFUL);
        }
        
        Platform.runLater(() -> {
            playlist.setFilePath(playlistFileToLoad.getParent());
            playlist.setFileName(playlistFileToLoad.getName());
            playlist.clear();
            playlist.add(loadedData);
            playlist.setCurrentTitleIndex(0);
        });
    }
    
    private void showFailedAlert(String playlistPath, List<String> failedToLoadFilePaths) {
        StringBuilder fileNamesStringBuilder = new StringBuilder();
        for (String failedToLoadFileName : failedToLoadFilePaths) {
            fileNamesStringBuilder.append(failedToLoadFileName);
            fileNamesStringBuilder.append("\n");
        }
        //TODO Keine GUI in Task
//        Alert alert = new Alert(Alert.AlertType.ERROR);
//        alert.setHeaderText("Fehler beim Laden der Wiedergabliste \"" + playlistPath + "\"!");
//        alert.setContentText("Folgende Titel konnten nicht gelesen werden:");
//        alert.getDialogPane().setExpandableContent(new Label(fileNamesStringBuilder.toString()));
//        Platform.runLater(alert::showAndWait);
    }
}