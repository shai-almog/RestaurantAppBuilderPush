package com.majimob.app.restaurantbuilder.ui;

import com.codename1.properties.PropertyBusinessObject;
import com.codename1.ui.Button;
import com.codename1.ui.ButtonGroup;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.Font;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.Label;
import com.codename1.ui.RadioButton;
import com.codename1.ui.Slider;
import com.codename1.ui.TextArea;
import com.codename1.ui.TextField;
import com.codename1.ui.animations.CommonTransitions;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.BoxLayout;
import com.codename1.ui.layouts.GridLayout;
import com.codename1.ui.plaf.Border;
import com.codename1.ui.plaf.Style;
import com.codename1.ui.plaf.UIManager;
import com.codename1.ui.spinner.Picker;
import com.codename1.util.SuccessCallback;
import com.majimob.app.restaurantbuilder.model.AppStorage;
import com.majimob.app.restaurantbuilder.model.StyleSettings;
import com.myrestaurant.app.ui.MainMenuForm;
import java.util.ArrayList;
import java.util.Hashtable;

public class StyleForm extends Form {
    private ArrayList<StyleSettings> styles = new ArrayList<>();
    public StyleForm() {
        super("Styling", new BorderLayout());
        
        // setup the toolbar styling manually otherwise things get messed up...
        Style toolbarStyle = getToolbar().getUnselectedStyle();
        toolbarStyle.setBgTransparency(255);
        toolbarStyle.setBgColor(0x3f51b5);
        toolbarStyle.setBorder(Border.createEmpty());
        
        Form previous = Display.getInstance().getCurrent();
        getToolbar().addMaterialCommandToLeftBar("", FontImage.MATERIAL_ARROW_BACK, 4, e -> {
            UIManager.initFirstTheme("/builderTheme");
            previous.showBack();
        });
        getToolbar().addMaterialCommandToRightBar("", FontImage.MATERIAL_CHECK, 4, e -> {
            for(StyleSettings s : styles) {
                AppStorage.getInstance().insertOrUpdate(s);
            }
            UIManager.initFirstTheme("/builderTheme");
            previous.showBack();
        });

        UIManager.initFirstTheme("/theme");
        
        Hashtable styleHash = new Hashtable();
        for(PropertyBusinessObject p : AppStorage.getInstance().fetchStyles()) {
            StyleSettings ss = (StyleSettings)p;
            if(ss.font.get() != null) {
                int size = Display.getInstance().convertToPixels(ss.fontSize.get());
                styleHash.put(ss.uiid.get() + ".font", Font.createTrueTypeFont(ss.font.get(), ss.font.get()).derive(size, Font.STYLE_PLAIN));
            }
            if(ss.fgColor.get() != null) {
                styleHash.put(ss.uiid.get() + ".fgColor", Integer.toHexString(ss.fgColor.get()));
            }
            if(ss.bgColor.get() != null) {
                styleHash.put(ss.uiid.get() + ".bgColor", Integer.toHexString(ss.bgColor.get()));
            }
        }
        UIManager.getInstance().addThemeProps(styleHash);
        
        MainMenuForm mainPreview = new MainMenuForm();
        mainPreview.getToolbar().getTitleComponent().setFocusable(true);
        add(BorderLayout.CENTER, mainPreview);
    }

    @Override
    public void pointerDragged(int x, int y) {
        if(!getContentPane().contains(x, y)) {
            super.pointerDragged(x, y);
        }
    }

    @Override
    public void pointerDragged(int[] x, int[] y) {
        pointerDragged(x[0], y[0]);
    }

    @Override
    public void pointerPressed(int x, int y) {
        if(!getContentPane().contains(x, y)) {
            super.pointerPressed(x, y);
        }
    }

    @Override
    public void pointerPressed(int[] x, int[] y) {
        pointerPressed(x[0], y[0]);
    }

    StyleSettings getStyleSetting(String uiid) {
        for(StyleSettings s : styles) {
            if(s.uiid.get().equals(uiid)) {
                return s;
            }
        }
        StyleSettings s = AppStorage.getInstance().fetchStyle(uiid);
        if(s == null) {
            s = new StyleSettings().uiid.set(uiid);
        } 
        styles.add(s);
        return s;
    }
    
