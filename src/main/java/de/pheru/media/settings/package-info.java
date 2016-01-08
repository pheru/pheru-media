@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value = StringPropertyAdapter.class, type = StringProperty.class),
    @XmlJavaTypeAdapter(value = BooleanPropertyAdapter.class, type = BooleanProperty.class),
    @XmlJavaTypeAdapter(value = IntegerPropertyAdapter.class, type = IntegerProperty.class),
    @XmlJavaTypeAdapter(value = NotificationsAlignmentPropertyAdapter.class, type = NotificationsAlignmentProperty.class),
    @XmlJavaTypeAdapter(value = DoublePropertyAdapter.class, type = DoubleProperty.class)})
package de.pheru.media.settings;

import de.pheru.media.settings.objectproperties.NotificationsAlignmentProperty;
import de.pheru.media.settings.xmladapters.BooleanPropertyAdapter;
import de.pheru.media.settings.xmladapters.DoublePropertyAdapter;
import de.pheru.media.settings.xmladapters.IntegerPropertyAdapter;
import de.pheru.media.settings.xmladapters.NotificationsAlignmentPropertyAdapter;
import de.pheru.media.settings.xmladapters.StringPropertyAdapter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;