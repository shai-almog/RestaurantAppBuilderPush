package com.myrestaurant.app.ui;

import com.codename1.components.ToastBar;
import com.codename1.location.Location;
import com.codename1.location.LocationManager;
import com.codename1.maps.Coord;
import com.codename1.properties.Property;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.animations.Transition;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.spinner.Picker;
import com.codename1.util.MathUtil;
import com.myrestaurant.app.model.Address;
import com.myrestaurant.app.model.Dish;
import com.myrestaurant.app.model.Restaurant;
import com.myrestaurant.app.payment.Purchase;
import java.io.IOException;
import java.util.Map;

public class CheckoutForm extends Form {
    final static String[] PICKER_STRINGS = new String[10];
    
    private Label totalPrice;
    private double totalPriceValue;
    private boolean haveAddress;
    
    static {
        PICKER_STRINGS[0] = "Delete";
        for(int iter = 1 ; iter < 10 ; iter++) {
            PICKER_STRINGS[iter] = iter + "x";
        }
    }
    private Transition oldTransition;
    private CheckoutForm(Form previous) {
        super(new BorderLayout());
        Button x = new Button("", "Title");
        FontImage.setMaterialIcon(x, FontImage.MATERIAL_CLOSE);
        x.addActionListener(e -> {
            previous.showBack();
            previous.setTransitionOutAnimator(oldTransition);
        });
        
        add(BorderLayout.NORTH, 
                BorderLayout.centerAbsoluteEastWest(new Label("Checkout", "Title"), null, x)
                );
        Button buy = new Button("Address & Pay", "CheckoutButton");
        add(BorderLayout.SOUTH, buy);

        Container itemsContainer = new Container(BoxLayout.y());

        buy.addActionListener(e -> {
            if(haveAddress) {
                new Purchase().startOrder();
                return;
            }
            Restaurant r = Restaurant.getInstance();
            if(r.cart.get().dishQuantity.size() == 0) {
                ToastBar.showErrorMessage("You need to add dishes to the order...");
                return;
            }
            double minimumOrder = r.minimumOrder.get();
            if(totalPriceValue < minimumOrder) {
                ToastBar.showErrorMessage("Minimum order is " + 
                        r.formatCurrency(minimumOrder));
                return;
            }
            try {
                Location l = LocationManager.getLocationManager().getCurrentLocation();
                if(distance(r.latitude.get(), r.longitude.get(), l.getLatitude(), l.getLongitude()) > r.shippingRangeKM.get()) {
                    ToastBar.showErrorMessage("Sorry, your current location is out of range for our delivery service");
                    return;
                }

                r.cart.get().latitude.set(l.getLatitude());
                r.cart.get().longitude.set(l.getLongitude());
                
                for(Component c : itemsContainer) {
                    c.setX(c.getX() + itemsContainer.getWidth());
                }
                itemsContainer.animateUnlayoutAndWait(400, 255);
                itemsContainer.removeAll();
                
                TextField name = new TextField();
                bindTextFieldToProp(name, r.cart.get().address.get().name);

                TextField line1 = new TextField();
                bindTextFieldToProp(line1, r.cart.get().address.get().line1);

                TextField line2 = new TextField();
                bindTextFieldToProp(line2, r.cart.get().address.get().line2);
                
                TextField city = new TextField();
                bindTextFieldToProp(city, r.cart.get().address.get().city);
                
                TextField phone = new TextField();
                phone.setConstraint(TextField.PHONENUMBER);
                bindTextFieldToProp(phone, r.cart.get().address.get().phone);
                
                TextField email = new TextField();
                email.setConstraint(TextField.EMAILADDR);
                bindTextFieldToProp(email, r.cart.get().address.get().email);
                
                TextField notes = new TextField();
                bindTextFieldToProp(notes, r.cart.get().address.get().notes);
                
                itemsContainer.addAll(name, line1, line2, city, phone, email, notes);
                itemsContainer.animateLayoutAndWait(300);
                haveAddress = true;
                buy.setText("Payment");
            } catch(IOException err) {
                ToastBar.showErrorMessage("We need access to location to verify the order");
            }
        });

        Container totalsContainer = new Container(BoxLayout.y());
        
        totalPriceValue = 0;
        for(Map.Entry<Dish, Integer> currentDish : Restaurant.getInstance().cart.get().dishQuantity) {
            itemsContainer.add(createShoppingCartContainer(currentDish.getKey(), 
                    currentDish.getValue()));
            totalPriceValue += (currentDish.getKey().price.get() * currentDish.getValue());
        }
        itemsContainer.setUIID("PaymentDialogTop");
        totalsContainer.setUIID("PaymentDialogBottom");
        totalPrice = new Label(totalPriceString(totalPriceValue), "PriceTotal");
        totalsContainer.add(totalPrice);
        Container checkout = BoxLayout.encloseY(itemsContainer, totalsContainer);
        checkout.setScrollableY(true);
        add(BorderLayout.CENTER, checkout);
    }
    
