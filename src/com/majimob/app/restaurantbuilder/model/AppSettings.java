package com.majimob.app.restaurantbuilder.model;

import com.codename1.processing.Result;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.PropertyIndex;
import com.codename1.ui.Display;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.geom.GeneralPath;
import com.myrestaurant.app.model.Restaurant;
import java.util.Map;

/**
 * Includes basic details about the app we are building
 */
public class AppSettings implements PropertyBusinessObject {
    public final Property<String, AppSettings> id = new Property<>("id");
    public final Property<String, AppSettings> publicKey = new Property<>("publicKey");
    public final Property<String, AppSettings> privateKey = new Property<>("privateKey");
    public final Property<String, AppSettings> merchantId = new Property<>("merchantId");
    public final Property<String, AppSettings> restaurantEmail = new Property<>("restaurantEmail");
    public final Property<String, AppSettings> aboutPageURL = new Property<>("aboutPageURL");
    public final Property<String, AppSettings> packageName = new Property<>("packageName");
    public final Property<String, AppSettings> appName = new Property<>("appName");
    
    private Image logo;
    
    public void setLogo(Image value) {
        if(value != null) {
            int mm = Display.getInstance().convertToPixels(2f);
            int size = Display.getInstance().convertToPixels(11);

            GeneralPath gp = new GeneralPath();
            float x = 0;
            float radius = mm;
            float y = 0;
            float widthF = size;
            float heightF = size;
            gp.moveTo(x + radius, y);
            gp.lineTo(x + widthF - radius, y);
            gp.quadTo(x + widthF, y, x + widthF, y + radius);
            gp.lineTo(x + widthF, y + heightF - radius);
            gp.quadTo(x + widthF, y + heightF, x + widthF - radius, y + heightF);
            gp.lineTo(x + radius, y + heightF);
            gp.quadTo(x, y + heightF, x, y + heightF - radius);
            gp.lineTo(x, y + radius);
            gp.quadTo(x, y, x + radius, y);
            gp.closePath();            
            Image mask = Image.createImage(size, size, 0xff000000);
            Graphics g = mask.getGraphics();
            g.setColor(0xffffff);
            g.setAntiAliased(true);
            g.fillShape(gp);
            Object m = mask.createMask();
            roundedScaledLogo = value.fill(size, size).applyMask(m);
        }
        logo = value;
    }
    
    public Image getLogo() {
        return logo;
    }
    
    private Image titleBackground;
    
    public void setTitleBackground(Image i) {
        titleBackground = i;
    }
    public Image getTitleBackground() {
        return titleBackground;
    }
    
    private Image roundedScaledLogo;
    private final PropertyIndex idx = new PropertyIndex(this, "AppSettings",  
            id, publicKey, privateKey, restaurantEmail, merchantId, aboutPageURL, 
            packageName, appName);
    
    @Override
    public PropertyIndex getPropertyIndex() {
        return idx;
    }
    
    public Image getRoundedScaledLogo() {
        return roundedScaledLogo;
    }
    
    public String toJSON() {
        Map<String, Object> m = idx.toMapRepresentation();
        m.remove("logo");
        m.remove("titleBackground");
        return Result.fromContent(m).toString();
    }

    public String toJSONWithRestaurant() {
        Map<String, Object> m = idx.toMapRepresentation();
        m.putAll(Restaurant.getInstance().getPropertyIndex().toMapRepresentation());
        m.remove(Restaurant.getInstance().cart.getName());
        m.remove(Restaurant.getInstance().menu.getName());
        m.remove("logo");
        m.remove("titleBackground");
        return Result.fromContent(m).toString();
    }
}
