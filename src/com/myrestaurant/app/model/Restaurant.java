package com.myrestaurant.app.model;

import com.codename1.io.Preferences;
import com.codename1.l10n.L10NManager;
import com.codename1.properties.DoubleProperty;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;

/**
 * This class abstracts basic details about the restaurant, it allow us to keep the UI
 * generic so we can rebuild this app to work for any restaurant out there
 */
public class Restaurant implements PropertyBusinessObject {
    public final Property<String, Restaurant> name = new Property<>("name");
    public final Property<String, Restaurant> tagline = new Property<>("tagline");
    public final DoubleProperty<Restaurant> latitude = new DoubleProperty<>("latitude", 0.0);
    public final DoubleProperty<Restaurant> longitude = new DoubleProperty<>("longitude", 0.0);
    public final Property<String, Restaurant> navigationAddress = new Property<>("navigationAddress");
    public final Property<String, Restaurant> address = new Property<>("address");
    public final Property<String, Restaurant> phone = new Property<>("phone");
    public final Property<String, Restaurant> website = new Property<>("website");
    public final Property<String, Restaurant> currency = new Property<>("currency", "$");
    public final Property<Menu, Restaurant> menu = new Property<>("menu", Menu.class, new Menu());
    public final Property<Order, Restaurant> cart = new Property<>("order", Order.class, new Order());
    public final DoubleProperty<Restaurant> minimumOrder = new DoubleProperty<>("minimumOrder", 0.0);
    public final DoubleProperty<Restaurant> shippingRangeKM = new DoubleProperty<>("shippingRangeKM", 0.0);
    public final DoubleProperty<Restaurant> deliveryExtraCost = new DoubleProperty<>("deliveryExtraCost", 0.0);
    
    private final PropertyIndex idx = new PropertyIndex(this, "Restaurant", 
            name, tagline, latitude, longitude, navigationAddress, address, 
            phone, website, currency, menu, cart, minimumOrder, shippingRangeKM,
            deliveryExtraCost);

    private static Restaurant instance;
    
    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }

    private void bindStringPropertyToPreference(Property<String, ? extends Object> p) {
        p.set(Preferences.get(p.getName(), p.get()));
        p.addChangeListener(pl -> Preferences.set(p.getName(), p.get()));
    }

    private void bindDoublePropertyToPreference(Property<Double, ? extends Object> p) {
        p.set(Preferences.get(p.getName(), p.get()));
        p.addChangeListener(pl -> Preferences.set(p.getName(), p.get()));
    }
    
    private Restaurant() {
        bindStringPropertyToPreference(name);
        bindStringPropertyToPreference(tagline);
        bindDoublePropertyToPreference(latitude);
        bindDoublePropertyToPreference(longitude);
        bindStringPropertyToPreference(navigationAddress);
        bindStringPropertyToPreference(address);
        bindStringPropertyToPreference(phone);
        bindStringPropertyToPreference(website);
        bindStringPropertyToPreference(currency);
        bindDoublePropertyToPreference(minimumOrder);
        bindDoublePropertyToPreference(shippingRangeKM);
        bindDoublePropertyToPreference(deliveryExtraCost);
    }

    public static Restaurant getInstance() {
        if(instance == null) {
            instance = new Restaurant();
        }
        return instance;
    }
 
    public String formatCurrency(double value) {
        return currency.get() + L10NManager.getInstance().format(value);
    }
}
