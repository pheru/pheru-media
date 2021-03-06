package de.pheru.media.desktop;

import de.pheru.fx.mvp.PheruFXLoader;
import de.pheru.fx.util.properties.ObservableProperties;
import de.pheru.media.core.io.file.FileIO;
import de.pheru.media.desktop.cdi.qualifiers.*;
import de.pheru.media.desktop.data.AudioLibrary;
import de.pheru.media.desktop.data.AudioLibraryData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class DesktopApplicationLoader extends PheruFXLoader {

    private static final Logger LOGGER = LogManager.getLogger(DesktopApplicationLoader.class);

    @Inject
    @Settings
    private ObservableProperties settings;
    @Inject
    @AudioLibraryIO
    private FileIO audioLibraryIO;
    @Inject
    @AudioLibraryDataIO
    private FileIO audioLibraryDataIO;
    @Inject
    @CurrentAudioLibrary
    private ObjectProperty<AudioLibrary> currentAudioLibrary;
    @Inject
    @CurrentAudioLibraryData
    private ObjectProperty<AudioLibraryData> currentAudioLibraryData;

    @Override
    public void load() throws Exception {
        updateMessage("Lade aktuelle Musik-Bibliothek ...");
        updateProgress(33, 100);
        loadCurrentAudioLibrary();
        updateProgress(66, 100);
        loadCurrentAudioLibraryData();
        updateProgress(100, 100);
    }

    private void loadCurrentAudioLibrary() {
        final StringProperty currentLibraryFileName = settings.stringProperty(Setting.CURRENT_AUDIO_LIBRARY_FILENAME);
        try {
            LOGGER.info("Reading current audio library \"" + currentLibraryFileName.get() + "\" ...");
            currentAudioLibrary.set(audioLibraryIO.read(
                    new File(AudioLibrary.DIRECTORY + "/" + currentLibraryFileName.get()),
                    AudioLibrary.class));
            LOGGER.info("Reading current audio library done.");
        } catch (final IOException e) {
            if (e instanceof FileNotFoundException) {
                LOGGER.info("No current audiolibrary found.");
            } else {
                LOGGER.error("Exception loading current audiolibrary!", e);
                fail("Fehler beim Laden der aktuellen Musikbibliothek-Datei \"" + currentLibraryFileName + "\"!", e);
            }
        }
        currentAudioLibrary.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                currentLibraryFileName.set(newValue.getFileName());
            } else {
                currentLibraryFileName.setValue(Setting.CURRENT_AUDIO_LIBRARY_FILENAME.getDefaultValue());
            }
        });
    }

    private void loadCurrentAudioLibraryData() {
        if (currentAudioLibrary.get() == null) {
            LOGGER.info("Loading data for current audiolibrary skipped.");
            currentAudioLibraryData.set(new AudioLibraryData());
            return;
        }
        try {
            currentAudioLibraryData.set(audioLibraryDataIO.read(
                    new File(AudioLibraryData.DIRECTORY + "/" + currentAudioLibrary.get().getFileName()),
                    AudioLibraryData.class));
        } catch (final IOException e) {
            currentAudioLibraryData.set(new AudioLibraryData());
            if (e instanceof FileNotFoundException) {
                LOGGER.info("No data for current audiolibrary found.");
            } else {
                LOGGER.error("Exception loading data for current audiolibrary!", e);
                fail("Fehler beim Laden der Daten zur aktuellen Musikbibliothek \"" + currentAudioLibrary.get().getName() + "\"!", e);
            }
        }
    }
}
