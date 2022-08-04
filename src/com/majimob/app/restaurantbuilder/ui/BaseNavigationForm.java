package com.majimob.app.restaurantbuilder.ui;

import com.codename1.components.ScaleImageLabel;
import com.codename1.ui.Button;
import com.codename1.ui.Command;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Label;
import com.codename1.ui.TextField;
import com.codename1.ui.Toolbar;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.FlowLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.ui.layouts.Layout;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.majimob.app.restaurantbuilder.model.AppSettings;
import com.majimob.app.restaurantbuilder.model.AppStorage;
import com.majimob.app.restaurantbuilder.model.Builder;
import com.myrestaurant.app.model.Dish;
import com.myrestaurant.app.model.Restaurant;
import com.myrestaurant.app.ui.MainMenuForm;

/**
 * Base class for forms with side navigation and core features
 */
public class BaseNavigationForm extends Form {
    private static DishListForm dishForm;
    private static BillingForm billingForm;
    private static DetailsForm detailsForm;
    private static AppForm appForm;
    private static AboutForm aboutForm;
    public BaseNavigationForm(AppSettings app, Layout l) {
        super(Restaurant.getInstance().name.get(), l);
        init(app);
    }
    
    public static void showAppForm(AppSettings app) {
        if(appForm == null) {
            appForm = new AppForm(app);
        }
        appForm.show();
    }

    public static void showAboutForm(AppSettings app) {
        if(aboutForm == null) {
            aboutForm = new AboutForm(app);
        }
        aboutForm.show();
    }

    public static void showDetailsForm(AppSettings app) {
        if(detailsForm == null) {
            detailsForm = new DetailsForm(app);
        }
        detailsForm.show();
    }

    public static void showBillingForm(AppSettings app) {
        if(billingForm == null) {
            billingForm = new BillingForm(app);
        }
        billingForm.show();
    }

    public static void showDishForm(AppSettings app) {
        if(dishForm == null) {
            dishForm = new DishListForm(app);
        }
        dishForm.show();
    }
    
