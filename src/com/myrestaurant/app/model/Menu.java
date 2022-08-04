package com.myrestaurant.app.model;

import com.codename1.properties.ListProperty;
import com.codename1.properties.MapProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 * Contains the list of available dishes
 */
public class Menu implements PropertyBusinessObject {
    public final ListProperty<Dish, Menu> dishes = new ListProperty<>("dishes");
    public final ListProperty<String, Menu> categories = new ListProperty<>("categories");
    public final Property<Boolean, Menu> dishDownloadFinished = new Property<>("dishDownloadFinished", false);

    private final PropertyIndex idx = new PropertyIndex(this, "Menu", 
            dishes, categories, dishDownloadFinished);

    
    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
}