    @Override
    public void pointerReleased(int x, int y) {
        if(!getContentPane().contains(x, y)) {
            super.pointerReleased(x, y);
        } else {
            Component cmp = getComponentAt(x, y);
            String uiid = cmp.getUIID();

            // Container is a special case, we don't want to allow customization of Container...
            if(uiid.equals("Container")) {
                return;
            }
            
            // check if there is nothing to customize for this component
            if(cmp.getUnselectedStyle().getBorder() != null) {
                if(cmp instanceof TextArea) {
                    if(((TextArea)cmp).getText() == null || ((TextArea)cmp).getText().length() == 0) {
                        return;
                    }
                } else {
                    if(cmp instanceof Label) {
                        if(((Label)cmp).getText() == null || ((Label)cmp).getText().length() == 0) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            
            Dialog customizerPopup = new Dialog(BoxLayout.y());
            customizerPopup.getContentPane().setScrollableY(false);
            Label title = new Label(uiid);
            title.getUnselectedStyle().setAlignment(CENTER);
            Button changeForegroundColor = new Button("Foreground Color");
            Button changeBackgroundColor = new Button("Background Color");
            Button changeFont = new Button("Font");
            Button defaultValue = new Button("Restore Style");
            
            changeForegroundColor.addActionListener(e -> {
                customizerPopup.dispose();
                Dialog fg = new Dialog(new BorderLayout());
                fg.add(BorderLayout.CENTER, colorPicker(cmp.getUnselectedStyle().getFgColor(), newColor -> {
                    cmp.getAllStyles().setFgColor(newColor);
                    getStyleSetting(uiid).fgColor.set(newColor);
                    cmp.repaint();
                }));
                fg.setTransitionInAnimator(CommonTransitions.createEmpty());
                fg.setTransitionOutAnimator(CommonTransitions.createEmpty());
                fg.showPopupDialog(cmp);
            });
            changeBackgroundColor.addActionListener(e -> {
                customizerPopup.dispose();
                Dialog fg = new Dialog(new BorderLayout());
                fg.add(BorderLayout.CENTER, colorPicker(cmp.getUnselectedStyle().getFgColor(), newColor -> {
                    cmp.getAllStyles().setBgColor(newColor);
                    cmp.getAllStyles().setBgTransparency(255);
                    getStyleSetting(uiid).bgColor.set(newColor);
                    cmp.repaint();
                }));
                fg.setTransitionInAnimator(CommonTransitions.createEmpty());
                fg.setTransitionOutAnimator(CommonTransitions.createEmpty());
                fg.showPopupDialog(cmp);
            });
            
            changeFont.addActionListener(e -> {
                customizerPopup.dispose();
                Dialog fg = new Dialog(new BorderLayout());
                fg.add(BorderLayout.CENTER, fontEditor(uiid, cmp.getUnselectedStyle().getFont(), newFont -> {
                    cmp.getAllStyles().setFont(newFont);
                    cmp.repaint();
                }));
                fg.setTransitionInAnimator(CommonTransitions.createEmpty());
                fg.setTransitionOutAnimator(CommonTransitions.createEmpty());
                fg.showPopupDialog(cmp);
            });
            
            defaultValue.addActionListener(e -> {
                cmp.setUIID(cmp.getUIID());
                customizerPopup.dispose();
            });

            if((cmp instanceof Label) || (cmp instanceof TextArea)) {
                if(cmp.getUnselectedStyle().getBorder() != null) {
                    customizerPopup.addAll(title, changeForegroundColor, changeFont, defaultValue);
                } else {
                    customizerPopup.addAll(title, changeBackgroundColor, changeForegroundColor, changeFont, defaultValue);
                }
            } else {
                customizerPopup.addAll(title, changeBackgroundColor);
            }
            
            customizerPopup.setTransitionInAnimator(CommonTransitions.createEmpty());
            customizerPopup.setTransitionOutAnimator(CommonTransitions.createEmpty());
            customizerPopup.showPopupDialog(cmp);
        }
    }

    @Override
    public void pointerReleased(int[] x, int[] y) {
        pointerReleased(x[0], y[0]);
    }
    
    Container colorPicker(int color, SuccessCallback<Integer> onChange) {
        Slider red = colorCustomizer(16, color);
        Slider green = colorCustomizer(8, color);
        Slider blue = colorCustomizer(0, color);
        TextField hex = new TextField(Integer.toHexString(color));
        Label preview = new Label(" ");
        Style previewStyle = preview.getUnselectedStyle();
        previewStyle.setBgTransparency(255);
        previewStyle.setBgColor(color);
        
        red.addDataChangedListener((e, ee) -> {
            updateColor(preview, hex, previewStyle.getBgColor() & 0xffff | ((red.getProgress() << 16) & 0xff0000), onChange);
        });
        green.addDataChangedListener((e, ee) -> {
            updateColor(preview, hex, previewStyle.getBgColor() & 0xff00ff | ((green.getProgress() << 8) & 0xff00), onChange);
        });
        blue.addDataChangedListener((e, ee) -> {
            updateColor(preview, hex, previewStyle.getBgColor() & 0xffff00 | (blue.getProgress() & 0xff), onChange);
        });
        hex.addDataChangedListener((e, ee) -> {
            if(acquireLock(preview)) {
                try {
                    int val = Integer.parseInt(hex.getText(), 16);
                    previewStyle.setBgColor(val);
                    red.setProgress((val >> 16) & 0xff);
                    green.setProgress((val >> 8) & 0xff);
                    blue.setProgress(val & 0xff);
                    onChange.onSucess(val);
                } catch(NumberFormatException err) {
                }
                releaseLock(preview);
            }
        });
        return BoxLayout.encloseY(red, green, blue, hex, preview);
    }
    
    private void updateColor(Label preview, TextField hex, int color, SuccessCallback<Integer> onChange) {
        if(acquireLock(preview)) {
            preview.getUnselectedStyle().setBgColor(color);
            hex.setText(Integer.toHexString(color));
            releaseLock(preview);
            onChange.onSucess(color);
        }
    }
    
    private boolean acquireLock(Label preview) {
        if(preview.getClientProperty("LOCK") != null) {
            return false;
        }
        preview.putClientProperty("LOCK", Boolean.TRUE);
        return true;
    }
    
    private void releaseLock(Label preview) {
        preview.putClientProperty("LOCK", null);
    }
    private Slider colorCustomizer(int shift, int color) {
        Slider s = new Slider();
        s.setMinValue(0);
        s.setMaxValue(255);
        s.setProgress((color >> shift) & 0xff);
        s.setEditable(true);
        Image m = Image.createImage(Display.getInstance().convertToPixels(4), Display.getInstance().
                convertToPixels(4), 0);
        Graphics g = m.getGraphics();
        g.setColor(0xff << shift);
        g.setAntiAliased(true);
        g.fillArc(0, 0, m.getWidth(), m.getHeight(), 0, 360);
        s.setThumbImage(m);
        Style stl = s.getSliderEmptyUnselectedStyle();
        stl.setBgTransparency(0);
        stl.setBorder(Border.createEmpty());
        stl = s.getSliderEmptySelectedStyle();
        stl.setBgTransparency(0);
        stl.setBorder(Border.createEmpty());
        stl = s.getSliderFullUnselectedStyle();
        stl.setBgTransparency(0);
        stl.setBackgroundType(Style.BACKGROUND_GRADIENT_LINEAR_HORIZONTAL);
        stl.setBackgroundGradientStartColor(0x77 << shift);
        stl.setBackgroundGradientEndColor(0xff << shift);
        stl = s.getSliderFullSelectedStyle();
        stl.setBgTransparency(0);
        stl.setBackgroundType(Style.BACKGROUND_GRADIENT_LINEAR_HORIZONTAL);
        stl.setBackgroundGradientStartColor(0x77 << shift);
        stl.setBackgroundGradientEndColor(0xff << shift);
        return s;
    }
    
    Container fontEditor(String uiid, Font fnt, SuccessCallback<Font> onChange) {
        int pixels = Display.getInstance().convertToPixels(1);
        int mm = (int)(fnt.getPixelSize() * 10 / pixels);
        if(mm % 5 != 0) {
            // fraction must be .5 and can't be something like .4 etc.
            if(mm % 5 > 2) {
                mm -= mm % 5 + 5; 
            } else {
                mm -= mm % 5;
            }
        } 
        if(!fnt.isTTFNativeFont()) {
            mm = 20;
        }
        
        Picker sizes = new Picker();
        sizes.setStrings("1.0", "1.5", "2.0", "2.5", "3.0", "3.5", "4.0", "4.5", "5.0", "5.5", "6.0", "6.5", "7.0");
        final int[] sizeValues = {10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70};
        for(int iter = 0 ; iter < sizeValues.length ; iter++) {
            if(sizeValues[iter] == mm) {
                sizes.setSelectedString(sizes.getStrings()[iter]);
                break;
            }
        }
        
        ButtonGroup bg = new ButtonGroup();
        RadioButton bold = RadioButton.createToggle("", bg);
        FontImage.setMaterialIcon(bold, FontImage.MATERIAL_FORMAT_BOLD);
        RadioButton italic = RadioButton.createToggle("", bg);
        FontImage.setMaterialIcon(italic, FontImage.MATERIAL_FORMAT_ITALIC);
        RadioButton thin = RadioButton.createToggle("thin", bg);
        RadioButton light = RadioButton.createToggle("light", bg);
        RadioButton normal = RadioButton.createToggle("normal", bg);
        
        Container group = GridLayout.encloseIn(5, bold, italic, thin, light, normal);
        
        TextField preview = new TextField("The quick brown fox jumped over the lazy dog");
        preview.getAllStyles().setFont(fnt);
        
        sizes.addActionListener(e -> {
            Font f = preview.getUnselectedStyle().getFont();
            int size = Display.getInstance().convertToPixels(sizeValues[sizes.getSelectedStringIndex()]);
            f = f.derive(size / 10, Font.STYLE_PLAIN);
            preview.getAllStyles().setFont(f);
            onChange.onSucess(f);
            getStyleSetting(uiid).fontSize.set(((float)sizeValues[sizes.getSelectedStringIndex()])/ 10.0f);
            if(getStyleSetting(uiid).font.get() == null) {
                getStyleSetting(uiid).font.set("native:MainLight");
            }
        });

        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                getStyleSetting(uiid).fontSize.set(((float)sizeValues[sizes.getSelectedStringIndex()])/ 10.0f);
                Font f = preview.getUnselectedStyle().getFont();
                if(bold.isSelected()) {
                    f = Font.createTrueTypeFont("native:MainBold", "native:MainBold").derive(f.getPixelSize(), Font.STYLE_BOLD);
                    getStyleSetting(uiid).font.set("native:MainBold");
                } else {
                    if(italic.isSelected()) {
                        f = Font.createTrueTypeFont("native:ItalicLight", "native:ItalicLight").derive(f.getPixelSize(), Font.STYLE_ITALIC);
                        getStyleSetting(uiid).font.set("native:ItalicLight");
                    } else {
                        if(thin.isSelected()) {
                            f = Font.createTrueTypeFont("native:MainThin", "native:MainThin").derive(f.getPixelSize(), Font.STYLE_PLAIN);
                            getStyleSetting(uiid).font.set("native:MainThin");
                        } else {
                            if(light.isSelected()) {
                                f = Font.createTrueTypeFont("native:MainLight", "native:MainLight").derive(f.getPixelSize(), Font.STYLE_PLAIN);
                                getStyleSetting(uiid).font.set("native:MainLight");
                            } else {
                                f = Font.createTrueTypeFont("native:MainRegular", "native:MainRegular").derive(f.getPixelSize(), Font.STYLE_PLAIN);
                                getStyleSetting(uiid).font.set("native:MainRegular");
                            }
                        }
                    }
                }
                preview.getAllStyles().setFont(f);
                onChange.onSucess(f);
            }
        };
        bold.addActionListener(al);
        italic.addActionListener(al);
        thin.addActionListener(al);
        light.addActionListener(al);
        normal.addActionListener(al);
        
        return BoxLayout.encloseY(
                new Label("Size (mm)", "TextFieldLabel"),
                sizes,
                group,
                preview
        );
        //add(new Label(label, "TextFieldLabel")).
    }
}
