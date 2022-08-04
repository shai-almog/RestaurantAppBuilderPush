package com.majimob.app.restaurantbuilder.ui;

import com.codename1.capture.Capture;
import com.codename1.components.ToastBar;
import com.codename1.io.Log;
import com.codename1.ui.Button;
import com.codename1.ui.Component;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.layouts.LayeredLayout;
import com.codename1.util.SuccessCallback;
import java.io.IOException;

/**
 * This form can be used to select an image for editing
 */
public class ImagePickerForm extends Form {
    Image currentImage;
    float zoom = 10;
    int panX = 0;
    int panY = 0;
    float factor;
    public ImagePickerForm(int width, int height, String name, Image img, SuccessCallback<Image> result) {
        super(name, new LayeredLayout());
        getToolbar().setUIID("BlueBar");
        Form previousForm = Display.getInstance().getCurrent();
        getToolbar().addMaterialCommandToLeftBar("", FontImage.MATERIAL_ARROW_BACK, e -> previousForm.showBack());
        getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_CHECK, e -> {
            if(currentImage != null) {
                Image m = Image.createImage(width, height);
                Graphics g = m.getGraphics();
                float w = ((float)currentImage.getWidth()) * (zoom / 10.0f * factor);
                float h = ((float)currentImage.getHeight()) * (zoom / 10.0f * factor);
                g.setAntiAliased(true);
                g.drawImage(currentImage, panX, panY, Math.round(w), Math.round(h));
                result.onSucess(m);
            }
            previousForm.showBack();
        });
        this.currentImage = img;
        getContentPane().setScrollableY(false);
        float ratio = ((float)width) / ((float)height);
                
        Component imagePainter = new Component() {
            int lastX = -1;
            int lastY = -1;
            
            @Override
            public void paint(Graphics g) {
                if(currentImage != null) {
                    float w = ((float)currentImage.getWidth()) * (zoom / 10.0f);
                    float h = ((float)currentImage.getHeight()) * (zoom / 10.0f);
                    g.setAlpha(255);
                    g.drawImage(currentImage, getX() + panX, getY() + panY, Math.round(w), Math.round(h));
                    
                    int rectWidth = Math.round(((float)getWidth() - 20.0f) * ratio);
                    int rectHeight = Math.round(((float)getWidth() - 20.0f) / ratio);
                    
                    if(rectWidth > getWidth() || rectHeight > getHeight()) {
                        rectWidth = Math.round(((float)getHeight() - 20.0f) * ratio);
                        rectHeight = Math.round(((float)getHeight() - 20.0f) / ratio);
                    } 
                    
                    factor = ((float)width) / ((float)rectWidth);
                    
                    g.setColor(0xff0000);
                    g.setAlpha(150);
                    g.drawRect((getWidth() - rectWidth) / 2, (getHeight() - rectHeight) / 2, rectWidth, rectHeight);
                    g.setAlpha(255);
                }
            }

            @Override
            protected boolean pinch(float scale) {
                Log.p("Scale: " + scale + " zoom: " + zoom);
                if(currentImage != null) {
                    float w = ((float)currentImage.getWidth()) * (zoom * scale / 10.0f);
                    float h = ((float)currentImage.getHeight()) * (zoom * scale  / 10.0f);
                    if(w < getWidth() - 20 && scale < 1) {
                        return true;
                    }
                    if(h < getHeight() - 20 && scale < 1) {
                        return true;
                    }
                }
                zoom *= scale;
                zoom = Math.min(60, Math.max(1f, zoom));
                repaint();
                return true;
            }

            @Override
            public void pointerDragged(int x, int y) {
                if(lastX < 0) {
                    lastX = x;
                    lastY = y;
                    return;
                }

                if(currentImage != null) {
                    float w = ((float)currentImage.getWidth()) * (zoom / 10.0f);
                    float h = ((float)currentImage.getWidth()) * (zoom / 10.0f);
                    float newPanX = panX - ((lastX - x) * (zoom / 10.0f));
                    float newPanY = panY - ((lastY - y) * (zoom / 10.0f));
                    if(newPanX + w > getWidth() - 20 && newPanX < 20) {
                        panX = (int)newPanX;
                    }
                    if(newPanY + h > getHeight() - 20 && newPanY < 20) {
                        panY = (int)newPanY;
                    }
                } else {
                    panX -= ((lastX - x) * (zoom / 10.0f));
                    panY -= ((lastY - y) * (zoom / 10.0f));
                }
                lastX = x;
                lastY = y;
                repaint();
            }

            @Override
            public void pointerPressed(int x, int y) {
                lastX = -1;
            }
            
            
        };
        add(imagePainter);
        
        Button camera = new Button("");
        FontImage.setMaterialIcon(camera, FontImage.MATERIAL_CAMERA);
        Button gallery = new Button("");
        FontImage.setMaterialIcon(gallery, FontImage.MATERIAL_ADD_A_PHOTO);
        add(BorderLayout.south(
                GridLayout.encloseIn(2, camera, gallery)
        ));
        
        camera.addActionListener(e -> {
            String photo = Capture.capturePhoto();
            if(photo != null) {
                try {
                    currentImage = Image.createImage(photo);
                    imagePainter.repaint();
                } catch(IOException err) {
                    Log.e(err);
                    ToastBar.showErrorMessage("Error loading image: " + err);
                }
            }
        });
    }
}
