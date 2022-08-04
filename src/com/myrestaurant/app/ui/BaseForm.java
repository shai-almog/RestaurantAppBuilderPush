package com.myrestaurant.app.ui;

import com.codename1.l10n.L10NManager;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.List;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.plaf.UIManager;
import com.majimob.app.restaurantbuilder.ui.BaseNavigationForm;
import com.myrestaurant.app.model.Dish;
import com.myrestaurant.app.model.Restaurant;
import java.util.Map;

/**
 * This is a base class for forms that allows us to configure common global 
 * settings e.g. install side menu etc.
 */
public abstract class BaseForm extends Form {    
    private Label titleCmp;
    private TextField searchField;
    private Command searchCommand;
    public BaseForm(String title) {
        super(title, new BorderLayout());
        init();
    }

    private void init() {
        Toolbar tb = getToolbar();
        tb.setUIID("Container");
        searchCommand = tb.addMaterialCommandToRightBar("", FontImage.MATERIAL_SEARCH, e -> onSearchImpl());
        Container topArea = FlowLayout.encloseRight(createCartButton());
        topArea.setUIID("TopBar");
        tb.addComponentToSideMenu(topArea);
        tb.addMaterialCommandToSideMenu("Menu", FontImage.MATERIAL_RESTAURANT_MENU, e -> showMainMenuForm());
        tb.addMaterialCommandToSideMenu("About", FontImage.MATERIAL_INFO, e -> showAboutForm());
        tb.addMaterialCommandToSideMenu("Contact Us", FontImage.MATERIAL_MAP, e -> showContactUsForm());
        tb.addMaterialCommandToSideMenu("Exit Preview", FontImage.MATERIAL_EXIT_TO_APP, e -> {
            UIManager.initFirstTheme("/builderTheme");        
            BaseNavigationForm.showDishForm(null);
        });
        
        Label filler = new Label(" ");
        filler.setPreferredSize(getTitleArea().getPreferredSize());
        
        Label currentOrderValue = new Label("Your Order: " + Restaurant.getInstance().formatCurrency(0), 
                "YourOrder");
        
        Restaurant.getInstance().cart.get().dishQuantity.addChangeListener(pl -> {
            double totalPriceValue = 0;
            for(Map.Entry<Dish, Integer> currentDish : Restaurant.getInstance().cart.get().dishQuantity) {
                totalPriceValue += (currentDish.getKey().price.get() * currentDish.getValue());
            }
            currentOrderValue.setText("Your Order: " + Restaurant.getInstance().formatCurrency(totalPriceValue));
            revalidate();
        });
        
        // This creates a grid layout where the white bar hides the bottom area of the image
        // unfortunately if the hight of the elements can't be divided by 0 it leaves a pixel uncovered
        // and a line showing the underlying image. So we force the preferred size to always return 
        // even numbers so the line can fit properly
        Container orderBar = new Container(new BorderLayout(BorderLayout.CENTER_BEHAVIOR_CENTER_ABSOLUTE)) {
            @Override
            protected Dimension calcPreferredSize() {
                Dimension d= super.calcPreferredSize();
                if(d.getHeight() % 2 != 0) {
                    d.setHeight(d.getHeight() + 1);
                }
                return d;
            }
        };
        orderBar.add(BorderLayout.CENTER, currentOrderValue).
                add(BorderLayout.EAST, createCartButton());
        
        
        // this is here to mask part of the image with a background so the line will go exactly in the 
        // center of the order bar
        Label opaque = new Label("", "OrderBarBackgroundOpaque");
        opaque.setShowEvenIfBlank(true);
        Container orderBackgroundGrid = GridLayout.encloseIn(1, new Container(), opaque);
        Container orderLayers = LayeredLayout.encloseIn(orderBackgroundGrid, orderBar);
        
        add(BorderLayout.CENTER, createContent());

        List<String> categoryList = createCategoryList();
        Container topBar;
        if(categoryList != null) {
            Label separator = new Label("", "WhiteSeparatorLine");
            separator.setShowEvenIfBlank(true);
            topBar = BoxLayout.encloseY(filler, categoryList, 
                    FlowLayout.encloseCenter(separator), 
                    orderLayers);
        } else {
            topBar = BoxLayout.encloseY(filler, orderLayers);
        }
        topBar.setUIID("TopBar");
        add(BorderLayout.NORTH, topBar);
        
        Component.setSameHeight(topArea, topBar);
    }

    private Button createCartButton() {
        Button cart = new Button("", "ShoppingCart");
        cart.addActionListener(e -> CheckoutForm.showCheckOut());
        FontImage.setMaterialIcon(cart, FontImage.MATERIAL_SHOPPING_CART);
        return cart;
    }
    
    protected abstract Container createContent();
    
    protected List<String> createCategoryList() {
        return null;
    }

    void onSearchImpl() {
        if(getToolbar().getTitleComponent() instanceof Label) {
            titleCmp = (Label)getToolbar().getTitleComponent();
            searchField = new TextField("", "Search"); 
            searchField.getHintLabel().setUIID("Title");
            searchField.setUIID("Title");
            searchField.getAllStyles().setAlignment(Component.LEFT);
            getToolbar().setTitleComponent(searchField);
            FontImage.setMaterialIcon(searchField.getHintLabel(), FontImage.MATERIAL_SEARCH);
            FontImage.setMaterialIcon(getToolbar().findCommandComponent(searchCommand), 
                    FontImage.MATERIAL_CANCEL);
            searchField.addDataChangedListener((a, b) -> onSearch(searchField.getText()));
            revalidate();
            searchField.startEditingAsync();
        } else {
            onSearch(null);
            getToolbar().setTitleComponent(titleCmp);
            FontImage.setMaterialIcon(getToolbar().findCommandComponent(searchCommand), 
                    FontImage.MATERIAL_SEARCH);
            revalidate();
        }
    }
    
    protected abstract void onSearch(String searchString);

    @Override
    protected void initGlobalToolbar() {
        if(Toolbar.isGlobalToolbar()) {
            Toolbar tb = new Toolbar(true);
            setToolbar(tb);
        }
    }   
    
    static MainMenuForm mainMenu;
    public static void showMainMenuForm() {
        if(mainMenu == null) {
            mainMenu = new MainMenuForm();
        }
        mainMenu.show();
    }

    static ContactUsForm contactUs;
    public static void showContactUsForm() {
        if(contactUs == null) {
            contactUs = new ContactUsForm();
        }
        contactUs.show();
    }

    static AboutForm about;
    public static void showAboutForm() {
        if(about == null) {
            about = new AboutForm();
        }
        about.show();
    }
}