    private void init(AppSettings app) {
        Toolbar tb = getToolbar();
        
        ScaleImageLabel sll = new ScaleImageLabel(app.getTitleBackground());
        sll.setUIID("Container");
        sll.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        Label logo = new Label(app.getRoundedScaledLogo());
        logo.getAllStyles().setAlignment(RIGHT);
        Label taglineSide = new Label(Restaurant.getInstance().tagline.get(), "SidemenuTagline");
        Restaurant.getInstance().tagline.addChangeListener(pl -> taglineSide.setText(Restaurant.getInstance().tagline.get()));

        tb.addComponentToSideMenu(LayeredLayout.encloseIn(
                sll,
                BoxLayout.encloseY(logo, taglineSide)
        ));
        
        Command cmd = tb.addMaterialCommandToSideMenu("Dishes", FontImage.MATERIAL_RESTAURANT_MENU, e -> showDishForm(app));
        if(isDishesForm()) {
            cmd.putClientProperty("uiid", "SelectedSideCommand");
            cmd.setIcon(cmd.getPressedIcon());
        }
        cmd = tb.addMaterialCommandToSideMenu("Details", FontImage.MATERIAL_DESCRIPTION, e -> showDetailsForm(app));
        if(isDetailsForm()) {
            cmd.putClientProperty("uiid", "SelectedSideCommand");
            cmd.setIcon(cmd.getPressedIcon());
        }
        cmd = tb.addMaterialCommandToSideMenu("Billing", FontImage.MATERIAL_CREDIT_CARD, e -> showBillingForm(app));
        if(isBillingForm()) {
            cmd.putClientProperty("uiid", "SelectedSideCommand");
            cmd.setIcon(cmd.getPressedIcon());
        }
        cmd = tb.addMaterialCommandToSideMenu("App", FontImage.MATERIAL_PHONE_IPHONE, e -> showAppForm(app));
        if(isAppForm()) {
            cmd.putClientProperty("uiid", "SelectedSideCommand");
            cmd.setIcon(cmd.getPressedIcon());
        }
        cmd = tb.addMaterialCommandToSideMenu("About", FontImage.MATERIAL_INFO, e -> showAboutForm(app));
        if(isAboutForm()) {
            cmd.putClientProperty("uiid", "SelectedSideCommand");
            cmd.setIcon(cmd.getPressedIcon());
        }
        
        TextField title = new TextField(Restaurant.getInstance().name.get());
        title.setUIID("NavigationTitle");
        title.addActionListener(a -> {
            Restaurant.getInstance().name.set(title.getText());
            new Builder().updateRestaurantSettings(app);
        });

        TextField tagline = new TextField(Restaurant.getInstance().tagline.get());
        tagline.setUIID("Tagline");
        tagline.addActionListener(a -> {
            Restaurant.getInstance().tagline.set(tagline.getText());
            new Builder().updateRestaurantSettings(app);
        });
        
        Button editBackground = new Button("Edit Background", "EditBackground");
        editBackground.addActionListener(e -> {
            new ImagePickerForm(750, 295, "Background Image", app.getTitleBackground(), 
                    newImage -> {
                        app.setTitleBackground(newImage);
                        sll.setIcon(newImage);
                    }).show();
        });
        
        
        Button logoImage = new Button("", app.getRoundedScaledLogo(), "Container");
        logoImage.addActionListener(e -> {
            new ImagePickerForm(512, 512, "Icon", app.getLogo(), 
                    newImage -> {
                        app.setLogo(newImage);
                        logoImage.setIcon(app.getRoundedScaledLogo());
                    }).show();
        });
        
        Container titleContainer = BoxLayout.encloseY(
                title, 
                tagline,
                FlowLayout.encloseCenterMiddle(logoImage));
        Button menu = new Button("", "NavigationTitle");
        Button preview = new Button("", "NavigationTitle");
        preview.addActionListener(e -> showPreview());
        FontImage.setMaterialIcon(menu, FontImage.MATERIAL_MENU);
        FontImage.setMaterialIcon(preview, FontImage.MATERIAL_PLAY_ARROW);
        Container titleWithCommands = BorderLayout.centerEastWest(
                titleContainer, 
                FlowLayout.encloseCenter(preview), 
                FlowLayout.encloseCenter(menu));
        
        menu.addActionListener(e -> tb.openSideMenu());
                
        ScaleImageLabel sl = new ScaleImageLabel(app.getTitleBackground());
        sl.setUIID("TitleBottomSpace");
        sl.setBackgroundType(Style.BACKGROUND_IMAGE_SCALED_FILL);
        if(super.shouldPaintStatusBar()) {
            titleWithCommands.getUnselectedStyle().setPaddingUnit(Style.UNIT_TYPE_DIPS);
            titleWithCommands.getUnselectedStyle().setPaddingTop(3);
        }
        tb.setTitleComponent(LayeredLayout.encloseIn(
                sl, 
                BorderLayout.south(editBackground),
                titleWithCommands));
    }

    @Override
    protected boolean shouldPaintStatusBar() {
        return false;
    }

    @Override
    protected Component createStatusBar() {
        Component c = super.createStatusBar(); 
        c.setUIID("Container");
        return c;
    }

    @Override
    protected void initGlobalToolbar() {
        if(Toolbar.isGlobalToolbar()) {
            setToolbar(new Toolbar() {
                @Override
                protected void initTitleBarStatus() {   
                }
            });
        }
    }
    
    protected boolean isDishesForm() {
        return false;
    }
    
    protected boolean isAppForm() {
        return false;
    }
    
    protected boolean isAboutForm() {
        return false;
    }
    
    protected boolean isBillingForm() {
        return false;
    }
    
    protected boolean isDetailsForm() {
        return false;
    }
    
    private void showPreview() {
        UIManager.initFirstTheme("/theme");        
        MainMenuForm mf = new MainMenuForm();
        mf.show();
    }
}
