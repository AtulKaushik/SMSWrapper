/**
 * 
 */
package com.mobilleon.smswrapper.activity;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.mobilleon.smswrapper.asynctasks.OAuthRequestTokenTask;
import com.mobilleon.smswrapper.asynctasks.RetrieveTwitterAccessTokenTask;
import com.mobilleon.smswrapper.asynctasks.TwitterAuthenticationTask;
import com.mobilleon.smswrapper.asynctasks.TwitterUpdateTask;
import com.mobilleon.smswrapper.facebook.FacebookConnector;
import com.mobilleon.smswrapper.library.facebook.utilities.SessionEvents;
import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.model.SMSWrapperData;
import com.mobilleon.smswrapper.root.R;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService.SMSProLifecycleServiceBinder;
import com.mobilleon.smswrapper.utilities.Constants;
import com.mobilleon.smswrapper.utilities.NetworkUtility.CONNECTION;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
@SuppressLint("HandlerLeak")
public class ShareAndSaveActivity extends SherlockActivity {

    private TextView content;
    private TextView sender;
    private TextView date;
    private Context mContext;
    private SharedPreferences prefs;
    private OAuthConsumer consumer; 
    private OAuthProvider provider;  
    private FacebookConnector facebookConnector;
    private final Handler mFacebookHandler = new Handler();
    private final Handler mTwitterHandler = new Handler();
    private String TAG = ShareAndSaveActivity.class.getSimpleName();
    private boolean mBound;
    
    @SuppressWarnings("unused")
    private SMSWrapperLifecycleService mService; // make use of this service object to invoke methods defined in service
    
