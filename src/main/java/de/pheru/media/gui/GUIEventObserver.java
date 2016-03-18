package de.pheru.media.gui;

import de.pheru.fx.controls.notification.Notification;
import de.pheru.media.cdi.events.SettingsLoadExceptionEvent;
import de.pheru.media.cdi.events.SettingsNotFoundEvent;
import de.pheru.media.cdi.events.TaskExceptionEvent;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import javax.enterprise.event.Observes;

/**
 * Created by Philipp on 29.02.2016.
 */
public class GUIEventObserver {

    private void settingsLoadException(@Observes SettingsLoadExceptionEvent settingsLoadExceptionEvent) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String text = "Fehler beim Laden der Einstellungen!";
        if (settingsLoadExceptionEvent.isInvalidSettingsFilePreserved()) {
            alert.setHeaderText(text);
            alert.setContentText("Die fehlerhafte Datei wurde aufbewahrt.");
        } else {
            alert.setContentText(text);
        }
        alert.showAndWait();
    }

    private void settingsNotFound(@Observes SettingsNotFoundEvent settingsNotFoundEvent) {
        Notification notification = new Notification(Notification.Type.INFO, "Keine Einstellungen vorhanden." +
                " Es wird eine neue Datei beim Schließen der Anwendung angelegt.");
        notification.show();
    }

    private void taskException(@Observes TaskExceptionEvent taskExceptionEvent) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Unerwarteter Fehler!");
        alert.setContentText("Um weiteres unerwartetes Verhalten zu vermeiden, sollte die Anwendung neu gestartet werden.");
        alert.showAndWait();
    }
}
