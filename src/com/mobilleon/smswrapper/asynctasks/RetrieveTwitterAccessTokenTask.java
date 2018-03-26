/**
 * task to retrieve
 * twitter access
 * token 
 * */

package com.mobilleon.smswrapper.asynctasks;

import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.model.SMSWrapperData;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */

public class RetrieveTwitterAccessTokenTask extends AsyncTask<Uri, Void, Boolean> {

    private OAuthProvider provider;
    private OAuthConsumer consumer;
    private SharedPreferences prefs;
    private Handler handler;
    private String TAG = RetrieveTwitterAccessTokenTask.class.getSimpleName();
    
    public RetrieveTwitterAccessTokenTask(Handler handler, OAuthConsumer consumer,OAuthProvider provider, SharedPreferences prefs) {
        this.handler = handler;
        this.consumer = consumer;
        this.provider = provider;
        this.prefs=prefs;
    }


    /**
     * Retrieve the oauth_verifier, and store the oauth and oauth_token_secret 
     * for future API calls.
     */
    @Override
    protected Boolean doInBackground(Uri...params) {
        final Uri uri = params[0];
        final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

        try {
            provider.retrieveAccessToken(consumer, oauth_verifier);

            final Editor edit = prefs.edit();
            edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
            edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
            edit.commit(); //consumes memory, better use edit.apply() but ignoring as of now due to QA costs 
            
            
            String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
            String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
            
            consumer.setTokenWithSecret(token, secret);

            Log.i(TAG, "OAuth - Access Token Retrieved");
            
        } catch (Exception e) {
            Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
            return false;
        }

        return true;
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        
        if (handler == null || result == null) return;
        
        SMSWrapperData<MyMessage> resultData = new SMSWrapperData<MyMessage>();
        if(result){
            resultData.requestSuccessful = true;
        }else{
            resultData.requestSuccessful = false;    
        }
        
        android.os.Message m = android.os.Message.obtain();
        m.obj = resultData;
        handler.sendMessage(m);
        
    }
}