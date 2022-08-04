package com.myrestaurant.app.model;

import com.codename1.io.ConnectionRequest;
import com.codename1.io.NetworkManager;
import com.codename1.io.Preferences;
import com.codename1.util.SuccessCallback;
import com.majimob.app.restaurantbuilder.RestaurantAppBuilder;

public class KeyManager {
    public static void fetchGoogleMapsKey(final SuccessCallback<String> onComplete) {
        ConnectionRequest cr = new ConnectionRequest(RestaurantAppBuilder.SERVER_URL + "key", false) {
            @Override
            protected void postResponse() {
                String s = new String(getResponseData());
                onComplete.onSucess(s);
            }
        };
        cr.addArgument("type", "gmk");
        cr.addArgument("appId", RestaurantAppBuilder.SERVER_URL);
        NetworkManager.getInstance().addToQueue(cr);
    }
}
