package com.myrestaurant.app.ui;

import com.codename1.io.Log;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Container;
import java.io.IOException;

public class AboutForm extends BaseForm {
    public AboutForm() {
        super("About");
    }
    
    @Override
    protected Container createContent() {
        BrowserComponent cmp = new BrowserComponent();
        try {
            cmp.setURLHierarchy("/index.html");
        } catch(IOException err) {
            Log.e(err);
            Log.sendLog();
        }
        return cmp;
    }

    @Override
    protected void onSearch(String searchString) {
    }
    
}
