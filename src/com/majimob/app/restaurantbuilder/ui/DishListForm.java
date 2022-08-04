package com.majimob.app.restaurantbuilder.ui;

import com.codename1.components.FloatingActionButton;
import com.codename1.components.ScaleImageButton;
import com.codename1.properties.ListProperty;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Label;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.util.Resources;
import com.majimob.app.restaurantbuilder.model.AppSettings;
import com.majimob.app.restaurantbuilder.model.AppStorage;
import com.majimob.app.restaurantbuilder.model.Builder;
import com.myrestaurant.app.model.Dish;
import com.myrestaurant.app.model.Menu;
import com.myrestaurant.app.model.Restaurant;

public class DishListForm extends BaseNavigationForm {
    private Builder bld = new Builder();
    
    private static GridLayout createLayout() {
        if(Display.getInstance().isTablet()) {
            return new GridLayout(6, 6);
        } else {
            return new GridLayout(3, 2);
        }
    }
    
    public DishListForm(AppSettings app) {
        super(app, createLayout());
        Menu menu = Restaurant.getInstance().menu.get();
        if(menu.dishes.size() == 0) {
            for(PropertyBusinessObject d : AppStorage.getInstance().fetchDishes()) {
                Dish dsh = (Dish)d;
                menu.dishes.add(dsh);
                if(!menu.categories.asList().contains(dsh.category.get())) {
                    menu.categories.add(dsh.category.get());
                }
            }
        }
        for(Dish d : Restaurant.getInstance().menu.get().dishes) {
            addDish(app, d);
        }
        AppStorage.getInstance().addDeleteListener(e -> {
            for(Component cmp : getContentPane()) {
                Dish d = (Dish)cmp.getClientProperty("dish");
                if(d == e.getSource()) {
                    cmp.remove();
                    revalidate();
                    return;
                }
            }
        });
        
        FloatingActionButton fab = FloatingActionButton.createFAB(FontImage.MATERIAL_ADD);
        fab.bindFabToContainer(getContentPane());
        fab.addActionListener(e -> {
            Dish d = new Dish().description.set("Description of the dish...").
                    name.set("Dish Name").
                    price.set(3.0);
            d.setFullSize(Resources.getGlobalResources().getImage("food1.jpg"));
            Restaurant.getInstance().menu.get().dishes.add(d);
            addDish(app, d);
            revalidate();
            new DishEditForm(app, d).show();
            d.id.addChangeListener(pl -> AppStorage.getInstance().insert(d));
            bld.addDish(app, d);
        });
    }
    
    public void addDish(AppSettings app, Dish d) {
        ScaleImageButton sb = new ScaleImageButton(d.getFullSize());
        sb.setUIID("Container");
        sb.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        
        Label price = new Label(Restaurant.getInstance().formatCurrency(d.price.get()), "PriceBadge");
        d.price.addChangeListener(pl -> price.setText(Restaurant.getInstance().formatCurrency(d.price.get())));
        
        Label title = new Label(d.name.get(), "DishName");
        d.name.addChangeListener(pl -> title.setText(d.name.get()));
        Label description = new Label(d.description.get(), "DishDescription");
        d.description.addChangeListener(pl -> description.setText(d.description.get()));
        Container titleAndDescription = BoxLayout.encloseY(title, description);
        titleAndDescription.setUIID("BlackGradient");
        Container cnt = LayeredLayout.encloseIn(sb,
                BorderLayout.south(titleAndDescription),
                FlowLayout.encloseRight(price)
        );
        cnt.putClientProperty("dish", d);
        add(cnt);
        cnt.setLeadComponent(sb);
        setLayout(createLayout());
        sb.addActionListener(e -> new DishEditForm(app, d).show());
    }

    @Override
    protected boolean isDishesForm() {
        return true;
    }
}
