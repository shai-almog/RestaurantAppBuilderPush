package com.majimob.app.restaurantbuilder.ui;

import com.codename1.components.ToastBar;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.properties.UiBinding;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.NumericConstraint;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;
import com.myrestaurant.app.model.Restaurant;
import java.io.IOException;
import java.util.ArrayList;

public class AddressForm extends Form {
    public AddressForm() {
        super("Address", BoxLayout.y());
        getToolbar().setUIID("BlueBar");
        
        ArrayList<UiBinding.Binding> bindingList = new ArrayList<>();
        
        UiBinding uib = new UiBinding();
        uib.setAutoCommit(false);
        
        TextField navigationAddress = new TextField();
        bindingList.add(uib.bind(Restaurant.getInstance().navigationAddress, navigationAddress));

        TextField lat = new TextField("", "", 5, TextField.DECIMAL);
        TextField lon = new TextField("", "", 5, TextField.DECIMAL);
        bindingList.add(uib.bind(Restaurant.getInstance().latitude, lat));
        bindingList.add(uib.bind(Restaurant.getInstance().longitude, lon));
        Button currentLocation = new Button();
        FontImage.setMaterialIcon(currentLocation, FontImage.MATERIAL_GPS_FIXED);
        Container locationEnclose = GridLayout.encloseIn(3, 
                BoxLayout.encloseY(new Label("latitude", "TextFieldLabel"), lat),
                BoxLayout.encloseY(new Label("longitude", "TextFieldLabel"), lon),
                currentLocation
            );
        currentLocation.addActionListener(e -> {
            try {
                Location l = LocationManager.getLocationManager().getCurrentLocation();
                lat.setText("" + l.getLatitude());
                lon.setText("" + l.getLongitude());
            } catch(IOException err) {
                ToastBar.showErrorMessage("Couldn't fetch location: " + err);
            }
        });
        
        TextField name = new TextField();
        bindingList.add(uib.bind(Restaurant.getInstance().name, name));

        TextArea address = new TextArea(2, 80);
        address.setUIID("TextField");
        address.setGrowByContent(false);
        bindingList.add(uib.bind(Restaurant.getInstance().address, address));
        
        TextField phone = new TextField("", "", 80, TextField.PHONENUMBER);
        bindingList.add(uib.bind(Restaurant.getInstance().phone, phone));

        TextField website = new TextField("", "", 80, TextField.URL);
        bindingList.add(uib.bind(Restaurant.getInstance().website, website));

        add(new Label("Navigation Address - Used to open the navigation app", "TextFieldLabel")).
                add(navigationAddress).
                add(locationEnclose).
                add(new Label("Name", "TextFieldLabel")).
                add(name).
                add(new Label("Addresss", "TextFieldLabel")).
                add(address).
                add(new Label("Phone", "TextFieldLabel")).
                add(phone).
                add(new Label("Website", "TextFieldLabel")).
                add(website);
        
        Form previousForm = Display.getInstance().getCurrent();
        getToolbar().addMaterialCommandToLeftBar("", FontImage.MATERIAL_ARROW_BACK, e -> {
            // Should work but has a bug...
            //UiBinding.unbind(Restaurant.getInstance());
            for(UiBinding.Binding b : bindingList) {
                b.disconnect();
            }
            
            previousForm.showBack();
        });
        Command cmd = getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_CHECK, e -> {
            for(UiBinding.Binding b : bindingList) {
                b.commit();
            }
            previousForm.showBack();
        });
        
        Validator validation = new Validator();
        validation.addConstraint(navigationAddress, new LengthConstraint(1, "Navigation address is required")).
                addConstraint(lat, new NumericConstraint(true, -90, 90, "Latitude must be between -90 to 90 degrees")).
                addConstraint(lon, new NumericConstraint(true, -180, 180, "Longitude must be between -180 to 180 degrees")).
                addConstraint(name, new LengthConstraint(1, "Name is required")).
                addConstraint(address, new LengthConstraint(1, "Address is required")).
                addConstraint(phone, new LengthConstraint(1, "Phone is required")).
                addConstraint(website, RegexConstraint.validURL()).
                addSubmitButtons(getToolbar().findCommandComponent(cmd));
    }
}
