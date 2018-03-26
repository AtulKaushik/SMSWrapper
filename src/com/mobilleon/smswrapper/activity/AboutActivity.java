package com.mobilleon.smswrapper.activity;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import com.mobilleon.smswrapper.adapters.AboutViewPager;
import com.mobilleon.smswrapper.root.R;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService.SMSProLifecycleServiceBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;



public class AboutActivity extends SherlockFragmentActivity {
	private ViewPager _mViewPager;
	private AboutViewPager _adapter;
	
	private boolean mBound;
	
	@SuppressWarnings("unused")
    private SMSWrapperLifecycleService mService; // make use of this service object to invoke methods defined in service
    private String TAG = AboutActivity.class.getSimpleName();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setUpView();
        setTab();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(AboutActivity.this, SMSWrapperActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            }
        return super.onOptionsItemSelected(item);
    }
    
    private void setUpView(){    	
   	 _mViewPager = (ViewPager) findViewById(R.id.viewPager);
     _adapter = new AboutViewPager(getApplicationContext(),getSupportFragmentManager());
     _mViewPager.setAdapter(_adapter);
	 _mViewPager.setCurrentItem(0);
    }
    private void setTab(){
			_mViewPager.setOnPageChangeListener(new OnPageChangeListener(){
			    		
						@Override
						public void onPageScrollStateChanged(int position) {}
						@Override
						public void onPageScrolled(int arg0, float arg1, int arg2) {}
						@Override
						public void onPageSelected(int position) {
							switch(position){
							case 0:
								findViewById(R.id.first_tab).setVisibility(View.VISIBLE);
								findViewById(R.id.second_tab).setVisibility(View.INVISIBLE);
								break;
								
							case 1:
								findViewById(R.id.first_tab).setVisibility(View.INVISIBLE);
								findViewById(R.id.second_tab).setVisibility(View.VISIBLE);
								break;
							}
					}
					
			});
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
            Toast.makeText(AboutActivity.this, "Service UNBOUND to "+TAG, Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            SMSProLifecycleServiceBinder serviceBinder = (SMSProLifecycleServiceBinder) service;
            mService = serviceBinder.getService();
           mBound = true;
            Toast.makeText(AboutActivity.this, "Service BOUND to "+TAG, Toast.LENGTH_SHORT).show();
        }
    };
}