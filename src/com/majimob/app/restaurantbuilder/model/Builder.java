package com.majimob.app.restaurantbuilder.model;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.MultipartRequest;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.io.Util;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.push.Push;
import com.codename1.ui.Display;
import com.codename1.ui.EncodedImage;
import com.codename1.ui.Image;
import com.codename1.util.SuccessCallback;
import com.majimob.app.restaurantbuilder.RestaurantAppBuilder;
import com.myrestaurant.app.model.Dish;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class Builder {
    private static ArrayList<SuccessCallback<String>> restaurantSetupRequestInProgress;
    public void createRestaurantIfNecessary(AppSettings app, SuccessCallback<String> s) {
        String rkey = Preferences.get("restaurant-key", null);
        if(rkey != null) {
            s.onSucess(rkey);
            return;
        }
        
        if(!Display.getInstance().isEdt()) {
            throw new IllegalStateException();
        }
        if(restaurantSetupRequestInProgress != null) {
            restaurantSetupRequestInProgress.add(s);
            return;
        }
        restaurantSetupRequestInProgress = new ArrayList<>();
        restaurantSetupRequestInProgress.add(s);
        final String json = app.toJSONWithRestaurant();
        ConnectionRequest cr = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "updateRestaurant", true) {
            private String result;
            @Override
            protected void readResponse(InputStream input) throws IOException {
                result = Util.readToString(input, "UTF-8");
            }

            @Override
            protected void buildRequestBody(OutputStream os) throws IOException {
                os.write(json.getBytes("UTF-8"));
            }
            
            @Override
            protected void postResponse() {
                int code = getResponseCode();
                if(code < 300 && code >= 200) {
                    Preferences.set("restaurant-key", result);
                    for(SuccessCallback<String> s : restaurantSetupRequestInProgress) {
                        s.onSucess(result);
                    }
                }
            }
        };
        cr.setContentType("application/json");
        cr.setHttpMethod("PUT");
        cr.setFailSilently(true);
        cr.setReadResponseForErrors(true);
        NetworkManager.getInstance().addToQueue(cr);
    }
    
    public void updateRestaurantSettings(AppSettings app) {
        final String json = app.toJSONWithRestaurant();
        ConnectionRequest cr = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "updateRestaurant", true) {
            private String result;
            @Override
            protected void readResponse(InputStream input) throws IOException {
                result = Util.readToString(input, "UTF-8");
            }

            @Override
            protected void buildRequestBody(OutputStream os) throws IOException {
                os.write(json.getBytes("UTF-8"));
            }
        };
        cr.setContentType("application/json");
        cr.setHttpMethod("PUT");
        cr.setFailSilently(true);
        cr.setReadResponseForErrors(true);
        NetworkManager.getInstance().addToQueue(cr);
    }
    
    public void buildApp(AppSettings app, String target) {
        createRestaurantIfNecessary(app, secret -> {
            ConnectionRequest cr = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "doBuildApp", true);
            cr.addArgument("secret", secret);
            if(Push.getPushKey() != null) {
                cr.addArgument("pushKey", Push.getPushKey());
            } else {
                ConnectionRequest polling = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "doBuildApp", false);
                NetworkManager.getInstance().addToQueueAndWait(polling);
                if(polling.getResponseCode() == 200) {
                    final String currentValue = new String(polling.getResponseData());
                    Timer t = new Timer();
                    t.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ConnectionRequest polling = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "doBuildApp", false);
                            NetworkManager.getInstance().addToQueueAndWait(polling);
                            if(polling.getResponseCode() == 200) {
                                final String value = new String(polling.getResponseData());
                                if(!value.equals(currentValue)) {
                                    cancel();
                                    RestaurantAppBuilder.onBuildResult(value);
                                }
                            }
                        }
                    }, 60000, 60000);
                }
            }
            cr.addArgument("targetType", target);
            cr.setReadResponseForErrors(true);
            NetworkManager.getInstance().addToQueue(cr);
        });
    }

    public void setImage(String name, Image img) {
        MultipartRequest cr = new MultipartRequest();
        
        cr.setUrl(RestaurantAppBuilder.SERVER_URL + "updateRestaurant");
        cr.addData(name, EncodedImage.createFromImage(img, false).getImageData(), "image/png");
        cr.setFilename(name, name);
        cr.setFailSilently(true);
        cr.setReadResponseForErrors(true);
        NetworkManager.getInstance().addToQueue(cr);
    }
    
    public void addDish(AppSettings app, Dish d) {
        createRestaurantIfNecessary(app, secret -> {
            ConnectionRequest cr = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "dish", true);
            cr.setHttpMethod("PUT");
            cr.setReadResponseForErrors(true);
            cr.setContentType("application/json");
            cr.setRequestBody(d.toJSON(secret));
            cr.addResponseListener(e -> {
                String id = new String(cr.getResponseData());
                d.id.set(id);
                updateDishImage(app, d, secret);
            });
            NetworkManager.getInstance().addToQueue(cr);
        });
    }
    
    public void updateDish(AppSettings app, Dish d) {
        createRestaurantIfNecessary(app, secret -> {
            ConnectionRequest cr = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "dish", true);
            cr.setReadResponseForErrors(true);
            cr.setHttpMethod("PUT");
            cr.setContentType("application/json");
            cr.setRequestBody(d.toJSON(secret));
            NetworkManager.getInstance().addToQueue(cr);
        });
    }

    public void deleteDish(AppSettings app, Dish d) {
        createRestaurantIfNecessary(app, secret -> {
            ConnectionRequest cr = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "dish", true);
            cr.setReadResponseForErrors(true);
            cr.setHttpMethod("DELETE");
            cr.setContentType("application/json");
            cr.setRequestBody(d.toJSON(secret));
            NetworkManager.getInstance().addToQueue(cr);
        });
    }

    public void updateDishImage(AppSettings app, Dish d) {
        createRestaurantIfNecessary(app, secret -> {
            updateDishImage(app, d, secret);
        });
    }

    private void updateDishImage(AppSettings app, Dish d, String secret) {
        MultipartRequest mr = new MultipartRequest();
        mr.setUrl(RestaurantAppBuilder.SERVER_URL + "dish");
        mr.setReadResponseForErrors(true);
        mr.addData("img", EncodedImage.createFromImage(d.getFullSize(), false).getImageData(), "image/png");
        mr.setFilename("img", "img");
        mr.addArgument("secret", secret);
        mr.addArgument("d", d.id.get());
        NetworkManager.getInstance().addToQueue(mr);
    }
    
    public void updateStyles(AppSettings app) {
        createRestaurantIfNecessary(app, secret -> {
            ConnectionRequest cr = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "style", true);
            cr.setHttpMethod("PUT");
            cr.setReadResponseForErrors(true);
            cr.setContentType("application/json");
            
            String json = "{\"secret\":\"" + secret + "\",\"styles\":[";
            
            boolean first = true;
            for(PropertyBusinessObject bo : AppStorage.getInstance().fetchStyles()) {
                if(!first) {
                    json += "\n,";
                }
                first = false;
                json += bo.getPropertyIndex().toJSON();
            }
            
            json +="]}";
            
            cr.setRequestBody(json);
            
            NetworkManager.getInstance().addToQueue(cr);
        });
    }    
}
