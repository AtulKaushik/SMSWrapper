package com.mobilleon.smswrapper.application;

import java.lang.reflect.Field;

import android.app.Application;
import android.os.StrictMode;
import android.view.ViewConfiguration;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class SMSWrapperApplication extends Application{

    @Override
    public void onCreate() {
        
        StrictMode.enableDefaults();
        getForcedOverflowMenu();
        super.onCreate();
    }
    
    /** getting forced overflow menu for devices having menu key */
    private void getForcedOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
