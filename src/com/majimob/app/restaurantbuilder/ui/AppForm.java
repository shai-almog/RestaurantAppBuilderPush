package com.majimob.app.restaurantbuilder.ui;

import com.codename1.components.MultiButton;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.UiBinding;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.RegexConstraint;
import com.codename1.ui.validation.Validator;
import com.majimob.app.restaurantbuilder.model.AppSettings;
import com.majimob.app.restaurantbuilder.model.AppStorage;
import com.majimob.app.restaurantbuilder.model.Builder;
import com.myrestaurant.app.payment.Purchase;

public class AppForm extends BaseNavigationForm {
    private Builder b = new Builder();
    private UiBinding bind = new UiBinding();
    private Validator val = new Validator();
    public static Runnable pendingBuild;
    
    public AppForm(AppSettings app) {
        super(app, new BorderLayout());
        
        Container content = new Container(BoxLayout.y());
        TextField packageName = addTextAndLabel(app, content, "Package Name", "", app.packageName);
        TextField appName = addTextAndLabel(app, content, "App Name", "", app.appName);
        
        val.addConstraint(packageName, new LengthConstraint(5, "Package must be at least 5 characters")).
                addConstraint(appName, new LengthConstraint(3, "Package must be at least 3 characters"));
        
        content.setScrollableY(true);
        add(BorderLayout.CENTER, content);
        
        Button help = new Button("Learn More", "GreenButton");
        Button build = new Button("Build App", "GreenButton");
        val.addSubmitButtons(build);
        FontImage.setMaterialIcon(help, FontImage.MATERIAL_HELP);
        FontImage.setMaterialIcon(build, FontImage.MATERIAL_PHONE_IPHONE);
        
        add(BorderLayout.SOUTH, GridLayout.encloseIn(2, help, build));
        build.addActionListener(e -> {
            Dialog dlg = new Dialog("Build App", BoxLayout.y());
            dlg.setDisposeWhenPointerOutOfBounds(true);
            Command ios = new Command("Target - iOS");
            Command android = new Command("Target - Android");
            Command cn1 = new Command("Source Code");
            if(Display.getInstance().getPlatformName().equals("ios")) {
                // Apple doesn't allow mention of other platforms...
                android.setCommandName("Other Device");
            }
            dlg.addAll(new Button(android), /*new Button(ios), */ new Button(cn1));
            
            Command result = dlg.showPacked(BorderLayout.CENTER, true);
            if(result != null) {
                String target;
                if(ios == result) {
                    target = "ios";
                } else {
                    if(android == result) {
                        target = "android";
                    } else {
                        target = "source";
                    }
                }
                pendingBuild = new Runnable() {
                    @Override
                    public void run() {
                        b.buildApp(app, target);
                    }
                };
                Display.getInstance().getInAppPurchase().purchase("build-for-" + target);
            }
        });
        help.addActionListener(e -> help());
    }
    
    private TextField addTextAndLabel(AppSettings app, Container content, String label, String value, PropertyBase prop) {
        TextField tf = new TextField(value);
        tf.setHint(label);
        content.add(new Label(label, "TextFieldLabel")).
                add(tf);
        bind.bind(prop, tf);
        tf.addActionListener(e -> {
            if(val.isValid()) {
                b.updateRestaurantSettings(app);
                AppStorage.getInstance().update(app);
            }
        });
        return tf;
    }

    @Override
    protected boolean isAppForm() {
        return true;
    }
    
    public void help() {
        // Placeholder for web page that should go here...
        Display.getInstance().execute("https://www.codenameone.com/");
    }
}