    private void bindTextFieldToProp(TextField tf, Property<String, Address> prop) {
        tf.setText(prop.get());
        tf.setHint(prop.getName());
        tf.addActionListener(e -> prop.set(tf.getText()));
    }
    
    private String totalPriceString(double totalPriceValue) {
        if(Restaurant.getInstance().deliveryExtraCost.get() > 0) {
            totalPriceValue += Restaurant.getInstance().deliveryExtraCost.get();
            return "Total + " + 
                    Restaurant.getInstance().formatCurrency(Restaurant.getInstance().deliveryExtraCost.get()) +
                    " (delivery): " + Restaurant.getInstance().formatCurrency(totalPriceValue);
        }
        return "Total: " + Restaurant.getInstance().formatCurrency(totalPriceValue);
    }

    // calculation code from http://www.geodatasource.com/developers/java
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = MathUtil.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
        
    private void updatePrice() {
        totalPriceValue = 0;
        for(Map.Entry<Dish, Integer> currentDish : Restaurant.getInstance().cart.get().dishQuantity) {
            totalPriceValue += (currentDish.getKey().price.get() * currentDish.getValue());
        }
        totalPrice.setText(totalPriceString(totalPriceValue));
        totalPrice.getParent().revalidate();
    }
    
    private Container createShoppingCartContainer(Dish di, int quantity) {
        Picker quantityButton = new Picker();
        quantityButton.setUIID("QuantityPicker");
        quantityButton.setType(Display.PICKER_TYPE_STRINGS);
        quantityButton.setStrings(PICKER_STRINGS);
        quantityButton.setSelectedString(quantity + "x");

        Container dishContainer = BoxLayout.encloseX(
                FlowLayout.encloseMiddle(quantityButton),
                new Label(di.getThumbnail(), "UnmarginedLabel"),
                FlowLayout.encloseMiddle(
                        BoxLayout.encloseY(
                            new Label(di.name.get(), "DishCheckoutTitle"),
                            new Label(Restaurant.getInstance().formatCurrency(di.price.get()), "CheckoutPrice")
                        )
                )
        );

        quantityButton.addActionListener(e -> {
            String sel = quantityButton.getSelectedString();
            if(sel == null) {
                return;
            }
            if(sel.equals(PICKER_STRINGS[0])) {
                Display.getInstance().callSerially(() -> {
                    dishContainer.setX(Display.getInstance().getDisplayWidth());
                    Container p = dishContainer.getParent();
                    p.animateUnlayoutAndWait(250, 255);
                    dishContainer.remove();
                    p.animateLayoutAndWait(200);
                    Restaurant.getInstance().cart.get().dishQuantity.remove(di);
                    updatePrice();
                });
            } else {
                Restaurant.getInstance().cart.get().dishQuantity.put(di, getPickerIndex(sel));                
                updatePrice();
            }
        });
        
        return dishContainer;
    }
    
    int getPickerIndex(String s) {
        for(int iter = 0 ; iter < PICKER_STRINGS.length ; iter++) {
            if(s == PICKER_STRINGS[iter]) {
                return iter;
            }
        }
        throw new RuntimeException(s);
    }
    
    public static void showCheckOut() {
        Form existingForm = Display.getInstance().getCurrent();
        CheckoutForm f = new CheckoutForm(existingForm);
        f.oldTransition = existingForm.getTransitionOutAnimator();;
        Image background = Image.createImage(existingForm.getWidth(), existingForm.getHeight());
        Graphics g = background.getGraphics();
        existingForm.paintComponent(g, true);
        g.setAlpha(150);
        g.setColor(0);
        g.fillRect(0, 0, background.getWidth(), background.getHeight());
        background = Display.getInstance().gaussianBlurImage(background, 10);
        f.getUnselectedStyle().setBgImage(background);
        f.getUnselectedStyle().setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        f.setTransitionOutAnimator(CommonTransitions.createUncover(CommonTransitions.SLIDE_VERTICAL, true, 200));

        existingForm.setTransitionOutAnimator(CommonTransitions.createEmpty());
        existingForm.setTransitionOutAnimator(CommonTransitions.createCover(CommonTransitions.SLIDE_VERTICAL, false, 200));
        f.show();
    }
    
}
