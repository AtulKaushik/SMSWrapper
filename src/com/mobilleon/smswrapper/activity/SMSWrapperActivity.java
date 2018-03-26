package com.mobilleon.smswrapper.activity;

import java.util.ArrayList;
import java.util.Date;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.mobilleon.smswrapper.adapters.MessageAdapter;
import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.root.R;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService.SMSProLifecycleServiceBinder;
import com.mobilleon.smswrapper.utilities.Constants;
import com.mobilleon.smswrapper.utilities.NetworkUtility.CONNECTION;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;


/**
 * @author AtulKaushik (atul.kaushik@gmail.com)
 *
 */
public class SMSWrapperActivity extends SherlockListActivity implements OnSharedPreferenceChangeListener, OnQueryTextListener {

    private MyMessage myMessage;
    private ArrayList<MyMessage> smsList = new ArrayList<MyMessage>();

    private String googleAddress = ""; /* sender Google email address */
    private String googlePassword = ""; /* sender Google password */
    private boolean rememberPreference; /* remember Preference */
    private ListView mListView;
    private MessageAdapter mAdapter;
    private String TAG = SMSWrapperActivity.class.getSimpleName();
    private boolean mBound;
    
    @SuppressWarnings("unused")
    private SMSWrapperLifecycleService mService; // make use of this service object to invoke methods defined in service

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* initialize configured email address */
        getCredentials();
        setContentView(R.layout.smspro_list);
        setActionBar();
        smsList = getSmsList();

        setSMSAdapter(smsList);
        
        clearPreferences();
        if (!validateCredentials()) {
            Toast.makeText(this, "No settings found : Please check settings", Toast.LENGTH_LONG).show();
        }
    }
         
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        //Create the search view
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        
        mListView = getListView();
        mListView.setTextFilterEnabled(true);
        
        setupSearchView(searchView);
        
        menu.add(0, 1, 1, "Export")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        menu.add(0, 2, 2, "Import")
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        menu.add(0, 3, 3, null)
        .setIcon(R.drawable.ic_action_overflow)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        SubMenu subMenu = menu.addSubMenu(0, 0, 2, null);
       
        subMenu.add(0, 4, 4, "Search Message")
        .setIcon(R.drawable.ic_search)
        .setActionView(searchView)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
       
        
        subMenu.add(0, 5, 5, "Settings")
        .setIcon(R.drawable.ic_settings);
        
        subMenu.add(0, 6, 6, "About")
        .setIcon(R.drawable.ic_about);
        
        MenuItem subMenuItem = subMenu.getItem();
        subMenuItem.setIcon(R.drawable.ic_action_overflow);
        subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        // to hide, extra action bar sherlock overflow menu
        menu.getItem(3).setVisible(false);
            
        return super.onCreateOptionsMenu(menu);
    }
    
    private void setupSearchView(SearchView mSearchView) {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint("Search Sender");
    }
    
    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            mListView.clearTextFilter();
        } else {
            mListView.setFilterText(newText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG,"getItemId = "+ item.getItemId());
        switch (item.getItemId()) {
            
            case 1:
                if (!validateCredentials()) {
                    Toast.makeText(this, "Set settings first : Please check settings", Toast.LENGTH_LONG).show();
                    break;
                }
                startActivity(new Intent(this, MessageExportActivity.class));  
            break;
            
            case 2:
                if (!validateCredentials()) {
                    Toast.makeText(this, "Set settings first : Please check settings", Toast.LENGTH_LONG).show();
                    break;
                }
                if(Constants.INTERNET_CONNECTION == CONNECTION.PROCEED){
                    startActivity(new Intent(this, MessageImportActivity.class));
                }else{
                    Toast.makeText(this, "NO INTERNET CONNECTION : PLEASE CHECK YOUR CONNECTION", Toast.LENGTH_LONG).show();
                }
                break;
                
            case 5:
                Intent preferencesActivity = new Intent(getBaseContext(), SMSWrapperSettingsActivity.class);
                startActivity(preferencesActivity);
                break;
                
            case 6:
                Intent intent;
                intent = new Intent(SMSWrapperActivity.this, AboutActivity.class);
                startActivity(intent);
                //Toast.makeText(SMSProActivity.this, "Soon to be about screen", Toast.LENGTH_SHORT).show();
                break;
        }
        //return super.onOptionsItemSelected(item);
        return true;
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);
        
        myMessage = new MyMessage();
        myMessage = (MyMessage) l.getItemAtPosition(position);
              
        Intent shareIntent =  new Intent(SMSWrapperActivity.this, ShareAndSaveActivity.class);
        
        Bundle shareBundle = new Bundle();
        shareBundle.putStringArray("smsValues", new String [] {myMessage.getSender(), myMessage.getDate().trim(), myMessage.getTextContent()});
        
        shareIntent.putExtra("SMS", shareBundle);
        
        startActivity(shareIntent);

    }

    /** setting a split tiled action bar */
    private void setActionBar() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            BitmapDrawable bg = (BitmapDrawable)getResources().getDrawable(R.drawable.bg_striped);
            bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setBackgroundDrawable(bg);

            BitmapDrawable bgSplit = (BitmapDrawable)getResources().getDrawable(R.drawable.bg_striped_split_img);
            bgSplit.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
            getSupportActionBar().setSplitBackgroundDrawable(bgSplit);
        }
    }

    /** getting list of sms from device inbox */
    private ArrayList<MyMessage> getSmsList() {

        ArrayList<MyMessage> inboxList = new ArrayList<MyMessage>();
        MyMessage myMessage;

        Uri smsUri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(smsUri, null, null, null, null);

        while (cursor.moveToNext()) {
            myMessage = new MyMessage();
            myMessage.setSender(cursor.getString(cursor.getColumnIndex("address")));
            myMessage.setTextContent(cursor.getString(cursor.getColumnIndexOrThrow("body")));
            Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));

            myMessage.setDate(Long.toString(date.getTime()));

            inboxList.add(myMessage);
        }
        cursor.close();
        return inboxList;
    }

    /** Setting adapter for list view */
    private void setSMSAdapter(ArrayList<MyMessage> messagesList) {

        if (!messagesList.isEmpty()) {
            mAdapter = new MessageAdapter(getApplicationContext(), messagesList, false);
            setListAdapter(mAdapter);
        } else {
            setListAdapter(null);
        }
    }

    
    /**
     * Clearing Preferences
     * */
    private void clearPreferences() {

        if (!rememberPreference) {
            googleAddress = "";
            googlePassword = "";    
        
        }
    }

    /**
     * rereads the configuration and updates the start screen
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {

        super.onResume();
        getCredentials();

        smsList = getSmsList();
        setSMSAdapter(smsList);

    }

    /**
     * updates the members according to the changed preference
     * 
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences,
     *      java.lang.String)
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.contentEquals("googleAddress")) {
            googleAddress = sharedPreferences.getString("googleAddress", "");
        } else if (key.contentEquals("googlePassword")) {
            googlePassword = sharedPreferences.getString("googlePassword", "");
        } else {
        }
    }

    /**
     * reads configuration
     */
    private void getCredentials() {
        SharedPreferences preferences;
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            googleAddress = preferences.getString("googleAddress", "");
            googlePassword = preferences.getString("googlePassword", "");
            rememberPreference = preferences.getBoolean("remember", false);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private boolean validateCredentials() {

        boolean flag = false;
        if (googleAddress.length() > 5 && googleAddress.contains("@") && googleAddress.contains(".")) {
            if (googlePassword.length() > 3) {
                flag = true;
            }
        }

        return flag;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
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