package com.majimob.app.restaurantbuilder.ui;

import com.codename1.components.MultiButton;
import com.codename1.io.Util;
import com.codename1.ui.Button;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.table.TableLayout;
import com.majimob.app.restaurantbuilder.model.AppSettings;
import com.majimob.app.restaurantbuilder.model.AppStorage;
import com.majimob.app.restaurantbuilder.model.Builder;
import com.myrestaurant.app.model.Restaurant;

public class BillingForm extends BaseNavigationForm {
    private Builder bld = new Builder();
    
    public BillingForm(AppSettings app) {
        super(app, new BorderLayout());
        
        Container content = new Container(BoxLayout.y());
        
        TableLayout tl = new TableLayout(2, 2);
        tl.setGrowHorizontally(true);
        Container table = new Container(tl);
        
        Restaurant r = Restaurant.getInstance();
        TextField currencySymbol = new TextField(r.currency.get(), "Currency Symbol", 5, TextField.ANY);
        TextField minimumOrder = new TextField("" + r.minimumOrder.get(), "Minimum Order", 5, TextField.DECIMAL);
        TextField deliveryRangeKM = new TextField("" + r.shippingRangeKM.get(), "Delivery Range (KM)", 5, TextField.DECIMAL);
        TextField deliverySurcharge = new TextField("" + r.deliveryExtraCost.get(), "Delivery Surcharge", 5, TextField.DECIMAL);
        
        currencySymbol.addActionListener(e -> r.currency.set(currencySymbol.getText()));
        minimumOrder.addActionListener(e -> r.minimumOrder.set(asDouble(minimumOrder.getText(), r.minimumOrder.get())));
        deliveryRangeKM.addActionListener(e -> r.shippingRangeKM.set(asDouble(deliveryRangeKM.getText(), r.shippingRangeKM.get())));
        deliverySurcharge.addActionListener(e -> r.deliveryExtraCost.set(asDouble(deliverySurcharge.getText(), r.deliveryExtraCost.get())));

        table.addAll(
                BoxLayout.encloseY(new Label("Currency Symbol", "TextFieldLabel"),
                        currencySymbol),
                BoxLayout.encloseY(new Label("Minimum Order", "TextFieldLabel"),
                        minimumOrder),
                BoxLayout.encloseY(new Label("Delivery Range (KM)", "TextFieldLabel"),
                        deliveryRangeKM),
                BoxLayout.encloseY(new Label("Delivery Surcharge", "TextFieldLabel"),
                        deliverySurcharge)
        );
        content.add(table);
        
        Label separator = new Label("", "Separator");
        separator.setShowEvenIfBlank(true);
        content.add(separator);
        
        content.add(new Label("Braintree Merchant Details"));
        
        TextField merchantId = new TextField(app.merchantId.get(), "Merchant ID");
        TextField publicKey = new TextField(app.publicKey.get(), "Public Key");
        TextField privateKey = new TextField(app.privateKey.get(), "Private Key");
        
        merchantId.addActionListener(e -> {
            app.merchantId.set(merchantId.getText());
            AppStorage.getInstance().update(app);
        });
        publicKey.addActionListener(e -> {
            app.publicKey.set(publicKey.getText());
            AppStorage.getInstance().update(app);
        });
        privateKey.addActionListener(e -> {
            app.privateKey.set(privateKey.getText());
            AppStorage.getInstance().update(app);
        });
        
        content.add(new Label("Merchant ID", "TextFieldLabel")).
            add(merchantId).
            add(new Label("Public Key", "TextFieldLabel")).
            add(publicKey).
            add(new Label("Private Key", "TextFieldLabel")).
            add(privateKey);
        
        content.setScrollableY(true);
        add(BorderLayout.CENTER, content);
        
        Button help = new Button("Learn About Billing", "GreenButton");
        FontImage.setMaterialIcon(help, FontImage.MATERIAL_HELP);
        
        add(BorderLayout.SOUTH, help);
        help.addActionListener(e -> help());
    }
    
    double asDouble(String d, double def) {
        try {
            return Double.parseDouble(d);
        } catch(NumberFormatException ne) {
            return def;
        }
    }

    @Override
    protected boolean isBillingForm() {
        return true;
    }

    public void help() {
        // Placeholder for web page that should go here...
        Display.getInstance().execute("https://www.codenameone.com/");
    }
}
