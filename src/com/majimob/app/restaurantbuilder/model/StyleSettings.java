package com.majimob.app.restaurantbuilder.model;

import com.codename1.properties.FloatProperty;
import com.codename1.properties.IntProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 * Styles for the application
 */
public class StyleSettings implements PropertyBusinessObject {
    public final Property<String, StyleSettings> uiid = new Property<>("uiid");
    public final Property<String, StyleSettings> font = new Property<>("font");
    public final FloatProperty<StyleSettings> fontSize = new FloatProperty<>("fontSize");
    public final IntProperty<StyleSettings> fgColor = new IntProperty<>("fgColor");
    public final IntProperty<StyleSettings> bgColor = new IntProperty<>("bgColor");
    
    private final PropertyIndex idx = new PropertyIndex(this, "StyleSettings",  
            uiid, font, fontSize, fgColor, bgColor);
    
    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StyleSettings && uiid.get().equals(((StyleSettings)obj).uiid.get());
    }

    @Override
    public int hashCode() {
        return uiid.get().hashCode();
    }
    
    
}
