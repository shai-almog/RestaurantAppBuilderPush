package com.myrestaurant.app.ui;

import com.codename1.components.ImageViewer;
import com.codename1.l10n.L10NManager;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.Painter;
import com.codename1.ui.TextArea;
import com.codename1.ui.Toolbar;
import com.codename1.ui.geom.Rectangle;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.myrestaurant.app.model.Dish;
import com.myrestaurant.app.model.Restaurant;

public class DishViewForm extends Form {

    public DishViewForm(Dish actualDish) {
        super(actualDish.name.get(), new LayeredLayout());
        ImageViewer iv = new ImageViewer();
        iv.setImage(actualDish.getFullSize());
        add(iv);
        iv.setImageInitialPosition(ImageViewer.IMAGE_FILL);
        
        Toolbar tb = getToolbar();
        tb.setUIID("TintToolbar");
        Form previous = Display.getInstance().getCurrent();
        tb.addMaterialCommandToLeftBar("", FontImage.MATERIAL_CLOSE, ev -> previous.showBack());
        tb.addMaterialCommandToRightBar("", FontImage.MATERIAL_ADD_SHOPPING_CART, ev -> {});
        
        TextArea description = new TextArea(actualDish.description.get());
        description.setEditable(false);
        description.setUIID("DishViewDescription");
        add(BorderLayout.south(description));
        
        Label priceLabel = new Label(Restaurant.getInstance().formatCurrency(actualDish.price.get()), "YourOrder");
        priceLabel.getUnselectedStyle().setPaddingRight(6);
        Image priceImage = Image.createImage(priceLabel.getPreferredW() - Display.getInstance().convertToPixels(4), 
                priceLabel.getPreferredH(), 0);
        priceLabel.setWidth(priceLabel.getPreferredW());
        priceLabel.setHeight(priceLabel.getPreferredH());
        priceLabel.paintComponent(priceImage.getGraphics(), false);
        setGlassPane((g, rect) -> {
            g.drawImage(priceImage, getWidth() - priceImage.getWidth(), getHeight() / 5);
        });
    }
    
    @Override
    protected void initGlobalToolbar() {
        if(Toolbar.isGlobalToolbar()) {
            Toolbar tb = new Toolbar(true);
            setToolbar(tb);
        }
    }   
}
