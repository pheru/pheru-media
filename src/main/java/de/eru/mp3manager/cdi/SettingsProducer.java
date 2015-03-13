package de.eru.mp3manager.cdi;

import de.eru.mp3manager.settings.Settings;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 *
 * @author Philipp Bruckner
 */
@ApplicationScoped
public class SettingsProducer {
    
    @Produces
    @XMLSettings
    @ApplicationScoped
    public Settings createSettings(){
        return Settings.load();
    }
}
