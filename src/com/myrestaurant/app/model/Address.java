package com.myrestaurant.app.model;

import com.codename1.io.Preferences;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

public class Address implements PropertyBusinessObject {
    public final Property<String, Address> name = new Property<>("name");
    public final Property<String, Address> line1 = new Property<>("line1");
    public final Property<String, Address> line2 = new Property<>("line2");
    public final Property<String, Address> city = new Property<>("city");
    public final Property<String, Address> phone = new Property<>("phone");
    public final Property<String, Address> email = new Property<>("email");
    public final Property<String, Address> notes = new Property<>("notes");
    
    private final PropertyIndex idx = new PropertyIndex(this, "Address", 
            name, line1, line2, city, phone, email, notes);

    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
    
    public Address() {
        // the address will be cached in storage seamlessly
        for(PropertyBase p : idx) {
            String pref = Preferences.get("address-" + p.getName(), null);
            if(pref != null) {
                ((Property)p).set(pref);
            }
            p.addChangeListener(pl -> {
                Preferences.set("address-" + pl.getName(), ((Property<String, Address>)pl).get());
            });
        }
    }
}
