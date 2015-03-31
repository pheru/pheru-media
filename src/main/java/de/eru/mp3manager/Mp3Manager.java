package de.eru.mp3manager;

import de.eru.pherufx.mvp.StartEvent;
import de.eru.pherufx.mvp.PheruFXApplication;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.Application.launch;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.jgroups.JChannel;

/**
 * Application-Klasse als Startpunkt für die JavaFX-Anwendung.
 *
 * @author Philipp Bruckner
 */
public class Mp3Manager extends PheruFXApplication {

    private static final String JAR_PATH = Mp3Manager.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    //TODO Geht das auch in schön?
    public static final String APPLICATION_PATH = JAR_PATH.substring(0,
            JAR_PATH.lastIndexOf("/app") >= 0 ? JAR_PATH.lastIndexOf("/app") : JAR_PATH.length());

    private static final Logger JAUDIOTAGGER_LOGGER = Logger.getLogger("org.jaudiotagger");

    public static JChannel channel;
    private static Alert startAlert;

    public static void main(String[] args) {
        //TODO Channel
//        try {
//            channel = new JChannel();
//            channel.connect("mp3manager");
//            if(channel.getView().getMembers().size() > 1){
//                channel.send(new Message(null, args));
//                channel.close();
//                System.exit(0);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        setOnStarting((StartEvent event) -> {
            startAlert = new Alert(Alert.AlertType.NONE);
            startAlert.setResult(ButtonType.CLOSE);
            startAlert.setTitle("Mp3Manager");
            startAlert.setContentText("Starte Mp3Manager...");
            startAlert.show();
        });
        setOnStartFinished((StartEvent event) -> {
            startAlert.hide();
        });
        JAUDIOTAGGER_LOGGER.setLevel(Level.WARNING);
        launch(args);
    }
}
