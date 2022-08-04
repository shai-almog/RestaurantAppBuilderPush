package com.majimob.app.restaurantbuilder.model;

import com.codename1.components.ToastBar;
import com.codename1.db.Database;
import com.codename1.io.Log;
import com.codename1.properties.Property;
import com.codename1.properties.PropertyBase;
import com.codename1.properties.PropertyBusinessObject;
import com.codename1.properties.SQLMap;
import com.codename1.ui.Display;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.events.ActionListener;
import com.codename1.ui.util.EventDispatcher;
import com.codename1.ui.util.Resources;
import com.myrestaurant.app.model.Dish;
import java.io.IOException;
import java.util.List;

public class AppStorage {
    private static SQLMap smap;
    private static Database db;
    private static AppStorage instance = new AppStorage();
    private EventDispatcher dispatcher = new EventDispatcher();
    
    private AppStorage() {}
    
    public static AppStorage getInstance() {
        initStorage();
        return instance;
    }
    
    private static void initStorage() {
        if(smap == null) {
            try {
                db = Display.getInstance().openOrCreate("RestDb.sql");
                smap = SQLMap.create(db);
                AppSettings app = new AppSettings();
                Dish dish = new Dish();
                StyleSettings styles = new StyleSettings();
                smap.setPrimaryKey(app, app.id);
                smap.setPrimaryKey(dish, dish.id);
                smap.setPrimaryKey(styles, styles.uiid);
                smap.createTable(app);
                smap.createTable(dish);
                smap.createTable(styles);
            } catch(IOException err) {
                Log.e(err);
            }
        }
    }
    
    public void insert(PropertyBusinessObject d) {
        try {
            smap.insert(d);
        } catch(IOException err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error saving: " + err);
        }
    }

    public void update(PropertyBusinessObject d) {
        try {
            updateImpl(d);
        } catch(IOException err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error updating storage: " + err);
        }
    }

    public void addDeleteListener(ActionListener<ActionEvent> onDelete) {
        dispatcher.addListener(onDelete);
    }

    public void removeDeleteListener(ActionListener<ActionEvent> onDelete) {
        dispatcher.removeListener(onDelete);
    }
    
    public void delete(PropertyBusinessObject d) {
        try {
            deleteImpl(d);
            dispatcher.fireActionEvent(new ActionEvent(d));
        } catch(IOException err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error deleting: " + err);
        }
    }

    public AppSettings fetchAppSettings() {
        try {
            AppSettings a = new AppSettings();
            List<PropertyBusinessObject> lp = smap.select(a, null, true, 1000, 0);
            if(lp.size() == 0) {
                a = new AppSettings();
                a.id.set("1");
                insert(a);
                return a;
            }
            return (AppSettings)lp.get(0);
        } catch(Exception err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error loading AppSettings: " + err);
            return null;
        }
    }
    
    public void insertOrUpdate(StyleSettings s) {
        StyleSettings ss = fetchStyle(s.uiid.get());
        if(ss == null) {
            insert(s);
        } else {
            update(s);
        }
    }
    
    public StyleSettings fetchStyle(String uiid) {
        try {
            StyleSettings s = new StyleSettings();
            s.uiid.set(uiid);
            List<PropertyBusinessObject> r = smap.select(s, null, true, 1, 0);
            if(r == null || r.size() == 0) {
                return null;
            }
            return (StyleSettings)r.get(0);
        } catch(Exception err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error finding style: " + err);
            return null;
        }
    }
    
    public List<PropertyBusinessObject> fetchStyles() {
        try {
            StyleSettings d = new StyleSettings();
            return smap.select(d, null, true, 1000, 0);
        } catch(Exception err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error loading styles: " + err);
            return null;
        }
    }

    public List<PropertyBusinessObject> fetchDishes() {
        try {
            Dish d = new Dish();
            List<PropertyBusinessObject> lp = smap.select(d, d.name, true, 1000, 0);
            
            // workaround for null images
            for(PropertyBusinessObject p : lp) {
                Dish dd = (Dish)p;
                if(dd.getFullSize() == null) {
                    dd.setFullSize(Resources.getGlobalResources().getImage("food1.jpg"));
                }
            }
            return lp;
        } catch(Exception err) {
            Log.e(err);
            ToastBar.showErrorMessage("Error loading dishes: " + err);
            return null;
        }
    }

    // workaround for issue in SQLMap
    private void deleteImpl(PropertyBusinessObject cmp) throws IOException {
        String pkName = (String)cmp.getPropertyIndex().getMetaDataOfClass("cn1$pk");
        String tableName = smap.getTableName(cmp);
        StringBuilder createStatement = new StringBuilder("DELETE FROM ");
        createStatement.append(tableName);
        createStatement.append(" WHERE ");

        createStatement.append(pkName);
        createStatement.append(" = ?");
        Property p = (Property)cmp.getPropertyIndex().getIgnoreCase(pkName);
        db.execute(createStatement.toString(), new Object[]{ p.get() });
    }

    // workaround for bug in SQLMap code
    private void updateImpl(PropertyBusinessObject cmp) throws IOException {
        String pkName = (String)cmp.getPropertyIndex().getMetaDataOfClass("cn1$pk");
        if(pkName == null) {
            throw new IOException("Primary key required for update");
        }
        String tableName = smap.getTableName(cmp);
        StringBuilder createStatement = new StringBuilder("UPDATE ");
        createStatement.append(tableName);
        createStatement.append(" SET ");

        int count = 0;
        Object[] values;
        values = new Object[cmp.getPropertyIndex().getSize() + 1];
        for(PropertyBase p : cmp.getPropertyIndex()) {
            if(count > 0) {
                createStatement.append(",");
            }
            if(p instanceof Property) {
                values[count] = ((Property)p).get();
            } else {
                // TODO
                values[count] = null;
            }
            count++;
            String columnName = smap.getColumnName(p);
            createStatement.append(columnName);
            createStatement.append(" = ?");
        }
        
        createStatement.append(" WHERE ");

        createStatement.append(pkName);
        createStatement.append(" = ?");
        
        Property p = (Property)cmp.getPropertyIndex().getIgnoreCase(pkName);
        values[values.length - 1] = p.get();
        
        db.execute(createStatement.toString(), values);
    }
}
