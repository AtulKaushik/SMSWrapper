/**
 * 
 */
package com.mobilleon.smswrapper.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class SMSWrapperLifecycleService extends Service{

    String TAG = SMSWrapperLifecycleService.class.getSimpleName();
 // Binder given to clients
    public final IBinder mBinder = new SMSProLifecycleServiceBinder();
    private SharedPreferences.Editor mEditor;
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
        public void onDestroy() {
        Log.i(TAG, "onDestroy");
        //Toast.makeText(SMSProLifecycleService.this, "App has been terminated!", Toast.LENGTH_SHORT).show();
        clearSession();
            super.onDestroy();
        }
    
    private void clearSession() {
        Log.i(TAG, "clearSession");
        mEditor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        mEditor.clear();
        mEditor.commit();
    }
    
    @Override
    public void onCreate() {
        //Toast.makeText(SMSProLifecycleService.this, "App has been brought to front!", Toast.LENGTH_SHORT).show();
        super.onCreate();
    }
    
    public class SMSProLifecycleServiceBinder extends Binder{
        
        public SMSWrapperLifecycleService getService() {
            // Return this instance of ApplicationLifecycleService so clients can call public methods
            return SMSWrapperLifecycleService.this;
        }
    }
    
}
