package com.majimob.app.restaurantbuilder.ui;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.ScaleImageLabel;
import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.codename1.ui.AutoCompleteTextField;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.validation.LengthConstraint;
import com.codename1.ui.validation.NumericConstraint;
import com.codename1.ui.validation.Validator;
import com.majimob.app.restaurantbuilder.model.AppSettings;
import com.majimob.app.restaurantbuilder.model.AppStorage;
import com.majimob.app.restaurantbuilder.model.Builder;
import com.myrestaurant.app.model.Dish;
import com.myrestaurant.app.model.Restaurant;

public class DishEditForm extends Form {
    private Builder bld = new Builder();
    public DishEditForm(AppSettings app, Dish d) {
        super(d.name.get(), new BorderLayout());
        Toolbar tb = getToolbar();
        Button back = new Button("", "Title");
        Button ok = new Button("", "Title");
        FontImage.setMaterialIcon(back, FontImage.MATERIAL_ARROW_BACK, 5);
        FontImage.setMaterialIcon(ok, FontImage.MATERIAL_CHECK, 5);
        
        TextField title = new TextField(d.name.get());
        title.setUIID("Title");
        tb.setTitleComponent(
                BorderLayout.centerEastWest(
                        BoxLayout.encloseY(title), 
                        FlowLayout.encloseRight(ok), 
                        FlowLayout.encloseIn(back)));
        tb.setUIID("BlueGradient");
        ScaleImageLabel backgroundImage = new ScaleImageLabel(d.getFullSize()) {
            @Override
            protected Dimension calcPreferredSize() {
                Dimension d = super.calcPreferredSize(); 
                d.setHeight(Math.min(d.getHeight(), Display.getInstance().convertToPixels(38)));
                return d;
            }
        };
        backgroundImage.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        add(BorderLayout.NORTH, backgroundImage);
        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_COLLECTIONS);
        Style fabStyle = fab.getAllStyles();
        fab.bindFabToContainer(getContentPane(), RIGHT, TOP);
        
        fab.addActionListener(e -> {
            new ImagePickerForm(800, 562, d.name.get(), d.getFullSize(), newImg -> {
                d.setFullSize(newImg);
                backgroundImage.setIcon(newImg);
            }).show();
        });
        
        final Form previous = Display.getInstance().getCurrent();
        Component.setSameHeight(tb, backgroundImage);
        fabStyle.setMarginUnit(Style.UNIT_TYPE_PIXELS, Style.UNIT_TYPE_DIPS, Style.UNIT_TYPE_DIPS, Style.UNIT_TYPE_DIPS);
        fabStyle.setMarginTop(tb.getPreferredH() - fab.getPreferredH() / 2);
        
        
        String[] cats = new String[Restaurant.getInstance().menu.get().categories.size()];
        Restaurant.getInstance().menu.get().categories.asList().toArray(cats);
        AutoCompleteTextField category = new AutoCompleteTextField(cats);
        
        TextField price = new TextField("" + d.price.get(), "Price", 5, TextField.DECIMAL);
        TextField description = new TextField(d.description.get(), "Description", 5, TextField.ANY);

        Button delete = new Button("Delete Dish", "DeleteButton");
        FontImage.setMaterialIcon(delete, FontImage.MATERIAL_DELETE);
        delete.addActionListener(e -> {
            previous.addShowListener(prev -> {
                previous.removeAllShowListeners();
                ToastBar.showMessage("Deleted " + d.name.get() + ". Undo?", FontImage.MATERIAL_UNDO, ee ->  {
                    DishListForm dlf = (DishListForm)previous;
                    dlf.addDish(app, d);
                    d.id.set(null);
                    bld.addDish(app, d);
                    d.id.addChangeListener(pl -> AppStorage.getInstance().insert(d));
                });                
            });
            previous.showBack();
            bld.deleteDish(app, d);
            AppStorage.getInstance().delete(d);            
        });
        ok.addActionListener(e -> {
            previous.showBack();
            if(!Restaurant.getInstance().menu.get().categories.asList().contains(category.getText())) {
                Restaurant.getInstance().menu.get().categories.add(category.getText());
            }
            d.category.set(category.getText());
            d.name.set(title.getText());
            d.description.set(description.getText());
            try {
                d.price.set(Double.parseDouble(price.getText()));
            } catch(NumberFormatException err) {
                Log.e(err);
                ToastBar.showErrorMessage("Malformed price");
            }
            bld.updateDish(app, d);
            if(d.id.get() != null) {
                AppStorage.getInstance().update(d);
            }
            //bld.updateDishImage(app, d);
        });
        back.addActionListener(e -> previous.showBack());
        add(BorderLayout.SOUTH, delete);
        
        description.setSingleLineTextArea(false);
        description.setRows(4);
        add(BorderLayout.CENTER, BoxLayout.encloseY(
                new Label("Category", "TextFieldLabel"),
                category,
                new Label("Price", "TextFieldLabel"),
                price,
                new Label("Description", "TextFieldLabel"),
                description
        ));
        
        Validator val = new Validator();
        val.addConstraint(category, new LengthConstraint(1, "Category is required"));
        val.addConstraint(price, new NumericConstraint(true, 0, 1000000, "Price must be a positive number"));
        val.addSubmitButtons(ok);
    }
    

    @Override
    protected void initGlobalToolbar() {
        setToolbar(new Toolbar(true));
    }    
}
