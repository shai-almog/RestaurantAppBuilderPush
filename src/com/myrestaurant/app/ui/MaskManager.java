package com.myrestaurant.app.ui;

import com.codename1.ui.Image;
import com.codename1.ui.util.Resources;

/**
 * Simple utility class to handle round rect masking of food item images
 */
public class MaskManager {
    private static Object mask;
    private static Image maskImage;
    public static Image maskWithRoundRect(Image img) {
        if(maskImage == null) {
            maskImage = Resources.getGlobalResources().getImage("round-rect-mask.png");
            mask = maskImage.createMask();
        }
        Image imgMasked = img.fill(maskImage.getWidth(), maskImage.getHeight());
        imgMasked = imgMasked.applyMask(mask);
        return imgMasked;
    } 
}
