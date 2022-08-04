package com.majimob.app.restaurantbuilder.ui;

import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.majimob.app.restaurantbuilder.model.AppSettings;
import java.io.IOException;

public class AboutForm extends BaseNavigationForm {
    
    public AboutForm(AppSettings app) {
        super(app, new BorderLayout());
        
        try {
            BrowserComponent bc = new BrowserComponent();
            add(BorderLayout.CENTER, bc);
            bc.setURLHierarchy("/placeholder.html");
        } catch(IOException err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error loading HTML: " + err);
        }
    }

    @Override
    protected boolean isAboutForm() {
        return true;
    }
}
