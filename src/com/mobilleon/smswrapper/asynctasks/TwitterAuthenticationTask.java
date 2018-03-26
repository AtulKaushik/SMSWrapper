package com.mobilleon.smswrapper.asynctasks;

import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.model.SMSWrapperData;
import com.mobilleon.smswrapper.utilities.Constants;

import oauth.signpost.OAuth;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class TwitterAuthenticationTask extends AsyncTask<SharedPreferences, Void, Boolean>{

    private SharedPreferences prefs;
    private Handler mTwitterAuthenticationHandler;
    private String TAG = TwitterAuthenticationTask.class.getSimpleName();
    
    public TwitterAuthenticationTask(Handler mTwitterAuthenticationHandler){
        super();
        this.mTwitterAuthenticationHandler = mTwitterAuthenticationHandler;
    }
    
    @Override
    protected Boolean doInBackground(SharedPreferences... params) {

        if(params == null || params[0] == null)return false;
        prefs = params[0];
        
        String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
        String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
        
        AccessToken a = new AccessToken(token,secret);
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
        twitter.setOAuthAccessToken(a);
    
        
        try {
            twitter.getAccountSettings();
        } catch (TwitterException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        
        Log.i(TAG, "result = "+result);
        if (mTwitterAuthenticationHandler == null) return;
        
        SMSWrapperData<MyMessage> resultData = new SMSWrapperData<MyMessage>();
        if(result != null && result){
            resultData.requestSuccessful = true;
        }else{
            resultData.requestSuccessful = false;
        }
        
        android.os.Message m = android.os.Message.obtain();
        m.obj = resultData;
        mTwitterAuthenticationHandler.sendMessage(m);

    }
}