    final Runnable updateFacebookNotification = new Runnable() {

        public void run() {

            Toast.makeText(getBaseContext(), "facebook wall updated successfully", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mContext = getParent();
        setContentView(R.layout.share_sms);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initUI();
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.facebookConnector = new FacebookConnector(Constants.FACEBOOK_APPID, this, getApplicationContext(), new String[] {
            Constants.FACEBOOK_PERMISSION
        });

        Bundle smsBundle = this.getIntent().getBundleExtra("SMS");
        String[] smsArray;
        smsArray = smsBundle.getStringArray("smsValues");

        setSmsValues(smsArray);

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        menu.add(0, 1, 1, R.string.save)
        .setIcon(R.drawable.ic_save)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        SubMenu subMenu = menu.addSubMenu(0, 0, 2, "SHARE");
        
        subMenu.add(0, 2, 2, R.string.tweet)
        .setIcon(R.drawable.twitter_icon);
        
        subMenu.add(0, 3, 3, R.string.facebook)
        .setIcon(R.drawable.facebook_icon);
        
        MenuItem subMenuItem = subMenu.getItem();
        subMenuItem.setIcon(R.drawable.ic_menu_share);
        subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            
            case android.R.id.home:
                Intent intent = new Intent(ShareAndSaveActivity.this, SMSWrapperActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            
            case 1:
                saveToSdCard(sender.getText().toString(), content.getText().toString());
            break;
            
            case 2:
                if(Constants.INTERNET_CONNECTION == CONNECTION.PROCEED){
                    /**
                     * Send a tweet. If the user hasn't authenticated to Tweeter yet, he'll be redirected via a browser
                     * to the twitter login page. Once the user authenticated, he'll authorize the Android application to send
                     * tweets on the users behalf.
                     */

                    new TwitterAuthenticationTask(mTwitterAuthenticationHandler).execute(prefs);
                }else{
                    Toast.makeText(this, "NO INTERNET CONNECTION : PLEASE CHECK YOUR CONNECTION", Toast.LENGTH_LONG).show();
                }
                break;

            case 3:
                if(Constants.INTERNET_CONNECTION == CONNECTION.PROCEED){
                    postMessage();
                }else{
                    Toast.makeText(this, "NO INTERNET CONNECTION : PLEASE CHECK YOUR CONNECTION", Toast.LENGTH_LONG).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        try {
            super.onWindowFocusChanged(hasFocus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        this.facebookConnector.getFacebook().authorizeCallback(requestCode, resultCode, data);
    }

    private String getFacebookMsg() {

        return getPostMeassage(content) + "\n" + "- Shared from my SMS Wrapper";
    }

    public void postMessage() {

        if (facebookConnector.getFacebook().isSessionValid()) {
            Log.i(TAG, "Session valid");
            postMessageInThread();
        } else {
            SessionEvents.AuthListener listener = new SessionEvents.AuthListener() {

                @Override
                public void onAuthSucceed() {

                    Log.i(TAG, "Session valid : first auth");

                    postMessageInThread();
                }

                @Override
                public void onAuthFail(String error) {

                    Log.i(TAG, "Session invalid");

                }
            };
            SessionEvents.addAuthListener(listener);
            facebookConnector.login();
        }
    }
    
    private void postMessageInThread() {

        Thread t = new Thread() {

            public void run() {

                try {
                    facebookConnector.postMessageOnWall(getFacebookMsg());
                    mFacebookHandler.post(updateFacebookNotification);
                } catch (Exception ex) {
                    Log.e(TAG, "error while posting message on wall ", ex);
                }
            }
        };
        t.start();
    }

    private void initUI() {

        sender = (TextView) findViewById(R.id.sender);
        date = (TextView) findViewById(R.id.date);
        content = (TextView) findViewById(R.id.sms_content);
    }

    private void setSmsValues(String[] smsValues) {

        sender.setText(smsValues[0]);
        date.setText(new Date(Long.parseLong(smsValues[1])).toLocaleString());
        content.setText(smsValues[2]);

    }

    private String getPostMeassage(TextView smsContent) {

        String post = "";
        try {
            post = smsContent.getText().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    private boolean checkExternalStorageState() {

        String auxSDCardStatus = Environment.getExternalStorageState();

        if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED))
            return true;
        else if (auxSDCardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            Toast.makeText(mContext,"SDCard is in read mode only.\nCan not save to SDCard.", Toast.LENGTH_LONG).show();
            return false;
        } else if (auxSDCardStatus.equals(Environment.MEDIA_NOFS)) {
            Toast.makeText(mContext,"SDCard has not corret format.\nCan not save to SDCard.",Toast.LENGTH_LONG).show();
            return false;
        } else if (auxSDCardStatus.equals(Environment.MEDIA_REMOVED)) {
            Toast.makeText(mContext,"SDCard not found.\nPlease insert a SDCard",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (auxSDCardStatus.equals(Environment.MEDIA_SHARED)) {
            Toast.makeText(mContext,"SDCard is in use.\nPlease plug out and try again.",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTABLE)) {
            Toast.makeText(
                    mContext,"SDCard can not be mounted.\nPlease try again or change SDCard", Toast.LENGTH_LONG).show();
            return false;
        } else if (auxSDCardStatus.equals(Environment.MEDIA_UNMOUNTED)) {
            Toast.makeText(mContext,"SDCard is not mounted.\nMount it before using the app.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return false;

    }

    private void saveToSdCard(String messageHead, String messageBody) {
        if(checkExternalStorageState()){
            try
            {
                File SMSProFile = new File(Environment.getExternalStorageDirectory(), "SMS Wrapper");
                if (!SMSProFile.exists()) {
                    SMSProFile.mkdirs();
                }
                FileWriter writer = new FileWriter(new File(SMSProFile, messageHead));
                writer.append(messageBody);
                writer.flush();
                writer.close();
                Toast.makeText(this, "Saved to SDCard", Toast.LENGTH_SHORT).show();
            }
            catch(IOException e)
            {
                 e.printStackTrace();
            }   
        }
    }

    final Runnable mUpdateTwitterNotification = new Runnable() {
        public void run() {
            Toast.makeText(getBaseContext(), "Tweet sent !", Toast.LENGTH_LONG).show();
        }
    };
    
    private String getTweetMsg() {
        return content.getText().toString();
    }   
    
    public void sendTweet() {

        try {
            new TwitterUpdateTask(getTweetMsg()).execute(prefs);
            mTwitterHandler.post(mUpdateTwitterNotification);
        } catch (Exception ex) {
            Toast.makeText(getBaseContext(), "Tweet not sent !", Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    
    }
    
    /**
     * Called when the OAuthRequestTokenTask finishes (user has authorized the request token).
     * The callback URL will be intercepted here.
     */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent); 
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final Uri uri = intent.getData();
        if (uri != null && uri.getScheme().equals(Constants.OAUTH_CALLBACK_SCHEME)) {
            Log.i(TAG, "Callback received : " + uri);
            Log.i(TAG, "Retrieving Access Token");
            new RetrieveTwitterAccessTokenTask(mTwitterUpdateHandler, consumer, provider, prefs).execute(uri);
            finish();   
        }
    }
        
    private Handler mTwitterUpdateHandler = new Handler(){
        
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            SMSWrapperData<MyMessage> responseData = (SMSWrapperData<MyMessage>) msg.obj;
            
            if(responseData == null){
                Toast.makeText(ShareAndSaveActivity.this, "Tweet failed !", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if(responseData.requestSuccessful){
                
                try {
                    new TwitterUpdateTask(getTweetMsg()).execute(prefs);
                } catch (Exception e) {
                    Log.e(TAG, "OAuth - Error sending to Twitter", e);
                }
                
                Toast.makeText(ShareAndSaveActivity.this, "Tweet sent !", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(ShareAndSaveActivity.this, "Tweet failed !", Toast.LENGTH_SHORT).show();
            }
        
        }
    };
    
    private Handler mTwitterAuthenticationHandler = new Handler(){
        
        @SuppressWarnings("unchecked")
        public void handleMessage(android.os.Message msg) {
            
            SMSWrapperData<MyMessage> responseData = (SMSWrapperData<MyMessage>) msg.obj;
            if(responseData.requestSuccessful){
                sendTweet();
            }else{
                
                /**
                 * Prepares a OAuthConsumer and OAuthProvider 
                 * 
                 * OAuthConsumer is configured with the consumer key & consumer secret.
                 * OAuthProvider is configured with the 3 OAuth endpoints.
                 * 
                 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the request.
                 * 
                 * After the request is authorized, a callback is made here.
                 * 
                 */
                
                try {
                    consumer = new CommonsHttpOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
                    provider = new CommonsHttpOAuthProvider(Constants.REQUEST_URL,Constants.ACCESS_URL,Constants.AUTHORIZE_URL);
                } catch (Exception e) {
                    Log.e(TAG, "Error creating consumer / provider",e);
                }

                Log.i(TAG, "Starting task to retrieve request token.");
                new OAuthRequestTokenTask(ShareAndSaveActivity.this,consumer,provider).execute();
            }
        };
    };
    
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
