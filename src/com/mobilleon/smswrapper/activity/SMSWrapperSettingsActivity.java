/**
 * preference activity
 * for providing
 * app settings
 */
package com.mobilleon.smswrapper.activity;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.mobilleon.smswrapper.root.R;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService.SMSProLifecycleServiceBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author AtulKaushik (atul.kaushik@gmail.com)
 *
 */
public class SMSWrapperSettingsActivity extends SherlockPreferenceActivity {

    private EditTextPreference userId;
    private EditTextPreference userPwd;
    private CheckBoxPreference remember;
    private boolean mBound;
    
    @SuppressWarnings("unused")
    private SMSWrapperLifecycleService mService; // make use of this service object to invoke methods defined in service
    private String TAG = AboutActivity.class.getSimpleName();

    /**
     * creates preference dialog from XML
     * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.preferences);
        initUI();
        clearPreferences();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(SMSWrapperSettingsActivity.this, SMSWrapperActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * initializing UI
     * */
    private void initUI() {

        userId = (EditTextPreference) findPreference("googleAddress");
        userPwd = (EditTextPreference) findPreference("googlePassword");
        remember = (CheckBoxPreference) findPreference("remember");

    }

    /**
     * Method to clear preferences
     * */
    private void clearPreferences() {

        if (!remember.isChecked()) {
            userId.setText("");
            userPwd.setText("");
        }
    }
    
    @Override
    protected void onStart() {
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("remember", false)){
            Intent intent = new Intent(this, SMSWrapperLifecycleService.class);
            bindService(intent , mServiceConnection, Context.BIND_AUTO_CREATE);    
        }
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        if(mBound)
            unbindService(mServiceConnection);  
        super.onStop();
    }
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            mBound = false;
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            SMSProLifecycleServiceBinder serviceBinder = (SMSProLifecycleServiceBinder) service;
            mService = serviceBinder.getService();
           mBound = true;
        }
    };
}
