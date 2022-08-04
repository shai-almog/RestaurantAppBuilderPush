package com.majimob.app.restaurantbuilder.ui;

import com.codename1.properties.UiBinding;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Command;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.RadioButton;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;
import com.majimob.app.restaurantbuilder.model.AppSettings;
import com.majimob.app.restaurantbuilder.model.AppStorage;
import com.myrestaurant.app.model.Restaurant;

public class AboutRestaurantForm extends Form {

    public AboutRestaurantForm(AppSettings app) {
        super("About " + Restaurant.getInstance().name.get(), new BorderLayout());
        getToolbar().setUIID("BlueBar");
        
        UiBinding uib = new UiBinding();
        uib.setAutoCommit(false);
        TextField aboutPageURL = new TextField("", "", 80, TextField.URL);
        add(BorderLayout.NORTH, 
                BoxLayout.encloseY(
                        new Label("About Page URL", "TextFieldLabel"),
                        aboutPageURL));
        
        UiBinding.Binding bnd = uib.bind(app.aboutPageURL, aboutPageURL);
        
                
        BrowserComponent bc = new BrowserComponent();
        add(BorderLayout.CENTER, bc);

        
        Form previousForm = Display.getInstance().getCurrent();
        getToolbar().addMaterialCommandToLeftBar("", FontImage.MATERIAL_ARROW_BACK, e -> {            
            bnd.disconnect();
            previousForm.showBack();
        });
        Command cmd = getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_CHECK, e -> {
            bnd.commit();
            AppStorage.getInstance().update(app);
            previousForm.showBack();
        });
        
        Validator validation = new Validator();
        validation.addConstraint(aboutPageURL, RegexConstraint.validURL()).
                addSubmitButtons(getToolbar().findCommandComponent(cmd));
        
        aboutPageURL.addActionListener(e -> {
            if(validation.isValid()) {
                bc.setURL(aboutPageURL.getText());
            }
        });
    }
    
}
