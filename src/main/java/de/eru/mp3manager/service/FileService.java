package de.eru.mp3manager.service;

import de.eru.mp3manager.data.Mp3FileData;
import de.eru.mp3manager.data.Playlist;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Service f�r den Zugriff auf Dateien und Verzeichnisse.
 *
 * @author Philipp Bruckner
 */
public final class FileService {

    private FileService() {
        //Utility-Klasse
    }

    /**
     * Speichert die ge�nderten MP3-Informationen ab.
     *
     * @param dataToSave Die zu �berschreibende Datei.
     * @param changeData Die zu speichernden MP3-Informationen.
     */
    public static void saveFile(Mp3FileData dataToSave, Mp3FileData changeData) {
        //TODO
    }

    /**
     * Speichert eine Wiedergabeliste.
     *
     * @param playlistFile Das File, in welche die Wiedergabeliste gespeichert werden soll.
     * @param playlist Die zu speichernde Wiedergabeliste.
     * @return true, wenn das Speichern erfolgreich war.
     * @throws java.io.IOException TODO Exception-Doc
     */
    public static boolean savePlaylist(File playlistFile, Playlist playlist) throws IOException {
        //TODO richtig implementieren
        if (playlistFile.exists()) {
            boolean success = playlistFile.delete(); //Funktioniert nicht wie erwartet (�nderungsdatum bleibt gleich)
            if (!success) {
                return false;
            }
        }
        try (FileWriter writer = new FileWriter(playlistFile)) {
            writer.append("Test\nTest2\nTest3");
        }
        return playlistFile.exists();
    }

    /**
     * L�scht eine Wiedergabeliste.
     *
     * @param playlistPath Der Pfad der Playlist-Datei.
     * @return true, wenn das L�schen erfolgreich war.
     */
    public static boolean deletePlaylist(String playlistPath) { //TODO richtig implementieren
        File playlistFile = new File(playlistPath);
        return playlistFile.delete();
    }

    /**
     * Sammelt alle MP3-Dateien aus einem Verzeichnis und dessen Unterverzeichnissen.
     *
     * @param directory Das auszulesende Verzeichnis.
     * @return Eine Liste von Files der MP3-Dateien.
     */
    public static ObservableList<File> collectMp3FilesFromDirectory(String directory) {
        ObservableList<File> fileList = FXCollections.observableArrayList();
        collect(directory, fileList);
        return fileList;
    }

    /**
     * Methode zum rekursiven Sammeln von MP3-Dateien aus einem Verzeichnis.
     *
     * @param directory Das auszulesende Verzeichnis.
     * @param fileList Die Liste von Files der MP3-Dateien.
     */
    private static void collect(String directory, ObservableList<File> fileList) {
        File dir = new File(directory);
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                collect(file.getAbsolutePath(), fileList);
            } else if (file.getName().endsWith(".mp3")) {
                fileList.add(file);
            }
        }
    }

    /**
     * TODO
     *
     * @param playlistFile
     * @return
     * @throws IOException
     */
    public static Playlist fileToPlaylist(File playlistFile) throws IOException {
        Playlist playlist = new Playlist();
        playlist.setAbsolutePath(playlistFile.getAbsolutePath());
        Files.lines(playlistFile.toPath()).forEach(System.out::println);
        return playlist;
    }
}
