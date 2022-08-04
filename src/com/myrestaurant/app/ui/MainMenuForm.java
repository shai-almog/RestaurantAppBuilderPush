package com.myrestaurant.app.ui;

import com.codename1.components.InfiniteProgress;
import com.codename1.components.SpanLabel;
import com.codename1.io.Log;
import com.codename1.l10n.L10NManager;
import com.codename1.properties.ListProperty;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.PropertyChangeListener;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.List;
import com.codename1.ui.TextArea;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.list.DefaultListCellRenderer;
import com.codename1.ui.list.DefaultListModel;
import com.codename1.ui.util.Resources;
import com.myrestaurant.app.model.Dish;
import com.myrestaurant.app.model.Menu;
import com.myrestaurant.app.model.Order;
import com.myrestaurant.app.model.Restaurant;
import java.io.IOException;
import java.util.Map;


/**
 *
 */
public class MainMenuForm extends BaseForm {
    private DefaultListModel<String> categoryModel;
    private Container dishesContainer;
    private String currentCategory;
    public MainMenuForm() {
        super("Menu");
    }

    @Override
    protected List<String> createCategoryList() {
        if(Restaurant.getInstance().
                    menu.get().
                    categories.size() == 0) {
            categoryModel = new DefaultListModel<>("Loading...");
        } else {
            categoryModel = new DefaultListModel<>(
                    Restaurant.getInstance().
                        menu.get().
                        categories.asList());
        }
        List<String> l = new List<String>(categoryModel) {
            @Override
            protected boolean shouldRenderSelection() {
                return true;
            }
        };
        ((DefaultListCellRenderer<String>)l.getRenderer()).setAlwaysRenderSelection(true);
        l.setIgnoreFocusComponentWhenUnfocused(false);
        l.setOrientation(List.HORIZONTAL);
        l.setFixedSelection(List.FIXED_CENTER);
        l.addSelectionListener((i, ii) -> {
            if(currentCategory != l.getSelectedItem()) {
                currentCategory = l.getSelectedItem();
                for(Component c : dishesContainer) {
                    Dish d = (Dish)c.getClientProperty("dish");
                    boolean hidden = d.category.get().equals(currentCategory);
                    c.setHidden(hidden);
                    c.setVisible(!hidden);
                }
                dishesContainer.animateLayout(150);
            }
        });
        return l;
    }

    
    @Override
    protected Container createContent() {
        Container c = new Container(BoxLayout.y());
        final Menu m = Restaurant.getInstance().menu.get();
        if(m.dishes.size() > 0) {
            for(Dish currentDish : m.dishes) {
                c.add(createDishContainer(currentDish));
            }
        } else {
            final Container infi = FlowLayout.encloseCenter(new InfiniteProgress());
            c.add(infi);
            m.dishDownloadFinished.addChangeListener(p -> {
                c.removeAll();
                for(Dish currentDish : m.dishes) {
                    c.add(createDishContainer(currentDish));
                }
                categoryModel.removeAll();
                for(String s : m.categories) {
                    categoryModel.addItem(s);
                }
                c.revalidate();
            });
        }
        c.setScrollableY(true);
        c.setScrollVisible(false);
        dishesContainer = c;
        return c;
    }

    private Container createDishContainer(Dish dish) {
        TextArea ta = new TextArea(dish.description.get(), 3, 80);
        ta.setEditable(false);
        ta.setFocusable(false);
        ta.setGrowByContent(false);
        ta.setUIID("DishListBody");
        
        Button order = new Button("Order " + Restaurant.getInstance().formatCurrency(dish.price.get()), "AddToOrderButton");
        Button moreInfo = new Button("More Info", "MoreInfoButton");
        moreInfo.addActionListener(e -> new DishViewForm(dish).show());
        order.addActionListener(e -> {
            Order o = Restaurant.getInstance().cart.get();
            if(o.dishQuantity.get(dish) != null) {
                o.dishQuantity.put(dish, o.dishQuantity.get(dish) + 1);
            } else {
                o.dishQuantity.put(dish, 1);                
            }
        });
        
        Container dishContainer = BorderLayout.center(
                BoxLayout.encloseY(
                        new Label(dish.name.get(), "DishListTitle"),
                        ta
                        )
        );
        dishContainer.add(BorderLayout.EAST, new Label(dish.getThumbnail()));
        dishContainer.add(BorderLayout.SOUTH, GridLayout.encloseIn(2, order, moreInfo));
        dishContainer.setUIID("DishListEntry");
        dishContainer.putClientProperty("dish", dish);
        return dishContainer;
    }
    
    @Override
    protected void onSearch(String searchString) {
        if(searchString == null) {
            for(Component c : dishesContainer) {
                c.setHidden(false);
                c.setVisible(true);
            }
        } else {
            String search = searchString.toLowerCase();
            for(Component c : dishesContainer) {
                Dish d = (Dish)c.getClientProperty("dish");
                boolean hidden = d.name.get().toLowerCase().indexOf(search) < 0 &&
                        d.description.get().toLowerCase().indexOf(search) < 0;
                c.setHidden(hidden);
                c.setVisible(!hidden);
            }
            dishesContainer.animateLayout(200);
        }
    }
    
}
