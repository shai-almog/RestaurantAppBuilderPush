package com.majimob.app.restaurantbuilder.ui;

import com.codename1.components.MultiButton;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BoxLayout;
import com.majimob.app.restaurantbuilder.model.AppSettings;
import com.majimob.app.restaurantbuilder.model.AppStorage;
import com.majimob.app.restaurantbuilder.model.Builder;
import com.myrestaurant.app.model.Restaurant;

public class DetailsForm extends BaseNavigationForm {
    private Builder bld = new Builder();
    public DetailsForm(AppSettings app) {
        super(app, BoxLayout.y());
        
        TextField emailField = addTextAndLabel("E-mail - will receive purchases from the generated app", "", TextField.EMAILADDR);
        emailField.addActionListener(e -> {
            app.restaurantEmail.set(emailField.getText());
            AppStorage.getInstance().update(app);
            bld.updateRestaurantSettings(app);
        });
        TextField urlField = addTextAndLabel("Website URL", "", TextField.URL);
        urlField.addActionListener(e -> {
            Restaurant.getInstance().website.set(urlField.getText());
            bld.updateRestaurantSettings(app);
        });
        
        MultiButton address = new MultiButton("Address & Location");
        if(Restaurant.getInstance().navigationAddress.get() != null && Restaurant.getInstance().navigationAddress.get().length() > 0) {
            address.setTextLine2(Restaurant.getInstance().navigationAddress.get());
        } else {
            address.setTextLine2("...");
        }
        add(address);
        address.addActionListener(e -> new AddressForm().show());

        MultiButton styles = new MultiButton("Colors & Fonts");
        styles.setTextLine2("...");
        add(styles);
        styles.addActionListener(e -> new StyleForm().show());

        MultiButton about = new MultiButton("About Page");
        about.setTextLine2("...");
        add(about);
        about.addActionListener(e -> new AboutRestaurantForm(app).show());
    }
    
    private TextField addTextAndLabel(String label, String value) {
        TextField tf = new TextField(value);
        tf.setHint(label);
        add(new Label(label, "TextFieldLabel")).
                add(tf);
        return tf;
    }

    private TextField addTextAndLabel(String label, String value, int constraint) {
        TextField tf = addTextAndLabel(label, value);
        tf.setConstraint(constraint);
        return tf;
    }

    @Override
    protected boolean isDetailsForm() {
        return true;
    }
}
