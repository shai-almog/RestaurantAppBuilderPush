package com.myrestaurant.app.model;

import com.codename1.io.Log;
import com.codename1.processing.Result;
import com.codename1.properties.DoubleProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Image;
import com.codename1.ui.URLImage;
import com.codename1.ui.util.Resources;
import com.myrestaurant.app.ui.MaskManager;
import java.io.IOException;
import java.util.Map;

/**
 * Abstraction of a generic dish not of an order 
 */
public class Dish implements PropertyBusinessObject {
    public final Property<String, Dish> id = new Property<>("id");
    public final DoubleProperty<Dish> price = new DoubleProperty<>("price");
    public final Property<String, Dish> name = new Property<>("name");
    public final Property<String, Dish> description = new Property<>("description");
    public final Property<String, Dish> category = new Property<>("category");
    public final Property<String, Dish> imageName = new Property<>("imageName");
    
    private Image thumbnail;
    public Image getThumbnail() {
        return thumbnail;
    }

    private Image fullSize;
    public Image getFullSize() {
        return fullSize;
    }

    public void setFullSize(Image fullSize) {
        this.fullSize = fullSize;
    }
    
    private final PropertyIndex idx = new PropertyIndex(this, "Dish", 
            id, price, name, description, category, imageName);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    @Override
    public boolean equals(Object obj) {
        return name.get().equals(((Dish)obj).name.get());
    }

    @Override
    public int hashCode() {
        return name.get().hashCode();
    }
    
    public String toJSON(String secret) {
        Map<String, Object> m = idx.toMapRepresentation();
        m.put("secret", secret);
        return Result.fromContent(m).toString();
    }
}
